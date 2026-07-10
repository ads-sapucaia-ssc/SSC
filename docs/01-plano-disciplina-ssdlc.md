# 01 · Plano da disciplina — SSDLC guiando a teoria

O semestre (~18 semanas) é organizado como um **SSDLC completo** aplicado ao VidaPlus.
Cada fase gera uma **demanda de conhecimento**; a aula teórica correspondente é dada
**no momento em que o grupo precisa aplicá-la** (*just-in-time*). Cada fase termina com
um **entregável versionado** e um **checkpoint** de 10 min por grupo.

Legenda de conformidade: 🟢 LGPD · 🔵 Privacy by Design/LINDDUN · 🟠 OWASP/ASVS · ⚙️ Pipeline.

---

## Fase 0 — Kickoff & inventário do legado · Semanas 1–2

**Demanda:** entender o que existe e o que está em risco antes de tocar em qualquer código.

- **Teoria (aula):** o que é SSDLC e por que "shift-left"; cenário de ameaças atual;
  risco específico de sistemas legados; CIA + Autenticidade/Autorização/Não-repúdio.
- **Prática no VidaPlus:** subir o sistema com `docker compose up`; explorar as
  funcionalidades; mapear endpoints, telas e tabelas.
- **Entregáveis:**
  - Inventário de ativos (telas, endpoints, tabelas, dados).
  - **Tabela de classificação de dados** 🟢 (Público/Interno/Confidencial/Crítico).
  - Diagrama de contexto (alto nível) + fronteiras.
- **Checkpoint:** o grupo consegue nomear os 3 fluxos mais críticos?

---

## Fase 1 — Requisitos & conformidade legal · Semanas 3–4

**Demanda:** dizer *o que o sistema deve garantir*, inclusive por lei.

- **Teoria:** engenharia de requisitos de segurança (SMART, testável); **LGPD** 🟢
  (princípios, bases legais, dados sensíveis art. 11, direitos do titular art. 18);
  **OWASP ASVS** 🟠 como catálogo de requisitos; SQUARE.
- **Prática:** para cada fluxo crítico, derivar Requisito Funcional de Segurança (RFS) e
  Requisito de Garantia (RGS); cruzar as **quatro fontes** (negócio, legal, padrões,
  ameaças).
- **Entregáveis:**
  - Catálogo inicial de requisitos de segurança + **base legal LGPD** por requisito.
  - Definição do **nível ASVS alvo** (L2) justificado pela classificação de dados.
- **Aula-ponte:** [02-lgpd.md](02-lgpd.md).

---

## Fase 2 — Privacidade & threat modeling · Semanas 5–7

**Demanda:** descobrir *como o sistema pode ser atacado* e *como a privacidade pode ser violada*.

- **Teoria:** **Privacy by Design** (7 princípios) 🔵; **LINDDUN** (Linkability,
  Identifiability, Non-repudiation, Detectability, Disclosure, Unawareness,
  Non-compliance) 🔵; **STRIDE** 🟠; DFD com trust boundaries; árvores de ataque.
- **Prática:** desenhar o **DFD** do recorte; rodar **LINDDUN** sobre o DFD para achar
  ameaças de privacidade; rodar **STRIDE-per-element** para ameaças de segurança;
  árvore de ataque para o ativo mais crítico (laudo/prontuário).
- **Entregáveis:**
  - DFD + trust boundaries.
  - Tabela **LINDDUN** (ameaça de privacidade → requisito/controle) 🔵.
  - Tabela **STRIDE-per-element** (ameaça → controle) 🟠.
  - Árvore de ataque com defesas-requisito.
- **Aula-ponte:** [03-privacy-by-design-linddun.md](03-privacy-by-design-linddun.md).

---

## Fase 3 — Projeto seguro (secure design) · Semanas 8–9

**Demanda:** decidir *como* os controles serão construídos, antes de codar.

- **Teoria:** arquitetura segura; criptografia aplicada (hash de senha Argon2id, TLS,
  cifra em repouso); autenticação/sessão e autorização (RBAC, deny-by-default);
  *secure defaults*; **Definition of Done** segura.
- **Prática:** especificar cada controle (ID, mecanismo, comportamento sob falha,
  representação BDD, nível ASVS); priorizar por risco (CVSS/DREAD → P0/P1/P2).
- **Entregáveis:**
  - Catálogo de requisitos consolidado (RFS + RGS) + priorização.
  - **Definition of Done** segura do projeto ⚙️.
  - Plano de correção (quais vulnerabilidades, em que ordem).

---

## Fase 4 — Implementação segura (remediação) · Semanas 10–13

**Demanda:** *corrigir de fato* o código legado.

- **Teoria:** **OWASP Top 10** 🟠; secure coding por classe de falha — Injection
  (consultas parametrizadas), Broken Access Control (IDOR, RBAC), Cryptographic
  Failures (hash, TLS, segredos), Auth Failures (rate limit, sessão), Security
  Misconfiguration, XSS; gestão de segredos.
- **Prática:** cada correção vira um **Pull Request** com descrição do problema, do
  controle e do teste. O código legado migra de vulnerável → seguro **incrementalmente**.
- **Entregáveis:**
  - PRs de correção por funcionalidade (ver [05-pacotes-trabalho-estudantes.md](05-pacotes-trabalho-estudantes.md)).
  - Implementação dos novos módulos exigidos pela LGPD (consentimento, exportação,
    exclusão/anonimização, auditoria) 🟢🔵.

---

## Fase 5 — Verificação (SAST/DAST/SCA) · Semanas 14–15

**Demanda:** *provar* que a correção funciona e não regride.

- **Teoria:** pirâmide de testes de segurança; **SAST** (CodeQL/Semgrep), **SCA**
  (dependências vulneráveis), **DAST** (OWASP ZAP), secret scanning; testes de
  segurança automatizados (IDOR, brute force, injection).
- **Prática:** escrever testes que exercitam cada requisito P0; ligar os scanners ao CI;
  transformar a falha em *gate bloqueante*.
- **Entregáveis:**
  - Suíte de testes de segurança + evidências.
  - Gates do pipeline configurados ⚙️.

---

## Fase 6 — Release & DevSecOps · Semana 16

**Demanda:** transformar segurança em **processo automatizado e repetível**.

- **Teoria:** DevSecOps; *supply chain* (SBOM, assinatura); gestão de segredos (cofre,
  variáveis injetadas); IaC seguro; *quality gates*.
- **Prática:** consolidar o `ssdlc-pipeline.yml` com todos os gates; badge de status;
  bloqueio de merge quando um gate crítico falha.
- **Entregáveis:**
  - Pipeline verde (ou com exceções documentadas) ⚙️.
  - SBOM gerado; sem segredos no repositório.
- **Aula-ponte:** [06-pipeline-cicd.md](06-pipeline-cicd.md).

---

## Fase 7 — Operação & resposta a incidentes · Semana 17

**Demanda:** e *depois* que está em produção?

- **Teoria:** logging/monitoração de eventos de segurança; detecção; **resposta a
  incidentes**; dever de **comunicação à ANPD** e ao titular (LGPD art. 48) 🟢.
- **Prática:** validar a trilha de auditoria (append-only, sem PII em texto claro);
  escrever um *runbook* de resposta a um vazamento de laudo.
- **Entregáveis:**
  - Trilha de auditoria funcional + verificação de integridade.
  - Plano de resposta a incidentes + fluxo de notificação ANPD.

---

## Fase 8 — Retro, métricas & auditoria · Semana 18

**Demanda:** *fechamos o fio completo? qual o risco residual?*

- **Teoria:** métricas de processo seguro; risco residual; melhoria contínua; auditoria.
- **Prática:** matriz de rastreabilidade final (ativo → ameaça → requisito → controle →
  teste → evidência → status); cálculo de métricas (% de ameaças com controle, % de
  requisitos com teste, gates verdes/total).
- **Entregáveis:**
  - **Matriz de rastreabilidade** completa.
  - Relatório de métricas + risco residual.
  - **Defesa final** (10–15 min) fechando o ciclo ao vivo.

---

## Cronograma-resumo

| Sem. | Fase | Marco |
|:---:|------|-------|
| 1–2 | 0 | Inventário + classificação de dados |
| 3–4 | 1 | Requisitos + LGPD |
| 5–7 | 2 | LINDDUN + STRIDE + árvore de ataque |
| 8–9 | 3 | Especificação + DoD + priorização |
| 10–13 | 4 | Correções (PRs) + novos módulos LGPD |
| 14–15 | 5 | Testes + SAST/DAST/SCA |
| 16 | 6 | Pipeline verde |
| 17 | 7 | Auditoria + resposta a incidentes |
| 18 | 8 | Rastreabilidade + defesa |

> **Avaliação contínua:** a nota não vem de uma prova única, mas do avanço mensurável a
> cada fase (entregáveis) e da **quantidade de gates de segurança que passam de vermelho
> a verde** no pipeline. Ver [08-rubrica-avaliacao.md](08-rubrica-avaliacao.md).
