# 08 · Setup dos grupos — passo a passo

Guia para cada grupo **começar** o projeto: gerar o repositório, subir o VidaPlus, rodar o
pipeline pela primeira vez e entender por que ele nasce **vermelho**.

---

## 1. Gerar o repositório do grupo

**Você (aluno) tem um caminho só:** o professor compartilha um **link de convite** do
assignment (Classroom 50); ao aceitar, o repositório **privado** do grupo é criado
**automaticamente sob a organização**, a partir do template e já com acesso **admin** — no
lugar certo (sob a org), com as permissões corretas, sem vocês precisarem criar nada à mão.

O repositório do grupo é **um só, compartilhado** (não um por pessoa). Para formá-lo:

1. **Só o líder** do grupo abre o link de convite e **aceita** — isso **cria o repositório
   compartilhado** e o deixa como **owner** (dono). Os **demais não** clicam no link de aceite
   (senão tentariam criar outro repo); eles entram como colaboradores (passos 2–5).
2. O **owner** **adiciona os demais** como **colaboradores**. Na página do assignment do grupo,
   clique no **lápis de edição**
   (canto superior direito) para abrir **Edit Assignment**:

   ![Editar o assignment do grupo no Classroom 50](img/web_assignment_edit_student.png)

3. Clique em **Manage collaborators** (gerenciar colaboradores):

   ![Janela Manage collaborators do Classroom 50](img/web_assignment_manage_collaborators_student.png)

4. Na janela **Group members**, o owner digita o **usuário do GitHub** de cada colega no campo
   *Add a GitHub username* e clica **+ Add**; ao terminar, clica **Save collaborators**.
   (Este assignment permite até **4** estudantes além do owner.)
5. Cada colega adicionado **aceita o convite da organização** da turma que chega do GitHub
   (e-mail ou `github.com/settings/organizations`) — só então ganha acesso ao repositório. Se o
   colega "não for encontrado" ao ser adicionado, ele ainda não está na organização: peça para
   aceitar o convite e autorizar o **Classroom 50** em `github.com/settings/applications`.

> Pronto: todos passam a ter acesso ao **mesmo** repositório. A configuração do repo
> ([§5.2](#52-proteger-a-main-branch-protection),
> [§5.3](#53-ambiente-production-com-aprovação-1-passo)) é feita **uma vez**, por um integrante
> com admin; cada um que for programar **clona** esse mesmo repositório (abaixo).

Depois, **clone** o repositório do grupo — isso baixa uma cópia para a sua máquina, onde você
vai trabalhar. Na página do repositório no GitHub, clique no botão verde **`Code` → aba
`HTTPS`** e copie a URL; ela tem o formato abaixo. No terminal, na pasta onde quer guardar o
projeto:

```bash
git clone https://github.com/<org>/ssc-grupo-A.git
cd ssc-grupo-A
```

- `<org>` é `ads-sapucaia-ssc`; `ssc-grupo-A` é o nome do repositório do seu grupo.
- **Vai pedir para entrar** (o repositório é **privado**). No **Windows/Mac**, o Git abre uma
  janela do navegador — basta logar na sua conta do GitHub e autorizar. No **Linux** (ou se o
  terminal pedir usuário **e senha**), a "senha" **não** é a do site: gere um **token** em
  **GitHub → sua foto (canto superior direito) → `Settings` → `Developer settings` →
  `Personal access tokens` → `Tokens (classic)` → `Generate new token (classic)`**, marque o
  escopo **`repo`**, clique em **Generate**, **copie** o token e **cole no lugar da senha**.

> **Seu entregável já vem no repo:** [`entregaveis/DOSSIE.md`](../entregaveis/DOSSIE.md) — o
> **dossiê**, que vocês **preenchem ao longo das fases** (não precisa criar nada; cada seção
> mapeia uma fase). Visão geral em [docs/09](09-dossie-template.md).

---

## 2. Subir o VidaPlus localmente

```bash
cd legacy-app
docker compose up --build
# Frontend: http://localhost:4200   API: http://localhost:8080   MySQL: localhost:3306
```

Explore as falhas (usuários de teste e comandos de exploração estão em
[../legacy-app/README.md](../legacy-app/README.md)). **Você deve conseguir**, por exemplo,
baixar o exame de outro paciente trocando o id (IDOR) — é isso que vão corrigir.

---

## 3. Rodar o pipeline pela primeira vez

O **GitHub Actions** é o serviço do GitHub que roda tarefas automáticas a cada `push`. A cada
envio de código, ele executa o
[`.github/workflows/ssdlc-pipeline.yml`](../.github/workflows/ssdlc-pipeline.yml) (o
*pipeline*). Para ver o resultado, na página do repositório no GitHub abra a aba **`Actions`**
(no topo), clique na **execução mais recente** e role até o **"Placar de gates SSDLC"** no fim
do _summary_ (o resumo da execução).

> ⚠️ **É esperado que quase tudo apareça vermelho.** O VidaPlus é legado e inseguro **de
> propósito**. O vermelho é o seu **ponto de partida**; a meta do semestre é transformar
> cada gate em verde, corrigindo as falhas de verdade.

> **Por que "quase" e não "tudo"?** Alguns gates **já nascem verdes**: o **build** do backend e
> do frontend (o código **compila e roda**), o **SBOM** (apenas inventaria) e o teste de
> sanidade **`login_valido`** (a app sobe e conecta no MySQL). O **SCA** não tem falha plantada
> — a stack é atual, nada de "dependência velha" de propósito —, então nasce verde e deve se
> **manter** verde. Vermelho **de propósito**: **Secret Scanning, SAST, DAST** e os **testes**
> de IDOR/RBAC/SQLi. (O **Container Scanning** pode acusar CVE da **imagem base** — higiene, não
> falha plantada.)

### Como ler o placar

| Gate | O que um vermelho significa | Você conserta em… |
|------|-----------------------------|-------------------|
| Secret Scanning | Há segredo no código (V-05) | Fase 4/6 |
| SAST (Semgrep/CodeQL) | Padrões inseguros: SQLi, MD5, XSS, JWT (V-01/02/07/10) | Fase 4 |
| SCA | CVE em dependências (higiene de supply chain) | Fase 5/6 |
| Container Scanning | CVE na imagem base/SO do backend/frontend | Fase 5/6 |
| DAST autenticado | Faltam headers, stack trace, SQLi/XSS e IDOR (com JWT) | Fase 5 |
| Testes de segurança | IDOR/RBAC e SQLi no login sem teste que prove correção | Fase 5 |

---

## 4. O aviso de "segredo detectado" no primeiro push

O repositório contém **segredos falsos plantados** (senha do banco, segredo do JWT — V-05)
para vocês aprenderem a removê-los. Por isso:

- o gate **Gitleaks** vai **acusar** — isso é **esperado e correto**;
- o **push protection** do GitHub **pode** — mas, para estes segredos, **provavelmente não
  vai** — bloquear o primeiro push. Ele barra com alta confiança **tokens de provedores**
  (formato reconhecível: `ghp_…`, `AKIA…`, `sk_live_…`); aqui os segredos são **genéricos** (a
  senha do banco e uma string inventada), que o push protection **clássico não pega** — só a
  detecção **genérica** (recurso mais novo, e apenas se habilitada no repo) *poderia*. Quem
  **garante** acusar é o gate **Gitleaks** (acima). **Se** o push for bloqueado:
  - **não reescreva nem apague o histórico do git** em pânico (nada de `git filter-branch`,
    `reset --hard` ou *force push* para "sumir" com o segredo): aqui os segredos são **falsos**,
    não há o que proteger, e reescrever o histórico **destrói a trilha append-only** que o
    projeto usa como evidência (é o mesmo motivo do *Block force pushes* na
    [§5.2](#52-proteger-a-main-branch-protection));
  - como são segredos **de exemplo** (não credenciais reais), **permita o push** pela própria
    mensagem de erro do GitHub (link "allow secret") — é o esperado no primeiro push;
  - **não corrija.** Os segredos no código são o **F-10**, que neste projeto é **analisado**,
    não implementado: o grupo identifica os segredos, explica o risco e escreve o **plano de
    remediação** (Fase 4) — sem mexer no código nem no pipeline. Por ora, apenas **documente** a
    decisão **no `DOSSIE.md`** do grupo (o entregável, já no repo em
    [`entregaveis/DOSSIE.md`](../entregaveis/DOSSIE.md)): é o **primeiro registro** de F-10.

---

## 5. Fluxo de trabalho (por funcionalidade)

Cada funcionalidade que você corrige segue o ciclo abaixo. Os comandos são digitados no
**terminal, dentro da pasta do repositório** que você clonou.

**1. Crie a branch da correção** — uma linha de trabalho separada, para não mexer direto na
`main`:

```bash
git checkout -b fix/F-02-idor
```

`fix/F-02-idor` é o **nome inteiro** da branch: a barra faz parte do nome (convenção `fix/…`,
`feature/…`); **não** existe uma branch `fix` à parte, e o `-b` cria a branch a partir da `main`.

**2. Registre o requisito** no `DOSSIE.md`: RFS+RGS + ameaça (STRIDE/LINDDUN) + base legal.

**3. Implemente o controle** (deny-by-default, query parametrizada, etc.).

**4. Escreva o teste** que prova a correção (ex.: A pede o exame de B → deve dar **403**).

**5. Grave o trabalho no histórico e envie ao GitHub:**

```bash
git add -A                                                   # marca tudo o que você alterou
git commit -m "fix(F-02): valida dono do laudo (anti-IDOR)"  # grava um ponto no histórico
git push -u origin fix/F-02-idor                             # envia a sua branch para o GitHub
```

**6. Abra o Pull Request (PR)** — é o pedido, revisável, para juntar a sua branch na `main`.
Logo após o `push`, abra o repositório no GitHub: aparece uma faixa **`Compare & pull
request`** → clique nela → confira título e descrição → **`Create pull request`**. O pipeline
(aba **`Actions`** do repositório) roda os gates sozinho, dentro do PR.

**7. Revisão por um colega do grupo** (autor ≠ revisor): **outro** integrante abre o PR no
GitHub, vai na aba **`Files changed`**, confere pelo
[checklist de revisão](07-rubrica-avaliacao.md#checklist-de-secure-code-review-para-os-prs-da-poc) e clica em **`Review changes → Approve`**.

**8. Faça o merge** — só com o **gate da classe corrigida verde** **e** a **revisão aprovada**:
na página do PR, clique em **`Merge pull request`** → **`Confirm merge`**.

**9. Atualize a matriz** de rastreabilidade no `DOSSIE.md` (requisito → teste → evidência →
status).

> **Os gates ficam informativos.** O workflow **já nasce verde** (conclui com sucesso — os gates
> são informativos por `continue-on-error`); o que fica vermelho é o **placar de gates**, não o
> build. A nota **não exige** o pipeline 100% verde — nota = dossiê + defesa; o pipeline é
> **evidência**. A proteção da `main` é configurada pelo professor (§5.2); o grupo **não**
> precisa tornar nenhum gate bloqueante.
>
> **Testes de segurança são um job só** (roda os `@security` de todos os pacotes juntos), mas cada
> teste-semente tem **dono(s)**: o de SQLi no login → **G1** (F-01); o de IDOR → **G2 e G4**
> (F-02); o de RBAC → **G3** (F-03). **Todo grupo tem um teste-semente pronto no seu P0** — é o seu
> 🔴→🟢 objetivo.
>
> Como esse job roda os testes de **todos** os pacotes, ele **continua vermelho** por causa das
> falhas dos outros grupos, mesmo depois que o seu P0 é corrigido. Por isso, a sua evidência é o
> **seu teste específico verde no log do job** (abra o job na aba **Actions** e procure o nome do
> seu cenário) + a **linha na matriz** do dossiê.
>
> *(Material opcional: o [docs/13](13-gate-bloqueante-do-grupo.md) mostra como criar um job
> só com os testes do seu pacote e torná-lo bloqueante. Não é exigido.)*

---

## 5.1 Testes de aceite já fornecidos

O professor já incluiu **testes de aceite mínimos** em
`legacy-app/backend/src/test/.../security/SegurancaAceiteTest.java` (`@Tag("security")`),
cobrindo IDOR, RBAC e SQLi no login. Eles **nascem vermelhos** no legado — são o **alvo**
do "pronto". O gate `Gate - Testes de seguranca` do pipeline os executa (com um MySQL de
serviço). Se quiser, o grupo pode **ampliar** a cobertura com testes próprios na Fase 5
(opcional); o mínimo é o teste do seu P0.

## 5.2 Proteger a `main` (branch protection)

> **Nesta turma, o professor faz esta configuração** em cada repositório. O grupo **não** precisa
> executar os passos abaixo: leia para **entender** o que foi configurado e **explicar** no
> dossiê (§8, Fase 6).

Isto é o que transforma "merge só com gate verde **e** revisão aprovada" (passos 6–7) em
**regra** — não dá para pular. Configure **uma vez**, por um integrante com permissão de
**admin** no repositório do grupo. É uma **configuração do GitHub** (não um job do workflow).

> **Faça um `push` antes.** Os status checks (gates) só aparecem para seleção depois de
> terem **rodado ao menos uma vez**.

**Caminho recomendado — Rulesets** (funciona inclusive em repositório privado gratuito):

1. **Settings → Rules → Rulesets → New ruleset → New branch ruleset**.
2. **Name:** `protege-main`. **Enforcement status:** **Active**.
3. **Target branches → Add target → Include default branch** (a `main`).
4. Em **Rules**, marque:
   - **Require a pull request before merging** → **Required approvals: `1`** (a revisão
     intra-grupo; o **autor não aprova o próprio PR**).
   - **Require status checks to pass** → **Add checks** e selecione **apenas os gates que o
     grupo realmente deixou 100% verdes** — o que, num repo que **ainda tem todas as falhas**,
     é **raro**: a maioria dos gates roda o repositório inteiro (ex.: `Gate - Testes de
     seguranca` cobre IDOR/RBAC/SQLi de **todos** os pacotes) e segue vermelho mesmo depois que
     você corrige o **seu** P0. ⚠️ **Não** adicione um gate que ainda nasce vermelho — ele
     **travaria** a `main`. Na prática essa lista costuma ficar **pequena ou vazia**, e tudo
     bem: o núcleo da proteção são os outros itens (PR + 1 aprovação + block force pushes).
   - **Block force pushes** — mantém o histórico da `main` **append-only**: ninguém
     reescreve/apaga commits (protege a trilha e as evidências, mesmo de quem tem admin).
5. **Create**.

**Regra de ouro:** só adicione um gate à lista de status checks obrigatórios **quando ele
ficar de fato verde** no seu repo — não basta remover o `continue-on-error`; o que aquele gate
cobre precisa estar corrigido. Assim a proteção da `main` cresce junto com as correções **sem**
travar o merge por falhas que não são do seu pacote.

## 5.3 Ambiente `production` com aprovação (1 passo)

> **Nesta turma, o professor também faz esta configuração.** Leia para entender e explicar no
> dossiê (§8, Fase 6).

O pipeline **já** builda, assina, atesta e promove a imagem (jobs `release-sign` →
`deploy-staging` → `deploy-prod`) — você **não configura ferramenta**. Só a **aprovação
manual** de produção exige criar o *environment* (é política do repo, não vem no arquivo).
> **Não confunda com a revisão do PR ([§5.2](#52-proteger-a-main-branch-protection)).** São **dois portões diferentes**: a **§5.2** é a
> **revisão de código** (um colega aprova o *merge* na `main`); esta **§5.3** é a **aprovação do
> deploy para produção** (um colega aprova a *promoção* da imagem já assinada). Nos dois vale o
> mesmo princípio: **quem faz não aprova o próprio** — separação de funções, **sem depender do
> professor** a cada deploy.

Faça **uma vez**, por um integrante com **admin**:

1. **Settings → Environments → New environment → `production`**.
2. Marque **Required reviewers** e adicione **1 revisor — um colega do grupo**, de preferência
   **quem não costuma dar o `push`/`merge` na `main`** (para não aprovar o próprio deploy). Se
   a opção **"Prevent self-review"** (impedir autoaprovação) aparecer, marque-a.
3. **Deployment branches → Selected → `main`** — **recomendado (defesa em profundidade).** O
   workflow já limita o CD à `main` (o job `release-sign` roda só com
   `if: github.ref == 'refs/heads/main'`, e o `deploy-prod` depende dele), então a pausa
   funciona sem este passo. Travar aqui é a **salvaguarda no nível da plataforma**: mesmo que o workflow
   seja alterado — e vocês **vão** editá-lo ([§5.2](#52-proteger-a-main-branch-protection)/[§6](#6-autograding-como-sua-nota-parcial-é-calculada)) — o GitHub segue recusando promover à
   produção qualquer branch que não seja a `main`.

A partir daí, o job `deploy-prod` **pausa aguardando aprovação** e só roda se a **assinatura
e a atestação** da imagem **passarem** na verificação (admission gate). *(O environment
`staging` é criado automaticamente, sem revisor.)*

> **"Required reviewers" não aparece?** O plano que habilita isso é o da **organização**, que
> **já** tem o **GitHub Education** — logo, num repositório criado **sob a org**, a opção
> aparece. Se ela some, quase sempre o repo foi criado na **sua conta pessoal**, não sob a
> organização: **refaçam pelo convite** ([§1](#1-gerar-o-repositório-do-grupo)) ou avisem o
professor. **Não deixem o repo público** para contornar —
> exporia o código do grupo. Mesmo sem a pausa, a **verificação da assinatura continua
> valendo**: o `deploy-prod` só deixa de *pausar para aprovação*, mas ainda recusa imagem não
> assinada.

## 6. Autograding (como sua nota parcial é calculada)

O `.github/classroom/autograding.json` roda a cada push e pontua **quando o padrão
vulnerável desaparece** (sem MD5, sem SQL concatenado, JWT com expiração, sem
`dangerouslySetInnerHTML`, sem CORS `*`, segredo em variável de ambiente) e quando os
**testes de segurança passam**. Atenção: "enganar" o teste sem corrigir de verdade é
**anti-padrão** e será pego na revisão manual dos PRs.

---

## 7. Checklist para o kickoff (Fase 0)

- [ ] Repositório do grupo criado a partir do template.
- [ ] `docker compose up` funcionando; app explorada.
- [ ] Primeiro push feito; pipeline executou; placar vermelho registrado (print).
- [ ] Combinado que a **liderança de cada fase** é definida quando a fase começa (revezando
  entre os integrantes) — ações por papel × fase em [docs/00](00-visao-geral.md).
- [ ] Inventário de ativos e classificação de dados iniciados (entregável da Fase 0).
