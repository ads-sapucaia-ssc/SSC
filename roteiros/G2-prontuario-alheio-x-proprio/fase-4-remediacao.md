# G2 · Fase 4 — Correção do P0, plano de remediação e especificação da exportação

> **Seção do dossiê:** §6 · **Lidera:** 💻 Dev (correção) e 🛡️ Segurança (revisão e plano) ·
> **Aula:** 6 · **Semanas:** 7–10
> A **Parte A** é feita na **aula de laboratório guiada**, com o professor. Não precisa saber Java:
> o roteiro explica cada linha.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G2**.

## O que você entrega no fim

1. **Parte A — Prova de conceito:** o P0 (IDOR no laudo) corrigido num PR revisado, com o
   `idor.feature` passando de 🔴 para 🟢, e a §6.1 escrita.
2. **Parte B — Plano de remediação:** a §6.2.
3. **Parte C — Especificação da criação (F-09):** a exportação do titular **projetada**.

---

# Parte A — Corrigir o P0 (aula guiada)

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório.
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

**A falha.** O `ExameController.getExame` faz duas coisas: confere se **existe um token válido**
(alguém logado) e busca o exame pelo número. Ele **nunca pergunta**: *"este exame é desta
pessoa?"*. Estar logado (autenticação) não é o mesmo que poder ver aquele dado (autorização).

**A correção: autorização no servidor, negando por padrão.** Antes de devolver o exame, o servidor
descobre **quem** está pedindo (pelo token) e aplica uma regra:

| Papel de quem pede | Pode ver o exame se… |
|---|---|
| PACIENTE | o exame é **dele** (o `paciente_id` do exame é o paciente ligado ao usuário) |
| MEDICO | ele **atende** o paciente do exame (tabela `atendimento` — o `ExameDao.medicoAtende` já existe!) |
| BIOQUIMICO | o exame **ainda não foi publicado** (é o trabalho do laboratório) |
| qualquer outro | **não pode** (*deny-by-default*) |

Se a regra disser "não", a resposta é **403**, **sem nada** do exame no corpo.

**Por que no servidor?** Esconder um botão na tela não protege nada: quem usa curl não vê a tela.

> **A regra da tabela é uma proposta.** Ela vem do quadro de atores do [`docs/00`](../../docs/00-visao-geral.md)
> e das decisões da Fase 2b ("necessidade de saber"). Se o grupo decidiu diferente, ajuste — e
> justifique no dossiê.

---

## Passo 1 — Faça o inventário da classe de falha

> 🔎 **Como o Grupo 5 fez:** antes de corrigir **um** ponto, procurou **todos** (Semgrep +
> **Ctrl+Shift+F**), montou a tabela arquivo/método/linha/entrada/endpoint e corrigiu a classe no
> mesmo PR. Veja: [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G2:** a falha de vocês é **lógica**: nenhum scanner a encontra (ela não tem
"cara" no código). O inventário é **à mão**:

1. **Ctrl+Shift+F** → `@PathVariable` e depois `pacienteId`. Cada resultado é um endpoint que
   recebe um número vindo do usuário.
2. Para cada um, pergunte: *ele confere se a pessoa pode ver/mexer naquele objeto?*
3. Monte a tabela no rascunho: arquivo | método | endpoint | que número vem do usuário | confere o
   dono? | é do pacote do G2?
4. **Decisão do G2:** os três caminhos até o laudo — `getExame`, `getPdf` e `porPaciente` — entram
   no mesmo PR: corrigir só o que o teste cobre deixaria o laudo vazando pelos outros dois. Os
   demais (ex.: `solicitar`, que aceita o `pacienteId` de outra pessoa; os endpoints de paciente e
   de nota clínica) vão para o plano (Parte B), com o pacote dono.

## Passo 2 — Crie a branch da correção

```bash
git checkout main
git pull
git checkout -b fix/F-02-idor
```

## Passo 3 — Registre o "antes"

> 🔎 **Como o Grupo 5 fez:** guardou a saída do ataque e o teste vermelho **antes** de mexer no
> código.

**▶ Agora no G2:** religue o VidaPlus com o banco original e registre quem vê o quê, e o teste
vermelho:

```bash
bash roteiros/comum/zerar-banco.sh
bash roteiros/G2-prontuario-alheio-x-proprio/acessos-por-papel.sh | tee entregaveis/evidencias/fase4-acessos-antes.txt
bash roteiros/comum/rodar-testes.sh "security & F-02" 2>&1 | tee entregaveis/evidencias/fase4-teste-antes.txt
```

O script `acessos-por-papel.sh` (abra-o para ler: são poucos comandos `curl`) entra com quatro
usuários e pede nove combinações de pessoa × endereço. Para cada uma, mostra o código recebido e o
que **deveria** acontecer. Hoje, **todas** dão 200 — inclusive as que deveriam dar 403. O teste
deve sair **vermelho** (`expected: <403> but was: <200>`).

## Passo 4 — Ensine o `UsuarioDao` a responder "quem é este usuário"

O token diz que a Ana é o **usuário 5**, mas a regra precisa saber que ela é a **paciente 1**
(Fase 0, Passo 5). Essa ligação está na tabela `usuario` (`paciente_id`) e, para médicos, na
tabela `medico` (`usuario_id`).

Abra `dao/UsuarioDao.java`. Logo **antes** do comentário de
várias linhas que começa com `/**` e cuja linha seguinte diz
`* Lista todos os usuarios (tela de gestao do ADMIN).` (procure com **Ctrl+F** →
`Lista todos os usuarios`; o `/**` fica na linha de cima), numa linha em branco, cole:

```java
    /** Numero do paciente ligado a este usuario (ou null, se nao for paciente). */
    public Long pacienteIdDoUsuario(long usuarioId) {
        return umNumero("SELECT paciente_id FROM usuario WHERE id = ?", usuarioId);
    }

    /** Numero do medico ligado a este usuario (ou null, se nao for medico). */
    public Long medicoIdDoUsuario(long usuarioId) {
        return umNumero("SELECT id FROM medico WHERE usuario_id = ?", usuarioId);
    }

    /** Roda uma consulta que devolve um unico numero. O "?" recebe o parametro. */
    private Long umNumero(String sql, long parametro) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;          // nenhuma linha
                long valor = rs.getLong(1);
                return rs.wasNull() ? null : valor;   // coluna vazia (NULL) -> null
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar usuario", e);
        }
    }
```

Repare que as consultas novas usam `?` (consulta parametrizada), nunca `+` com o dado. É o padrão
do `alternarAtivo`, no fim do mesmo arquivo.

## Passo 5 — Escreva a regra de acesso no `ExameController`

Abra `controller/ExameController.java`.

**5.1 — Imports.** No topo, troque a linha `import org.springframework.web.bind.annotation.*;` por:

```java
import br.ifsul.vidaplus.dao.UsuarioDao;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
```

**5.2 — A regra.** Logo **depois** das linhas

```java
    @Autowired
    private ExameDao exameDao;
```

cole:

```java
    @Autowired
    private UsuarioDao usuarioDao;

    /**
     * Deny-by-default: so pode ver os dados clinicos de um paciente
     * o proprio paciente ou o medico que o atende. Qualquer outro papel: nao.
     */
    private boolean podeVerPaciente(Long uid, String papel, long pacienteId) {
        if ("PACIENTE".equals(papel)) {
            Long meuPaciente = usuarioDao.pacienteIdDoUsuario(uid);
            return meuPaciente != null && meuPaciente == pacienteId;
        }
        if ("MEDICO".equals(papel)) {
            Long meuMedico = usuarioDao.medicoIdDoUsuario(uid);
            return meuMedico != null && exameDao.medicoAtende(meuMedico, pacienteId);
        }
        return false;   // qualquer outro papel (ou papel desconhecido): nega
    }

    /** O laboratorio ve os exames ainda em processamento (nao publicados). */
    private boolean podeVerExame(Long uid, String papel, Map<String, Object> exame) {
        if ("BIOQUIMICO".equals(papel)) {
            return ((Number) exame.get("publicado")).intValue() == 0;
        }
        long pacienteId = ((Number) exame.get("paciente_id")).longValue();
        return podeVerPaciente(uid, papel, pacienteId);
    }
```

Lendo devagar:

- `podeVerPaciente` aplica a tabela dos "Conceitos" para **um paciente**. Repare no `return false`
  no fim: tudo o que não foi expressamente permitido é **negado**.
- `podeVerExame` acrescenta a regra do laboratório e, para os outros papéis, pergunta "pode ver o
  **paciente** deste exame?".
- `((Number) exame.get("paciente_id")).longValue()` só tira o número de dentro do `Map` que o DAO
  devolve.

**5.3 — Use a regra no `getExame`.** No método `getExame`, troque:

```java
        // FALHA: nenhuma verificacao de que 'id' pertence a 'uid'
        return exameDao.porId(id);
```

por:

```java
        String papel = JwtUtil.papelDoToken(auth.substring(7));
        Map<String, Object> exame = exameDao.porId(id);
        if (exame == null || !podeVerExame(uid, papel, exame)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return exame;
```

- `auth.substring(7)` tira o `"Bearer "` do começo do cabeçalho e deixa só o token.
- `exame == null` (o exame não existe) e "não pode ver" recebem **a mesma resposta: 403**. Foi a
  decisão do oráculo de existência (Fase 2b): o atacante não consegue distinguir "não existe" de
  "não é seu".
- `ResponseStatusException(HttpStatus.FORBIDDEN)` faz o Spring responder **403**, sem o exame.

**5.4 — Agora com você: `getPdf` e `porPaciente`.**

- No `getPdf`, troque:

  ```java
          Map<String, Object> exame = exameDao.porId(id);
          // FALHA: devolve o caminho publico do PDF sem checar ownership
  ```

  por (o `return Map.of(...)` que vem logo depois **continua** como está):

  ```java
          String papel = JwtUtil.papelDoToken(auth.substring(7));
          Map<String, Object> exame = exameDao.porId(id);
          if (exame == null || !podeVerExame(uid, papel, exame)) {
              throw new ResponseStatusException(HttpStatus.FORBIDDEN);
          }
  ```

- No `porPaciente`, antes do `return exameDao.doPaciente(pacienteId);`, apague o comentário
  `// FALHA` e coloque:

  ```java
          String papel = JwtUtil.papelDoToken(auth.substring(7));
          if (!podeVerPaciente(uid, papel, pacienteId)) {
              throw new ResponseStatusException(HttpStatus.FORBIDDEN);
          }
  ```

## Passo 6 — Remonte, ataque de novo e rode os testes

> 🔎 **Como o Grupo 5 fez:** repetiu **o mesmo ataque** do "antes", conferiu o **uso legítimo**,
> rodou os testes e guardou cada saída como "depois".

**▶ Agora no G2:**

1. Remonte o backend com o código novo e o banco original (a compilação acontece aqui: se houver
   erro de digitação, o script termina com `ERRO ao ligar` e mostra o arquivo e a linha):

   ```bash
   bash roteiros/comum/zerar-banco.sh
   ```

2. Rode de novo o script de acessos, agora gravando no arquivo do "depois":

   ```bash
   bash roteiros/G2-prontuario-alheio-x-proprio/acessos-por-papel.sh | tee entregaveis/evidencias/fase4-acessos-depois.txt
   ```

3. Compare com o "antes". Esperado:

   | Pedido | Antes | Depois |
   |---|:-:|:-:|
   | Ana → 101 (dela) | 200 | **200** |
   | Ana → 102 (do Bruno) | 200 | **403** |
   | Ana → 102/pdf | 200 | **403** |
   | Ana → lista do paciente 2 | 200 | **403** |
   | House → 101 (Ana, que ela atende) | 200 | **200** |
   | House → 102 (Bruno, que ela não atende) | 200 | **403** |
   | Bio → 104 (não publicado) | 200 | **200** |
   | Bio → 101 (já publicado) | 200 | **403** |

4. **Pela tela:** entre como Ana e troque `/exames/101` por `/exames/102` na barra de endereços: a
   tela deve mostrar "Exame não encontrado ou acesso negado.". Entre como Bio e abra a **Fila de
   Laudos**: o exame 104 continua abrindo e o botão **Publicar laudo** aparece?
5. **A resposta negada não revela nada:**
   `curl -s localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_ANA"` não pode conter
   "Glicemia" nem "paciente_id".
6. **Os testes:**

   ```bash
   bash roteiros/comum/rodar-testes.sh "security & F-02" 2>&1 | tee entregaveis/evidencias/fase4-teste-depois.txt
   bash roteiros/comum/rodar-testes.sh 2>&1 | tee entregaveis/evidencias/fase4-todos-depois.txt
   ```

   O primeiro deve sair **verde**. O segundo continua vermelho pelos testes de SQLi e RBAC (de
   outros grupos): confira que **nenhuma** falha fala de `idor` e que `login_valido` não falhou.

   > ⚠️ **Os testes mudam o banco:** o teste de RBAC (do G3) **publica o laudo 104**. Depois de
   > rodá-los, o Bio deixa de ver o 104 (ele só vê os não publicados). Por isso a tabela de
   > acessos roda **antes** dos testes — e, se quiser repeti-la, **zere o banco** antes (rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`).

**Deu errado?**

| Sintoma | Causa provável |
|---|---|
| `cannot find symbol: class UsuarioDao` ou `HttpStatus` | faltou um `import` (5.1) |
| `cannot find symbol: method pacienteIdDoUsuario` | o Passo 4 não foi salvo, ou o nome está diferente |
| A Ana não vê nem o **próprio** exame | a ligação usuário → paciente: confira o SQL `SELECT paciente_id FROM usuario WHERE id = ?` |
| O Bio não abre a Fila de Laudos | a regra do `BIOQUIMICO` em `podeVerExame` |

## Passo 7 — Grave, abra o PR e peça a revisão

```bash
git add legacy-app/backend/src/main/java entregaveis/evidencias
git commit -m "fix(F-02): autorizacao por dono no acesso ao laudo (deny-by-default)"
git push -u origin fix/F-02-idor
```

Abra o PR ([`comum/03` §3, passo 7](../comum/03-git-e-github.md#passo-7--abra-o-pull-request)). Na
descrição: **o que muda** (a regra, os três endpoints), **como testar** (o bloco do Passo 3 e a
tabela esperada) e **o teste que deve passar** (`idor.feature`).

**Quem revisa** usa o checklist do [`docs/07`](../../docs/07-rubrica-avaliacao.md#checklist-de-secure-code-review-para-os-prs-da-poc)
— em especial "autorização server-side (ownership + RBAC deny-by-default)" — e **roda** o Passo 6
na própria máquina antes de aprovar.

## Passo 8 — Escreva a §6.1

> 🔎 **Como o Grupo 5 fez:** **Antes**, **Depois** (arquivo por arquivo), **O que rejeitamos e por
> quê**, **O que esta correção NÃO resolve**, **PR**, **Teste**. Veja:
> [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G2:** os mesmos blocos. Para **"O que rejeitamos"**, pense: por que não bastava
esconder o exame alheio **na tela**? Por que não bastava conferir o dono **só** no `getExame`?
Para **"O que esta correção NÃO resolve"**:

- o **arquivo PDF** continua na pasta pública, baixável sem login?
- os números continuam sequenciais?
- quem não está logado recebe **401** ou ainda um erro 500?
- a publicação continua aberta a qualquer papel?

Cada "sim" vai para a Parte B.

---

# Parte B — Plano de remediação

> 🔎 **Como o Grupo 5 fez:** tabela **funcionalidade | falha | controle proposto | esforço (P/M/G)
> | prioridade | observação**, com a escala de esforço definida; o que dependia de outro pacote,
> marcado como "transferido". Veja: [`docs/12` §6.2](../../docs/12-exemplo-dossie-preenchido.md#62-plano-de-remediação-demais-falhas-do-pacote--não-implementadas).

**▶ Agora no G2:** uma linha para **cada falha não corrigida**. Pontos de partida:

| Falha | Perguntas para chegar ao controle |
|---|---|
| PDF na pasta pública (F-08) | o arquivo deveria sair da pasta pública e ser entregue **pela API**, com a mesma regra do `podeVerExame`? Ou por um link que expira? Inclua o teste com o laudo **não publicado** |
| Publicação por qualquer papel (F-07) | só o laboratório publica — e é o P0 do G3 (`rbac.feature`): como os dois controles se completam? |
| Integridade do laudo (F-07) | como preencher o `hash_laudo` na publicação, e quem confere depois? |
| Números sequenciais | trocar por identificadores aleatórios (UUID)? Quem é dono disso? |
| `solicitar` aceita o `pacienteId` de outra pessoa | o paciente deveria vir do token, nunca do corpo do pedido |
| Sem login → 500 em vez de 401 | qual pacote cuida de erros (F-16)? |

---

# Parte C — Especificar a exportação do titular (F-09)

Nesta turma, a criação é **especificada**, não implementada. O nível esperado é o do Controle 2
do Grupo 5 — **sem** a implementação.

> 🔎 **Como o Grupo 5 fez** (a criação dele era a anonimização): **entidade**, **gatilho** com
> colunas que existem, **tratamento atributo por atributo** (tabela), **limitações** assumidas e o
> `.feature @analise`. Veja o Controle 2 em [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G2** — escopo mínimo ([`docs/05`](../../docs/05-pacotes-trabalho-estudantes.md#escopo-mínimo-da-poc--criação--para-equiparar-o-esforço)):
dados de **1 titular**, formato **estruturado** (JSON ou CSV), **autenticado**. Escreva:

1. **Quem pede e como o sistema sabe que é ele.** Regra de ouro (é a lição do P0): o titular vem
   **do token**, **nunca** de um número enviado no pedido.
2. **O endereço proposto** (ex.: um `GET` sem número de paciente) e o que ele responde.
3. **O conteúdo, tabela por tabela** — tabela | colunas incluídas | colunas excluídas | por quê.
   Partam da lista do Passo 4 da Fase 0. Decidam sobre: notas clínicas, PDFs, `publicado_por`,
   nome do médico, `senha_hash`.
4. **O formato:** um exemplo do JSON (inventado, curto) mostrando a estrutura.
5. **A entrega:** download direto na sessão autenticada, ou link? Se link, por quanto tempo vale?
6. **O registro do pedido:** usem a `entity/SolicitacaoTitular.java` (`tipo = EXPORTACAO`,
   `status`, datas). Qual o prazo legal de resposta? (Pesquisem o art. 19 da LGPD.)
7. **Comportamento sob falha** (Fase 3) e **limites** (quantas exportações por dia?).
8. **Limitações assumidas:** o que este desenho **não** cobre?
9. **O `.feature @analise`** da F-09 (Fase 1), com pelo menos: "o titular recebe os próprios
   dados" e "o arquivo não contém dados de outro paciente".

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
- [ ] Tenho a tabela de acessos e o teste, **antes e depois**, em `entregaveis/evidencias/`.
- [ ] Cada papel continua fazendo o seu trabalho legítimo (Ana, House, Bio).
- [ ] A §6.1 tem antes, depois, o que rejeitamos, o que **não** resolve, PR e teste.
- [ ] Toda falha não corrigida está na §6.2.
- [ ] A F-09 está especificada nos 9 itens da Parte C.
