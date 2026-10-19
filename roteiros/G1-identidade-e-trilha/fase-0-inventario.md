# G1 · Fase 0 — Inventário e classificação de dados

> **Seção do dossiê:** §1 · **Lidera:** 🛡️ Segurança e 📋 DPO · **Aula:** 1 · **Semana:** 1
> **Antes:** faça o **Passo 0**, logo abaixo.

## Como usar este roteiro

Cada passo tem duas partes:

1. **🔎 Como o Grupo 5 fez** — o que o grupo do dossiê de exemplo
   ([`docs/12`](../../docs/12-exemplo-dossie-preenchido.md)) executou nesse passo: o comando, o
   que apareceu e o que ele escreveu no dossiê. Leia para entender **o raciocínio**.
2. **▶ Agora no G1** — o mesmo passo, aplicado às funcionalidades do **seu** pacote. É isto que
   você executa.

O Grupo 5 trabalhou a busca de pacientes e o prontuário; vocês trabalham login, sessão,
recuperação de senha, cabeçalhos e auditoria. O **caminho** é o mesmo; os **achados** são outros.

## O que você entrega no fim

Na §1 do `DOSSIE.md`: a lista de **fluxos críticos** (3 a 5) e a **tabela de ativos** com nível,
atributo e quem demanda. A coluna **Base legal** fica para a Fase 1.

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório (se você seguiu o [`comum/00`](../comum/00-antes-de-comecar.md#4-clone-o-repositório-do-grupo), é a `vidaplus`, dentro da pasta `ssc` da sua pasta pessoal).
   **Todos os comandos deste roteiro são digitados nesta pasta.**
2. **Entre na branch da fase.** Quem **lidera** a fase cria a branch e a envia ao GitHub:

   ```bash
   git checkout main
   git pull
   git checkout -b dossie/fase-0
   git push -u origin dossie/fase-0
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-0
   ```

   Antes de começar a editar, a cada aula: `git pull` (traz o que os colegas já enviaram).
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

- **Ativo:** tudo o que tem valor e que alguém poderia querer roubar, alterar ou destruir — um
  dado (senha, CPF), um **segredo do sistema** (a chave que assina os tokens) ou um **registro**
  (o log de quem entrou).
- **Fluxo crítico:** um caminho do sistema por onde passa um ativo importante.
- **Nível** (do mais ao menos sensível): **Crítico → Confidencial → Interno → Público**. Um conjunto
  que junta níveis diferentes fica com **o mais alto**.
- **Atributo prioritário:** **C**onfidencialidade (ninguém indevido lê), **I**ntegridade
  (ninguém indevido altera), **D**isponibilidade (está lá quando precisa), **Autenticidade** (é
  quem diz ser), **Não-repúdio** (quem fez não consegue negar).
- **Quem demanda/define:** negócio, jurídico/DPO, segurança, operações ou o titular.

Crie agora um rascunho para anotar tudo o que observar: `entregaveis/evidencias/fase0-anotacoes.txt`.

---

## Passo 1 — Percorra as telas com cada papel

> 🔎 **Como o Grupo 5 fez:** entrou com os seis usuários e anotou as telas de cada papel. Viu que
> **Recepção, Médico, Bioquímico e Admin** têm o menu **Pacientes** (a busca) e que a ficha do
> paciente mostra o **prontuário** (nota clínica). Essas duas telas eram o centro do pacote dele.

**▶ Agora no G1:**

1. Entre com os seis usuários ([`comum/01` §4](../comum/01-subir-o-vidaplus.md#4-entrar-no-sistema)).
   Para cada um, anote: telas do menu; nome que aparece no canto superior direito.
2. Na tela de login, clique em **Esqueci minha senha**, digite um e-mail e envie. Anote o que
   aparece.
3. Entre e clique em **Sair**. Anote o que acontece na tela.

O centro do seu pacote é **o que acontece antes e durante o login**, e **o que fica guardado
depois dele**.

---

## Passo 2 — Siga a trilha de cada funcionalidade, da tela ao banco

> 🔎 **Como o Grupo 5 fez:** para a busca (F-13), seguiu a trilha como em
> [`comum/02` §4.3](../comum/02-observar-o-sistema.md#43-exercício-siga-a-trilha-de-meus-exames-da-ana):
>
> `BuscaPacientes.jsx → services/paciente.js (buscar) → GET /api/pacientes/busca?q= → PacienteController.buscar → PacienteDao.buscar → tabela paciente`
>
> Fez o mesmo para o prontuário: `Prontuario.jsx → notasClinicas → GET /api/pacientes/{id}/notas → PacienteDao.notaDoPaciente → tabela nota_clinica`.

**▶ Agora no G1:** a tabela de trilhas está no [`README` do G1](README.md#3-onde-cada-funcionalidade-está-no-vidaplus).
**Confira cada uma no código**, abrindo os arquivos:

1. **Login (F-01):** abra `pages/Login.jsx` e procure (**Ctrl+F**) `login(`. Abra
   `services/auth.js` e ache a função `login`. Ela chama que endereço? Abra
   `controller/AuthController.java`: que método atende esse endereço? Ele chama qual método do
   `UsuarioDao`? Abra `dao/UsuarioDao.java` e ache o SQL desse método. Que tabelas aparecem no
   `FROM` e nos `JOIN`?
2. **Recuperação de senha (F-12):** mesmo exercício, começando por `solicitarReset` em
   `services/auth.js`.
3. **Sessão (F-11):** em `services/auth.js`, leia `salvarSessao`, `getToken` e `logout`. Onde o
   token é guardado? O que o `logout` faz — e o que **não** faz? Depois abra `security/JwtUtil.java`
   e leia os três métodos.
4. **Cabeçalhos (F-17):** abra `config/WebConfig.java` e `frontend/nginx.conf`. Leia os comentários.
5. **Auditoria (F-05):** abra `entity/Auditoria.java` e o final de `db/schema.sql`.

Escreva as trilhas no rascunho. Elas viram os processos e depósitos do DFD na Fase 2a.

---

## Passo 3 — Observe a funcionalidade principal por dentro

> 🔎 **Como o Grupo 5 fez:** abriu a busca como Recepção, pesquisou "Silva" com o **F12 → Rede**
> aberto e viu que a resposta trazia **religião e renda** — campos que a tela de busca nem precisa.
> Depois repetiu a busca pelo terminal **sem token nenhum**:
>
> ```bash
> curl -s "localhost:8080/api/pacientes/busca?q=Silva"
> ```
>
> Resposta: o cadastro da Ana, completo. Daí a frase do dossiê: *"A busca **não pede login** e
> devolve CPF, religião e renda."*

**▶ Agora no G1 — o login (F-01):**

1. Na tela de login, **F12 → Rede → XHR**. Entre como Ana.
2. Clique no pedido `login`:
   - aba **Requisição / Payload**: o que a tela enviou? Em que formato a senha viaja?
   - aba **Resposta**: liste os campos devolvidos.
3. **Saia** e tente com a **senha errada**. Que **código de status** veio? O que a resposta diz?
4. Repita pelo terminal. O comando abaixo tenta entrar como Ana com a senha `errada`, mostra a
   resposta completa (`-i` inclui o código de status) e grava uma cópia como evidência (`tee`):

   ```bash
   mkdir -p entregaveis/evidencias
   curl -s -i -X POST localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"ana.paciente@email.com","senha":"errada"}' \
     | tee entregaveis/evidencias/fase0-login-senha-errada.txt
   ```

   (O `mkdir -p` cria a pasta das evidências, se ela ainda não existir.) Esperado: a primeira
   linha começa com `HTTP/1.1` e um número de três dígitos, e a última traz um corpo JSON com a
   palavra `erro`. Anote a primeira linha (`HTTP/1.1 ...`) e o corpo. Compare com a tabela de códigos de status
   ([`comum/02` §1.2](../comum/02-observar-o-sistema.md#12-códigos-de-status-http-que-você-vai-encontrar)):
   uma senha errada deveria devolver esse código?
5. Agora com um e-mail que **não existe**:

   ```bash
   curl -s -i -X POST localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"naoexiste@teste.com","senha":"errada"}' \
     | tee entregaveis/evidencias/fase0-login-email-inexistente.txt
   ```

   Esperado: de novo `HTTP/1.1 ...` e um corpo com `erro`. A resposta é diferente da senha
   errada? (Se for igual, ótimo; se for diferente, é um achado: revela quem tem conta.) Para
   comparar os dois arquivos linha a linha:

   ```bash
   diff entregaveis/evidencias/fase0-login-senha-errada.txt entregaveis/evidencias/fase0-login-email-inexistente.txt
   ```

   O `diff` mostra só as linhas **diferentes**. Se não aparecer nada, os dois arquivos são
   iguais; se aparecer só a linha `Date:` (a hora da resposta), também. Se aparecer qualquer outra
   linha, anote qual — é a diferença que um estranho enxergaria.

---

## Passo 4 — Olhe no banco o que o sistema guarda

> 🔎 **Como o Grupo 5 fez:** rodou `DESCRIBE paciente;` e achou as colunas `religiao`,
> `estado_civil` e `renda_mensal`. Procurou no código (**Ctrl+Shift+F** → `religiao`) quem usava
> esses campos: só a busca, que os devolvia sem necessidade. Escreveu: *"coletados, mas nenhum
> fluxo usa"* — e classificou como Crítico, porque **religião é dado sensível**.

**▶ Agora no G1:** no terminal, rode os dois comandos (cada um abre o MySQL dentro do container
do banco, executa uma consulta e mostra o resultado):

```bash
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "DESCRIBE usuario;"
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, email, senha_hash, papel, ativo, reset_token FROM usuario;"
```

Anote e pense:

1. Quais colunas são ativos? Qual o nível de cada uma?
2. A senha não aparece; aparece `senha_hash`. Abra `security/LegacyHash.java`: que algoritmo gera
   esse valor? Procure na internet "MD5 senha segura" e anote em uma linha o que encontrar.
3. **Compare o `senha_hash` da Ana e do Bruno.** O que você nota? O que isso permite a quem roubar
   esta tabela? (Pista: o que o MD5 faz com duas senhas iguais?)
4. A coluna `reset_token` está vazia agora. Ela vai ser preenchida no Passo 6.

---

## Passo 5 — Olhe o "crachá" da sessão

> 🔎 **Como o Grupo 5 fez:** registrou o **token de sessão no `localStorage`** como ativo Crítico,
> com a observação *"fica no navegador e não expira: quem copia o token usa a conta"*. Para saber
> que ele não expira, leu o comentário `V-07` em `JwtUtil.java` — e confirmou olhando o conteúdo
> do token.

**▶ Agora no G1 (F-11):**

1. Entre como Ana no navegador. **F12 → Armazenamento/Aplicativo → Armazenamento local →
   `http://localhost:4200`**. Anote o que está guardado.
2. Veja **o que há dentro do token** da Ana. O primeiro comando abaixo faz login com os usuários
   de teste e guarda o token de cada um numa variável (`TOKEN_ANA`, `TOKEN_BRUNO`…); o segundo
   recorta a parte do meio do token da Ana e a decodifica. Um token JWT tem três partes separadas por ponto
   (`cabeçalho.conteúdo.assinatura`); a do meio é só texto codificado (não é segredo!):

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   echo "$TOKEN_ANA" | cut -d. -f2 | base64 -d; echo
   ```

   Resposta esperada: algo como `{"sub":"5","papel":"PACIENTE"}`.
   (Se aparecer `invalid input`, o texto precisa de complemento: rode
   `echo "$(echo "$TOKEN_ANA" | cut -d. -f2)==" | base64 -d; echo` e ignore o aviso.)
3. Pense num **crachá de visitante** de uma empresa: ele tem nome, setor… e o que mais? (Validade!)
   O token tem validade? Anote.
4. Em `JwtUtil.java`, onde está a **chave** que assina o token? Quem tem acesso a ela?

---

## Passo 6 — Teste as funcionalidades secundárias

> 🔎 **Como o Grupo 5 fez:** testou a nota clínica **sem token**:
>
> ```bash
> curl -s localhost:8080/api/pacientes/1/notas
> ```
>
> Recebeu a nota da Ana com um trecho de HTML escondido (`<img ... onerror=...>`). Escreveu dois
> achados: *"dado de saúde em texto livre"* e *"a API entrega a nota sem login"*.

**▶ Agora no G1 — recuperação de senha (F-12):**

```bash
curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" -d '{"email":"ana.paciente@email.com"}'; echo
curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" -d '{"email":"naoexiste@teste.com"}'; echo
```

Esperado: duas linhas JSON, cada uma com um campo `"mensagem"` — e a primeira também com um
`"reset_token"`.

1. Compare as duas respostas. O que a diferença revela a um estranho?
2. Olhe o `reset_token` da primeira. Rode o comando de novo e compare os dois valores. Abra
   `AuthController.reset`: como o código é formado? Dá para **adivinhar** o código de outra pessoa?
3. Consulte de novo a tabela `usuario`: o `reset_token` foi gravado?

   ```bash
   docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, email, reset_token FROM usuario;"
   ```

   Esperado: as seis contas, com a coluna `reset_token` de cada uma. Compare o valor da Ana com
   o que a API devolveu no item 1.

**▶ Agora no G1 — cabeçalhos e CORS (F-17):**

```bash
curl -s -i -H "Origin: http://site-qualquer.example" "localhost:8080/api/pacientes/busca?q=Silva" | head -15
curl -s -I localhost:4200/
```

O primeiro finge ser **outro site** pedindo dados à API; o segundo pede só os cabeçalhos do site
(`-I`).

1. Procure a linha `Access-Control-Allow-Origin`. O que o valor significa?
2. Procure (provavelmente **não vai achar**) `Content-Security-Policy`, `Strict-Transport-Security`,
   `X-Frame-Options`, `X-Content-Type-Options`. Anote quais faltam. Os nomes vão aparecer na Fase 2a.

---

## Passo 7 — Procure o que deveria existir e não existe

> 🔎 **Como o Grupo 5 fez:** procurou no código (**Ctrl+Shift+F**) por `DELETE FROM` e por
> "anonim": nada. Leu no `schema.sql` o comentário *"exclusão apenas lógica via 'ativo' →
> retenção infinita"*. Escreveu como fluxo crítico: *"(Ausente) **Fim de vida do dado** — nenhum
> dado de paciente é descartado ou anonimizado."*

**▶ Agora no G1 (F-05):**

1. Acompanhe o **log** do backend ao vivo. O log é o "diário" que o servidor escreve enquanto
   trabalha. Este comando mostra as linhas novas assim que aparecem (`-f` = "seguir"; `--tail 0` =
   "não mostre as antigas"):

   ```bash
   docker compose -f legacy-app/docker-compose.yml logs backend -f --tail 0
   ```

   O terminal fica "parado", esperando. Deixe-o assim.
2. Abra um **segundo terminal**: no painel do terminal do VS Code, clique no **+** (canto direito).
   Nele, faça um login certo e um errado:

   ```bash
   curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"email":"ana.paciente@email.com","senha":"Paciente123"}'; echo
   curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"email":"ana.paciente@email.com","senha":"errada"}'; echo
   ```

3. Volte ao **primeiro** terminal (clique no nome dele, na lista à direita do painel). Que linhas
   apareceram depois dos dois logins? Para parar de acompanhar, clique nele e aperte **Ctrl+C**
   (isso não desliga o VidaPlus).
4. Liste as tabelas do banco:

   ```bash
   docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SHOW TABLES;"
   ```

   Esperado: a lista `Tables_in_vidaplus` com seis nomes. Existe tabela de auditoria?
5. Abra `entity/Auditoria.java` (**Ctrl+P** → `Auditoria.java`) e o fim do `db/schema.sql`
   (**Ctrl+P** → `schema.sql`, depois **Ctrl+End**): o que estava previsto?

Escreva o fluxo ausente: "(Ausente) **Registro de acessos** — ..." com o que você constatou.

---

## Passo 8 — Procure os segredos do sistema

> 🔎 **Como o Grupo 5 fez:** abriu `application.properties` e `docker-compose.yml`, viu a senha do
> banco e a chave do token escritas ali, e criou a linha *"Segredos do sistema (chave que assina
> os tokens, senha do banco)"*, com base legal *"— (não é dado pessoal)"* e a observação *"quem lê
> o repositório cria um token de qualquer usuário ou entra direto no banco"*.

**▶ Agora no G1:** abra os três arquivos (**Ctrl+P** e digite o nome): `application.properties`,
`docker-compose.yml` e `JwtUtil.java`. Em cada um, procure (**Ctrl+F**) as palavras `password`,
`senha`, `secret` e `SECRET`. Para o G1, a **chave do token** é um ativo
central: ela é, na prática, a "chave mestra" da F-11. Anote **o que** cada segredo é e **onde**
está — **não** copie os valores para o dossiê.

---

## Passo 9 — Monte a §1 do dossiê

> 🔎 **Como o Grupo 5 fez:** transformou as anotações em 5 fluxos críticos e 9 linhas de ativos.
> Veja o resultado: [`docs/12` §1](../../docs/12-exemplo-dossie-preenchido.md#1-fase-0--inventário--classificação-de-dados).
> Repare na coluna "Observação": cada linha diz **o que foi visto**, não uma generalidade.

**▶ Agora no G1:**

**Fluxos críticos** — 3 a 5, cada um com: **nome** — o que acontece em uma frase — **por que é
crítico**. Inclua o ausente.

**Tabela de ativos** — uma linha por ativo:

| Coluna | Como preencher |
|---|---|
| **Ativo / Dado** | nome + **onde está** (tabela.coluna, arquivo, `localStorage`). Ex.: "Hash de senha (`usuario.senha_hash`)" |
| **Fluxo** | em qual fluxo crítico aparece |
| **Nível** | Crítico, Confidencial, Interno ou Público |
| **Atributo** | C, I, D, Autenticidade, Não-repúdio |
| **Quem demanda** | negócio, jurídico/DPO, segurança, operações, titular |
| **Base legal** | **em branco agora** (Fase 1) |
| **Observação** | **o que você viu** que torna o ativo frágil. "Ana e Bruno têm o mesmo hash" é bom; "hash fraco" é pouco |

Ativos que um G1 atento encontra (confira se os seus estão lá): o e-mail de login, a senha e o
seu hash, o papel, o token de sessão, a chave que assina o token, o código de recuperação de
senha, a senha do banco e o **registro de acessos** (ausente).

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| Na aba **Rede** (DevTools) não aparece o pedido de login | as ferramentas foram abertas **depois** do clique em Entrar | com o DevTools aberto na aba Rede, saia e entre de novo |
| `base64: invalid input` ao decodificar o token | o pedaço do meio do token não tem o complemento `=` no fim | use o comando alternativo do Passo 5, item 2 (entre parênteses, logo depois do primeiro) |
| O terminal parou depois do `logs -f` e não aceita comandos | o `-f` acompanha o log ao vivo, de propósito | faça os pedidos num **segundo** terminal (o **+** do painel); para sair do primeiro, **Ctrl+C** |

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-0`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 0 - inventario"
git push -u origin dossie/fase-0
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
   **compare** escolha `dossie/fase-0` → **Create pull request**.
2. Título: "Dossiê — Fase 0: inventário". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] Cada linha da tabela foi **observada** por mim (curl, tela, banco ou arquivo).
- [ ] A coluna "Observação" diz o que eu vi, com detalhe.
- [ ] Incluí pelo menos um ativo que **não é dado pessoal**.
- [ ] Incluí o que **falta** (registro de acessos) como fluxo ausente.
- [ ] As saídas importantes estão em `entregaveis/evidencias/`.

## Erros comuns

- **Classificar tudo como Crítico.** Justifique cada nível.
- **Esquecer os segredos do sistema.** A chave do token vale mais que qualquer senha individual.
- **Pôr artigo de lei na base legal agora.** É na Fase 1.
