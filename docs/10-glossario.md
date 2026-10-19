# 10 · Glossário

Consulta rápida dos termos usados na disciplina. Para o detalhe, veja o documento indicado.

## Processo / SSDLC

- **SSDLC** (*Secure Software Development Lifecycle*) — ciclo de desenvolvimento com
  segurança e privacidade embutidas em **todas** as fases, não só no fim. → [01](01-plano-disciplina-ssdlc.md)
- **Shift-left** — antecipar as atividades de segurança para o começo do ciclo (requisitos,
  design) em vez de deixá-las para o fim.
- **DevSecOps** — integrar segurança ao fluxo de CI/CD (testes, SAST/DAST/SCA, gates).
- **Prova de conceito (PoC)** — corrigir **poucos controles** ponta-a-ponta para demonstrar o
  ciclo completo, sem corrigir tudo. No VidaPlus: o **P0** do pacote e os segredos (F-10), em
  aulas guiadas. → [05](05-pacotes-trabalho-estudantes.md)
- **Rastreabilidade** — ligar ativo → ameaça → requisito → controle → teste → evidência.
- **DoD** (*Definition of Done*) segura — critérios que definem quando algo está "pronto"
  com segurança (teste + evidência + rastreabilidade).

## Requisitos

- **RFS** (Requisito Funcional de Segurança) — o **mecanismo** que protege (autenticação,
  autorização, cripto, auditoria). Responde "o que o sistema faz para se proteger?".
  ≈ **SFR** (Common Criteria).
- **RGS** (Requisito de Garantia de Segurança / *assurance*) — a **evidência** de que o
  mecanismo funciona (teste, cobertura, nível ASVS). Responde "quanta confiança temos?".
  ≈ **SAR** (Common Criteria).
- **Declaração *shall*** — o requisito em linguagem natural, **sem tecnologia** ("O sistema
  deve…"). No VidaPlus é a **Forma 1**, escrita no dossiê; faz o papel do RFS.
- **Cenário Gherkin / `.feature`** — o mesmo requisito como **critério de aceite executável**
  (*Dado/Quando/Então*), num arquivo `.feature`. No VidaPlus é a **Forma 2**; faz o papel do RGS.
- **`@security` / `@analise`** — etiquetas dos cenários do VidaPlus: `@security` **roda** no
  pipeline (o requisito foi implementado); `@analise` fica **especificado**, sem rodar.
- **ASVS** (*Application Security Verification Standard*, OWASP) — catálogo de requisitos de
  segurança em 3 níveis (L1/L2/L3). O projeto adota **L2**.
- **EARS / Gherkin (BDD)** — notações para escrever requisitos/critérios testáveis
  (*SE…ENTÃO…* / *Dado/Quando/Então*).
- **Policy-as-code** — o requisito vira **regra executável** (ex.: OPA/Rego:
  `deny[msg] { input.owner != input.user_id }`) — aproxima requisito e verificação.
- **SMART** — requisito específico, mensurável, atingível, relevante e testável.
- **SQUARE** (*Security Quality Requirements Engineering*, CMU/SEI) — processo formal de
  9 passos para elicitar, categorizar e priorizar requisitos de segurança. O plano da
  disciplina é "aproximado do SQUARE" (ver [01](01-plano-disciplina-ssdlc.md)).
- **Common Criteria** (ISO/IEC 15408) — padrão de requisitos **certificáveis**: separa
  **SFR** (o que protege) de **SAR** (prova de que protege), com níveis de rigor **EAL 1–7**
  (TOE = alvo avaliado; PP/ST = perfis/alvos de requisitos).

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
- **Attack-defense tree** — árvore de ataque que também modela as defesas (avançado).

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

## Princípios de design seguro (nomeados)

Muitos já aparecem na prática do projeto (deny-by-default, least privilege, fail-secure).
Vale reconhecê-los pelo nome — são a base atemporal de todo o SSDLC.

- **Saltzer & Schroeder (1975) — 8 princípios:**
  1. *Economy of mechanism* — design simples e pequeno (menos código, menos bugs).
  2. *Fail-safe defaults* — padrão é **negar**; permissão é decisão explícita.
  3. *Complete mediation* — toda requisição é verificada (sem cache de autorização).
  4. *Open design* — segurança não depende do segredo do mecanismo (Kerckhoffs), só da chave.
  5. *Separation of privilege* — ações críticas exigem mais de uma condição/chave.
  6. *Least privilege* — cada identidade só com o mínimo necessário.
  7. *Least common mechanism* — minimizar recursos compartilhados entre usuários.
  8. *Psychological acceptability* — interface segura tem de ser usável (senão é contornada).
- **Secure by Design** — segurança como propriedade emergente da arquitetura (threat
  modeling em cada feature; eliminar classes inteiras de vulnerabilidade).
- **Secure by Default** — o usuário recebe a configuração mais segura sem precisar mudar
  nada (MFA ligado, TLS forçado, sem senha padrão, sem segredo hardcoded).
- **Defense in depth** — camadas independentes; uma falha não compromete tudo.
- **Segregation of Duties (SoD)** — nenhuma pessoa controla um processo crítico de ponta a
  ponta (ex.: quem escreve o PR não é quem aprova nem quem faz o deploy).

## Controles & princípios

- **Deny-by-default** — negar acesso por padrão; liberar só o explicitamente permitido.
- **Fail-secure / fail-safe** — em caso de falha, o sistema recai no estado **seguro**
  (nega), não no permissivo.
- **RBAC** (*Role-Based Access Control*) — autorização baseada em papéis.
- **Least privilege** — cada ator com o mínimo de acesso necessário.
- **Argon2id / bcrypt** — funções de hash de senha fortes (com salt); substituem MD5/SHA-1.
- **TLS** — cifra em trânsito (HTTPS). **Cifra em repouso** — dados cifrados no
  armazenamento.
- **Hash-chain** (encadeamento por hash) — cada registro da auditoria guarda um hash dos
  **próprios dados + o hash do registro anterior** (`hash[n] = H(dados[n] + hash[n-1])`).
  Alterar, apagar ou inserir uma linha **quebra a cadeia** dali em diante, tornando a
  adulteração **detectável**. Com **append-only** (só acrescenta) e **sem PII**, dá à trilha
  integridade e não-repúdio (STRIDE-**R**; arts. 37/46). É o mesmo princípio do git/blockchain.
  No VidaPlus, a trilha (F-05) é **especificada**, não implementada.

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

- **Linter** — **ferramenta** de análise estática **leve** que aplica regras ao código-fonte
  (estilo + bugs simples; ex.: ESLint, Ruff, Checkstyle). Roda em segundos no **pre-commit** —
  a triagem mais barata do *shift-left*. *Linting* é a **prática** de usá-lo. Mais raso que o
  **SAST** (que faz análise de fluxo de dados focada em vulnerabilidades).
- **SAST** — análise **estática** do código (ex.: Semgrep, CodeQL).
- **DAST** — análise **dinâmica** da aplicação em execução (ex.: OWASP ZAP).
- **SCA** (*Software Composition Analysis*) — análise de dependências vulneráveis (ex.:
  Dependency-Check, Trivy).
- **Secret scanning** — detecção de segredos no repositório (ex.: Gitleaks).
- **SBOM** (*Software Bill of Materials*) — inventário de componentes (ex.: CycloneDX).
- **Proveniência** (*provenance*) — registro **verificável de origem** do artefato: de qual
  **fonte (repo + commit)**, por qual **builder** e com quais **insumos** ele foi produzido
  (identificado pelo **digest**). Permite verificar que o binário não foi trocado/adulterado
  **no build** (defesa tipo anti-SolarWinds).
- **SLSA** (*Supply-chain Levels for Software Artifacts*, OpenSSF) — níveis (**L0–L3**) que
  medem **quão confiável** é a proveniência: `L1` existe · `L2` **assinada** por plataforma
  hospedada (meta de referência comum) · `L3` build **isolado/não-forjável**. O pipeline do
  VidaPlus ainda **não** chega ao L2 (ver *cosign*).
- **Sigstore / cosign** — assinatura de artefatos por **identidade OIDC** (*keyless*, sem
  gerenciar chaves), registrada no log público **Rekor**. No projeto, assina a imagem e o SBOM;
  para o SLSA L2 faltaria assinar também a **proveniência**. → [06](06-pipeline-cicd.md)
- **Gate** — verificação no CI que pode **bloquear** o merge. No projeto, os gates ficam
  **informativos**: mostram o problema, mas não travam o merge. → [06](06-pipeline-cicd.md)
- **CVSS / DREAD** — métodos de pontuação de risco para priorização (P0/P1/P2).

## Gestão de vulnerabilidades

- **CVE** (*Common Vulnerabilities and Exposures*) — ID universal de uma vulnerabilidade
  publicada (ex.: CVE-2021-44228, Log4Shell).
- **CWE** (*Common Weakness Enumeration*) — categoria/tipo de fraqueza (ex.: CWE-89 SQLi).
- **EPSS** (*Exploit Prediction Scoring System*) — probabilidade (0–1) de a CVE ser
  explorada em 30 dias; atualizado diariamente. Complementa o CVSS com contexto real.
- **KEV** (*Known Exploited Vulnerabilities*) — catálogo da CISA de vulnerabilidades **já
  exploradas no mundo real**; estar no KEV **eleva a prioridade**.
- **VEX** (*Vulnerability Exploitability eXchange*) — documento que afirma se uma CVE
  presente no produto é (ou não) explorável naquele contexto.
- **Priorização moderna** — não basta CVSS: combine **CVSS × EPSS × KEV** (ver a matriz
  em [`DOSSIE.md`](../entregaveis/DOSSIE.md), Fase 3).
- **SLA de remediação** — prazo para corrigir por severidade (ex.: críticas em < 14 dias).

## Frameworks de controle e maturidade

Catálogos aos quais se pode **mapear** requisitos para demonstrar conformidade (ver o mapa
em [02-lgpd.md](02-lgpd.md), seção 9):

- **NIST SP 800-53** — catálogo de controles em famílias (**AC** acesso, **AU** auditoria,
  **IA** identificação/autenticação, **SC** cripto/comunicação…). Referência para sistemas
  críticos.
- **ISO/IEC 27001:2022** — SGSI; **Anexo A** com 93 controles (ex.: A.5.17 autenticação,
  A.8.15 logging, A.8.24 cripto).
- **CIS Controls v8** — 18 controles priorizados; pragmático para times menores.
- **NIST SSDF** (SP 800-218) — liga requisitos ao ciclo (PO/PS/PW/RV) e à atestação. →
  [01](01-plano-disciplina-ssdlc.md).
- **OWASP SAMM / BSIMM** — modelos de **maturidade** de AppSec (quão maduro o processo é).

## No dia a dia do projeto (termos dos roteiros)

- **Container / imagem / Docker Compose** — a *imagem* é o pacote pronto de um programa; o
  *container* é ela rodando; o *Compose* liga vários containers juntos (banco, backend,
  frontend). → [`roteiros/comum/01`](../roteiros/comum/01-subir-o-vidaplus.md)
- **Token / JWT** — o "crachá" que o sistema entrega no login e que vai em cada requisição
  (cabeçalho `Authorization: Bearer …`). É **assinado** com uma chave secreta: quem tem a chave
  fabrica crachás. → [`roteiros/comum/02`](../roteiros/comum/02-observar-o-sistema.md)
- **401 × 403** — *401*: "não sei quem você é" (sem login ou token inválido); *403*: "sei quem
  você é, mas você não pode" (falta permissão). Um IDOR corrigido responde **403**.
- **Variável de ambiente** — um valor que o sistema operacional entrega ao programa ao ligá-lo
  (ex.: `JWT_SECRET`); o código lê com `System.getenv`. Tira o segredo de dentro do código.
- **`.env` / `.gitignore`** — o `.env` guarda os segredos **no seu computador**; o `.gitignore`
  garante que ele **nunca** vá para o Git. O `.env.example` (sem valores) vai, como modelo. →
  [`roteiros/comum/08`](../roteiros/comum/08-segredos-fora-do-codigo.md)
- **Teste-semente** — o cenário `@security` do P0 que **já vem pronto** no repositório: nasce
  vermelho no legado e fica verde quando a correção funciona. →
  [`roteiros/comum/04`](../roteiros/comum/04-testes-e-pipeline.md)
- **Fingerprint (Gitleaks)** — a "impressão digital" de um achado (commit + arquivo + regra + linha). Ir
  no `.gitleaksignore` diz ao Gitleaks: "este achado antigo do histórico já foi tratado".
- **Branch / commit / Pull Request** — linha de trabalho separada / ponto gravado no histórico /
  pedido revisável para juntar a branch na `main`. →
  [`roteiros/comum/03`](../roteiros/comum/03-git-e-github.md)
- **Workflow / job / placar** — o arquivo do pipeline / cada tarefa dele / o resumo dos gates no
  fim de uma execução. → [`roteiros/comum/06`](../roteiros/comum/06-pipeline-do-zero.md)
