# 08 · Setup dos grupos — passo a passo

Guia para cada grupo **começar** o projeto: gerar o repositório, subir o VidaPlus, rodar o
pipeline pela primeira vez e entender por que ele nasce **vermelho**.

---

## 1. Gerar o repositório do grupo

**Você (aluno) tem um caminho só:** o professor compartilha um **link de convite** do
assignment (Classroom 50); ao aceitar, você **cria/entra num time** e o repositório
**privado** do grupo é criado **automaticamente sob a organização**, a partir do template e já
com acesso **admin** — no lugar certo (sob a org), com o time e as permissões corretas, sem
vocês precisarem criar nada à mão.

> **Todos do grupo clicam no mesmo link — mas o repositório é um só.** O **primeiro** a aceitar
> **cria o time** (escolhe o nome); os **demais** abrem o **mesmo link** e **entram no time já
> existente** — ninguém cria um repositório novo. O grupo fica com **um único repositório
> compartilhado** (não um por pessoa). A configuração do repo
> ([§5.2](#52-proteger-a-main-branch-protection),
> [§5.3](#53-ambiente-production-com-aprovação-1-passo)) é feita **uma vez**, por um integrante
> com admin; cada um que for programar **clona** esse mesmo repositório.

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
  - **não corrija agora.** Mover os segredos para variável de ambiente é a **remediação de
    F-10**, que acontece na **Fase 4/6** (com requisito, ameaça e teste **antes**) — não na
    semana 1. Por ora, apenas **documente** a decisão **no `DOSSIE.md`** do grupo (o entregável,
    já no repo em [`entregaveis/DOSSIE.md`](../entregaveis/DOSSIE.md)): é o **primeiro registro**
    de F-10, que vira o seu **plano de remediação** na Fase 4.

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
[checklist de revisão](07-rubrica-avaliacao.md#L113) e clica em **`Review changes → Approve`**.

**8. Faça o merge** — só com o **gate da classe corrigida verde** **e** a **revisão aprovada**:
na página do PR, clique em **`Merge pull request`** → **`Confirm merge`**.

**9. Atualize a matriz** de rastreabilidade no `DOSSIE.md` (requisito → teste → evidência →
status).

Quando terminar de corrigir uma classe de falha, **torne o gate bloqueante** removendo o
`continue-on-error: true` daquele job no workflow (ver [06-pipeline-cicd.md](06-pipeline-cicd.md)).

> **Cada grupo só torna bloqueante o gate do seu pacote.** O workflow **já nasce verde**
> (conclui com sucesso — os gates são informativos por `continue-on-error`); o que fica
> vermelho é o **placar de gates**, não o build. A nota **não exige** o pipeline 100% verde
> — nota = dossiê + defesa; o pipeline é **evidência**. Os gates das funcionalidades de
> **outros pacotes** (ou apenas *analisadas*, não implementadas) **permanecem informativos**,
> e isso é o esperado.
>
> **Testes de segurança são um job só** (roda os `@security` juntos), mas cada teste-semente
> tem **dono(s)**: `sqli_no_login` → **G1 e G5** (F-01); `idor…403` → **G2 e G4** (F-02);
> `rbac…403` → **G3** (F-03; toca também F-07, de G2). **Todo grupo tem um teste-semente no
> seu P0 de correção** — é o seu 🔴→🟢 objetivo. A funcionalidade de **criação** (✨) do
> pacote **não** tem teste-semente: o grupo escreve o **teste próprio** (Fase 5) e usa o
> dossiê. Para tornar bloqueante **só o seu** teste
> sem depender de outro pacote, restrinja a execução aos seus testes (ex.: uma `@Tag` mais
> específica ou `-Dtest=...`); ou mantenha o gate informativo e use o **teste específico verde
> no log** + a **matriz de rastreabilidade** como evidência.

---

## 5.1 Testes de aceite já fornecidos

O professor já incluiu **testes de aceite mínimos** em
`legacy-app/backend/src/test/.../security/SegurancaAceiteTest.java` (`@Tag("security")`),
cobrindo IDOR, RBAC e SQLi no login. Eles **nascem vermelhos** no legado — são o **alvo**
do "pronto". O gate `Gate - Testes de seguranca` do pipeline os executa (com um MySQL de
serviço). **Você deve expandir** a cobertura com testes próprios na Fase 5; estes são só o
mínimo.

## 5.2 Proteger a `main` (branch protection)

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
     grupo já tornou bloqueantes** (ex.: `Gate - Testes de seguranca`,
     `Gate - Secret Scanning (Gitleaks)`). ⚠️ **Não** selecione todos: os gates ainda
     informativos nascem vermelhos e **travariam** a `main` com o legado.
   - **Block force pushes** — mantém o histórico da `main` **append-only**: ninguém
     reescreve/apaga commits (protege a trilha e as evidências, mesmo de quem tem admin).
5. **Create**.

**Regra de ouro:** à medida que o grupo torna um gate bloqueante (removendo o
`continue-on-error`), **adicione esse gate** à lista de status checks obrigatórios. Assim a
proteção da `main` cresce junto com as correções.

## 5.3 Ambiente `production` com aprovação (1 passo)

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
- [ ] Papéis do grupo definidos (Security Champion, Dev, DevSecOps, DPO).
- [ ] Inventário de ativos e classificação de dados iniciados (entregável da Fase 0).
