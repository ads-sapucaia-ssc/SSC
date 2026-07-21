# 09 · Setup dos grupos — passo a passo

Guia para cada grupo **começar** o projeto: gerar o repositório, subir o VidaPlus, rodar o
pipeline pela primeira vez e entender por que ele nasce **vermelho**.

---

## 1. Gerar o repositório do grupo

**Via Classroom 50 (recomendado):** o professor compartilha um **link de convite** do
assignment; ao aceitar, você **cria/entra num time** e o Classroom 50 gera automaticamente
o repositório **privado** do grupo a partir do template, já com acesso **admin** do grupo.

**Ou manualmente** (sem Classroom 50): na página do template na org, clique em **"Use this
template" → "Create a new repository"**, marque **Private** e dê um nome (ex.: `ssc-grupo-1`).

> O **GitHub Classroom** oficial foi **descontinuado** (2026). Usamos o **Classroom 50**
> (alternativa open-source endossada pelo GitHub, roda sobre GitHub Actions).

Depois, clone:

```bash
git clone git@github.com:<sua-conta-ou-org>/ssc-grupo-A.git
cd ssc-grupo-A
```

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

Ao fazer o primeiro `push`, o **GitHub Actions** executa o
[`.github/workflows/ssdlc-pipeline.yml`](../.github/workflows/ssdlc-pipeline.yml). Veja em
**Actions** e, ao final, o **"Placar de gates SSDLC"** no _summary_ da execução.

> ⚠️ **É esperado que quase tudo apareça vermelho.** O VidaPlus é legado e inseguro **de
> propósito**. O vermelho é o seu **ponto de partida**; a meta do semestre é transformar
> cada gate em verde, corrigindo as falhas de verdade.

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
- o **push protection** do GitHub **pode bloquear** o primeiro push ao detectar um padrão
  de segredo. Se isso acontecer:
  - **não** apague o histórico às cegas;
  - como são segredos **de exemplo** (não credenciais reais), você pode **permitir o push**
    pela própria mensagem de erro do GitHub (link "allow secret") **ou** já iniciar a
    correção (mover os segredos para variável de ambiente) antes de reenviar;
  - documente essa decisão — ela é o começo da funcionalidade **F-10 (gestão de segredos)**.

---

## 5. Fluxo de trabalho (por funcionalidade)

```
1. branch:      git checkout -b fix/F-02-idor
2. requisito:   registre RFS+RGS + ameaça (STRIDE/LINDDUN) + base legal
3. correção:    implemente o controle (deny-by-default, parametrizado, etc.)
4. teste:       escreva o teste que prova a correção (ex.: A pede exame de B -> 403)
5. PR:          abra o Pull Request; o pipeline roda os gates
6. review:      revisão intra-grupo, autor != revisor (checklist em docs/08); aprovar
7. merge:       só com o gate da classe corrigida verde E a revisão aprovada
8. matriz:      atualize a rastreabilidade (requisito -> teste -> evidência -> status)
```

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
> tem **um dono**: `sqli_no_login` → Grupo 1 (F-01); `idor…403` → Grupo 2 (F-02);
> `rbac…403` → Grupo 2 (F-03/F-07). Grupos 3, 4 e 5 **não** têm teste-semente — provam o seu
> pacote com **testes próprios** (Fase 5) + dossiê. Para tornar bloqueante **só o seu** teste
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
Faça **uma vez**, por um integrante com **admin**:

1. **Settings → Environments → New environment → `production`**.
2. Marque **Required reviewers** e adicione **1 revisor** (um integrante ou o professor).
3. (opcional) **Deployment branches → Selected → `main`**.

A partir daí, o job `deploy-prod` **pausa aguardando aprovação** e só roda se a **assinatura
e a atestação** da imagem **passarem** na verificação (admission gate). *(O environment
`staging` é criado automaticamente, sem revisor.)*

> ⚠️ **"Required reviewers" não aparece?** As regras de proteção de *environment* só existem
> em repositório **público** ou **privado** com plano **Pro/Team/Enterprise/Education**. Num
> repo **privado gratuito** a opção fica oculta — nesse caso o `deploy-prod` **roda sem
> pausar** (a verificação da assinatura **continua** valendo). Para ter a pausa em repo
> privado, a **organização** precisa do plano **Team** — **grátis via GitHub Education** para
> docente verificado (o IFSul se qualifica). Alternativa: deixar o repo **público**.

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
