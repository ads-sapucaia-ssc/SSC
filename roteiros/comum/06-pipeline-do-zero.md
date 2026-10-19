# 06 · Pipeline (CI/CD) do zero — o que é, como ler e onde clicar

**Objetivo:** entender, sem nenhum conhecimento prévio, o que é o **pipeline** do VidaPlus, o que
cada parte dele faz, e saber **encontrar qualquer resultado** dele no GitHub — clique por clique.

**Usado nas Fases:** 5 (verificação), 6 (pipeline) e 8 (defesa). Também ajuda a entender a Fase 4.

**Antes:** ter feito pelo menos um `push` no repositório do grupo ([`03`](03-git-e-github.md)).
Ajuda ter lido [`01`](01-subir-o-vidaplus.md) (Docker).

---

## 1. O problema que o pipeline resolve

Imagine uma fábrica de remédios. A cada lote produzido, antes de ir para a farmácia, o lote passa
por uma **bancada de inspeção**: um teste confere a dosagem, outro procura contaminação, outro
confere o rótulo. Se um teste falha, o lote fica marcado. Ninguém precisa lembrar de inspecionar:
**a inspeção acontece sozinha, sempre, do mesmo jeito**.

O **pipeline** é essa bancada de inspeção para o código. A cada vez que alguém do grupo envia
código ao GitHub (`git push`), o GitHub, sozinho:

1. pega uma cópia do código;
2. compila o VidaPlus;
3. roda uma série de **verificações de segurança** (procura senhas no código, padrões perigosos,
   bibliotecas com falhas conhecidas, roda os testes de segurança, ataca a aplicação ligada…);
4. mostra o resultado de cada verificação: ✓ verde ou ✗ vermelho.

E, na `main`, ainda **empacota** o sistema e o **entrega** num ambiente de testes e, depois de uma
aprovação humana, em "produção".

## 2. As palavras, em uma frase cada

| Termo | O que é, aqui |
|---|---|
| **CI** (*Continuous Integration*, integração contínua) | a parte que **verifica** o código a cada envio (passos 1 a 4 acima) |
| **CD** (*Continuous Delivery/Deployment*, entrega contínua) | a parte que **empacota e entrega** o sistema |
| **Pipeline** | o conjunto CI + CD: a "linha de montagem com inspeção" |
| **GitHub Actions** | o serviço do GitHub que roda o pipeline. É a aba **Actions** do repositório |
| **Workflow** | o **arquivo** que descreve o pipeline: `.github/workflows/ssdlc-pipeline.yml` |
| **Execução** (*run*) | uma vez que o pipeline rodou. Cada `push` gera uma execução nova |
| **Job** | uma das verificações do pipeline (ex.: "Gate - SAST (Semgrep)"). Os jobs rodam **em paralelo**, cada um num computador separado |
| **Step** (etapa) | um passo dentro de um job (ex.: "baixar o código", "rodar o Semgrep") |
| **Runner** | o computador que o GitHub **empresta** para rodar um job. Ele nasce limpo, roda o job e é **apagado** no fim |
| **Gate** (portão) | um job de **segurança**. Verde = não achou problema; vermelho = achou |
| **Log** | o texto que o job escreve enquanto roda — o "diário" dele |
| **Artefato** (*artifact*) | um arquivo que o job deixa guardado para você baixar depois (ex.: o `sbom`) |
| **Informativo × bloqueante** | um gate **informativo** mostra o problema mas não impede nada; um **bloqueante** impede o merge. **Aqui, todos são informativos** |

## 3. Um passeio pelo arquivo do pipeline

Abra no VS Code: **Ctrl+P** → `ssdlc-pipeline.yml`.

### 3.1 O formato YAML, em 1 minuto

O arquivo está em **YAML**: texto organizado por **recuo** (espaços no começo da linha).

```yaml
nome-da-chave: valor          # uma informação
lista:
  - primeiro item             # o "-" marca um item de lista
  - segundo item
grupo:
  dentro-do-grupo: valor      # 2 espaços a mais = "está dentro de grupo"
```

Tudo depois de `#` é **comentário** (só para humanos). O número de espaços **muda o significado**:
por isso, nesta disciplina, **vocês não editam** este arquivo — só leem.

### 3.2 Quando o pipeline roda

Logo no começo:

```yaml
on:
  push:
    branches: [ "**" ]
  pull_request:
  workflow_dispatch:
```

Em palavras: roda a cada **push**, em **qualquer branch** (`"**"`); roda em todo **Pull Request**; e
pode ser disparado **à mão** (`workflow_dispatch` — o botão **Run workflow**, no §5.8).

### 3.3 Um job, linha por linha

Procure (**Ctrl+F**) `security-tests:`. Você vai ver algo assim (resumido):

```yaml
  security-tests:                                  # o "apelido" do job no arquivo
    name: "Gate - Testes de seguranca"             # o nome que aparece na tela
    runs-on: ubuntu-latest                         # o runner: um Ubuntu emprestado
    continue-on-error: true                        # INFORMATIVO: se falhar, o pipeline segue
    services:
      mysql:                                       # liga um MySQL só para este job
        image: mysql:8.4
    steps:                                         # as etapas, em ordem:
      - uses: actions/checkout@v4                  # 1. baixa o código do repositório
      - uses: actions/setup-java@v4                # 2. instala o Java
      - name: Carregar schema + seed no MySQL de teste
        run: |                                     # 3. roda comandos (cria o banco de teste)
          mysql ... < legacy-app/db/schema.sql
      - name: Testes de aceite de seguranca (@Tag security)
        run: mvn -B -q test -Dgroups=security      # 4. roda os testes de segurança
```

- `uses:` = "use uma peça pronta" (uma *action*, feita por alguém e publicada no GitHub);
- `run:` = "rode estes comandos no terminal do runner" — os mesmos tipos de comando que você roda
  no seu terminal;
- `continue-on-error: true` = é isso que torna o gate **informativo**.

Repare: a etapa 4 faz **a mesma coisa** que o `bash roteiros/comum/rodar-testes.sh` faz no seu
computador. O pipeline é, no fundo, uma lista de comandos que alguém rodaria à mão.

### 3.4 Todos os jobs do VidaPlus

| Nome na tela | O que faz, em palavras | Ferramenta | Quando roda | O que deixa |
|---|---|---|---|---|
| **Build backend (Maven)** | compila o Java e roda os testes comuns | Maven | sempre | log |
| **Build frontend (React)** | monta as telas | npm/Vite | sempre | log |
| **Gate - Secret Scanning (Gitleaks)** | procura senhas e chaves gravadas no código **e no histórico** | Gitleaks | sempre | log |
| **Gate - SAST (Semgrep)** | lê o código procurando padrões perigosos | Semgrep | sempre | alertas na aba Security (se disponível) |
| **Gate - SAST (CodeQL)** | lê o código seguindo o caminho dos dados | CodeQL | sempre | alertas na aba Security (se disponível) |
| **Gate - SCA (Dependency-Check + Trivy)** | procura falhas conhecidas (CVE) nas **bibliotecas** usadas | Dependency-Check, Trivy | sempre | log |
| **Gate - Container Scanning (Trivy image)** | procura CVE nas **imagens** Docker (sistema operacional + programas dentro dela) | Trivy | sempre | log |
| **SBOM (CycloneDX)** | gera a lista de componentes do software | Syft | sempre | artefato `sbom` |
| **Gate - DAST autenticado (OWASP ZAP)** | **liga** o VidaPlus e o examina de fora, como um visitante | OWASP ZAP | sempre | log e artefato do relatório |
| **Gate - Testes de seguranca** | roda os testes `@security` (os `.feature` e o `SegurancaAceiteTest`) | Maven + JUnit + Cucumber | sempre | log |
| **Placar de gates SSDLC** | junta o resultado dos gates numa tabela | — | sempre | o resumo da execução |
| **Release - build, push, sign & attest SBOM** | empacota o backend numa imagem, publica e **assina** | Docker, cosign | **só na `main`** | imagem em **Packages** |
| **Deploy staging + smoke** | liga a imagem num ambiente de teste e confere se responde | Docker | só na `main` | log |
| **Deploy prod - verifica assinatura + aprovacao** | confere a assinatura e **espera um humano aprovar** antes de "publicar" | cosign | só na `main` | log |

As ferramentas de segurança, uma a uma, estão em [`07`](07-ferramentas-de-seguranca.md).

---

## 4. Por que quase tudo aparece vermelho

O VidaPlus é inseguro **de propósito**. No começo, quase todos os gates acham problemas. Isso é o
**ponto de partida**, não um erro de vocês. E como cada repositório tem as falhas de **todos** os
pacotes (inclusive as que outros grupos corrigem nos repositórios deles), vários gates **continuam
vermelhos** até o fim do semestre — e tudo bem: por isso eles são informativos.

O que importa para o seu dossiê é **o seu cenário** e **o seu achado** mudando de estado — e isso
se lê **dentro** do log (§5.3).

---

## 5. Procedimentos na tela — clique por clique

### 5.1 Ver a lista de execuções

1. No navegador, abra o repositório do grupo no GitHub.
2. Clique na aba **Actions** (barra no alto da página, ao lado de **Pull requests**).
3. Na coluna da esquerda, clique em **SSDLC - VidaPlus** (o nome do workflow).
4. À direita aparece a lista de execuções, **a mais recente em cima**. Cada linha mostra:
   - um ícone: ✓ verde (concluída), ✗ vermelho (algum job obrigatório falhou), bolinha amarela
     (rodando);
   - o **título** (a mensagem do commit, ou do merge);
   - a **branch** (etiqueta cinza) e quem disparou;
   - quando rodou e quanto demorou.
5. Para ver só uma branch: clique no filtro **Branch** (acima da lista) e escolha, por exemplo,
   `main`.

> **Por que a execução fica verde se há gates vermelhos?** Porque os gates são informativos
> (`continue-on-error: true`). O ✓ da execução quer dizer "o pipeline terminou", não "está tudo
> seguro".

### 5.2 Abrir uma execução e ler o placar

1. Na lista (§5.1), clique no **título** de uma execução.
2. Abre a página da execução:
   - à **esquerda**, a lista de **jobs**, cada um com ✓ ou ✗;
   - no **centro**, um desenho com os jobs e as setas entre eles (quem depende de quem);
   - **rolando para baixo**, aparece o **"Placar de gates SSDLC"**: uma tabela com cada gate e o
     resultado (`success` = verde; `failure` = vermelho);
   - **mais abaixo**, a seção **Artifacts**, com os arquivos que os jobs deixaram (§5.5).
3. Para guardar como evidência: tire um print do placar (no Ubuntu, **Shift+Print Screen** e
   selecione a área; a imagem vai para a pasta **Imagens**) e copie a **URL** da barra do
   navegador — ela identifica essa execução para sempre.

### 5.3 Ler o log de um job e procurar um texto

1. Na página da execução (§5.2), clique no **nome do job** na lista da esquerda (ex.:
   **Gate - Testes de seguranca**).
2. Aparece a lista de **etapas** do job. Clique na etapa que interessa (ex.: **Testes de aceite de
   seguranca (@Tag security)**) para abri-la. As linhas do log aparecem numeradas.
3. Para procurar: no canto superior direito do log, clique na caixa **Search logs**, digite o texto
   (ex.: o nome do seu cenário) e aperte **Enter**. As ocorrências ficam destacadas; use as setas ao
   lado da caixa para ir de uma a outra.
4. Para **apontar uma linha** no dossiê: clique no **número** da linha. A URL muda e passa a
   apontar exatamente para ela. Copie essa URL.
5. Para ver o log inteiro como texto puro: clique no ícone de **engrenagem** (canto superior
   direito do log) → **View raw logs**.

### 5.4 Achar a execução de "antes" e a de "depois" de uma correção

A correção entra na `main` pelo **merge** de um PR. Então:

1. Siga o §5.1 e filtre **Branch** = `main`.
2. Ache a execução cujo título começa com **"Merge pull request #"** seguido do número do PR da
   correção (por exemplo, "Merge pull request #5 from ads-sapucaia-ssc/fix/F-02-idor"). Essa é a
   execução de **depois**.
3. A execução **logo abaixo dela** na lista (mais antiga, também na `main`) é a de **antes**.
4. Abra cada uma (§5.2) e leia o log do job que interessa (§5.3).

Outro caminho para o "depois": abra o PR da correção (aba **Pull requests** → **Closed** → clique
no PR) → aba **Checks**. Ela mostra os jobs que rodaram para aquele PR.

### 5.5 Baixar um artefato (ex.: o SBOM, o relatório do ZAP)

1. Abra a execução (§5.2) e role até a seção **Artifacts** (no fim da página).
2. Clique no nome do artefato (ex.: `sbom`). O navegador baixa um arquivo **`.zip`** para a pasta
   **Downloads**.
3. No terminal, crie uma pasta para ele e descompacte (troque `sbom.zip` pelo nome que baixou):

   ```bash
   mkdir -p ~/Downloads/artefato-sbom
   unzip ~/Downloads/sbom.zip -d ~/Downloads/artefato-sbom
   ls ~/Downloads/artefato-sbom
   ```

4. Abra o arquivo no VS Code: **File → Open File…** → `Downloads/artefato-sbom/sbom.cdx.json`.

> Artefatos **expiram** (o GitHub apaga depois de alguns dias). Se precisar de um como evidência,
> baixe e guarde em `entregaveis/evidencias/`.

### 5.6 Ver os resultados de um job dentro de um Pull Request

1. Abra o PR (aba **Pull requests** → clique no título).
2. Role até a **caixa de checks**, logo acima do botão de merge. Ela lista cada job, com ✓, ✗ ou
   uma bolinha amarela (rodando). Se estiver recolhida, clique em **Show all checks**.
3. Clique em **Details**, ao lado de um job, para abrir o log dele (e siga o §5.3).

### 5.7 Ver alertas na aba Security (SAST)

1. No repositório, clique na aba **Security** → no menu da esquerda, **Code scanning**.
2. Aparece a lista de alertas do Semgrep e do CodeQL, com a regra, o arquivo e a linha. Use o
   filtro **Tool** para escolher a ferramenta, e a busca para filtrar por texto (ex.: `sql`).
3. Clique num alerta para ver o trecho de código marcado.

> **A aba pode não mostrar nada** em repositórios privados, dependendo do plano da organização. Se
> for o seu caso, escreva isso no dossiê e use o Semgrep no seu computador
> ([`07` §2](07-ferramentas-de-seguranca.md#2-semgrep--procura-padrões-perigosos-no-código)).

### 5.8 Rodar o pipeline de novo, sem novo push

- **Re-executar uma execução:** abra a execução (§5.2) → botão **Re-run jobs** (canto superior
  direito) → **Re-run all jobs**.
- **Disparar à mão:** **Actions** → **SSDLC - VidaPlus** → botão **Run workflow** (à direita) →
  escolha a branch → **Run workflow**.

---

## 6. A entrega (CD) e a entrega segura, do zero

Esta parte só roda na `main`. Vocês **não configuram nada** dela: o trabalho é **ler e explicar**
no dossiê (Fase 6).

### 6.1 Imagem e registro

Em [`01`](01-subir-o-vidaplus.md) você viu que o VidaPlus roda em **containers**, criados a partir
de **imagens**. Uma imagem é o **pacote completo** do programa (o código compilado + tudo o que ele
precisa para rodar). Para entregar o sistema, o pipeline:

1. **constrói** a imagem do backend;
2. **publica** a imagem num **registro** — um "depósito de imagens". O do GitHub se chama **GHCR**
   e aparece no repositório, na lateral direita da página inicial, em **Packages**.

### 6.2 Assinatura: provar quem fez

Qualquer um poderia publicar uma imagem com o mesmo nome. Como o servidor de produção sabe que a
imagem foi feita **pelo pipeline de vocês**, e não por um atacante? Pela **assinatura digital**:

- o job **Release** assina a imagem com a ferramenta **cosign**, usando a **identidade do próprio
  pipeline** (o GitHub atesta "esta assinatura foi feita pelo workflow X do repositório Y") — sem
  nenhuma senha guardada;
- a assinatura fica registrada num **livro-razão público** (o *Rekor*), que ninguém consegue apagar.

E o job **Deploy prod**, antes de publicar, **verifica** a assinatura **e quem assinou**. Se a
imagem não tiver assinatura, ou tiver sido assinada por outro repositório, ele **recusa**.

> Assinar sem verificar não serve para nada — como um cheque assinado que ninguém confere.

### 6.3 SBOM atestado

O pipeline também gera o **SBOM da imagem** (a lista de componentes dentro dela) e o anexa à
imagem, **assinado** (uma "atestação"). Assim, quem recebe a imagem sabe **do que** ela é feita, com
garantia de que a lista não foi trocada.

### 6.4 Aprovação humana (environment `production`)

O job **Deploy prod** está ligado a um **ambiente** chamado `production`, configurado pelo
professor para exigir **aprovação de uma pessoa** antes de rodar. Na tela:

1. Abra uma execução da `main` (§5.2). O job **Deploy prod - verifica assinatura + aprovacao**
   aparece com um relógio (**Waiting**).
2. No alto da página aparece o aviso **"Review pending deployments"** (ou o botão **Review
   deployments**). Quem é revisor marca `production` → **Approve and deploy**. Só então o job roda.

### 6.5 SLSA — e por que isso ainda não é o nível 2

**SLSA** é uma escala (níveis 0 a 3) de quanta confiança se pode ter na **origem** de um software.
O nível 2 exige um documento chamado **proveniência**, assinado pela plataforma de build, que diz
**como** a imagem foi construída (qual repositório, qual commit, qual workflow). O pipeline do
VidaPlus assina a imagem e o SBOM, mas **não gera a proveniência** — por isso **ainda não é SLSA
L2**. Explique essa diferença no dossiê ([`docs/06` §5](../../docs/06-pipeline-cicd.md#5-entrega-segura-já-pré-fiada-assinatura-verificação-e-promoção)).

### 6.6 A proteção da `main`

Também configurada pelo professor (um *ruleset* do GitHub). Ela faz duas coisas:

1. **ninguém envia direto para a `main`**: tudo entra por PR, com **aprovação de outro
   integrante** (você vê isso no PR como **"Review required"**);
2. **ninguém reescreve o histórico** da `main` (o histórico é a trilha de quem mudou o quê).

As duas aplicam a mesma ideia de segurança: **quem faz não aprova o próprio** (separação de
funções).

---

## 7. Checklist

- [ ] Sei explicar, com as minhas palavras, o que é CI, CD, pipeline, job, gate, runner e artefato.
- [ ] Abri o `ssdlc-pipeline.yml` e achei o job dos testes de segurança e o `continue-on-error`.
- [ ] Abri uma execução, li o placar e procurei um texto num log (Search logs).
- [ ] Achei a execução de "antes" e a de "depois" de um PR.
- [ ] Baixei e abri o artefato `sbom`.
- [ ] Sei explicar assinatura, verificação de quem assinou, aprovação humana e por que não é SLSA L2.
