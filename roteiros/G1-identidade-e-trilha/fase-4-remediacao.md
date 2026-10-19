# G1 · Fase 4 — Correção do P0, plano de remediação e especificação da trilha

> **Seção do dossiê:** §6 · **Lidera:** 💻 Dev (correção) e 🛡️ Segurança (revisão e plano) ·
> **Aula:** 6 · **Semanas:** 7–10
> A **Parte A** é feita na **aula de laboratório guiada**, com o professor. Não precisa saber Java:
> o roteiro explica cada linha.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. **Parte A — Prova de conceito:** o P0 (injeção de SQL no login) corrigido num Pull Request
   revisado, com o teste `sqli.feature` passando de 🔴 para 🟢, e a §6.1 do dossiê escrita.
2. **Parte B — Plano de remediação:** a §6.2, com uma linha para cada falha do pacote que **não**
   foi corrigida.
3. **Parte C — Especificação da criação (F-05):** a trilha de auditoria **projetada** (não
   programada), dentro da §6.

---

# Parte A — Corrigir o P0 (aula guiada)

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório (se você seguiu o [`comum/00`](../comum/00-antes-de-comecar.md#4-clone-o-repositório-do-grupo), é a `vidaplus`, dentro da pasta `ssc` da sua pasta pessoal).
   **Todos os comandos deste roteiro são digitados nesta pasta.**
2. **Branches desta fase:** a correção do código tem a sua própria branch (Passo 2 da Parte A); a
   escrita da §6 no dossiê é gravada no fim, numa branch `dossie/fase-4` (último passo).
3. **Ligue o VidaPlus com o banco original e carregue os tokens** dos usuários de teste:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   source roteiros/comum/tokens.sh
   mkdir -p entregaveis/evidencias
   ```

   O primeiro termina com `PRONTO: VidaPlus no ar...` (a primeira vez demora alguns minutos); o
   segundo mostra seis linhas `TOKEN_... ok`. O terceiro cria a pasta das evidências, se ela ainda não existir. Os tokens valem **só neste terminal**: se abrir
   outro, rode o `source` de novo.

---

## Conceitos em 5 minutos

**Injeção de SQL.** O backend monta o comando SQL **colando** o texto que o usuário digitou:

```java
"... WHERE u.email = '" + email + "' AND u.senha_hash = '" + senhaHash + "' ..."
```

Se o usuário digita um e-mail normal, o comando fica certo. Mas se ele digita
`admin@vidaplus.local' -- `, o comando vira:

```sql
... WHERE u.email = 'admin@vidaplus.local' -- ' AND u.senha_hash = '...' AND u.ativo = 1
```

A aspa **fecha** o texto antes da hora, e o `-- ` (dois hífens e um espaço) diz ao MySQL "o resto
da linha é comentário". A conferência da senha **sumiu do comando**. O banco devolve o admin, e o
sistema o deixa entrar.

**A correção: consulta parametrizada.** Em vez de colar o texto no comando, você escreve o comando
com um **marcador** `?` e entrega o valor **separado**:

```java
"... WHERE u.email = ? AND u.ativo = 1"      // o comando, fixo
ps.setString(1, email);                       // o dado, à parte
```

O banco recebe **comando e dado separados**: o dado **nunca** vira comando, não importa o que
tenha dentro. Não é "filtrar caracteres perigosos" — é mudar a forma de conversar com o banco.

**E a senha?** Hoje o hash da senha também está colado no SQL (`AND u.senha_hash = '...'`). Na
correção, o SQL busca o usuário **só pelo e-mail**, e a senha é conferida **no Java**, com
`passwordEncoder.matches(senhaDigitada, hashDoBanco)`. Isso também prepara a troca do MD5 por um
algoritmo forte no futuro (o comentário em `config/SenhaConfig.java` explica por quê).

---

## Passo 1 — Faça o inventário da classe de falha

> 🔎 **Como o Grupo 5 fez:** antes de corrigir **um** ponto, procurou **todos**. Rodou o Semgrep
> com as regras do projeto (7 achados de `vidaplus-sql-concat`) e conferiu à mão com
> **Ctrl+Shift+F** → `createStatement(`. Montou uma tabela arquivo/método/linha/entrada/endpoint e
> decidiu corrigir a classe inteira no mesmo PR. Veja a tabela em
> [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G1:**

1. Rode o **Semgrep** — uma ferramenta que lê o código procurando padrões perigosos (o que ele é e
   como ler a saída: [`comum/07`](../comum/07-ferramentas-de-seguranca.md)). Ele roda dentro de um
   container, sem instalar nada:

   ```bash
   docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
     semgrep scan --config security/semgrep/vidaplus.yml --metrics=off legacy-app 2>&1 \
     | tee entregaveis/evidencias/fase4-semgrep-antes.txt
   ```

   Procure os achados `vidaplus-sql-concat`: em quais arquivos e linhas? Para **contar** os achados
   de cada regra, rode também (a saída em JSON tem uma entrada por achado):

   ```bash
   docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
     semgrep scan --config security/semgrep/vidaplus.yml --metrics=off --json --quiet legacy-app \
     | grep -o '"check_id": *"[^"]*"' | sed 's/.*semgrep\.//; s/"$//' | sort | uniq -c \
     | tee entregaveis/evidencias/fase4-semgrep-contagem-antes.txt
   ```

   No código original, `vidaplus-sql-concat` aparece com **7**.
2. No VS Code, **Ctrl+Shift+F** → `createStatement(`. Para cada resultado, abra e veja se o SQL
   junta **texto digitado pelo usuário**. Atenção: `UsuarioDao.listarTodos` usa `createStatement`,
   mas o SQL é fixo — **não** é vulnerável. Saber distinguir é parte do trabalho.
3. Monte, no rascunho, a tabela do inventário **do `UsuarioDao`** (o arquivo do seu pacote):
   método, linha, que entrada é colada, que endpoint a expõe.
4. **Decisão do G1:** o `autenticar` (login, F-01) e o `porEmail` (recuperação de senha, F-12)
   estão no seu pacote e têm a mesma falha. Corrijam **os dois** no mesmo PR: corrigir só o login
   deixaria a classe meio corrigida. Os achados do `PacienteDao` são da busca e do prontuário
   (F-13/F-14), que não estão no pacote de nenhum grupo da turma: registrem-nos no plano (Parte B)
   como "fora do pacote".

## Passo 2 — Crie a branch da correção

```bash
git checkout main
git pull
git checkout -b fix/F-01-sqli-login
```

(Explicação de cada comando em [`comum/03` §3](../comum/03-git-e-github.md#3-o-ciclo-completo-passo-a-passo).)

## Passo 3 — Registre o "antes"

> 🔎 **Como o Grupo 5 fez:** antes de mexer no código, guardou a prova de que a falha existia: a
> saída do ataque e o teste vermelho. Sem o "antes", não há como mostrar o 🔴→🟢.

**▶ Agora no G1:** religue o VidaPlus com o banco original, rode o ataque e o teste, gravando as
duas saídas:

```bash
bash roteiros/comum/zerar-banco.sh

curl -s -i -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@vidaplus.local' -- \",\"senha\":\"qualquer\"}" \
  | tee entregaveis/evidencias/fase4-sqli-antes.txt

bash roteiros/comum/rodar-testes.sh "security & F-01" 2>&1 | tee entregaveis/evidencias/fase4-teste-antes.txt
```

O primeiro deve devolver **200 com `access_token`**; o segundo, **vermelho**
(`expected: <401> but was: <200>`).

## Passo 4 — Corrija o `UsuarioDao.autenticar`

Abra `legacy-app/backend/src/main/java/br/ifsul/vidaplus/dao/UsuarioDao.java` e encontre o método
`autenticar` (**Ctrl+F** → `public Map<String, Object> autenticar`).

**O código de hoje** monta o SQL juntando pedaços com `+` e usa `Statement` (que executa o texto
como está). **Substitua o método inteiro** — da linha `public Map<String, Object> autenticar(` até
a `}` que fecha o método, que é a última linha antes do comentário de
várias linhas que começa com `/**` e cuja linha seguinte diz
`* Lista todos os usuarios (tela de gestao do ADMIN).` (procure com **Ctrl+F** →
`Lista todos os usuarios`; o `/**` fica na linha de cima) — por este:

```java
    public Map<String, Object> autenticar(String email, String senha) {
        // 1) O comando SQL é FIXO. O "?" marca onde entra o dado.
        //    A senha NÃO entra no SQL: ela é conferida no Java, mais abaixo.
        String sql = """
                SELECT u.id, u.email, u.papel, u.paciente_id, u.senha_hash, m.id AS medico_id,
                       COALESCE(p.nome, m.nome, SUBSTRING_INDEX(u.email, '@', 1)) AS nome
                FROM usuario u
                LEFT JOIN paciente p ON p.id = u.paciente_id
                LEFT JOIN medico m ON m.usuario_id = u.id
                WHERE u.email = ? AND u.ativo = 1
                """;
        // 2) PreparedStatement: o banco recebe o comando e o dado SEPARADOS.
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);                       // o 1º "?" recebe o e-mail
            try (ResultSet rs = ps.executeQuery()) {
                // 3) Achou o usuário E a senha confere? (matches compara a senha digitada
                //    com o hash guardado no banco)
                if (rs.next() && passwordEncoder.matches(senha, rs.getString("senha_hash"))) {
                    Map<String, Object> u = new HashMap<>();
                    u.put("id", rs.getLong("id"));
                    u.put("email", rs.getString("email"));
                    u.put("papel", rs.getString("papel"));
                    u.put("paciente_id", rs.getObject("paciente_id"));
                    u.put("medico_id", rs.getObject("medico_id"));
                    u.put("nome", rs.getString("nome"));
                    return u;
                }
                return null;                              // e-mail não existe OU senha errada
            }
        } catch (Exception e) {
            // 4) Mensagem sem detalhes internos (o detalhe fica na causa, para o log).
            throw new RuntimeException("Erro no login", e);
        }
    }
```

Entendendo as partes novas:

| Trecho | O que faz |
|---|---|
| `"""` ... `"""` | um **bloco de texto** do Java: escreve o SQL em várias linhas sem precisar de `+`. Sem `+` perto do SQL, qualquer `+` que aparecer numa revisão chama a atenção |
| `c.prepareStatement(sql)` | prepara o comando com os `?` |
| `ps.setString(1, email)` | entrega o valor do 1º `?` |
| `passwordEncoder.matches(senha, hash)` | confere a senha **no Java** |
| `return null` | o controller entende `null` como "credenciais inválidas" |

Repare que o arquivo **já importa** `PreparedStatement` (o método `alternarAtivo`, no fim do
arquivo, já usa — ele é o **modelo** que existia no próprio código).

## Passo 5 — Corrija o `UsuarioDao.porEmail` (agora com você)

Faça a mesma transformação no método `porEmail`, que é usado pela recuperação de senha (F-12).
Roteiro:

1. troque a montagem do SQL com `+ email +` por um SQL fixo com `WHERE email = ? AND ativo = 1`;
2. troque `Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)` por
   `PreparedStatement ps = c.prepareStatement(sql)` + `ps.setString(1, email)` +
   `ResultSet rs = ps.executeQuery()` — copie a **estrutura** do `autenticar` que você acabou de
   escrever;
3. mantenha o resto (montar o `Map` com `id`, `email`, `papel`).

Tente sozinho primeiro. Depois, **confira** (clique para abrir):

<details>
<summary>Confira a sua versão do <code>porEmail</code></summary>

O começo do método deve ficar assim (o `catch` do fim não muda):

```java
    public Map<String, Object> porEmail(String email) {
        String sql = "SELECT id, email, papel FROM usuario WHERE email = ? AND ativo = 1";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> u = new HashMap<>();
                    u.put("id", rs.getLong("id"));
                    u.put("email", rs.getString("email"));
                    u.put("papel", rs.getString("papel"));
                    return u;
                }
                return null;
            }
        } catch (Exception e) {
```

Três sinais de que está certo: o SQL **não** tem `+`; existe um `ps.setString(1, email)`; e, no
Passo 7, o Semgrep **não** acusa mais nada no `UsuarioDao.java`.

</details>

**Não** mude o que o reset responde ("encontrado"/"não cadastrado") nem o código previsível: isso
é a F-12, que fica no **plano** (Parte B). Um PR, um assunto.

## Passo 6 — Faça o login falho responder 401

Abra `controller/AuthController.java`.

1. No topo, logo abaixo da linha `import org.springframework.beans.factory.annotation.Autowired;`,
   acrescente:

   ```java
   import org.springframework.http.HttpStatus;
   import org.springframework.web.server.ResponseStatusException;
   ```

2. No método `login`, troque:

   ```java
           if (u == null) {
               resp.put("erro", "credenciais invalidas");
               return resp;
           }
   ```

   por:

   ```java
           if (u == null) {
               throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciais invalidas");
           }
   ```

   `ResponseStatusException` com `UNAUTHORIZED` faz o Spring responder **401** — o código certo
   para "não sei quem você é" ([`comum/02` §1.2](../comum/02-observar-o-sistema.md#12-códigos-de-status-http-que-você-vai-encontrar)).

> **A tela continua funcionando?** Sim: com o 401, a tela de login mostra "Não foi possível
> entrar. Tente novamente.". Quer uma mensagem melhor? É **opcional**: em `pages/Login.jsx`, no
> `catch`, mostre "E-mail ou senha inválidos" quando o status for 401. Se fizer, inclua no PR.

## Passo 7 — Remonte, ataque de novo e rode os testes

> 🔎 **Como o Grupo 5 fez:** depois da correção, repetiu **o mesmo ataque** do "antes", conferiu
> que o **uso legítimo** continuava funcionando, rodou os testes e o Semgrep de novo. Guardou cada
> saída como o "depois".

**▶ Agora no G1:**

1. Remonte o backend com o código novo e o banco original. A compilação do Java acontece aqui: se
   houver erro de digitação, ele aparece nas mensagens e o script termina com `ERRO ao ligar`.

   ```bash
   bash roteiros/comum/zerar-banco.sh
   ```

   Espere a mensagem `PRONTO: VidaPlus no ar...`.
2. **O ataque:**

   ```bash
   curl -s -i -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d "{\"email\":\"admin@vidaplus.local' -- \",\"senha\":\"qualquer\"}" \
     | tee entregaveis/evidencias/fase4-sqli-depois.txt
   ```

   Esperado: **`HTTP/1.1 401`**, sem `access_token`. O corpo da resposta vem com um texto longo
   (`"trace":"org.springframework...`): é o **erro que conta demais** (F-16), que não é assunto
   deste PR. Para conferir de forma objetiva que nenhum token saiu:

   ```bash
   grep -c access_token entregaveis/evidencias/fase4-sqli-depois.txt
   ```

   Esperado: `0`.
3. **O uso legítimo** (não pode ter quebrado):

   ```bash
   curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/api/auth/login \
     -H "Content-Type: application/json" -d '{"email":"ana.paciente@email.com","senha":"Paciente123"}'
   ```

   Esperado: **200**. Entre também pela tela do navegador.
4. **O teste do P0:**

   ```bash
   bash roteiros/comum/rodar-testes.sh "security & F-01" 2>&1 | tee entregaveis/evidencias/fase4-teste-depois.txt
   ```

   Esperado: `RESULTADO: todos os testes ... PASSARAM (verde)`.
5. **Todos os testes de segurança** — para conferir que não quebrou o `login_valido_funciona` (ele
   não tem a tag `F-01`, então o filtro acima não o roda):

   ```bash
   bash roteiros/comum/rodar-testes.sh 2>&1 | tee entregaveis/evidencias/fase4-todos-depois.txt
   ```

   Vai continuar **vermelho** por causa dos testes de IDOR e RBAC (de outros grupos). Confira que
   **nenhuma** falha listada fala de `sqli` ou de `login_valido`.
6. **O Semgrep** de novo, agora gravando no arquivo do "depois":

   ```bash
   docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
     semgrep scan --config security/semgrep/vidaplus.yml --metrics=off legacy-app 2>&1 \
     | tee entregaveis/evidencias/fase4-semgrep-depois.txt
   ```

   Em quais arquivos sobraram achados `vidaplus-sql-concat`? Para **contar** quantos achados há de
   cada regra (a saída de texto agrupa os achados por arquivo, então contar linhas engana), peça a
   saída em formato JSON e conte:

   ```bash
   docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
     semgrep scan --config security/semgrep/vidaplus.yml --metrics=off --json --quiet legacy-app \
     | grep -o '"check_id": *"[^"]*"' | sed 's/.*semgrep\.//; s/"$//' | sort | uniq -c \
     | tee entregaveis/evidencias/fase4-semgrep-contagem-depois.txt
   ```

   Cada linha mostra um número e o nome de uma regra. Compare com o
   `fase4-semgrep-contagem-antes.txt` do Passo 1. Esperado: `vidaplus-sql-concat` cai de **7**
   para **4**, e as outras regras continuam com **1** cada. Os 4 que sobram estão todos no
   `PacienteDao.java` (a busca e a nota — outro pacote): na saída de texto acima, nenhum deve
   estar no `UsuarioDao.java`.

**Deu errado?**

| Sintoma | Causa provável |
|---|---|
| `docker compose up --build` mostra `ERROR` e `cannot find symbol` | faltou um `import` ou há erro de digitação. A mensagem diz o arquivo e a linha |
| O login legítimo deu 401 | a senha está sendo conferida errado. Confira se usou `rs.getString("senha_hash")` e se a coluna `u.senha_hash` está no `SELECT` |
| O Semgrep ainda acusa `vidaplus-sql-concat` no `UsuarioDao.java` | o `autenticar` ou o `porEmail` ainda tem `+` no SQL. Compare com o Passo 4 e com a conferência do Passo 5 |
| `grep -c access_token` deu `1` | o login com o ataque ainda devolve token: o `autenticar` novo não foi salvo, ou o VidaPlus não foi remontado (`zerar-banco.sh`) |
| O teste continua vermelho | você esqueceu de remontar e zerar: `bash roteiros/comum/zerar-banco.sh` |

## Passo 8 — Grave, abra o PR e peça a revisão

```bash
git status
git add legacy-app/backend/src/main/java entregaveis/evidencias
git commit -m "fix(F-01): login com consulta parametrizada e 401 na falha"
git push -u origin fix/F-01-sqli-login
```

Abra o PR ([`comum/03` §3, passo 7](../comum/03-git-e-github.md#passo-7--abra-o-pull-request)). Na
descrição, escreva:

- **o que muda:** login e recuperação de senha com consulta parametrizada; senha conferida no
  Java; 401 na falha;
- **como testar:** os comandos do Passo 7 e o que se espera de cada um;
- **o teste que deve passar:** `sqli.feature` ("payload de injeção no login não autentica").

**Quem revisa** (outro integrante) usa o checklist do [`docs/07`](../../docs/07-rubrica-avaliacao.md#checklist-de-secure-code-review-para-os-prs-da-poc).
Para este PR, os itens que mais importam: "consultas parametrizadas (sem concatenação SQL)", "erro
genérico ao cliente", "teste cobre o caminho de ataque". O revisor também **roda** o Passo 7 na
máquina dele antes de aprovar.

## Passo 9 — Escreva a §6.1

> 🔎 **Como o Grupo 5 fez:** para o Controle 1, escreveu: **Antes** (o que estava errado, com o
> inventário), **Depois** (o que mudou, arquivo por arquivo), **O que rejeitamos e por quê**
> (filtrar caracteres — lista negra), **O que esta correção NÃO resolve**, **PR** (número, branch,
> autor, revisor) e **Teste**. Veja: [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G1:** escreva os mesmos blocos para o controle de vocês. Para o "**O que esta
correção NÃO resolve**", pense no recorte do G1 — **o fluxo de autenticação**:

- a senha continua guardada em MD5, sem sal (Ana e Bruno com o mesmo hash)?
- alguém ainda consegue testar mil senhas seguidas?
- a recuperação de senha ainda revela quem tem conta?
- o token ainda vale para sempre?

Cada "sim" vai para o plano (Parte B).

---

# Parte B — Plano de remediação

> 🔎 **Como o Grupo 5 fez:** uma tabela com **funcionalidade, falha, controle proposto, esforço
> (P/M/G), prioridade e observação**, definindo antes a escala de esforço (P = até um dia; M =
> alguns dias; G = mais de uma semana ou mudança de arquitetura). O que **dependia de outro
> pacote** ficou como "transferido", com o ID. Veja: [`docs/12` §6.2](../../docs/12-exemplo-dossie-preenchido.md#62-plano-de-remediação-demais-falhas-do-pacote--não-implementadas).

**▶ Agora no G1:** uma linha para **cada falha do pacote que não foi corrigida**. Para cada uma,
o **controle proposto** precisa ser concreto o bastante para alguém implementar depois. Pontos de
partida (pesquisem e completem):

| Falha | Perguntas para chegar ao controle |
|---|---|
| Senha em MD5 sem sal (F-01) | que algoritmo o `SenhaConfig.java` sugere? Leia o "ATENCAO 2" do mesmo arquivo: o que acontece com as senhas que **já estão** no banco, e com o tamanho da coluna `senha_hash`? Como migrar sem obrigar todo mundo a trocar a senha? |
| Força bruta (F-01) | limitar por conta, por IP ou pelos dois? Por quanto tempo? Como evitar que o atacante tranque a conta de outra pessoa (o "D" da Fase 2a)? |
| Token sem validade e "Sair" que não invalida (F-11) | qual validade faz sentido numa clínica? O que o servidor precisa guardar para recusar um token depois do Sair? O token deveria ficar no `localStorage`? |
| Recuperação de senha (F-12) | resposta igual para e-mail existente ou não; código aleatório, de uso único, com prazo; enviado por e-mail, **nunca** na resposta |
| CORS e cabeçalhos (F-17) | que origem a API deve aceitar? Que cabeçalhos o nginx deve mandar (CSP, HSTS, X-Frame-Options, X-Content-Type-Options) e o que cada um impede? |
| Injeção no `PacienteDao` | fora do pacote (F-13/F-14, sem grupo na turma): registre e explique |

Os **segredos no código** (F-10) vão na §8 (Fase 6).

---

# Parte C — Especificar a trilha de auditoria (F-05)

Nesta turma, a criação é **especificada**, não implementada. O nível esperado é o do Controle 2
do Grupo 5 — **sem** a implementação.

> 🔎 **Como o Grupo 5 fez** (a criação dele era a anonimização): definiu a **entidade**, o
> **gatilho** com colunas que existem no banco, o **tratamento atributo por atributo** (tabela), as
> **limitações** assumidas (o que a rotina não alcança) e o `.feature @analise`. Veja o Controle 2
> em [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G1** — escopo mínimo do [`docs/05`](../../docs/05-pacotes-trabalho-estudantes.md#escopo-mínimo-da-criação--o-que-a-especificação-cobre):
**1 ação sensível** registrada + **verificação de integridade** do registro. Escreva:

1. **A ação sensível escolhida** e por quê. Candidatas: login (sucesso e falha); desativação de
   uma conta (`POST /api/usuarios/{id}/alternar-ativo`). Escolham **uma**.
2. **Onde no código o registro seria feito** (qual método de qual controller), e em que momento
   (antes ou depois da ação? e se a ação falhar?).
3. **Os campos do registro** — tabela campo | conteúdo | por quê. Partam do modelo em
   `entity/Auditoria.java` (`ts`, `evento`, `user_id`, `recurso`, `prev_hash`, `hash`) e do
   comentário no fim de `db/schema.sql`. Para cada campo, digam o que **nunca** pode ir nele
   (senha, token, CPF, nome).
4. **Como provar que ninguém alterou o registro — o encadeamento de hash.** Explique com as
   palavras de vocês: cada linha guarda um `hash` calculado a partir **dos dados dela + o `hash`
   da linha anterior** (`prev_hash`). Se alguém alterar ou apagar uma linha do meio, os hashes das
   linhas seguintes deixam de bater. Mostrem um **exemplo com 3 linhas** (pode ser em pseudocódigo:
   `hash3 = SHA-256(dados3 + hash2)`) e descrevam **o procedimento de verificação** (percorrer a
   tabela recalculando).
5. **Como impedir alteração na origem (append-only):** o usuário do banco da aplicação deveria
   poder fazer `UPDATE` e `DELETE` nesta tabela? Como configurar isso?
6. **Comportamento sob falha** (decidido na Fase 3).
7. **Prazo de guarda** e **quem pode ler** (Fase 2b).
8. **Limitações assumidas:** o que este desenho **não** garante? (Ex.: quem administra o banco
   consegue apagar a tabela inteira e recriar a cadeia?)
9. **O `.feature @analise`** da F-05, se ainda não existir (Fase 1).

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| O teste do "antes" (Passo 3) já deu **verde** | a correção já está nesta branch | `git log --oneline -3`: se aparecer um commit `fix(F-01)`, você já corrigiu; o "antes" tem de ser gravado **antes** do Passo 4 |
| Problemas ao compilar ou nos resultados do Passo 7 | — | veja a tabela **Deu errado?** dentro do Passo 7 |

---

## Último passo — Grave no Git e abra o Pull Request

A correção já entrou pelo PR da Parte A. Agora grave a **§6 do dossiê** (Partes A, B e C)
numa branch própria, criada a partir da `main` atualizada:

```bash
git checkout main
git pull
git checkout -b dossie/fase-4
```

Escreva a §6 no `entregaveis/DOSSIE.md`, salve (**Ctrl+S**) e grave:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 4 - remediacao"
git push -u origin dossie/fase-4
```

- `git status` lista o que mudou: confira que só aparecem os arquivos que você quer gravar;
- `git add` escolhe o que entra; `git commit` grava; `git push` envia ao GitHub;
- se o `push` pedir usuário e senha: o seu usuário do GitHub e, na senha, o **token** do GitHub
  ([`comum/00` §2.1](../comum/00-antes-de-comecar.md#21-crie-um-token-de-acesso-a-senha-do-git));
- se o `push` for recusado (`rejected`), um colega enviou algo antes: rode `git pull` e repita o
  `git push`.

Depois, no navegador:

1. Abra o repositório do grupo no GitHub e clique em **Compare & pull request** (faixa amarela).
   Se a faixa não aparecer: **Pull requests** → **New pull request** → em **base** deixe `main`, em
   **compare** escolha `dossie/fase-4` → **Create pull request**.
2. Título: "Dossiê — Fase 4: remediação". Na descrição: o que foi feito e quem participou. Clique em
   **Create pull request**.
3. **Outro integrante** (não quem abriu) abre o PR → aba **Files changed** → lê → **Review
   changes** → **Approve** → **Submit review**.
4. Com a aprovação: **Merge pull request** → **Confirm merge** → **Delete branch**.
5. No terminal de cada integrante:

   ```bash
   git checkout main
   git pull
   ```

Conflito ou erro? Veja [`comum/03`](../comum/03-git-e-github.md) §4.2 e §5.

---

## Confira antes de seguir

- [ ] PR da correção revisado por outro integrante e mergeado.
- [ ] Tenho o "antes" e o "depois" do ataque, do teste e do Semgrep em `entregaveis/evidencias/`.
- [ ] O login legítimo continua funcionando (curl e tela) e o `login_valido_funciona` não falha.
- [ ] A §6.1 tem antes, depois, o que rejeitamos, o que **não** resolve, PR e teste.
- [ ] Toda falha não corrigida do pacote está na §6.2, com controle concreto, esforço e prioridade.
- [ ] A F-05 está especificada nos 9 itens da Parte C.
