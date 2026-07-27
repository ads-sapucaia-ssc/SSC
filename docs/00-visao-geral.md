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

**O que cada papel faz em cada fase** — todos têm uma ação em **toda** fase; **★ = quem lidera**
aquela fase, os demais **apoiam**. Cada fase alimenta a seção correspondente do `DOSSIE.md` (o
cabeçalho de cada seção do arquivo indica a fase):

| Fase | 🛡️ Segurança | 💻 Dev | ⚙️ DevSecOps | 📋 DPO |
|------|--------------|--------|-------------|--------|
| **0** Inventário | ★ mapeia ativos, endpoints e fluxos críticos | explora a app e reproduz o IDOR | faz o 1º push; confirma o pipeline; registra o placar | ★ classifica cada dado por sensibilidade/impacto |
| **1** Requisitos + LGPD | ★ escreve RFS+RGS e o nível ASVS | revisa a viabilidade técnica de cada RFS | liga cada requisito ao gate do CI que o prova | ★ atribui a base legal LGPD a cada dado |
| **2** Threat modeling | ★ DFD + STRIDE-per-element + árvore de ataque | ajuda no DFD (quem chama o quê no código) | marca no DFD onde cada scanner/gate atua | ★ LINDDUN-per-element + esboço do RIPD |
| **3** Projeto seguro | ★ especifica controles, prioriza P0/P1/P2 e escreve a DoD | estima o esforço e como implementar cada controle | indica qual gate provará cada controle | garante os controles de privacidade no catálogo |
| **4** Remediação (PoC) | revisa o PR (checklist) + plano de remediação | ★ corrige 1 P0 + 1 P1 ponta-a-ponta num PR | ★ acompanha os gates dentro do PR | faz o módulo LGPD do pacote |
| **5** Verificação | confere que a evidência é real (CI verde) | ★ escreve o teste do controle (`@Tag("security")`) | ★ lê os scanners e torna o gate bloqueante | confere que o requisito de privacidade tem teste |
| **6** Release/DevSecOps | confere o placar e o que virou bloqueante | garante que o build da imagem passa | ★ SBOM; remove os segredos (F-10); explica assinatura/atestação | confirma que não há PII/segredo nos artefatos |
| **7** Operação/incidente | ★ escreve o runbook do incidente | instrumenta o app para gravar o evento auditado | valida a trilha de auditoria (append-only, sem PII) | ★ escreve a notificação à ANPD/titular (art. 48) |
| **8** Rastreabilidade | ★ monta a matriz ativo→ameaça→…→evidência | confirma que cada controle tem teste ligado na matriz | extrai as métricas do pipeline | calcula métricas de privacidade + risco residual |

> **Liderar não é fazer sozinho.** O ★ apenas **coordena e responde** por aquela entrega — os
> **demais integrantes atuam junto** em toda fase (é o que cada coluna descreve). Isso importa
> porque a **avaliação é individual**: na defesa, o professor pergunta a **qualquer** integrante
> sobre **qualquer** parte, e o **fator individual** ([rubrica](07-rubrica-avaliacao.md))
> rebaixa quem domina só o próprio pedaço. Ninguém se isola na sua fase — todos precisam
> entender e saber defender o **fio inteiro**.

> A carga é **desigual de propósito** — como num time real, cada especialidade **lidera** (★)
> nas fases em que é mais exigida (só a Fase 4 exige os quatro no auge), e **apoia** nas demais.
> Por isso o **rodízio**: garanta que, no total, cada integrante **lidere ao menos uma fase**.

## Princípios inegociáveis do projeto

1. **Nada de "segurança no fim"** — cada sprint entrega segurança verificável.
2. **Deny by default / fail-secure** — na dúvida, negar acesso.
3. **Privacidade desde a concepção** — minimização, finalidade, retenção.
4. **Rastreabilidade** — todo controle liga-se a um ativo, uma ameaça e um teste.
5. **Evidência** — um requisito só está "pronto" quando há teste que o exercita e
   evidência (log do CI, print, relatório) que o comprova.
