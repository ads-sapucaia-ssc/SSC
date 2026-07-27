# Dossiê de Segurança e Privacidade

> **Este arquivo é o entregável do grupo** — já vem no repositório; **preencha-o** ao longo
> das fases (não é preciso criá-lo). Ele **é o entregável avaliado** (com a defesa). As linhas
> _em itálico_ são exemplos — **apague-as** e coloque o conteúdo do seu grupo. Cada seção
> mapeia uma fase do [plano](../docs/01-plano-disciplina-ssdlc.md) e um critério da
> [rubrica](../docs/07-rubrica-avaliacao.md).
>
> Lembre: **todas** as funcionalidades do pacote são **analisadas**; só **1–2** são
> **implementadas** (prova de conceito). O resto vira **plano de remediação**.

---

## 0. Identificação

- **Grupo:** _A / B / C / D_
- **Integrantes:** _Fulano, Beltrano, Cicrano, Fulana_
- **Liderança por fase (rodízio):** _a cada fase, um integrante **lidera** aquela entrega (os
  demais atuam junto) — ver o quadro de papéis × fase em
  [docs/00](../docs/00-visao-geral.md); registrem aqui quem liderou cada fase._
- **Pacote de funcionalidades:** _F-01, F-11, F-12, F-03…_
- **Controle(s) escolhido(s) para prova de conceito:** _F-02 (P0) + F-01 (P1)_

---

## 1. Fase 0 — Inventário & classificação de dados

**Fluxos críticos identificados:** _1) …  2) …  3) …_

| Ativo / Dado | Fluxo/Funcionalidade | Nível | Atributo prioritário | Quem demanda/define | Base legal | Observação |
|--------------|----------------------|:-----:|----------------------|---------------------|-----------|-----------|
| _Laudo (PDF)_ | _download (F-02)_ | _Crítico_ | _Confidencialidade+Integridade_ | _negócio, DPO, titular_ | _(Fase 1)_ | _saúde = sensível_ |
|  |  |  |  |  |  |  |

> **Preencha agora (Fase 0):** **Nível** e **Atributo** por **sensibilidade/impacto**; e, em
> **Quem demanda/define**, quem **exige ou define** aquele dado/fluxo — **negócio** (finalidade),
> **jurídico/DPO** (base legal), **segurança** (controles), **operações** (disponibilidade) ou o
> **titular** (direitos). A coluna **Base legal** fica para a **Fase 1** (após a aula de LGPD);
> aqui basta marcar o que é sensível/PII. *(A coluna "Quem demanda/define" não é item de nota —
> só orienta os requisitos da Fase 1.)*

---

## 2. Fase 1 — Requisitos de segurança & LGPD

Cada requisito de segurança do pacote é especificado em **duas formas
complementares**, propositalmente em níveis diferentes:

1. **Declaração *shall*** — **tecnologicamente agnóstica**, escrita **aqui no
   dossiê** (tabela abaixo). Diz *o que* o sistema deve garantir, em linguagem
   natural estruturada, **sem citar** framework, endpoint ou status HTTP. É a
   forma legível para negócio/DPO e que **sobrevive a uma troca de tecnologia**.
2. **Cenário Gherkin** — **vinculado à tecnologia**, escrito em um arquivo
   **`.feature`** versionado em
   `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/`. É a
   *mesma* exigência de forma **executável e verificável** (rota, status HTTP,
   corpo sem o dado sensível), que o **pipeline roda**.

A *shall* declara (agnóstica); o Gherkin prova (concreto). Um requisito só existe
quando tem as **duas** formas — a declaração no dossiê **e** o seu `.feature`.

> **shall × Cenário ≈ SFR × SAR (Common Criteria / ISO 15408).** A *shall*
> (agnóstica) é o **SFR** — o que o produto faz para se proteger; o `Cenário`
> (tecnológico) é o **SAR** — como se prova. O rigor do cenário é proporcional à
> criticidade — a mesma ideia dos níveis **EAL**.

### Forma 1 — Declarações *shall* (agnósticas, aqui no dossiê)

| Requisito | Declaração *shall* | Prioridade | Base legal / ASVS | Ameaça |
|-----------|--------------------|:----------:|-------------------|--------|
| _REQ-AUTH-SQLI-01_ | _O sistema **deve** rejeitar tentativas de login com conteúdo de injeção, sem autenticar e sem emitir credencial de sessão._ | _P0_ | _art. 46 · ASVS V5_ | _Injeção_ |
| _REQ-AC-IDOR-01_ | _O sistema **deve** validar a titularidade do recurso no servidor e negar o acesso ao laudo — sem revelar metadados — quando o solicitante não for o dono._ | _P0_ | _art. 11/46 · ASVS V4_ | _EoP/IDOR_ |
| _REQ-AC-RBAC-01_ | _O sistema **deve** permitir publicar laudo apenas a quem tem o papel autorizado; qualquer outro papel **deve** ser negado (deny-by-default)._ | _P0_ | _art. 46 · ASVS V4_ | _EoP_ |
| _REQ-SESS-01_ | _O sistema **deve** expirar a sessão e rejeitar a credencial após logout ou troca de senha._ | _P1_ | _art. 46 · ASVS V3_ | _Spoofing_ |
|  |  |  |  |  |

**Nível ASVS alvo:** _L2_ — **justificativa:** _dados sensíveis de saúde…_

### Forma 2 — Cenários Gherkin (`.feature`, vinculados à tecnologia)

Cada `.feature` (uma funcionalidade por arquivo) carrega as dimensões em **tags**
(`@REQ-…` liga de volta à declaração *shall*, `@P0`, `@lgpd-art11`, `@asvs-V4`,
`@stride-EoP`…), o `Cenário` como prova executável, e **uma** tag de estado:

- `@security` — requisito **implementado** na PoC: entra no gate
  `mvn test -Dgroups=security`, reusando os passos prontos (o *glue* em
  `src/test/java/br/ifsul/vidaplus/cucumber/`). Nasce 🔴 no legado e fecha 🟢
  após a correção (ver Fase 4);
- `@analise` — requisito **apenas analisado**: fica como spec executável de
  referência, **fora do gate** (o runner filtra `not @analise`), então **não
  quebra o CI** mesmo com passos ainda sem implementação.

| Requisito | Arquivo `.feature` | Estado |
|-----------|--------------------|--------|
| _REQ-AUTH-SQLI-01_ | _sqli.feature_ | _`@security` (implementado)_ |
| _REQ-AC-IDOR-01_ | _idor.feature_ | _`@security` (implementado)_ |
| _REQ-AC-RBAC-01_ | _rbac.feature_ | _`@security` (implementado)_ |
| _REQ-SESS-01_ | _sessao-analise.feature_ | _`@analise` (só especificado)_ |
|  |  |  |

> **Como rodar:** gate de segurança `mvn test -Dgroups=security` (roda os
> `@security`, ignora os `@analise`); build normal `mvn test -DexcludedGroups=security`
> (não roda requisito de segurança — os `@analise` continuam fora, sem quebrar).

---

## 3. Fase 2a — Threat modeling (STRIDE)

**DFD com trust boundaries:** _(inserir diagrama — imagem ou link)_

| Elemento (tipo) | S | T | R | I | D | E | Ameaça concreta | Requisito de mitigação |
|-----------------|:-:|:-:|:-:|:-:|:-:|:-:|-----------------|------------------------|
| _API REST (processo)_ | X |  |  | X |  | X | _bypass de authz…_ | _REQ-AC-IDOR-01…_ |
|  |  |  |  |  |  |  |  |  |

**Árvore de ataque (ativo mais crítico):** _(inserir — folhas, custo/esforço, defesa-requisito)_

---

## 4. Fase 2b — Privacidade: LINDDUN + Privacy by Design + RIPD

### 4.1 LINDDUN-per-element

| Elemento (DFD) | L | I | N | D | D | U | N | Ameaça de privacidade concreta | Controle/PET | Requisito |
|----------------|:-:|:-:|:-:|:-:|:-:|:-:|:-:|-------------------------------|--------------|-----------|
| _Logs_ |  | X |  |  |  |  |  | _log grava CPF em texto claro_ | _pseudonimização_ | _REQ-PRIV-LOG-01_ |
|  |  |  |  |  |  |  |  |  |  |  |

### 4.2 Privacy by Design (decisões)

| Princípio PbD | Como foi aplicado no VidaPlus | Evidência |
|---------------|-------------------------------|-----------|
| _Privacidade como padrão_ | _deny-by-default; coleta mínima_ | _config/schema_ |
|  |  |  |

### 4.3 RIPD/DPIA (esboço — LGPD art. 38)

| Ameaça de privacidade | Prob. | Impacto ao titular | Risco | Medida mitigadora | Risco residual |
|-----------------------|:-----:|--------------------|:-----:|-------------------|:--------------:|
| _Disclosure de laudo (IDOR)_ | _Alta_ | _Alto_ | _Crítico_ | _ownership server-side_ | _Baixo_ |
|  |  |  |  |  |  |

**Minimização:** _campos removidos por falta de finalidade (ex.: religião, renda) → …_

---

## 5. Fase 3 — Especificação, priorização & Definition of Done

**Priorização por risco (CVSS/DREAD + EPSS/KEV → P0/P1/P2):**

Além do CVSS/DREAD, considere o **contexto real de exploração**: **EPSS** (probabilidade
de ser explorada em 30 dias, 0–1) e **KEV** (está no catálogo CISA de vulnerabilidades já
exploradas?). Regra: **item no KEV sobe um nível** de prioridade.

| ID | Ameaça | Prob. | Impacto | Método | Pontuação | EPSS | KEV? | Risco | Prioridade |
|----|--------|:-----:|:-------:|:------:|:---------:|:----:|:----:|:-----:|:----------:|
| _REQ-AC-IDOR-01_ | _IDOR_ | _Alta_ | _Alto_ | _CVSS 4.0_ | _8.7_ | _n/a*_ | _—_ | _Alto_ | _P0_ |
|  |  |  |  |  |  |  |  |  |  |

_\* EPSS/KEV aplicam-se a **CVEs de dependências** (via SCA). Para falhas do próprio
código (IDOR, SQLi), use CVSS/DREAD; para as CVEs das libs, use CVSS + EPSS + KEV._

**Definition of Done segura do projeto:** _(lista de critérios que fecham uma funcionalidade)_

---

## 6. Fase 4 — Remediação

### 6.1 Prova de conceito (1–2 controles implementados)

Para cada controle implementado:
- **Controle:** _F-02 / REQ-AC-IDOR-01_
- **Problema (antes):** _`GET /api/exames/{id}` não checa dono → IDOR_
- **Correção (depois):** _validação de ownership server-side + deny-by-default_
- **PR:** _link_
- **Teste:** _`idor.feature` (@security) — A pede exame de B → 403 sem metadados; nasce 🔴, fecha 🟢_
- **Evidência:** _log do CI / print / relatório_

### 6.2 Plano de remediação (demais falhas do pacote — NÃO implementadas)

| Funcionalidade | Falha | Controle proposto | Esforço | Prioridade | Observação |
|----------------|-------|-------------------|:-------:|:----------:|-----------|
| _F-11 sessão_ | _JWT eterno_ | _expiração + rotação_ | _M_ | _P1_ | _… _ |
|  |  |  |  |  |  |

---

## 7. Fase 5 — Verificação & evidências

| Requisito | Teste (ID/tipo) | Ferramenta | Resultado | Evidência |
|-----------|-----------------|-----------|-----------|-----------|
| _REQ-AC-IDOR-01_ | _`idor.feature` (@security) / integração_ | _JUnit + Cucumber + ZAP_ | _Pass_ | _ci-idor.txt_ |
|  |  |  |  |  |

**Scanners como evidência:** _SAST (…), SCA (…), Secret scanning (…), DAST (…)._

---

## 8. Fase 6 — Pipeline (gerador de evidência)

| Gate | Estado inicial | Estado após PoC | Bloqueante? |
|------|:--------------:|:---------------:|:-----------:|
| _Testes de segurança_ | _🔴_ | _🟢_ | _sim_ |
| _SAST_ | _🔴_ | _🟡 (em curso)_ | _não_ |
|  |  |  |  |

**SBOM gerado:** _sim/não_ · **Segredos no repo:** _removidos?_

---

## 9. Fase 7 — Operação & resposta a incidente

- **Trilha de auditoria:** _descreva a sua trilha e como cada garantia é (ou será) atendida:
  é **append-only**? está **sem PII** em texto claro? tem **integridade verificável**
  (ex.: hash-chain)?_

### 9.1 Runbook de incidente — vazamento de laudo

> Preencha cada passo com **ação concreta**, **quem faz** e **o que registrar**. Regra de ouro:
> **conter antes de erradicar**, e **nunca apagar o log** (é a prova).

| Passo | O que fazer (concreto) | Quem | Evidência a registrar |
|-------|------------------------|------|-----------------------|
| **1 Detecção** | _como o incidente é percebido? (alerta da auditoria: acesso `user_id ≠ dono`)_ | _quem_ | _quem/quando/qual exame; log preservado_ |
| **2 Contenção** | _revogar sessão? bloquear/rate-limit endpoint? tirar URL do ar?_ | _quem_ | _hora da contenção; ações tomadas_ |
| **3 Erradicação** | _causa-raiz e correção (ownership no servidor; deny-by-default; URL assinada)_ | _quem_ | _PR da correção_ |
| **4 Recuperação** | _redeploy + validação com teste anti-IDOR (A→B = 403); volta a monitorar_ | _quem_ | _log do teste/CI verde_ |
| **5 Lições** | _post-mortem sem culpados; qual requisito novo nasce?_ | _quem_ | _linha do tempo; item na matriz_ |

### 9.2 Notificação LGPD (art. 48)

- **Há risco/dano relevante?** _dado de saúde é sensível → justifique a decisão._
- **Prazo:** _comunicar à ANPD e aos titulares em **até 3 dias úteis** da ciência._
- **Conteúdo mínimo:** _natureza dos dados afetados; nº de titulares; medidas técnicas
  adotadas; riscos; ações de mitigação e recomendações ao titular._

---

## 10. Fase 8 — Rastreabilidade, métricas & risco residual

### 10.1 Matriz de rastreabilidade (o "fio completo")

| ID | Requisito | Atributo | Origem (LGPD/ASVS) | Ameaça (STRIDE/LINDDUN) | Controle | Teste | Evidência | Status |
|----|-----------|----------|--------------------|--------------------------|----------|-------|-----------|--------|
| _REQ-AC-IDOR-01_ | _ownership_ | _Autz._ | _art.11; ASVS V4_ | _EoP/Disclosure_ | _server-side_ | _`idor.feature` (@security)_ | _ci-idor.txt_ | _Verificado_ |
|  |  |  |  |  |  |  |  |  |

### 10.2 Métricas do processo

| Métrica | Fórmula | Valor |
|---------|---------|:-----:|
| _% ameaças com controle_ | _mitigadas/total_ | _…_ |
| _% requisitos com teste_ | _com teste/total_ | _…_ |
| _% ameaças de privacidade (LINDDUN) com controle_ | _…_ | _…_ |

**Risco residual aceitável?** _justificar._

---

## 11. Defesa (roteiro)

**Formato:** 10–15 min, **banca = o professor**. É do **grupo**, mas **cada integrante conduz
ao menos um elo do fio e responde sobre qualquer parte** do projeto — o professor pode
perguntar a qualquer um sobre qualquer etapa. Prepare-se para defender o **todo**, não só a
sua parte. *(Como a defesa é pontuada — nota da defesa 0–10 e fator individual — em
[07-rubrica-avaliacao.md](../docs/07-rubrica-avaliacao.md), seção "Defesa — como pontuar".)*

Prepare-se para **fechar o fio ao vivo** partindo de **um** ativo:

```
Ativo (ex.: laudo) → Ameaça (STRIDE: Disclosure / LINDDUN: Disclosure)
   → Requisito (REQ-AC-IDOR-01) → Controle (ownership server-side)
   → Teste (idor.feature @security) → Evidência (HTTP 403, sem metadados, log do CI)
```

E saber responder: _qual dado é sensível e por quê (LGPD)? qual base legal? que ameaça de
privacidade o LINDDUN revelou? o que ficou no plano de remediação e por quê?_

### Checklist de preparação (cada integrante)

- [ ] Sei percorrer o **fio completo** de **um** ativo, do dado à **evidência**, sem consultar.
- [ ] Consigo **mostrar ao vivo** a evidência: o teste passando no **CI** e a linha na **matriz**.
- [ ] Sei explicar **um** achado de privacidade: ameaça **LINDDUN → PET/controle → RIPD**.
- [ ] Sei **justificar a priorização** (por que P0/P1/P2) e **o que ficou no plano** e por quê.
- [ ] Sei o **comportamento sob falha** (fail-secure) do controle e por que é o correto.
- [ ] Conheço as **bases legais** dos dados do nosso pacote (LGPD) e por que são sensíveis.
- [ ] Combinei com o grupo **qual elo cada um conduz** — e estudei também os **elos dos colegas**.
