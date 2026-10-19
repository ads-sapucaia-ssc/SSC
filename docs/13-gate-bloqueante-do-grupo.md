# 13 · Como tornar bloqueante o gate do seu grupo

> **Material opcional.** Nesta turma, os gates ficam **informativos** e ninguém precisa torná-los
> bloqueantes: na Fase 6, basta **explicar** no dossiê por que o gate do seu pacote deveria
> bloquear num projeto real. Este guia é para o grupo que quiser ir além e fazer isso na prática.

Este guia mostra, passo a passo, como fazer o GitHub **impedir que um código entre na `main`**
quando um controle de segurança do **seu** pacote quebrar.

Não é preciso já saber GitHub Actions: o guia explica cada termo e cada clique. Se você nunca clonou
o repositório do grupo, faça antes o `docs/08` §1.

---

## 1. Antes de começar

Você vai precisar de:

- **o repositório do grupo clonado** no seu computador (`docs/08` §1);
- **o VS Code** (ou outro editor) e **um terminal** — no VS Code, menu **Terminal → New Terminal**;
- **para o passo 9**, um integrante com permissão de **admin** no repositório. Como saber: na página
  do repositório no GitHub, só quem é admin vê a aba **Settings** (engrenagem, no alto, à direita).

## 2. Vocabulário mínimo

| Termo | O que é, neste projeto |
|---|---|
| **Repositório** | a pasta do projeto do grupo, guardada no GitHub. |
| **Branch** | uma "linha paralela" do projeto. Você muda o código numa branch sua, sem mexer na `main`, que é a versão oficial. |
| **Commit** | um "ponto salvo" no histórico, com uma mensagem dizendo o que mudou. |
| **Push** | enviar os seus commits do computador para o GitHub. |
| **Pull Request (PR)** | o pedido, feito no GitHub, para juntar a sua branch à `main`. É onde um colega revisa e onde os testes automáticos rodam. |
| **Merge** | o ato de juntar a branch à `main`, depois do PR aprovado. |
| **Pipeline / workflow** | a lista de verificações automáticas que o GitHub roda a cada push. Está no arquivo `.github/workflows/ssdlc-pipeline.yml`. |
| **Job** | uma das verificações do pipeline. Cada job tem um nome, por exemplo "Gate - Secret Scanning (Gitleaks)". |
| **Gate** | um job de segurança: verde = passou, vermelho = encontrou problema. |
| **Check** | como o GitHub chama o resultado de um job dentro de um PR. |
| **YAML** | o formato do arquivo do pipeline. É texto organizado por **recuo (espaços)**: o número de espaços no início da linha muda o significado. |
| **Ruleset** | uma regra do GitHub que protege a `main` — por exemplo, "só faz merge com PR aprovado". |
| **Tag** | uma etiqueta num teste (`@F-02`, `@Tag("F-02")`), usada para escolher quais testes rodar. |

## 3. Como enxergar o pipeline funcionando

Antes de mudar qualquer coisa, veja o pipeline rodando:

1. Abra o repositório do grupo no GitHub e clique na aba **Actions** (no alto da página).
2. A lista mostra as **execuções**: cada push gera uma. Clique na mais recente.
3. À esquerda aparece a lista de **jobs**. Cada um tem um ícone:
   - **✓ verde** — o job passou;
   - **✗ vermelho** — o job falhou.
4. Clique num job para ver o **log**: a saída de cada etapa. Clique numa etapa para expandi-la. A
   caixa **"Search logs"**, no canto superior direito, procura um texto dentro do log.

Repare: alguns jobs aparecem com **✗ vermelho**, mas a execução inteira termina como **concluída**.
É o efeito da linha `continue-on-error: true` que esses jobs têm no arquivo do pipeline. Ela diz ao
GitHub: "se este job falhar, siga em frente". Um job assim é **informativo**: mostra o problema, mas
não impede nada.

## 4. O que significa "bloqueante"

Um gate só bloqueia de verdade quando **duas coisas** acontecem juntas:

1. **No pipeline:** o job **reprova a execução** quando falha. Para isso, ele **não** pode ter a
   linha `continue-on-error: true`.
2. **Na proteção da `main`:** o GitHub **exige** que aquele job esteja verde antes de liberar o
   merge. Isso é configurado no **ruleset**. Sem essa regra, mesmo com o job vermelho, o botão de
   merge continua liberado.

Este guia faz as duas coisas.

## 5. Por que não usar o "Gate - Testes de seguranca"

O job **"Gate - Testes de seguranca"** roda os testes de segurança de **todos** os pacotes. O seu
repositório ainda tem as falhas dos outros grupos — que vocês não vão corrigir —, então esse job
**continua vermelho** mesmo depois que vocês corrigirem o seu P0. Se ele virar bloqueante, ninguém
do grupo consegue mais fazer merge.

**Deixe esse job como está (informativo).** Você vai criar um job **novo**, chamado **"Gate - Testes
do pacote"**, que roda **só os testes do seu grupo**.

## 6. Como o job novo sabe quais são os seus testes

Pelas **tags de funcionalidade**, que funcionam como etiquetas.

Neste projeto, cada grupo **implementa só o P0** do seu pacote; a funcionalidade de criação é apenas
**especificada** (fica num `.feature` com `@analise`, que nunca roda no pipeline). E o teste do P0
**já vem pronto** no projeto, como um cenário num arquivo `.feature`, com a tag da funcionalidade na
segunda linha. Por exemplo, o
`legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/idor.feature` começa assim:

```gherkin
# language: pt
@F-02 @REQ-AC-IDOR-01 @P0 @lgpd-art11 @lgpd-art46 @asvs-V4 @stride-EoP @linddun-divulgacao @security
```

O `@F-02` diz que esse cenário prova a funcionalidade F-02. Por isso **você não precisa marcar nada**:
o teste do seu P0 já entra no filtro.

**O teste de cada grupo** (todos os arquivos ficam em
`legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/`):

| Grupo | Arquivo | Tag | Cenário | O que o teste verifica |
|---|---|---|---|---|
| **G1** | `sqli.feature` | `@F-01` | "payload de injeção no login não autentica" | tenta entrar com um e-mail que contém um ataque de injeção de SQL; o login precisa ser **recusado** (resposta 401) e **não** pode devolver token de acesso |
| **G2** | `idor.feature` | `@F-02` | "paciente não acessa o laudo de outro paciente" | a paciente Ana, logada, pede o exame 102, que é do Bruno; o acesso precisa ser **negado** (resposta 403) e a resposta **não** pode mostrar o tipo do exame ("Glicemia") |
| **G3** | `rbac.feature` | `@F-03` | "paciente não pode publicar laudo" | a paciente Ana, logada, tenta publicar o laudo do exame 104; precisa ser **negado** (resposta 403), porque só o laboratório publica |
| **G4** | `idor.feature` | `@F-02` | "paciente não acessa o laudo de outro paciente" | o mesmo teste do G2: o P0 dos dois grupos é o F-02, cada um com o seu recorte |

**No código original, o teste do seu grupo falha** — é de propósito: ele mostra a falha que o seu P0
precisa corrigir. Por isso o job "Gate - Testes do pacote" só fica **verde depois** que a correção
do grupo entrar na `main`. Abra o arquivo do seu grupo e leia o cenário: ele é curto e descreve,
em português, exatamente o que precisa passar a acontecer.

**O filtro** usa uma expressão como `security & F-02`. Leia assim: "testes de segurança **e** da
funcionalidade F-02" (`&` = **e**). Os testes dos outros pacotes ficam de fora.

> O `SegurancaAceiteTest.java`, que veio pronto no projeto, tem os mesmos testes-semente, mas só com a
> tag `security`, e por isso **não** entra no filtro. Não faz falta: o mesmo teste existe como
> cenário no `.feature`, que entra. **Não altere o `SegurancaAceiteTest.java`.**

## 7. A expressão do seu grupo

| Grupo | P0 implementado | Expressão do filtro | O que entra |
|---|---|---|---|
| **G1** — Identidade & Trilha de Acesso | F-01 | `security & F-01` | o cenário do `sqli.feature` |
| **G2** — Prontuário: dado alheio × próprio | F-02 | `security & F-02` | o cenário do `idor.feature` |
| **G3** — Papéis & Consentimento | F-03 | `security & F-03` | o cenário do `rbac.feature` |
| **G4** — Vazamento & Ciclo de Vida | F-02 | `security & F-02` | o cenário do `idor.feature` |

- **Os cenários que vocês acrescentarem ao `.feature` do P0** (por exemplo, mais casos no
  `idor.feature`) entram junto, porque a tag está no cabeçalho do arquivo.
- **As especificações da criação** (`.feature` com `@analise`) **não** entram, e é o certo: são
  requisitos só especificados, sem implementação.

---

## 8. Passo a passo

### Passo 1 — Abra o projeto e o terminal

1. No VS Code, abra a pasta do repositório do grupo (**File → Open Folder…**).
2. Abra o terminal: **Terminal → New Terminal**. Ele aparece na parte de baixo da janela, já dentro
   da pasta do projeto.
3. Confira que está no lugar certo:

   ```bash
   git status
   ```

   A resposta começa com **`On branch …`**. Se aparecer `fatal: not a git repository`, o terminal
   está na pasta errada: abra a pasta certa no VS Code e repita.

### Passo 2 — Atualize a `main` e crie a sua branch

Rode um comando de cada vez:

```bash
git checkout main
```

Muda para a `main`. Resposta esperada: `Switched to branch 'main'` (ou `Already on 'main'`).

```bash
git pull
```

Traz do GitHub o que os colegas já juntaram à `main`. Resposta esperada: a lista de arquivos
atualizados, ou `Already up to date.`

```bash
git checkout -b ci/gate-do-pacote
```

Cria uma branch nova chamada `ci/gate-do-pacote` e muda para ela. Resposta esperada:
`Switched to a new branch 'ci/gate-do-pacote'`. A partir daqui, tudo o que você mudar fica nessa
branch, sem afetar a `main`.

> **Se o `git checkout main` reclamar de "local changes"**, você tem alterações não salvas em outra
> branch. Grave-as primeiro (passo 5) ou peça ajuda ao grupo antes de seguir.

### Passo 3 — Só se o grupo escreveu testes extras do P0

Na Fase 5, o grupo pode **ampliar** os testes do seu P0 (o `docs/08` §5.1 incentiva). Se fizer isso,
os testes novos precisam da tag do P0 para entrar no filtro:

- **Se o teste novo for um cenário num `.feature`:** escreva-o no **mesmo arquivo** do P0 (por
  exemplo, no `idor.feature`). A tag do cabeçalho já vale para ele. Se criar um arquivo novo, copie
  para a segunda linha as tags do arquivo do P0, incluindo `@F-0x` e `@security`.
- **Se o teste novo for uma classe JUnit** (em
  `legacy-app/backend/src/test/java/br/ifsul/vidaplus/security/`): logo abaixo da linha
  `@Tag("security")`, acrescente a tag do seu P0. Não precisa de nenhum `import` novo.

  ```java
  @Tag("security")
  @Tag("F-02")      // ← linha nova: a funcionalidade do seu P0
  class IdorExtraTest {
      ...
  }
  ```

**Não** escreveu testes extras? Pule este passo: o cenário pronto do P0 já está marcado.

### Passo 4 — Crie o job no arquivo do pipeline

1. No Explorer, abra `.github/workflows/ssdlc-pipeline.yml`.
2. Aperte **Ctrl+F** e busque `security-tests:`. Você chega ao começo do job "Gate - Testes de
   seguranca".
3. Desça até o **fim** desse job. A última linha dele é:

   ```yaml
           run: mvn -B -q test -Dgroups=security
   ```

   Logo depois vem uma linha em branco e um bloco de comentários que começa com
   `# ====================`.
4. Clique no fim da linha `run: mvn -B -q test -Dgroups=security`, aperte **Enter** duas vezes e
   **cole** o job abaixo:

   ```yaml
     testes-do-pacote:
       name: "Gate - Testes do pacote"
       runs-on: ubuntu-latest
       services:
         mysql:
           image: mysql:8.4
           env:
             MYSQL_ROOT_PASSWORD: root123
             MYSQL_DATABASE: vidaplus
           ports:
             - "3306:3306"
           options: >-
             --health-cmd="mysqladmin ping -h 127.0.0.1 -uroot -proot123"
             --health-interval=10s --health-timeout=5s --health-retries=10
       steps:
         - uses: actions/checkout@v4
         - uses: actions/setup-java@v4
           with: { distribution: temurin, java-version: "21", cache: maven }
         - name: Carregar schema + seed no MySQL de teste
           run: |
             sudo apt-get update && sudo apt-get install -y mysql-client
             mysql -h127.0.0.1 -uroot -proot123 < legacy-app/db/schema.sql
             mysql -h127.0.0.1 -uroot -proot123 vidaplus < legacy-app/db/seed.sql
         - name: Testes de seguranca do nosso pacote
           working-directory: legacy-app/backend
           run: mvn -B -q test "-Dgroups=security & F-02"   # <-- troque pela expressão do seu grupo
   ```

5. Na última linha, troque `security & F-02` pela **expressão do seu grupo** (item 7).
   Mantenha as aspas e o `-Dgroups=` como estão.
6. Salve (**Ctrl+S**).

**Confira a indentação — é o erro mais comum.** O job copia a estrutura do `security-tests`, que está
logo acima:

- a linha `testes-do-pacote:` começa com **2 espaços**, exatamente na mesma coluna de
  `security-tests:`;
- `name:`, `runs-on:`, `services:` e `steps:` começam com **4 espaços**;
- o resto segue o recuo do bloco colado.

Dica: no VS Code, a posição do cursor aparece no canto inferior direito ("Ln 290, Col 3"). Na linha
`testes-do-pacote:`, o `t` deve ficar na **coluna 3** (2 espaços antes).

**Não** coloque a linha `continue-on-error: true` neste job: é a ausência dela que o torna
bloqueante.

**Por que as aspas no `-Dgroups`:** sem elas, o terminal do GitHub interpretaria o `&` como
um comando dele, e não como parte do filtro.

### Passo 5 — Grave no Git e envie ao GitHub

Rode um comando de cada vez:

```bash
git status
```

Lista o que você mudou. Deve aparecer o `ssdlc-pipeline.yml` e, se fez o passo 3, os testes novos.

```bash
git add .github/workflows/ssdlc-pipeline.yml legacy-app/backend/src/test
```

Marca essas mudanças para entrar no próximo commit. Não mostra resposta quando dá certo.

```bash
git commit -m "ci: cria o gate bloqueante com os testes do pacote"
```

Cria o "ponto salvo" com uma mensagem. Resposta esperada: uma linha com o nome da branch e um resumo,
como `1 file changed, 30 insertions(+)`.

```bash
git push -u origin ci/gate-do-pacote
```

Envia a branch para o GitHub. Resposta esperada: algumas linhas terminando com
`branch 'ci/gate-do-pacote' set up to track 'origin/ci/gate-do-pacote'`. Se pedir usuário e senha,
veja a autenticação no `docs/08` §1.

### Passo 6 — Abra o Pull Request

1. Abra o repositório do grupo no GitHub.
2. Logo após o push, aparece uma faixa amarela com o botão **"Compare & pull request"**. Clique nele.
   - **A faixa não apareceu?** Clique na aba **Pull requests → New pull request**. Em **base**,
     deixe `main`; em **compare**, escolha `ci/gate-do-pacote`. Depois, **Create pull request**.
3. Escreva um título (por exemplo, "Gate bloqueante com os testes do pacote") e clique em
   **"Create pull request"**.

### Passo 7 — Acompanhe o pipeline no PR

1. Na página do PR, desça até a caixa de **checks**, logo acima do botão de merge. Ela mostra cada job
   do pipeline rodando, com um ícone amarelo enquanto roda.
2. Espere terminar — pode levar alguns minutos. Se a lista estiver recolhida, clique em
   **"Show all checks"**.
3. Encontre **"Gate - Testes do pacote"** e clique em **"Details"**. Abre o log do job.
4. Clique na etapa **"Testes de seguranca do nosso pacote"** para expandi-la e confira:
   - **quais testes rodaram:** use **"Search logs"** e procure `Tests run:`. Devem aparecer só
     classes e cenários do seu grupo;
   - **quantos:** deve haver pelo menos o cenário do seu P0. Se aparecer `Tests run: 0`, veja a
     seção 10.
5. O resultado precisa ser **✓ verde**:
   - **✓ verde:** siga para o passo 8;
   - **✗ vermelho:** algum teste do seu pacote falhou. O seu P0 ainda não está
     corrigido. Corrija e faça um novo push para a mesma branch: o PR atualiza sozinho e o pipeline
     roda de novo. **Não faça o passo 9 com o job vermelho**: um check obrigatório vermelho trava a
     `main` do grupo.

### Passo 8 — Revisão e merge

1. **Outro** integrante (não quem abriu o PR) abre o PR, clica na aba **"Files changed"**, confere as
   mudanças e clica em **"Review changes" → "Approve" → "Submit review"**.
2. Com a aprovação e o job verde, clique em **"Merge pull request" → "Confirm merge"**.
3. O GitHub oferece **"Delete branch"**: pode clicar, porque a branch já foi juntada à `main`.

### Passo 9 — Torne o check obrigatório na `main`

Feito por um integrante com permissão de **admin**:

1. No repositório, aba **Settings**.
2. No menu da esquerda, **Rules → Rulesets**.
3. Clique no ruleset **`protege-main`**.
   - **Não existe?** Crie-o antes, seguindo o `docs/08` §5.2.
4. Desça até **"Require status checks to pass"** e marque a opção, se ainda não estiver marcada.
5. Clique em **"Add checks"**, digite **`Gate - Testes do pacote`** e selecione o resultado que
   aparecer.
   - **Não aparece nada?** O check só fica disponível depois de ter rodado pelo menos uma vez — por
     isso os passos 6 e 7 vêm antes. Confira se o PR do passo 6 rodou o job.
6. Clique em **"Save changes"**, no fim da página.

### Passo 10 — Confirme que bloqueia

A partir de agora, em todo PR para a `main`:

- se algum teste do seu pacote falhar, a caixa de checks mostra o aviso de que o merge está
  **bloqueado** (*"Merging is blocked"*) e o botão **"Merge pull request"** fica desabilitado;
- só depois de corrigir, com o job verde, o merge é liberado.

Para ver o bloqueio funcionando, um integrante pode abrir um PR de teste que quebre de propósito um
teste do grupo, conferir o aviso e **fechar o PR sem fazer merge** (botão **"Close pull request"**).

### Passo 11 — Registre no dossiê

Na §8 (Fase 6) do dossiê:

1. Na tabela de gates, acrescente a linha **"Testes do pacote"**: o que ele verifica (só os testes
   do seu P0), o estado antes e depois da correção, e que ele **já é bloqueante** no seu
   repositório.
2. Na linha **"Testes de segurança"** (o geral), explique em uma frase por que ele continua
   informativo: roda os testes de todos os pacotes e segue vermelho pelas falhas dos outros
   (item 5).
3. Como evidência, anexe um print da tela do ruleset com o check na lista, ou do PR de teste
   bloqueado.

---

## 9. Problemas comuns

| O que aparece | O que significa | O que fazer |
|---|---|---|
| Na aba **Actions**, a execução falha com **"Invalid workflow file"** e um número de linha | o YAML ficou com erro, quase sempre de indentação | abra o `ssdlc-pipeline.yml`, vá até a linha indicada e confira os espaços (passo 4) |
| O job **"Gate - Testes do pacote"** não aparece no PR | o job não foi salvo no arquivo, ou o YAML está inválido | confira se o arquivo foi salvo e entrou no commit (`git status` / `git log`) |
| No log, **`Tests run: 0`** ou "No tests to run" | o filtro não encontrou nenhum teste | confira a expressão do item 7 (letras maiúsculas, hífen: `F-02`, não `F02`) e as tags no passo 3 |
| No log, roda teste de **outro** pacote | a expressão tem uma tag a mais | deixe só as funcionalidades do seu grupo |
| No ruleset, **"Add checks"** não acha o nome | o job ainda não rodou nenhuma vez | rode o pipeline num PR (passos 6 e 7) e tente de novo |
| O PR ficou **travado** e o job está vermelho por causa de teste de outro grupo | o check obrigatório é o "Gate - Testes de seguranca" (o geral), não o do pacote | no ruleset, remova esse check e deixe só o "Gate - Testes do pacote" |
| **`git push`** recusado com "rejected" | a `main` ou a sua branch mudou no GitHub | rode `git pull` na sua branch e tente o push de novo |
