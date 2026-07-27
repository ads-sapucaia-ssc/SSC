# 00 · Visão geral do projeto

## Natureza do projeto (leia primeiro)

O foco da disciplina é **SSDLC + LGPD + privacidade** — **não** desenvolvimento de software.
O VidaPlus é um **cenário de simulação** ("projeto real em escala acadêmica") que existe
para os alunos **vivenciarem, na prática, a aplicação da teoria**. Consequências:

- O **produto avaliado é o processo seguro** e seus artefatos (classificação de dados,
  requisitos+LGPD, threat model STRIDE e LINDDUN, Privacy by Design, RIPD/DPIA,
  rastreabilidade, verificação, resposta a incidente), **não** a quantidade de código.
- A **remediação é prova de conceito**: cada grupo corrige **1–2 controles ponta-a-ponta**
  para demonstrar que sabe fechar o fio ativo→ameaça→requisito→controle→teste→evidência;
  o restante das falhas é **analisado e planejado** (plano de remediação), não codado.
- O **pipeline e o autograding** são **geradores de evidência e feedback**, não a nota.

## Contexto narrativo

> A **Clínica VidaPlus** opera desde 2015 com um sistema web próprio, o **VidaPlus**,
> desenvolvido por uma equipe que já não está mais na empresa. O sistema "funciona":
> pacientes agendam consultas, baixam laudos de exames, médicos consultam resultados,
> a recepção cadastra pessoas e o laboratório publica laudos. Mas nunca passou por
> qualquer avaliação de segurança ou de conformidade com a **LGPD**.
>
> Com a fiscalização da **ANPD** e um quase-incidente (um paciente relatou ter visto o
> exame de outra pessoa trocando um número na URL), a direção contratou a sua equipe
> para **adequar o sistema à realidade atual**: corrigir as falhas, implementar
> privacidade desde a concepção e deixar o processo de desenvolvimento seguro e
> auditável — sem reescrever tudo do zero.

Esse enquadramento é proposital: o mundo real raramente é *greenfield*. Trabalhar sobre
**legado** obriga o aluno a ler código alheio, avaliar risco no que já existe, priorizar
correções e provar que a mudança não quebrou nada — competências que um projeto do zero
não exercita.

## Objetivos de aprendizagem

Ao final do semestre, o estudante será capaz de:

1. **Avaliar** a postura de segurança e privacidade de um sistema existente (inventário,
   classificação de dados, superfície de ataque).
2. **Elicitar e especificar** requisitos de segurança rastreáveis, incluindo obrigações
   legais (**LGPD**) e padrões (**OWASP ASVS**).
3. **Modelar ameaças** de segurança (**STRIDE**) e de **privacidade (LINDDUN)**, aplicando
   **Privacy by Design**.
4. **Projetar e implementar** controles seguros, corrigindo vulnerabilidades reais em
   frontend, backend e banco.
5. **Verificar** as correções com testes automatizados e ferramentas **SAST/DAST/SCA**
   integradas ao **CI/CD**.
6. **Operar** com trilha de auditoria, monitoração e um plano de **resposta a incidentes**
   compatível com o dever de comunicação à ANPD.
7. **Rastrear e defender** o trabalho: ativo → ameaça → requisito → controle → teste →
   evidência.

## Atores do sistema VidaPlus

| Ator | Papel | Acesso legítimo esperado |
|------|-------|--------------------------|
| Paciente | Titular dos dados | Apenas os próprios dados, exames e laudos |
| Médico | Acompanha resultados | Exames dos pacientes que **ele** atende/solicitou |
| Recepção | Cadastro e agendamento | Dados cadastrais; **não** o conteúdo clínico |
| Bioquímico/Laboratório | Publica laudos | Exames em processamento |
| Administrador | Gestão de usuários/papéis | Configuração; sem acesso clínico rotineiro |
| **Atacante** (implícito) | Adversário externo/insider | Nenhum — é o que se deve impedir |
| **Encarregado (DPO)** | Conformidade LGPD | Relatórios de tratamento, consentimento, incidentes |

## Dados tratados (ponto de partida — refinar na Fase 0)

| Ativo / Dado | Sensibilidade | Base legal provável |
|--------------|---------------|---------------------|
| Laudo/resultado de exame (PDF) | **Crítico** (saúde) | LGPD art. 11 |
| CPF, nome, data de nascimento, endereço | **Crítico** (PII) | LGPD art. 7 |
| Senha / credencial | **Crítico** | LGPD; ASVS V2 |
| Histórico clínico / diagnóstico | **Crítico** (saúde) | LGPD art. 11 |
| E-mail / telefone | Confidencial | LGPD |
| Dados de pagamento (exame particular) | **Crítico** | PCI-DSS |
| Logs de acesso e auditoria | Confidencial | LGPD; PCI-DSS 10 |
| Registro de consentimento | Confidencial | LGPD art. 8 |

## Papéis na equipe (responsabilidade individual, definida por fase)

Para dar **responsabilidade individual**, cada fase tem um(ns) **responsável(is)** — quem
lidera e responde por aquela entrega. Como a teoria é **just-in-time** (cada conceito é
ensinado na fase em que se usa), **os papéis não são travados na semana 1**: a cada fase,
quando o conceito dela é apresentado, o grupo define **quem lidera** — e **revezam** ao longo
do semestre, para cada integrante conduzir fases diferentes.

Há quatro **tipos de contribuição** (um mesmo aluno pode assumir tipos diferentes em fases
diferentes):

- **Security Champion (Segurança)** — requisitos, threat modeling (STRIDE), priorização, matriz.
- **Dev de aplicação (Dev)** — implementa as correções (frontend/backend) e escreve os testes.
- **DevSecOps** — pipeline, gates de CI, scanners, SBOM, segredos.
- **Encarregado de dados (DPO)** — LGPD, base legal, LINDDUN, consentimento, retenção, ANPD.

**O que cada responsável faz, fase a fase** (a última coluna indica a seção do `DOSSIE.md` que
a entrega alimenta):

| Fase | Quem lidera | Ações concretas |
|------|-------------|-----------------|
| **0** Inventário | Segurança + DPO | mapear ativos, endpoints e fluxos críticos; classificar cada dado por **sensibilidade/impacto** (`DOSSIE §1`) |
| **1** Requisitos + LGPD | Segurança + DPO | escrever **RFS+RGS** por funcionalidade e o nível ASVS (Segurança); atribuir a **base legal** LGPD a cada dado (DPO) — `DOSSIE §2` |
| **2** Threat modeling | Segurança + DPO | **DFD** + **STRIDE-per-element** + árvore de ataque (Segurança); **LINDDUN-per-element** + esboço do **RIPD** (DPO) — `DOSSIE §3–4` |
| **3** Projeto seguro | Segurança (Dev apoia) | especificar cada controle (mecanismo/BDD/ASVS); **priorizar** P0/P1/P2 (CVSS/DREAD × EPSS/KEV); escrever a **Definition of Done** — `DOSSIE §5` |
| **4** Remediação (PoC) | **os quatro** | **Dev**: corrige 1 P0 + 1 P1 ponta-a-ponta num **PR**; **DevSecOps**: acompanha os gates no PR; **Segurança**: revisa o PR (checklist) + escreve o plano de remediação; **DPO**: faz o módulo LGPD do pacote — `DOSSIE §6` |
| **5** Verificação | Dev + DevSecOps | **Dev**: escreve o teste que prova o controle (`@Tag("security")`); **DevSecOps**: lê os scanners e torna bloqueante o gate da classe corrigida — `DOSSIE §7` |
| **6** Release/DevSecOps | DevSecOps | conferir o **SBOM**; **remover os segredos** do repo (F-10); explicar **assinatura/atestação/promoção** no dossiê — `DOSSIE §8` |
| **7** Operação/incidente | DPO + Segurança | **DPO**: escreve a **notificação à ANPD/titular** (art. 48); **Segurança**: escreve o **runbook** do incidente; **DevSecOps**: valida a **trilha de auditoria** (append-only, sem PII) — `DOSSIE §9` |
| **8** Rastreabilidade | Segurança (todos) | montar a **matriz** ativo→ameaça→requisito→controle→teste→evidência; calcular **métricas** e **risco residual** — `DOSSIE §10` |

> A carga é **desigual de propósito** — como num time real, cada especialidade lidera nas fases
> em que é mais exigida (a Fase 4 é a única que ativa os quatro ao mesmo tempo). Por isso o
> rodízio: garanta que, no total, **cada integrante lidere ao menos uma fase**.

## Princípios inegociáveis do projeto

1. **Nada de "segurança no fim"** — cada sprint entrega segurança verificável.
2. **Deny by default / fail-secure** — na dúvida, negar acesso.
3. **Privacidade desde a concepção** — minimização, finalidade, retenção.
4. **Rastreabilidade** — todo controle liga-se a um ativo, uma ameaça e um teste.
5. **Evidência** — um requisito só está "pronto" quando há teste que o exercita e
   evidência (log do CI, print, relatório) que o comprova.
