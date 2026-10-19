# 11 · Apostila — material de estudo

Texto de leitura que **acompanha os slides** ([aulas/](../aulas/)). Cada seção corresponde
a uma aula; leia antes ou depois da aula para fixar. O conteúdo é o mesmo das **notas de
estudo** embutidas nos `.pptx`, em formato corrido. Ao final de cada aula está o
**entregável do projeto** que aquela base teórica habilita.

> Fio condutor da disciplina: *Requisito bem-feito = ativo + ameaça + atributo + critério
> verificável + rastreabilidade.*

---

## Aula 1 — Contexto e Dados · Semana 1 · Fase 0

**Objetivo:** conhecer o projeto e o funcionamento da disciplina; entender por que a
segurança precisa estar em todo o ciclo; e aprender a classificar os dados de um sistema.

**O projeto (contexto).** O cenário do semestre é o **VidaPlus**, portal web de uma clínica
(paciente, médico, laboratório, admin) que opera desde 2015 e **nunca passou por revisão de
segurança nem LGPD**. Um quase-incidente (um paciente viu o exame de outro trocando a URL —
IDOR) e a fiscalização da ANPD motivam a missão: **adequar o sistema à realidade atual, sem
reescrever do zero**. A stack é **atual** (React + Java 21/Spring Boot 3 + MySQL) — o
"legado" é o descuido com segurança, não a tecnologia.

**Como funciona a disciplina.** É **um projeto único e vivo** o semestre inteiro; a **teoria
é puxada pela demanda** de cada fase (você aprende o conceito quando precisa aplicá-lo). O
trabalho é em **grupos de 3–4**, cada um com um pacote de funcionalidades. A avaliação é
**por artefato** (o dossiê) + defesa; o pipeline é evidência, não a nota. O foco é o
**processo** (SSDLC + LGPD + privacidade), não codar muito: você **analisa** tudo, corrige em
aula guiada o **P0** do seu pacote e os **segredos no código** (F-10) e entrega um **plano de
remediação** do restante.

**Cronograma (16 semanas).** Fase 0 (sem. 1) kickoff+classificação · Fase 1 (sem. 2–3)
requisitos+LGPD · Fase 2 (sem. 4–5) STRIDE+LINDDUN+RIPD · Fase 3 (sem. 6) projeto
seguro+priorização+DoD · Fase 4 (sem. 7–10) remediação · Fase 5 (sem. 11–12)
verificação · Fases 6–7 (sem. 13–14) DevSecOps+operação · Fase 8 (sem. 15)
rastreabilidade · sem. 16 defesas. Detalhe em [01-plano-disciplina-ssdlc.md](01-plano-disciplina-ssdlc.md).

**Assuntos abordados.** SSDLC & fundamentos (shift-left, CIA+AAN, princípios de design) ·
requisitos & LGPD (RFS/RGS, 4 fontes, ASVS) · threat modeling (DFD, STRIDE, árvore de
ataque) · privacidade (Privacy by Design, LINDDUN, PETs, RIPD) · implementação segura (OWASP
Top 10, secure coding, cripto, segredos) · verificação & DevSecOps (SAST/DAST/SCA, CI/CD,
SBOM, auditoria, métricas).

**Por que SSDLC.** Segurança não é custo opcional: uma violação de dados custa, em média,
US$ 4,88 milhões e leva ~194 dias só para ser detectada (IBM 2024); 60% das brechas
exploram vulnerabilidades **já conhecidas** (Verizon DBIR) — ou seja, corrigíveis. O
**SDLC tradicional** trata segurança como um teste no fim (pentest na véspera do release),
quando corrigir é caro e muitas vezes se "adia". O **SSDLC (Secure SDLC)** embute segurança
em requisitos, design, código, teste e operação. Corrigir um bug na fase de requisitos
custa até **100× menos** que em produção.

**Pilares.** A tríade clássica é **CIA** — Confidentiality (só quem tem autorização
acessa), Integrity (o dado não é alterado indevidamente) e Availability (o sistema está
disponível). Sistemas modernos acrescentam **Autenticidade** (provar quem é quem),
**Autorização** (o que cada um pode fazer) e **Não-repúdio** (não poder negar a autoria).
Toda ameaça viola um desses atributos, e todo requisito existe para preservar um deles —
por isso o 1º passo de qualquer requisito é perguntar *"qual atributo preciso garantir?"*.

**Classificação de dados.** É um exercício de **segurança** (por impacto), não de direito:
pergunte *"que dano se este dado vazar ou for alterado?"*. Quanto maior o dano, maior o
nível (**Crítico → Confidencial → Interno → Público**), e o nível calibra o rigor (e o
custo) dos controles. **Regra do nível mais alto:** um fluxo herda o nível do dado mais
sensível que toca — a busca de pacientes é "só um formulário", mas retorna CPF, logo é
crítica. No VidaPlus, laudo de exame é dado de saúde = o mais sensível = **Crítico**.

**Entregável (Fase 0):** inventário de ativos/telas/endpoints/tabelas + tabela de
classificação por sensibilidade + os 3 fluxos críticos (login, laudo, auditoria) + trust
boundaries. A **base legal (LGPD)** de cada dado é preenchida só na Fase 1 (Aula 2) — aqui
basta marcar o que é sensível.

---

## Aula 2 — Requisitos & LGPD · Semanas 2–3 · Fase 1

**Objetivo:** transformar "quero segurança" em requisitos específicos e testáveis, usando a
LGPD como fonte formal.

**Funcional × não-funcional.** RF é *o que* o sistema faz (ex.: "recuperar a senha por
e-mail"); RNF é *como* se comporta (desempenho, segurança). Segurança aparece nos dois: há
funções de segurança (autenticar, cifrar, auditar) e há segurança como atributo transversal.
Tratar segurança como "RNF que dá para adiar" é a raiz da maioria dos incidentes —
reposicione: *"o sistema deve fazer X e somente X, mesmo sob ataque"* é requisito de negócio.

**RFS × RGS.** Distinção central da disciplina. O **RFS** (Requisito Funcional de Segurança)
é o **mecanismo** que protege (MFA, RBAC, cripto, auditoria) — responde *"o que o sistema
faz para se proteger?"*. O **RGS** (Requisito de Garantia / *assurance*) é a **evidência**
de que o mecanismo funciona (teste, cobertura, nível ASVS) — responde *"quanta confiança
temos?"*. Analogia: o RFS é "a porta deve ter fechadura"; o RGS é "a fechadura deve ser
testada e certificada". Equivalem ao par **SFR × SAR** do Common Criteria. Todo controle P0
tem um RGS associado.

> **No VidaPlus (recorte do projeto):** cada requisito é escrito em **duas formas**. A
> declaração ***shall*** (Forma 1, no dossiê, sem tecnologia) diz **o que** o sistema deve
> fazer — o papel do RFS. O **cenário Gherkin** (Forma 2, arquivo `.feature`) diz **como provar**
> — o papel do RGS: com a etiqueta `@security` ele roda no pipeline; com `@analise` fica
> especificado, sem rodar.

**As quatro fontes.** Requisitos não se inventam; garimpa-se em: **Negócio** (ativos, apetite
a risco), **Legal** (LGPD, PCI-DSS — não-negociáveis), **Padrões** (OWASP ASVS/Top 10, NIST,
CIS) e **Ameaças** (threat modeling, abuse cases). O negócio diz o que importa; a lei, o que
é obrigatório; os padrões, o esperado; as ameaças, contra o que defender.

**OWASP ASVS.** O *Application Security Verification Standard* é o exemplo concreto da fonte
"Padrões": um catálogo pronto de requisitos de segurança **verificáveis**, agrupado em três níveis
de rigor — **L1** (básico, verificável de fora/black-box), **L2** (padrão recomendado para
aplicações com **dados sensíveis**; exige revisão de projeto e código) e **L3** (avançado, para
sistemas críticos como saúde e finanças). Escolhe-se o **nível-alvo** pela classificação de dados
da Fase 0, e cada item do ASVS já vem testável — casando com o RGS. No VidaPlus, os dados de saúde
justificam mirar **L2** como linha de base, subindo a **L3** nos módulos mais sensíveis (prontuário,
exames).

### LGPD como fonte formal de requisitos

**O que é.** A LGPD (Lei 13.709/2018, em vigor desde 2020) disciplina o tratamento de dados
pessoais no Brasil. Aplica-se a qualquer organização — pública ou privada, de qualquer porte —
que trate dados de pessoas no país. Inspirada no GDPR europeu, sucede o Marco Civil da Internet.
Não é "só um tema jurídico": decisões técnicas (modelagem, arquitetura, logs, segurança) impactam
a privacidade, e a maioria dos vazamentos tem origem técnica — por isso a **TI é parte essencial
da conformidade**.

**Conceitos.** *Dado pessoal* = informação que identifica ou torna identificável uma pessoa (nome,
CPF, e-mail, IP). *Dado sensível* (**art. 11**) = saúde, biometria, origem racial, religião, opinião
política — regime reforçado. *Tratamento* = qualquer operação com o dado (coleta, uso, acesso,
compartilhamento, eliminação — praticamente toda ação em um sistema). *Titular* = a pessoa a quem
o dado se refere. No VidaPlus, prontuário e exame são **dados de saúde → sensíveis → o ativo mais
crítico**.

**Agentes.** *Controlador* decide as finalidades e os meios (a clínica); *Operador* trata em nome
dele (cloud, TI terceirizada); *Encarregado* (DPO) é o canal com titulares e a ANPD. A TI viabiliza
tecnicamente os deveres dos três.

**Princípios (art. 6).** Dez regras: finalidade, adequação, necessidade (**minimização** — coletar
só o mínimo), livre acesso, qualidade, transparência, segurança, prevenção, não discriminação e
responsabilização (*accountability* — comprovar conformidade com evidência).

**Bases legais (art. 7 e 11).** Todo tratamento precisa de uma; sem ela é irregular. Principais:
consentimento (livre, específico, revogável — *checkbox nunca pré-marcado*), obrigação legal
(retenção: prontuário ~20 anos pelo CFM, log 6 meses pelo Marco Civil), execução de contrato e
legítimo interesse (exige avaliação de impacto). Para dado sensível (art. 11), a base típica de uma
clínica é a **tutela da saúde**, não o consentimento. Erro comum: "usar consentimento para tudo".

**Direitos do titular (art. 18 e 19).** Confirmação, acesso, correção, eliminação/anonimização
(avaliando a retenção legal — exclusão lógica × física), portabilidade (CSV/JSON), informação sobre
compartilhamento e revogação do consentimento. Cada direito vira funcionalidade. Prazos (art. 19):
resposta simplificada imediata; declaração completa em até **15 dias**.

**Segurança e incidentes (art. 46 e 48).** O art. 46 exige medidas técnicas *e* administrativas
(cripto, controle de acesso, auditoria). Incidente = acesso não autorizado, vazamento, perda ou
alteração indevida (*todo vazamento é incidente; nem todo incidente vira vazamento*). Com risco
relevante, o art. 48 obriga comunicar à **ANPD e ao titular** (prazo regulamentado: **3 dias
úteis**). Sem logs não há como demonstrar diligência. A ANPD fiscaliza e aplica sanções
(advertência, multa, bloqueio, suspensão) — o risco técnico vira risco legal.

**ECA Digital × LGPD (Lei 15.211/2025).** O ECA Digital (em vigor desde 17/03/2026) estende o
Estatuto da Criança e do Adolescente ao ambiente digital e conversa diretamente com a LGPD. A LGPD
já dava tratamento especial aos dados de menores no **art. 14**: o tratamento deve atender ao
**melhor interesse** da criança/adolescente e, em regra, depende de **consentimento específico e
destacado de um dos pais ou responsável** (§1); é vedado condicionar o uso à coleta de dados
excessivos (§3); e a informação deve estar em **linguagem acessível à idade** (§6). O ECA Digital
transforma esses princípios em exigências técnicas — quase todas com raiz na LGPD: **verificação de
idade** confiável e proporcional (não o "clique se tem 18"), **privacidade por padrão** nas contas
de menores (o mesmo Privacy by Design da Aula 4, agora obrigatório), **minimização** reforçada,
**vedação a perfilamento e publicidade direcionada** a crianças, e **RIPD obrigatório** (o relatório
de impacto do art. 38) antes de liberar função acessível a menores. No VidaPlus, o prontuário de um
paciente pediátrico soma **duas camadas** — dado sensível de saúde *e* dado de criança —, e a tabela
de sanções da ANPD já classifica como **grave** a infração que envolve dados de crianças e
adolescentes; o ECA Digital acrescenta multa de até **R$ 50 milhões** por infração e suspensão da
atividade.

**Do artigo ao requisito.** Cada cláusula aplicável vira um requisito rastreável ("art. 46 → cifrar
dados em repouso com AES-256"). Artigos que mais impactam o VidaPlus: **art. 6** (princípios),
**art. 7/11** (bases legais; saúde = sensível), **art. 14** (dados de crianças/adolescentes;
ECA Digital), **art. 18** (direitos do titular), **arts. 8/9** (consentimento), **art. 46**
(segurança), **art. 38** (RIPD — feito na Aula 4).

**Bom requisito (SMART).** Específico (sem "ser seguro"), mensurável/testável, rastreável.
*Ruim:* "o sistema deve proteger as senhas". *Bom:* "senhas com Argon2id (m=19MiB), salt de
16 bytes; MD5/SHA-1 proibidos" — verificável por teste. Requisito sem critério de aceite é
só uma intenção.

**Entregável (Fase 1):** catálogo inicial com cada requisito nas duas formas (*shall* +
cenário Gherkin) por funcionalidade, base legal por dado, nível ASVS alvo (L2) justificado.

---

## Aula 3 — DFD & STRIDE · Semana 4 · Fase 2a

**Objetivo:** descobrir, de forma sistemática, como o sistema pode ser atacado — e derivar
requisitos de cada ameaça.

**As 4 perguntas (Shostack):** (1) o que construímos? — o DFD; (2) o que pode dar errado? —
STRIDE por elemento; (3) o que vamos fazer? — mitigar/transferir/aceitar/eliminar cada
ameaça; (4) fizemos um bom trabalho? — validar cobertura.

**DFD com trust boundaries.** O Data Flow Diagram mostra *como* os dados circulam: entidades
externas (retângulo), processos (círculo), armazenamentos (duas linhas), fluxos (seta) e
**trust boundaries** (tracejado vermelho) — a linha onde o nível de confiança muda
(internet → aplicação → banco). Toda travessia de fronteira é um ponto de controle
obrigatório: ali entram autenticação, validação de entrada e log.

**Exemplo trabalhado (slide).** No DFD do "recuperar exame" do VidaPlus, o titular
(Usuário/navegador, lado *não confiável*) cruza a fronteira para os processos *1.0 Autenticar* e
*2.0 Recuperar exame* (lado *confiável*), que acessam o armazenamento *D1 Exames (PII saúde)*. O
requisito de segurança aparece como uma **nota presa ao elemento que ele protege**: na travessia da
fronteira, **REQ-AUTH** (autenticar + validar entrada, TLS 1.3); no acesso ao dado sensível,
**REQ-CR** (cifrar em AES-256 e liberar só ao dono — o controle contra o IDOR do exame).

**STRIDE.** Cada letra é uma categoria de ameaça mapeada a uma propriedade violada:
**S**poofing (vs autenticidade), **T**ampering (vs integridade), **R**epudiation (vs
não-repúdio), **I**nformation disclosure (vs confidencialidade), **D**enial of service (vs
disponibilidade), **E**levation of privilege (vs autorização). A técnica prática é
**STRIDE-per-element**: para cada elemento do DFD, pergunte quais das 6 ameaças se aplicam.
Cada ameaça termina num **requisito** — é assim que o threat modeling "gera" requisitos.
Exemplo: *Download de exame · Elevation of Privilege → validar ownership no servidor → 403*.

**Árvore de ataque** (Schneier): raiz = objetivo do atacante, folhas = passos concretos com
custo/esforço; a defesa de cada folha é um requisito. Priorize defender o caminho mais
**barato** para o atacante.

**Entregável (Fase 2a):** DFD + STRIDE-per-element + árvore de ataque do recorte; alvo do
exercício: o **IDOR** no download (paciente A baixa o exame de B).

---

## Aula 4 — Privacidade (LINDDUN) · Semana 5 · Fase 2b

**Objetivo:** modelar ameaças de **privacidade** (não só de segurança) e produzir o RIPD.

**Privacy by Design** (Cavoukian, base da LGPD/GDPR): 7 princípios — proativo (não reativo),
privacidade **por padrão**, embarcada no design (não é "add-on"), funcionalidade total
(win-win, não trade-off), segurança fim a fim, transparência e respeito ao titular. A ideia:
privacidade nasce no design, é o padrão e não pede esforço do usuário.

**LINDDUN** — o "STRIDE da privacidade", roda sobre o **mesmo DFD**. As 7 categorias:
**L**inkability (ligar registros ao mesmo titular), **I**dentifiability (identificar por
trás do pseudônimo), **N**on-repudiation, **D**etectability (deduzir que um dado existe),
**D**isclosure (vazar dado), **U**nawareness (titular não sabe) e **N**on-compliance (fere a
lei). Um mesmo problema aparece em várias lentes: o IDOR do laudo é *Elevation of Privilege*
(STRIDE), *Disclosure + Linkability* (LINDDUN) e violação do art. 11/46 (LGPD).

**PETs (Privacy Enhancing Technologies)** — o repertório de controle: **minimização**
(coletar só o necessário — art. 6), **pseudonimização** (id opaco no lugar do CPF),
**anonimização** (titular não identificável, irreversível) e **retenção** (apagar/anonimizar
após o prazo). Minimização e retenção atacam a causa; pseudonimização e anonimização reduzem
o dano de um vazamento.

**RIPD / DPIA (art. 38):** documento formal exigido em tratamento de alto risco (caso do
VidaPlus). A tabela LINDDUN priorizada já é o miolo do RIPD (ameaça, risco, medida, risco
residual).

**Entregável (Fase 2b):** LINDDUN-per-element + controles/PET + esboço de RIPD + minimização
(campos sem finalidade, como religião/renda no cadastro). É a fase de **maior peso** na
avaliação.

---

## Aula 5 — Projeto Seguro · Semana 6 · Fase 3

**Objetivo:** decidir *como* os controles serão construídos, antes de codar.

**Princípios de Saltzer & Schroeder (1975):** *fail-safe defaults* (o padrão é NEGAR),
*complete mediation* (verifique SEMPRE — não confie que "já autenticou antes"), *least
privilege* (o mínimo necessário), *economy of mechanism* (simples = menos bugs), *open
design* (não depende de obscuridade) e *separation of privilege* (duas chaves). No VidaPlus,
o IDOR viola *complete mediation*; o banco com usuário root viola *least privilege*.

**Fail Secure × Fail Safe.** Falhas vão acontecer — a pergunta é: para qual estado o sistema
recai? Em software de segurança (autenticação, acesso a dado), **Fail Secure**: na dúvida,
NEGUE. *Fail Safe* (na dúvida, preservar vidas) é para sistemas físicos (porta de incêndio
abre). Todo controle do VidaPlus deve ser fail-secure: erro/timeout → acesso negado.

**Priorização por risco** = probabilidade × impacto. **CVSS** pontua a gravidade (0–10);
**DREAD** é qualitativo (documente os critérios — é subjetivo); **EPSS** dá a probabilidade
de exploração em 30 dias; **KEV** (CISA) marca o que já é explorado no mundo real. Um
requisito que mitiga risco crítico é **P0** e bloqueia o release; item no KEV **sobe um
nível** de prioridade.

**Representações do requisito** (use ≥2 por P0): declaração *shall*, **EARS** (*SE…ENTÃO…*),
**BDD/Gherkin** (*Dado/Quando/Então* — vira teste executável), item de catálogo **ASVS**,
**policy-as-code** (`deny[msg] { input.owner != input.user_id }`). Uma forma para
especificar, outra para verificar.

**Definition of Done segura:** a lista que precisa estar verde para "pronto" — threat model
atualizado, SAST/SCA sem críticos, testes de authn/authz passando, sem segredos, logging,
ASVS verificado. É o que impede a segurança de "cair" na pressa do release.

**Entregável (Fase 3):** catálogo consolidado (*shall* + Gherkin) + priorização (P0/P1/P2) +
DoD. O P0 que será corrigido na Fase 4 já vem atribuído ao seu pacote
([05](05-pacotes-trabalho-estudantes.md)); a **funcionalidade de criação** (✨) é só
especificada.

---

## Aula 6 — Implementação Segura · Semanas 7–10 · Fase 4

**Objetivo:** corrigir o código — as classes de falha do OWASP Top 10 e como remediá-las.

**OWASP Top 10:2025** (categorias mais frequentes): **A01** Broken Access Control (IDOR,
RBAC — o #1), **A02** Security Misconfiguration, **A03** Software Supply Chain (nova),
**A04** Cryptographic Failures, **A05** Injection, **A06** Insecure Design, **A07** Auth
Failures, **A08** Integrity Failures, **A09** Logging & Alerting, **A10** Mishandling of
Exceptional Conditions (nova). Quase toda falha plantada no VidaPlus cai numa dessas classes.

**Correções essenciais.** *Injection:* nunca concatenar SQL — use **consultas
parametrizadas** ("... WHERE email = ?"). *Access Control:* valide no **servidor** que o
recurso pertence ao usuário (deny by default, RBAC). *Crypto:* Argon2id, TLS 1.3,
AES-256-GCM — nunca MD5/SHA-1. *Segredos:* fora do código, em cofre/variável de ambiente.

**Exemplo — corrigir o IDOR (F-02).** *Antes:* `GET /api/exames/{id}` devolve qualquer
exame. *Ataque:* A troca o id e baixa o exame de B. *Correção:* o servidor descobre **de quem
é** o exame e compara com **quem está logado** — atenção: o id do **usuário** (no token) não é
o id do **paciente** (no exame); é preciso traduzir um no outro antes de comparar (no VidaPlus, a
Ana é o usuário 5 e a paciente 1). Se não for o dono, **403**. *Fail-secure:* deny by default;
403 **sem vazar metadados** (senão vira enumeração). *Garantia (RGS):* teste anti-IDOR no CI —
se a proteção some, o teste falha. (No VidaPlus os gates são informativos: o teste fica
vermelho, mas não bloqueia o merge; num projeto real, ele bloquearia.)

**Módulos LGPD que faltam no legado:** consentimento (versionado, revogável — art. 8/9),
portabilidade (exportar dados — art. 18), eliminação (anonimizar o prontuário por dever de
guarda + remover credencial), auditoria (trilha append-only, sem PII, hash-chain). *No VidaPlus
(recorte do projeto):* esses módulos são as funcionalidades de **criação** (✨) dos pacotes —
**especificadas** no dossiê, não implementadas.

**Entregável (Fase 4):** **prova de conceito** — corrigir o **P0** do pacote ponta-a-ponta
(aula guiada), num Pull Request, com o cenário `@security` que o prova; para o restante, um
**plano de remediação**. O objetivo é fechar o fio completo, não corrigir tudo.

---

## Aula 7 — Verificação · Semanas 11–12 · Fase 5

**Objetivo:** provar que a correção funciona e não regride.

**Tipos de teste** (cada um vê uma parte — use combinados): **SAST** (lê o código sem
executar — SQLi, hardcoded secret), **DAST** (ataca a app rodando — headers, config, ZAP),
**IAST** (instrumenta durante os testes), **SCA** (dependências e CVEs), **fuzzing**
(entradas inesperadas), **pentest** (visão humana). Atenção: **IDOR e RBAC são falhas
lógicas** — não aparecem em scanner genérico; precisam de teste de segurança escrito.

**Onde entram no CI/CD:** rápidos e baratos cedo (linter, SAST, secret scan a cada commit);
caros e profundos depois (DAST no pre-deploy, pentest 1–2×/ano). O pipeline do VidaPlus já
traz SAST, SCA, DAST e um gate de testes de segurança com MySQL.

**Verificação × Validação.** *Verificação* = "construímos o produto certo?" (atende à
especificação — testes, SAST/DAST). *Validação* = "construímos a coisa certa?" (resolve o
risco real; há ameaça sem requisito?). As duas são necessárias.

**Exemplo — teste anti-IDOR (BDD).** *Dado* A logado e o exame 222 (de B); *Quando* `GET
/api/exames/222` com o token de A; *Então* HTTP 403 e nenhum metadado de B. Um teste só
prova algo se **falha quando a proteção some** ("quebrar para ver vermelho"): no legado ele
falha (200); após a correção, passa (403).

**Entregável (Fase 5):** o teste do seu P0 (que já vem pronto) mostrado **vermelho** antes e
**verde** depois da correção, com a evidência real do CI, e a leitura dos relatórios dos scanners.
Requisito só está pronto com teste **E** evidência. Os gates ficam **informativos** — o workflow
já conclui **verde** desde o 1º push, e o vermelho dos testes de outros pacotes é esperado. A
nota **não** exige o pipeline 100% verde (nota = dossiê + defesa; pipeline = evidência).

---

## Aula 8 — DevSecOps & Supply Chain · Semana 13 · Fase 6

**Objetivo:** transformar segurança em processo automatizado e proteger a cadeia de
suprimentos.

**DevSecOps é cultura.** *"Security is everyone's job, but it's still someone's specialty."*
**Security Champions** (1 dev por squad como elo), **threat modeling** leve por feature,
**guardrails > gates** (o pipeline ajuda a fazer certo, não só bloqueia) e **blameless
post-mortem** (foco no sistema, não na pessoa).

**Supply chain — "assinar é o novo HTTPS".** Ataques cresceram +742%. Defesas: **SBOM** (a
"lista de ingredientes" do software, legível por máquina — CycloneDX/SPDX); **SLSA** (níveis
de garantia da proveniência do build, L0→L3; o L2 é uma meta de referência comum);
**Sigstore/cosign** (assinar
artefatos por identidade OIDC, sem gerenciar chaves). O próprio pipeline é alvo de alto valor
— trate-o como sistema crítico (runners efêmeros, OIDC, branch protection).

**Priorização de CVEs.** Não basta CVSS: um CVSS 10 com **EPSS** baixo e fora do **KEV** é
importante, não urgente; um CVSS 7 no KEV é urgente (explorado agora). Combine **CVSS × EPSS
× KEV**. O **VEX** documenta quando sua aplicação não é afetada por uma CVE.

**Entregável (Fase 6):** pipeline como **evidência** (não é a nota) — placar de gates
(vermelho→verde), SBOM e os segredos no código (F-10) **corrigidos em aula guiada** — fora do
código, valores trocados, Gitleaks 🔴→🟢. A **entrega segura já vem pré-fiada** (você **não configura ferramenta**): na `main`,
`release-sign` (build+push+**assina**+**atesta o SBOM**; ainda sem a proveniência que o SLSA L2
exige) → `deploy-staging`
(smoke/DAST) → `deploy-prod` (**verifica a assinatura** e exige **aprovação manual**). Seu
trabalho: **ler e explicar** o fluxo e a evidência no dossiê. A proteção da `main` e o
*environment* `production` são configurados pelo professor.

---

## Aula 9 — Operação & Incidentes · Semana 14 · Fase 7

**Objetivo:** operar com segurança e responder a incidentes.

**Auditoria e não-repúdio** — os requisitos mais esquecidos e os mais cobrados em incidentes.
Registrar: autenticação, autorização negada, acesso/alteração de dado sensível, ações
administrativas — **sem expor PII/segredos** no log (isso vira nova vulnerabilidade).
Qualidade do log: **imutável/assinado**, tempo sincronizado (NTP), retenção conforme a lei,
alertas em padrões anômalos. Uma técnica de integridade é o **hash-chain** (cada entrada
encadeia o hash da anterior: adulterar uma quebra a corrente), com `user_id` opaco no lugar de
nome/CPF. *No VidaPlus (recorte do projeto):* o legado **não tem** trilha de auditoria; ela é a
F-05 (criação, só especificada). O que existe para investigar são os **logs dos containers**
(`docker compose logs`), usados no roteiro da Fase 7.

**Métricas de operação:** **MTTD** (tempo até detectar — média do setor: 194 dias), **MTTR**
(tempo até remediar — meta: críticas < 14 dias), **% de cobertura** (código + dependências),
**escape rate** (bugs que chegaram à produção).

**Resposta a incidente** (fluxo de um vazamento de laudo): detecção → contenção → erradicação
(corrigir a causa-raiz) → recuperação → **notificação à ANPD e ao titular (LGPD art. 48)** →
lições (post-mortem realimenta os requisitos). Sem trilha de auditoria confiável, é impossível
provar quem fez o quê.

**Runbook-exemplo (modelo a adaptar) — vazamento de laudo no VidaPlus.** Cenário: a trilha de
auditoria detecta que o paciente A baixou o laudo do paciente B (IDOR). O modelo supõe a trilha
**já criada** (F-05); sem ela, a detecção depende dos logs do backend — e essa ausência entra no
runbook como lição (passo 6).

1. **Detecção.** Alerta da auditoria: acesso a `/api/exames/{id}` com `user_id ≠ dono do
   exame` (ou pico de downloads). Registre **quem, quando, qual exame**; **preserve o log**
   (hash-chain) — é a prova.
2. **Contenção** (minutos). **Revogue** as sessões/tokens suspeitos e **bloqueie/rate-limite**
   o endpoint afetado (se for PDF público, tire as URLs do ar). Objetivo: **estancar sem
   destruir evidência** — nunca apague o log para "limpar".
3. **Erradicação** (causa-raiz). Corrija o **IDOR**: validar **ownership no servidor** (o
   `user_id` vem do JWT, não do cliente), **deny-by-default**; se for o laudo público, exigir
   autenticação + **URL assinada com expiração**.
4. **Recuperação.** Faça o **redeploy** da correção e **valide com o teste anti-IDOR** (A pede
   exame de B → **403 sem metadados**); confirme a auditoria íntegra e volte a **monitorar**.
5. **Avaliação de risco + notificação (art. 48).** Dado de saúde é **sensível** → quase sempre
   há "risco ou dano relevante". Comunique **ANPD e titulares afetados em até 3 dias úteis**,
   com: **natureza dos dados** (laudos/exames), **nº de titulares**, **medidas técnicas
   adotadas**, **riscos** e **ações de mitigação/recomendações** ao titular. Registre tudo.
6. **Lições** (post-mortem sem culpados). Documente a **linha do tempo** e a **causa-raiz**;
   gere **novos requisitos** (ex.: teste anti-IDOR obrigatório no CI + **alerta** de acesso
   *cross-owner*) e atualize a **matriz de rastreabilidade**. O incidente vira requisito.

**Entregável (Fase 7):** operação das funcionalidades do pacote (o que monitorar, que eventos
registrar, sem PII) + runbook de resposta a um incidente do pacote, com o vazamento de laudo acima
como modelo. Quem tem o F-05 no pacote **especifica** a trilha (eventos, campos sem PII,
integridade), sem implementá-la.

---

## Aula 10 — Rastreabilidade & Métricas · Semana 15 · Fase 8

**Objetivo:** fechar o ciclo — provar que cada controle existe por uma razão e foi testado.

**Matriz de rastreabilidade.** Garante que cada requisito foi implementado **e** testado. É
bidirecional: do requisito ao teste (foi verificado?) e do código ao requisito (por que este
controle existe?). **Linha sem teste = requisito não verificado** — é onde mora o risco
residual.

**Mapa a frameworks de controle.** O requisito ganha força quando mapeado: **OWASP ASVS**
(L1/L2/L3), **NIST 800-53** (famílias AC/AU/IA/SC), **ISO 27001:2022** (Anexo A, 93
controles), **NIST SSDF** (liga ao ciclo). Fio completo estendido: *REQ → ameaça
(STRIDE/LINDDUN) → LGPD/ASVS → NIST 800-53 → ISO 27001*.

**Métricas do processo** (tornam segurança mensurável): % de ameaças com controle
(cobertura do threat model), % de requisitos com teste (verificação), % de ameaças de
privacidade mitigadas (LINDDUN), e o **placar de gates verdes/total do seu pacote** — a
métrica-mestra do progresso (medida sobre as funcionalidades que o grupo implementa, não
sobre todos os gates do repositório).

**Anti-padrões × boas práticas.** Pior antipadrão: requisito vago ("ser seguro") que ninguém
testa; segundo: "segurança no fim". Boas práticas: requisitos SMART e testáveis, elicitar
cedo (custo até 100× menor), vincular requisito → ameaça → teste, rastreabilidade viva. A rubrica
valoriza as boas práticas; os antipadrões derrubam a nota da própria fase (sem penalidades à
parte).

**Entregável (Fase 8 + defesa):** matriz completa + métricas + risco residual; na **defesa**,
o grupo escolhe um ativo e percorre o **fio completo ao vivo**: *laudo → IDOR → REQ-AC-01 →
403 → evidência do CI*.

---

> **Semana 16:** defesas. Sem conteúdo novo — recapitule os conceitos-chave e
> ensaie o fechamento do fio completo.
