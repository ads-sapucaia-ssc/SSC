# 01 · Plano da disciplina — SSDLC guiando a teoria (16 semanas)

Disciplina de **16 semanas × 3h** de projeto (48h; ~40–44h efetivas) — parte das **17 semanas** do calendário: a **semana restante** fica de **reserva para emergências**, fora do plano abaixo. **Teoria e prática
acontecem na mesma aula.** O foco é **SSDLC + LGPD + privacidade** — o VidaPlus é o
**cenário de simulação** onde os alunos aplicam a teoria. Cada fase gera uma **demanda de
conhecimento**; a aula teórica correspondente é dada **quando o grupo precisa aplicá-la**
(*just-in-time*), e termina com um **artefato versionado**.

> **A remediação é prova de conceito.** O grupo corrige **1–2 controles ponta-a-ponta**
> (para gerar evidência de que fecha o fio completo) e **analisa/planeja** o restante. Não
> é maratona de código — é engenharia de segurança e privacidade.

Legenda: 🟢 LGPD · 🔵 Privacy by Design/LINDDUN · 🟠 OWASP/ASVS · ⚙️ Pipeline (evidência).

---

## Orçamento de horas (teoria vs prática)

| Sem. | Fase | Teoria | Prática | Artefato entregue |
|:---:|------|:---:|:---:|-------------------|
| 1 | 0 · Kickoff + inventário | 1h | 2h | Inventário + classificação por sensibilidade |
| 2 | 1 · Requisitos de segurança (RFS+RGS, ASVS) | 1,5h | 1,5h | Catálogo inicial de requisitos 🟠 |
| 3 | 1 · Conformidade legal (LGPD: bases, art. 11/18) | 1,5h | 1,5h | Base legal por requisito 🟢 |
| 4 | 2 · DFD + STRIDE | 1h | 2h | DFD + STRIDE-per-element 🟠 |
| 5 | 2 · LINDDUN + PbD + RIPD | 1h | 2h | LINDDUN + esboço de RIPD/DPIA 🔵 |
| 6 | 3 · Projeto seguro + DoD | 1h | 2h | Especificação + priorização + DoD |
| 7 | 4 · Remediação (design do controle) | 1h | 2h | Design do P0 do pacote + plano |
| 8–9 | 4 · Remediação (implementação PoC) | 0,5h/sem | 2,5h/sem | 1–2 controles ponta-a-ponta |
| 10 | 4 · Remediação (fechamento) + plano do restante | 0,5h | 2,5h | Plano de remediação das demais falhas |
| 11 | 5 · Testes de segurança | 1h | 2h | Testes que exercitam o(s) controle(s) |
| 12 | 5 · SAST/DAST/SCA (evidência) | 1h | 2h | Evidências do pipeline ⚙️ |
| 13 | 6 · DevSecOps | 1h | 2h | Pipeline como gerador de evidência ⚙️ |
| 14 | 7 · Operação + resposta a incidente | 1,5h | 1,5h | Auditoria + plano de resposta/ANPD 🟢 |
| 15 | 8 · Rastreabilidade + métricas | 0,5h | 2,5h | Matriz de rastreabilidade + métricas |
| 16 | Defesas | — | 3h | Defesa final |

> **Regra das 3h:** ~1/3 teoria, ~2/3 prática. Para caber, **LGPD e LINDDUN têm leitura/
> vídeo prévio** ([02-lgpd.md](02-lgpd.md), [03-privacy-by-design-linddun.md](03-privacy-by-design-linddun.md))
> e o **ambiente já vem pronto** (pipeline + devcontainer) — não se gasta aula montando
> o ferramental.

---

## Fase 0 — Kickoff & inventário do legado · Semana 1
- **Teoria:** o que é SSDLC e "shift-left"; risco de legado; CIA + Aut./Autz./Não-repúdio.
- **Prática:** subir o VidaPlus; explorar telas/endpoints/tabelas; sentir as falhas.
- **Artefato:** inventário de ativos + **classificação de dados por sensibilidade/impacto**
  (Crítico→Público, regra do nível mais alto) + fluxos críticos.

> **Sequenciamento:** a classificação da Fase 0 é por **sensibilidade** (atividade de
> segurança — não exige LGPD). A **base legal** de cada dado (LGPD art. 11, 46…) é
> atribuída na **Fase 1**. Aqui o aluno só marca *o que* é
> sensível; *por que juridicamente* vem na semana seguinte.

## Fase 1 — Requisitos & conformidade legal · Semanas 2–3
- **Teoria:** requisitos de segurança (SMART/testável); **LGPD** 🟢 (princípios, bases
  legais, art. 11 saúde, art. 18 direitos); **ASVS** 🟠 como catálogo.
- **Prática:** derivar RFS + RGS dos fluxos críticos; cruzar as quatro fontes.
- **Artefato:** catálogo inicial de requisitos + **base legal** por requisito.

## Fase 2 — Privacidade & threat modeling · Semanas 4–5
- **Teoria:** **STRIDE**, DFD, árvore de ataque (sem. 4); **Privacy by Design + LINDDUN**
  🔵 e **RIPD/DPIA** (art. 38) (sem. 5).
- **Prática:** DFD com trust boundaries; **STRIDE-per-element**; **LINDDUN-per-element**;
  árvore de ataque do ativo mais crítico.
- **Artefatos:** DFD; STRIDE 🟠; **LINDDUN + esboço de RIPD** 🔵; árvore de ataque.
- **Este é o coração da disciplina** — a fase com maior peso na avaliação.

## Fase 3 — Projeto seguro · Semana 6
- **Teoria:** arquitetura segura, cripto aplicada (Argon2id, TLS), authn/authz
  (RBAC/deny-by-default), *secure defaults*, **Definition of Done** segura.
- **Prática:** especificar controles (ID, mecanismo, falha, BDD, ASVS); priorizar por
  risco (**CVSS/DREAD + EPSS/KEV** → P0/P1/P2). *EPSS* dá a probabilidade de exploração e
  *KEV* (catálogo CISA) marca o que já é explorado no mundo real — um item no KEV **sobe
  um nível** de prioridade, mesmo com CVSS médio.
- **Artefatos:** catálogo consolidado (RFS+RGS) + priorização + **DoD**.

## Fase 4 — Remediação (PROVA DE CONCEITO) · Semanas 7–10
- **Teoria:** OWASP Top 10 e secure coding **da classe do controle escolhido** (só o
  necessário para a PoC).
- **Prática:** cada grupo corrige, **ponta-a-ponta** (código → teste → evidência) num PR, o
  **P0 de correção já atribuído ao seu pacote** ([05](05-pacotes-trabalho-estudantes.md)) —
  e, se optar por fazer os dois, também a **funcionalidade de criação** (✨) do pacote. Não
  há escolha de *qual* funcionalidade corrigir: isso já está balanceado entre os grupos.
  Para as demais falhas do seu pacote, produz um **plano de remediação** (o quê, como,
  esforço, prioridade) — **sem precisar codar**. A correção do P0 é feita em **aula de
  laboratório guiada**, com o professor: são poucas linhas de código por grupo.
- **Artefatos:** 1–2 controles corrigidos + testes + evidência; **plano de remediação** do
  restante; módulo LGPD atribuído ao grupo (quando aplicável — consentimento/exportação/
  eliminação/auditoria) 🟢🔵.

## Fase 5 — Verificação · Semanas 11–12
- **Teoria:** testes de segurança; **SAST/DAST/SCA** e secret scanning como **evidência**.
- **Prática:** mostrar que o teste do P0 (que já vem pronto) estava vermelho e ficou verde
  depois da correção; ler os relatórios dos scanners com o roteiro da aula.
- **Artefatos:** teste + **evidências reais** do CI ⚙️.

## Fase 6 — Release & DevSecOps · Semana 13
- **Teoria:** DevSecOps; supply chain (SBOM); gestão de segredos.
- **Prática:** interpretar o pipeline como **gerador de evidência** e **explicar** no dossiê o
  que cada gate verifica, o SBOM e a entrega assinada. Os segredos no código (F-10) são
  **analisados** e entram no **plano de remediação** — sem mexer no código nem no pipeline. A
  configuração do repositório (proteção da `main`, aprovação de produção) é feita pelo professor.
- **Artefato:** pipeline evidenciando a PoC + placar de gates ⚙️.

## Fase 7 — Operação & resposta a incidentes · Semana 14
- **Teoria:** logging/monitoração; **resposta a incidentes**; dever de **comunicação à
  ANPD** e ao titular (LGPD art. 48) 🟢.
- **Prática:** descrever a operação das funcionalidades do pacote (o que monitorar, que eventos
  registrar, sem PII — e, para quem tem o F-05, validar a trilha: append-only, íntegra); escrever o
  *runbook* de um incidente das funcionalidades do pacote (o vazamento de laudo da aula é o modelo).
- **Artefatos:** trilha de auditoria + **plano de resposta a incidente**.

## Fase 8 — Retro, métricas & auditoria · Semana 15
- **Teoria:** métricas de processo; risco residual; melhoria contínua.
- **Prática:** matriz de rastreabilidade final (ativo→ameaça→requisito→controle→teste→
  evidência→status); métricas (% de ameaças com controle, % de requisitos com teste, etc.).
- **Artefatos:** **matriz de rastreabilidade** + relatório de métricas + risco residual.

## Defesas · Semana 16
- **Defesa final** (10–15 min): fechar o fio completo ao vivo, partindo de **um ativo →
  uma ameaça (STRIDE/LINDDUN) → o requisito → o controle → o teste → a evidência**.
> A **semana de reserva** do calendário (emergências) fica **fora** deste plano de 16 (ver a nota no topo).

---

> **Avaliação contínua e por artefato** — não há prova única. O peso está nos **artefatos
> de SSDLC/LGPD/privacidade** (ver [07-rubrica-avaliacao.md](07-rubrica-avaliacao.md)); a
> remediação entra como **prova de conceito**, e o pipeline como **evidência**, não como a
> nota em si.

---

## Anexo — mapa das fases do projeto ↔ NIST SSDF (SP 800-218)

Para dar o **vocabulário de framework** (sem virar estudo de catálogo), cada fase do
projeto corresponde a práticas do **NIST SSDF**, agrupadas em PO/PS/PW/RV:

| Grupo SSDF | Prática SSDF (exemplos) | Fase(s) do projeto |
|-----------|-------------------------|:-----:|
| **PO** — Prepare the Organization | PO.1 documentar requisitos de segurança; PO.5 separar ambientes | 0, 1 |
| **PS** — Protect the Software | PS.1 proteger o código (MFA, branch protection); PS.2 publicar **SBOM**/proveniência | 6 |
| **PW** — Produce Well-Secured Software | PW.1 **threat modeling** documentado; PW.4 reusar componentes seguros; PW.7 code review + **SAST**; PW.8 **DAST**/testes | 2, 4, 5 |
| **RV** — Respond to Vulnerabilities | RV.1 coletar reports; RV.2 triagem por severidade e SLA; RV.3 análise de causa | 5, 7, 8 |

> Assim o aluno relaciona o SSDLC "próprio" da disciplina ao padrão oficial usado na
> atestação federal dos EUA — e entende que **SSDF diz "o que fazer", SAMM "quão maduro",
> SDL "como codar"** (frameworks complementares).

---

## Anexo — as fases de requisitos ↔ SQUARE (CMU/SEI)

O **SQUARE** (*Security Quality Requirements Engineering*) é o processo formal de 9 passos
para elicitar, categorizar e priorizar requisitos de segurança. As fases 0–3 e 6/8 do
projeto **percorrem exatamente esses passos** (por isso o plano é "aproximado do SQUARE"):

| Passo SQUARE | Fase do projeto |
|--------------|:---------------:|
| 1 · Acordar definições (vocabulário comum) | 0 (kickoff) |
| 2 · Identificar ativos e metas de segurança | 0 |
| 3 · Desenvolver artefatos (use/misuse/abuse cases) | 1–2 |
| 4 · Avaliação de risco | 3 (priorização) |
| 5 · Selecionar técnica de elicitação | 1 |
| 6 · Elicitar os requisitos | 1 |
| 7 · Categorizar os requisitos (RFS × RGS) | 1, 3 |
| 8 · Priorizar por risco (P0/P1/P2) | 3 |
| 9 · Inspecionar e validar | 8 (V&V) + defesa |
