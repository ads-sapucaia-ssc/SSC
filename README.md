# Projeto Semestral SSC — VidaPlus (Sistema de Gestão em Saúde)

**Disciplina:** Segurança em Sistemas Computacionais (SSC)
**Câmpus:** IFSul — Sapucaia do Sul · Tecnologia em Análise e Desenvolvimento de Sistemas
**Professor:** Dr. Alex Mulattieri Suarez Orozco
**Modalidade:** projeto em grupos (3–4) conduzido ao longo de ~18 semanas

---

## 1. A ideia central

A disciplina inteira gira em torno de **um único projeto vivo**. Em vez de teoria
solta seguida de exercícios, o grupo recebe um **software legado real, inseguro e
desatualizado** — o **VidaPlus**, um sistema de gestão de clínica — e tem a missão
de **adequá-lo à realidade atual** de segurança e privacidade, seguindo um
**SSDLC (Secure Software Development Lifecycle)** completo.

O conteúdo teórico é **puxado pela demanda do projeto** (*just-in-time learning*):
quando a etapa exige modelar ameaças, a aula é threat modeling; quando exige
tratar dados pessoais, a aula é LGPD; quando exige provar que a correção funciona,
a aula é SAST/DAST/SCA no CI. O aluno aprende a teoria **no momento em que precisa
aplicá-la**, o que fixa o conhecimento e evita o antipadrão "segurança no fim".

> **Frase-síntese da disciplina:**
> *Requisito bem-feito = ativo + ameaça + atributo + critério verificável + rastreabilidade.*
> Agora aplicada não a um sistema hipotético, mas a **código que precisa ser corrigido**.

---

## 2. O software legado (VidaPlus)

Um sistema de clínica escrito por volta de 2015, no estilo da época, com **falhas
de segurança e privacidade plantadas de propósito**:

| Camada | Stack | Papel |
|--------|-------|-------|
| Frontend | **Angular** | Portal web (paciente, médico, recepção, laboratório, admin) |
| Backend | **Java + Spring Boot** (estilo legado) | API REST |
| Banco | **MySQL** | Dados cadastrais, prontuários, exames, logs |
| Orquestração | **Docker Compose** | Sobe tudo com um comando |

O VidaPlus lida com **dados sensíveis de saúde** — PII sob a **LGPD art. 11** — e por
isso é o pano de fundo ideal para **Privacy by Design** e **LINDDUN**.

As vulnerabilidades plantadas (SQL Injection, autenticação quebrada, IDOR, segredos
no código, ausência de auditoria, ausência de consentimento, sobrecoleta de dados,
ausência de retenção/eliminação etc.) estão catalogadas — **para o professor** — em
[docs/07-catalogo-vulnerabilidades.md](docs/07-catalogo-vulnerabilidades.md).

---

## 3. Estrutura do repositório

```
projeto-vidaplus/
├── README.md                        ← este arquivo
├── docs/                            ← desenho da disciplina e do projeto
│   ├── 00-visao-geral.md            visão, contexto do legado, papéis
│   ├── 01-plano-disciplina-ssdlc.md SSDLC ↔ teoria ↔ 18 semanas
│   ├── 02-lgpd.md                   LGPD embutida no SSDLC
│   ├── 03-privacy-by-design-linddun.md  PbD + LINDDUN
│   ├── 04-catalogo-funcionalidades.md   funcionalidades por criticidade
│   ├── 05-pacotes-trabalho-estudantes.md pacotes de trabalho por grupo
│   ├── 06-pipeline-cicd.md          gates de segurança ↔ fases do SSDLC
│   ├── 07-catalogo-vulnerabilidades.md  GABARITO do professor (não distribuir)
│   └── 08-rubrica-avaliacao.md      avaliação alinhada às fases
├── legacy-app/                      ← o software a ser corrigido
│   ├── docker-compose.yml
│   ├── db/         schema.sql + seed.sql (MySQL)
│   ├── backend/    Java Spring Boot (Maven)
│   └── frontend/   Angular
├── .github/workflows/              ← pipeline CI/CD (GitHub Actions)
│   └── ssdlc-pipeline.yml
└── security/                       ← configs dos scanners (ZAP, Semgrep, etc.)
```

---

## 4. Como rodar o legado

```bash
cd legacy-app
docker compose up --build
# Frontend:  http://localhost:4200
# API:       http://localhost:8080
# MySQL:     localhost:3306  (vidaplus / senha em docker-compose.yml — de propósito!)
```

Usuários de teste, endpoints e as falhas exploráveis estão em
[legacy-app/README.md](legacy-app/README.md).

---

## 5. O fio condutor (SSDLC → teoria → entrega)

| Fase do SSDLC | Semanas | Teoria puxada pela demanda | Entrega do grupo |
|---------------|:-------:|----------------------------|------------------|
| 0 · Kickoff & inventário do legado | 1–2 | SSDLC, cenário de ameaças, risco de legado | Inventário de ativos + classificação de dados |
| 1 · Requisitos & conformidade | 3–4 | Eng. de requisitos de segurança, **LGPD**, ASVS | Requisitos de segurança + base legal |
| 2 · Privacidade & threat modeling | 5–7 | **Privacy by Design, LINDDUN**, STRIDE, DFD | DFD + LINDDUN + STRIDE + árvore de ataque |
| 3 · Projeto seguro | 8–9 | Arquitetura segura, cripto, authn/authz, DoD | Especificação + Definition of Done |
| 4 · Implementação segura | 10–13 | OWASP Top 10, secure coding, remediação | Correção do código legado (PRs) |
| 5 · Verificação | 14–15 | SAST/DAST/SCA, testes de segurança | Testes + evidências no CI |
| 6 · Release & DevSecOps | 16 | Pipeline, supply chain, gestão de segredos | Pipeline verde com gates |
| 7 · Operação & resposta | 17 | Logging, monitoração, resposta a incidentes, ANPD | Trilha de auditoria + plano de resposta |
| 8 · Retro & auditoria | 18 | Métricas, risco residual, melhoria contínua | Matriz de rastreabilidade + defesa |

Detalhe completo em [docs/01-plano-disciplina-ssdlc.md](docs/01-plano-disciplina-ssdlc.md).

---

## 6. Para o professor

- O **gabarito das vulnerabilidades** e o mapeamento falha → fase → correção estão em
  [docs/07-catalogo-vulnerabilidades.md](docs/07-catalogo-vulnerabilidades.md).
- A **rubrica** por fase está em [docs/08-rubrica-avaliacao.md](docs/08-rubrica-avaliacao.md).
- O pipeline nasce **vermelho de propósito** (os gates falham no código legado). O
  progresso do grupo é medido pela quantidade de gates que passam a ficar verdes ao
  longo do semestre — a segurança vira **métrica objetiva e versionada**.
