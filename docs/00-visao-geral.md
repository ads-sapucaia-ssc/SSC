# 00 · Visão geral do projeto

## Contexto narrativo (para dar aos estudantes)

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

## Papéis na equipe (sugestão dentro de cada grupo)

Para dar responsabilidade individual dentro do trabalho em grupo, sugere-se rotacionar
estes chapéus a cada fase:

- **Security Champion** — conduz threat modeling e revisão de requisitos.
- **Dev de aplicação** — implementa correções (frontend/backend).
- **DevSecOps** — mantém o pipeline e os gates de CI.
- **Encarregado de dados (DPO)** — cuida da LGPD, consentimento, LINDDUN e retenção.

## Princípios inegociáveis do projeto

1. **Nada de "segurança no fim"** — cada sprint entrega segurança verificável.
2. **Deny by default / fail-secure** — na dúvida, negar acesso.
3. **Privacidade desde a concepção** — minimização, finalidade, retenção.
4. **Rastreabilidade** — todo controle liga-se a um ativo, uma ameaça e um teste.
5. **Evidência** — um requisito só está "pronto" quando há teste que o exercita e
   evidência (log do CI, print, relatório) que o comprova.
