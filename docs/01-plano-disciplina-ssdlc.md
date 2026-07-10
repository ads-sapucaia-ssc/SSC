# 01 · Plano da disciplina — SSDLC guiando a teoria (16 semanas)

Disciplina de **16 semanas × 3h** (48h de aula; ~40–44h efetivas). **Teoria e prática
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
| 1 | 0 · Kickoff + inventário | 1h | 2h | Inventário + classificação de dados 🟢 |
| 2 | 1 · Requisitos + LGPD | 1,5h | 1,5h | Requisitos de segurança + base legal 🟢🟠 |
| 3 | 2 · DFD + STRIDE | 1h | 2h | DFD + STRIDE-per-element 🟠 |
| 4 | 2 · LINDDUN + PbD + RIPD | 1h | 2h | LINDDUN + esboço de RIPD/DPIA 🔵 |
| 5 | 3 · Projeto seguro + DoD | 1h | 2h | Especificação + priorização + DoD |
| 6 | 4 · Remediação (escolha + design do controle) | 1h | 2h | Controle P0 escolhido + plano |
| 7–8 | 4 · Remediação (implementação PoC) | 0,5h/sem | 2,5h/sem | 1–2 controles ponta-a-ponta |
| 9 | 4 · Remediação (fechamento) + plano do restante | 0,5h | 2,5h | Plano de remediação das demais falhas |
| 10 | 5 · Testes de segurança | 1h | 2h | Testes que exercitam o(s) controle(s) |
| 11 | 5 · SAST/DAST/SCA (evidência) | 1h | 2h | Evidências do pipeline ⚙️ |
| 12 | 6 · DevSecOps | 1h | 2h | Pipeline como gerador de evidência ⚙️ |
| 13 | 7 · Operação + resposta a incidente | 1,5h | 1,5h | Auditoria + plano de resposta/ANPD 🟢 |
| 14 | 8 · Rastreabilidade + métricas | 0,5h | 2,5h | Matriz de rastreabilidade + métricas |
| 15–16 | Defesas + buffer | — | 3h + 3h | Defesa final |

> **Regra das 3h:** ~1/3 teoria, ~2/3 prática. Para caber, **LGPD e LINDDUN têm leitura/
> vídeo prévio** ([02-lgpd.md](02-lgpd.md), [03-privacy-by-design-linddun.md](03-privacy-by-design-linddun.md))
> e o **ambiente já vem pronto** (pipeline + devcontainer) — não se gasta aula montando
> toolchain.

---

## Fase 0 — Kickoff & inventário do legado · Semana 1
- **Teoria:** o que é SSDLC e "shift-left"; risco de legado; CIA + Aut./Autz./Não-repúdio.
- **Prática:** subir o VidaPlus; explorar telas/endpoints/tabelas; sentir as falhas.
- **Artefato:** inventário de ativos + **classificação de dados** 🟢 + fluxos críticos.

## Fase 1 — Requisitos & conformidade legal · Semana 2
- **Teoria:** requisitos de segurança (SMART/testável); **LGPD** 🟢 (princípios, bases
  legais, art. 11 saúde, art. 18 direitos); **ASVS** 🟠 como catálogo.
- **Prática:** derivar RFS + RGS dos fluxos críticos; cruzar as quatro fontes.
- **Artefato:** catálogo inicial de requisitos + **base legal** por requisito.

## Fase 2 — Privacidade & threat modeling · Semanas 3–4
- **Teoria:** **STRIDE**, DFD, árvore de ataque (sem. 3); **Privacy by Design + LINDDUN**
  🔵 e **RIPD/DPIA** (art. 38) (sem. 4).
- **Prática:** DFD com trust boundaries; **STRIDE-per-element**; **LINDDUN-per-element**;
  árvore de ataque do ativo mais crítico.
- **Artefatos:** DFD; STRIDE 🟠; **LINDDUN + esboço de RIPD** 🔵; árvore de ataque.
- **Este é o coração da disciplina** — a fase com maior peso na avaliação.

## Fase 3 — Projeto seguro · Semana 5
- **Teoria:** arquitetura segura, cripto aplicada (Argon2id, TLS), authn/authz
  (RBAC/deny-by-default), *secure defaults*, **Definition of Done** segura.
- **Prática:** especificar controles (ID, mecanismo, falha, BDD, ASVS); priorizar por
  risco (CVSS/DREAD → P0/P1/P2).
- **Artefatos:** catálogo consolidado (RFS+RGS) + priorização + **DoD**.

## Fase 4 — Remediação (PROVA DE CONCEITO) · Semanas 6–9
- **Teoria:** OWASP Top 10 e secure coding **da classe do controle escolhido** (só o
  necessário para a PoC).
- **Prática:** cada grupo escolhe **1 P0 (obrigatório) + até 1 P1** e o corrige
  **ponta-a-ponta** (código → teste → evidência) num PR. Para as demais falhas do seu
  pacote, produz um **plano de remediação** (o quê, como, esforço, prioridade) — **sem
  precisar codar**.
- **Artefatos:** 1–2 controles corrigidos + testes + evidência; **plano de remediação** do
  restante; módulo LGPD atribuído ao grupo (quando aplicável — consentimento/exportação/
  eliminação/auditoria) 🟢🔵.

## Fase 5 — Verificação · Semanas 10–11
- **Teoria:** testes de segurança; **SAST/DAST/SCA** e secret scanning como **evidência**.
- **Prática:** escrever teste que prova o controle da PoC; ler os relatórios dos scanners;
  tornar **bloqueante** o gate da classe corrigida.
- **Artefatos:** teste + **evidências reais** do CI ⚙️.

## Fase 6 — Release & DevSecOps · Semana 12
- **Teoria:** DevSecOps; supply chain (SBOM); gestão de segredos.
- **Prática:** interpretar o pipeline como **gerador de evidência**; SBOM; sem segredos no
  repo.
- **Artefato:** pipeline evidenciando a PoC + placar de gates ⚙️.

## Fase 7 — Operação & resposta a incidentes · Semana 13
- **Teoria:** logging/monitoração; **resposta a incidentes**; dever de **comunicação à
  ANPD** e ao titular (LGPD art. 48) 🟢.
- **Prática:** validar a trilha de auditoria (append-only, sem PII); escrever o *runbook*
  de um vazamento de laudo.
- **Artefatos:** trilha de auditoria + **plano de resposta a incidente**.

## Fase 8 — Retro, métricas & auditoria · Semana 14
- **Teoria:** métricas de processo; risco residual; melhoria contínua.
- **Prática:** matriz de rastreabilidade final (ativo→ameaça→requisito→controle→teste→
  evidência→status); métricas (% de ameaças com controle, % de requisitos com teste, etc.).
- **Artefatos:** **matriz de rastreabilidade** + relatório de métricas + risco residual.

## Defesas + buffer · Semanas 15–16
- **Defesa final** (10–15 min): fechar o fio completo ao vivo, partindo de **um ativo →
  uma ameaça (STRIDE/LINDDUN) → o requisito → o controle → o teste → a evidência**.
- Buffer para imprevistos (feriados, ajustes, reforço).

---

> **Avaliação contínua e por artefato** — não há prova única. O peso está nos **artefatos
> de SSDLC/LGPD/privacidade** (ver [08-rubrica-avaliacao.md](08-rubrica-avaliacao.md)); a
> remediação entra como **prova de conceito**, e o pipeline como **evidência**, não como a
> nota em si.
