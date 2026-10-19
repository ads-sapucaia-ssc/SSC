# 02 · LGPD embutida no SSDLC

A **Lei Geral de Proteção de Dados (Lei 13.709/2018)** não é um apêndice do projeto: ela
gera **requisitos** (Fase 1), **ameaças de não-conformidade** (Fase 2, via LINDDUN),
**controles** (Fases 3–4), **testes** (Fase 5) e **obrigações operacionais** (Fase 7).
Este documento mapeia os artigos que mais impactam o VidaPlus e **onde** cada um entra.

> O VidaPlus trata **dados pessoais sensíveis de saúde** → aplica-se o regime reforçado do
> **art. 11**. Isso eleva quase todo fluxo ao nível *Crítico*.

---

## 1. Princípios (art. 6) → viram requisitos e critérios de projeto

| Princípio (art. 6) | O que exige no VidaPlus | Entra na fase |
|--------------------|-------------------------|:-------------:|
| Finalidade | Cada dado coletado tem propósito declarado | 1, 3 |
| Adequação / Necessidade (minimização) | Não coletar dado que o fluxo não usa | 2 (LINDDUN), 3 |
| Livre acesso | Titular vê e exporta seus dados | 4 (módulo de portabilidade) |
| Qualidade dos dados | Dados corretos e atualizáveis | 4 |
| Transparência | Informar tratamento em linguagem clara | 4 (avisos), 7 |
| Segurança | Controles técnicos (art. 46) | 3, 4, 5 |
| Prevenção | Threat modeling, secure design | 2, 3 |
| Não discriminação | Sem uso indevido de dado sensível | 1, 8 |
| Responsabilização (accountability) | Provar conformidade (auditoria, rastreabilidade) | 6, 7, 8 |

---

## 2. Bases legais (arts. 7 e 11) → justificam cada tratamento

- **Art. 7** (dados pessoais): consentimento, execução de contrato, obrigação legal,
  tutela da saúde, legítimo interesse etc.
- **Art. 11** (dados sensíveis — **saúde**): regime mais estrito; no VidaPlus o tratamento
  de laudos/diagnósticos se apoia em **tutela da saúde** e/ou **consentimento específico
  e destacado**.

> **Requisito derivado:** o sistema deve registrar, para cada dado sensível, **qual base
> legal** o sustenta (campo `base_legal` no registro de consentimento/tratamento).

---

## 3. Direitos do titular (art. 18) → viram funcionalidades

Estes direitos **não existem** no VidaPlus legado e precisam ser **criados**. *No VidaPlus
(recorte do projeto):* viram as funcionalidades de criação ✨ (F-04, F-06, F-09), que os grupos
**especificam**, sem implementar.

| Direito (art. 18) | Funcionalidade a construir | Criticidade |
|-------------------|----------------------------|:-----------:|
| Confirmação e acesso | Titular consulta seus dados | Alto |
| Correção | Titular/recepção corrige cadastro (com auditoria) | Médio |
| **Portabilidade** (exportação) | Exportar todos os dados do titular (JSON/CSV) | **Alto** |
| **Eliminação / anonimização** | Excluir conta e dados, respeitando retenção legal | **Crítico** |
| Informação sobre compartilhamento | Listar com quem o dado foi compartilhado | Médio |
| Revogação de consentimento | Titular revoga; sistema para o tratamento | Alto |

---

## 4. Consentimento (arts. 8 e 9) → novo módulo

O legado coleta dados **sem registrar consentimento**. Falta um módulo de **Gestão de
Consentimento** (F-04 — no projeto, especificado pelo grupo que o recebe):

- Consentimento **específico, destacado e informado** (finalidade explícita).
- **Versionado** (a que termo/versão o titular consentiu e quando).
- **Revogável** a qualquer momento, com efeito no tratamento.
- Registro **auditável** (quem, quando, qual finalidade, qual base legal).

---

## 5. Segurança e boas práticas (arts. 46–49) → controles técnicos

- **Art. 46:** medidas técnicas e administrativas para proteger dados de acessos não
  autorizados e de situações acidentais/ilícitas. → cifra em trânsito (TLS) e em repouso,
  hash de senha, RBAC, deny-by-default, trilha de auditoria.
- **Art. 48:** **comunicação de incidente** à ANPD e ao titular quando houver risco/dano
  relevante. → entra na **Fase 7** (plano de resposta a incidentes).
- **Art. 50:** boas práticas e governança (programa de conformidade) → o próprio SSDLC do
  projeto é a evidência.

---

## 6. Papel do Encarregado (DPO) — art. 41

Dentro de cada grupo, o **Encarregado de dados** conduz os artefatos de LGPD: registro de
tratamento, matriz de bases legais, revisão de minimização (com o resultado do LINDDUN) e
o plano de resposta a incidentes.

---

## 7. Onde a LGPD aparece no pipeline

| Gate do CI | O que verifica (proxy de LGPD) |
|------------|--------------------------------|
| Secret scanning | Segredos/credenciais fora do código (art. 46) |
| SAST | Injeção, exposição de dados, cripto fraca (art. 46) |
| DAST | Cabeçalhos de segurança ausentes, erro que vaza detalhes internos (art. 46) |
| Testes de segurança (`@security`) | IDOR/vazamento de laudo, papel indevido, injeção no login (art. 46; acesso restrito ao dado de saúde, art. 11) |

> **Num projeto completo**, entrariam também **testes de auditoria** (trilha imutável — art. 37
> e art. 48) e **testes de consentimento/eliminação** (direitos do art. 18 exercitáveis). *No
> VidaPlus (recorte do projeto):* essas funcionalidades são só especificadas, então seus
> cenários ficam `@analise` e não rodam no pipeline.

---

## 8. RIPD / DPIA (art. 38)

Como o VidaPlus trata **dados sensíveis em larga escala**, é cabível um **Relatório de
Impacto à Proteção de Dados Pessoais (RIPD)**. O resultado do **LINDDUN** (Fase 2)
alimenta diretamente esse relatório: cada ameaça de privacidade identificada, seu risco e
a medida mitigadora compõem o RIPD. Ver [03-privacy-by-design-linddun.md](03-privacy-by-design-linddun.md).

---

## 9. Mapa a frameworks de controle (rastreabilidade estendida)

Um requisito ganha força quando **mapeado a controles padronizados** — facilita demonstrar
conformidade e reaproveitar catálogos. Além de **LGPD** e **OWASP ASVS**, ligue os
requisitos do VidaPlus a **NIST SP 800-53** (famílias) e **ISO/IEC 27001:2022 Anexo A**:

| Requisito (exemplo) | Ameaça | LGPD / ASVS | NIST 800-53 | ISO 27001:2022 |
|---------------------|--------|-------------|-------------|----------------|
| Autenticação forte (F-01) | Spoofing | art. 46 / ASVS V2 | IA-2, IA-5 | A.5.17 (autenticação) |
| Controle de acesso/IDOR (F-02/03) | EoP | art. 11,46 / ASVS V4 | AC-3, AC-6 | A.5.15, A.8.3 |
| Trilha de auditoria (F-05) | Repudiation | art. 37,46 / ASVS V7 | AU-2, AU-9 | A.8.15 (logging) |
| Cifra em repouso/trânsito (F-08/13-front) | Info disclosure | art. 46 / ASVS V6,V9 | SC-13, SC-28 | A.8.24 (cripto) |
| Gestão de segredos (F-10) | Spoofing/Tampering | art. 46 | IA-5, SC-12 | A.8.24, A.5.17 |

> Fio completo estendido: **REQ → ameaça (STRIDE/LINDDUN) → LGPD/ASVS → NIST 800-53 → ISO
> 27001**. Basta acrescentar essas colunas de origem na matriz de rastreabilidade (Fase 8)
> quando quiser demonstrar conformidade a mais de um framework. *Correlatos: **CIS Controls
> v8** (18 controles priorizados) para times menores; **NIST SSDF** para ligar ao ciclo.*

---

## Checklist LGPD do projeto (auto-verificação)

- [ ] Todo dado sensível tem base legal registrada (art. 11).
- [ ] Coleta minimizada — nenhum campo sem finalidade (art. 6).
- [ ] Consentimento específico, versionado e revogável (arts. 8–9).
- [ ] Direitos do titular exercitáveis: acesso, portabilidade, eliminação (art. 18).
- [ ] Cifra em trânsito e em repouso; senhas com hash forte (art. 46).
- [ ] Trilha de auditoria imutável, sem PII em texto claro (arts. 37/46).
- [ ] Plano de resposta e notificação à ANPD (art. 48).
- [ ] RIPD elaborado a partir do LINDDUN (art. 38).
