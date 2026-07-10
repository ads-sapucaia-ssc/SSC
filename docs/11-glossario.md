# 11 · Glossário

Consulta rápida dos termos usados na disciplina. Para o detalhe, veja o documento indicado.

## Processo / SSDLC

- **SSDLC** (*Secure Software Development Lifecycle*) — ciclo de desenvolvimento com
  segurança e privacidade embutidas em **todas** as fases, não só no fim. → [01](01-plano-disciplina-ssdlc.md)
- **Shift-left** — antecipar as atividades de segurança para o começo do ciclo (requisitos,
  design) em vez de deixá-las para o fim.
- **DevSecOps** — integrar segurança ao fluxo de CI/CD (testes, SAST/DAST/SCA, gates).
- **Prova de conceito (PoC)** — aqui: corrigir **1–2 controles** ponta-a-ponta para
  demonstrar o ciclo completo, sem corrigir tudo. → [05](05-pacotes-trabalho-estudantes.md)
- **Rastreabilidade** — ligar ativo → ameaça → requisito → controle → teste → evidência.
- **DoD** (*Definition of Done*) segura — critérios que definem quando algo está "pronto"
  com segurança (teste + evidência + rastreabilidade).

## Requisitos

- **RFS** (Requisito Funcional de Segurança) — o **mecanismo** que protege (autenticação,
  autorização, cripto, auditoria). Responde "o que o sistema faz para se proteger?".
- **RGS** (Requisito de Garantia de Segurança / *assurance*) — a **evidência** de que o
  mecanismo funciona (teste, cobertura, nível ASVS). Responde "quanta confiança temos?".
- **ASVS** (*Application Security Verification Standard*, OWASP) — catálogo de requisitos de
  segurança em 3 níveis (L1/L2/L3). O projeto adota **L2**.
- **EARS / Gherkin (BDD)** — notações para escrever requisitos/critérios testáveis
  (*Dado/Quando/Então*).
- **SMART** — requisito específico, mensurável, atingível, relevante e testável.

## Ameaças — STRIDE (segurança)

- **S**poofing — passar-se por outro (falha de autenticidade).
- **T**ampering — adulterar dados/código (integridade).
- **R**epudiation — negar uma ação por falta de registro (não-repúdio).
- **I**nformation disclosure — vazar informação (confidencialidade).
- **D**enial of service — indisponibilizar (disponibilidade).
- **E**levation of privilege — obter permissões indevidas (autorização). → [03](03-privacy-by-design-linddun.md)

## Ameaças — LINDDUN (privacidade)

- **L**inkability — ligar dois itens ao mesmo titular.
- **I**dentifiability — identificar o titular por trás do dado.
- **N**on-repudiation (privacidade) — prender o titular a algo que ele negaria.
- **D**etectability — deduzir que um item existe (ex.: enumeração).
- **D**isclosure of information — vazar dado a quem não deveria.
- **U**nawareness — titular não sabe/controla o tratamento.
- **N**on-compliance — sistema desobedece política/lei. → [03](03-privacy-by-design-linddun.md)

## Modelagem

- **DFD** (*Data Flow Diagram*) — diagrama de fluxo com entidades, processos,
  armazenamentos, fluxos e **trust boundaries**.
- **Trust boundary** — fronteira onde o dado cruza zonas de confiança distintas (ponto de
  controle obrigatório).
- **Árvore de ataque** — decomposição dos caminhos para atingir um objetivo do atacante,
  com custo/esforço e a defesa-requisito de cada folha.
- **Attack-defense tree** — árvore de ataque que também modela as defesas (bônus).

## Privacidade / LGPD

- **LGPD** — Lei Geral de Proteção de Dados (Lei 13.709/2018). → [02](02-lgpd.md)
- **Dados sensíveis** (art. 11) — incluem **saúde**; regime reforçado.
- **Base legal** (arts. 7 e 11) — fundamento que autoriza o tratamento (consentimento,
  tutela da saúde, obrigação legal…).
- **Direitos do titular** (art. 18) — acesso, correção, **portabilidade**, **eliminação**,
  informação sobre compartilhamento, revogação de consentimento.
- **Minimização** (art. 6) — coletar só o dado necessário à finalidade.
- **Privacy by Design (PbD)** — privacidade proativa, como padrão, embutida no design.
- **PET** (*Privacy Enhancing Technology*) — técnica que reduz risco à privacidade
  (pseudonimização, anonimização, cifra, minimização).
- **Pseudonimização** — substituir identificadores por referências (reversível com chave).
- **Anonimização** — tornar o titular não identificável (irreversível).
- **RIPD / DPIA** (art. 38) — Relatório de Impacto à Proteção de Dados Pessoais; alimentado
  pelo **LINDDUN**.
- **ANPD** — Autoridade Nacional de Proteção de Dados (fiscalização; notificação de
  incidente, art. 48).
- **DPO / Encarregado** (art. 41) — responsável pela conformidade LGPD.

## Controles & princípios

- **Deny-by-default** — negar acesso por padrão; liberar só o explicitamente permitido.
- **Fail-secure / fail-safe** — em caso de falha, o sistema recai no estado **seguro**
  (nega), não no permissivo.
- **RBAC** (*Role-Based Access Control*) — autorização baseada em papéis.
- **Least privilege** — cada ator com o mínimo de acesso necessário.
- **Argon2id / bcrypt** — funções de hash de senha fortes (com salt); substituem MD5/SHA-1.
- **TLS** — cifra em trânsito (HTTPS). **Cifra em repouso** — dados cifrados no
  armazenamento.
- **Hash-chain** — encadeamento de registros por hash para tornar a auditoria à prova de
  adulteração (append-only).

## Vulnerabilidades comuns

- **SQL Injection** — injeção via concatenação de SQL; mitiga-se com consultas
  **parametrizadas**.
- **IDOR** (*Insecure Direct Object Reference*) — acessar recurso de outro trocando o id;
  mitiga-se validando **ownership** no servidor.
- **XSS** (*Cross-Site Scripting*) — injeção de script; mitiga-se com *output encoding* e
  CSP.
- **Broken Access Control / Auth** — falhas de autorização/autenticação (OWASP A01/A07).
- **Hardcoded secret** — segredo no código; mitiga-se com variável de ambiente/cofre.

## Verificação & pipeline

- **SAST** — análise **estática** do código (ex.: Semgrep, CodeQL).
- **DAST** — análise **dinâmica** da aplicação em execução (ex.: OWASP ZAP).
- **SCA** (*Software Composition Analysis*) — análise de dependências vulneráveis (ex.:
  Dependency-Check, Trivy).
- **Secret scanning** — detecção de segredos no repositório (ex.: Gitleaks).
- **SBOM** (*Software Bill of Materials*) — inventário de componentes (ex.: CycloneDX).
- **Gate** — verificação no CI que pode **bloquear** o merge; no projeto, começa
  informativo e vira bloqueante conforme o grupo corrige. → [06](06-pipeline-cicd.md)
- **CVSS / DREAD** — métodos de pontuação de risco para priorização (P0/P1/P2).
