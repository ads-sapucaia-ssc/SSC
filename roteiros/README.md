# Roteiros práticos — do VidaPlus ao dossiê do seu grupo

Esta pasta é o **passo a passo** para o seu grupo preencher o
[`entregaveis/DOSSIE.md`](../entregaveis/DOSSIE.md), fase por fase, usando o VidaPlus de verdade:
abrindo telas, enviando requisições, olhando o banco e lendo o código.

Cada roteiro funciona como uma **aula prática**: você segue os passos na ordem, observa o que o
sistema faz, anota, e no fim escreve a seção correspondente do dossiê.

> **Não é preciso saber segurança, Git, Spring, React nem MySQL para começar.** Os roteiros
> explicam cada comando, cada clique e cada termo no momento em que ele aparece. Se algo não
> estiver claro, é falha do roteiro: avise o professor.

---

## 1. Como a pasta está organizada

```
roteiros/
├── README.md                ← este arquivo: comece por aqui
├── comum/                   ← guias usados por todos os grupos
│   ├── 00-antes-de-comecar.md       instalar/conferir ferramentas e clonar o repositório
│   ├── 01-subir-o-vidaplus.md       ligar, desligar e "zerar" o sistema
│   ├── 02-observar-o-sistema.md     navegador, curl, banco de dados, código e logs
│   ├── 03-git-e-github.md           branch, commit, Pull Request, revisão, conflitos
│   ├── 04-testes-e-pipeline.md      rodar os testes e ler o resultado do pipeline
│   ├── 05-escrever-o-dossie.md      Markdown, tabelas, imagens, evidências, DFD
│   ├── 06-pipeline-do-zero.md       CI/CD e GitHub Actions do zero: o que é e onde clicar
│   ├── 07-ferramentas-de-seguranca.md  Semgrep, CodeQL, Gitleaks, ZAP, Trivy, SBOM, cosign
│   ├── 08-segredos-fora-do-codigo.md   aula guiada da F-10 (Fase 6): tirar e trocar os segredos
│   ├── zerar-banco.sh               religa o VidaPlus com o banco original (e recompila)
│   ├── tokens.sh                    faz login com os 6 usuários e guarda os tokens
│   ├── rodar-testes.sh              roda os testes de segurança no seu computador
│   └── fabricar-token.sh            monta um token com a chave do código (demonstração da F-10)
├── G1-identidade-e-trilha/          ← roteiros do Grupo 1
├── G2-prontuario-alheio-x-proprio/  ← roteiros do Grupo 2
├── G3-papeis-e-consentimento/       ← roteiros do Grupo 3
└── G4-vazamento-e-ciclo-de-vida/    ← roteiros do Grupo 4
```

Dentro da pasta de cada grupo há um roteiro por fase:

| Arquivo | Fase | Seção do dossiê | Aula | Semana(s) |
|---|---|:-:|:-:|:-:|
| `README.md` | apresentação do pacote do grupo | — | — | 1 |
| `fase-0-inventario.md` | 0 · Inventário e classificação de dados | §1 | 1 | 1 |
| `fase-1-requisitos.md` | 1 · Requisitos de segurança e LGPD | §2 | 2 | 2–3 |
| `fase-2a-stride.md` | 2a · DFD, STRIDE e árvore de ataque | §3 | 3 | 4 |
| `fase-2b-privacidade.md` | 2b · LINDDUN, Privacy by Design e RIPD | §4 | 4 | 5 |
| `fase-3-priorizacao.md` | 3 · Priorização e Definition of Done | §5 | 5 | 6 |
| `fase-4-remediacao.md` | 4 · Correção do P0 e plano de remediação | §6 | 6 | 7–10 |
| `fase-5-verificacao.md` | 5 · Verificação e evidências | §7 | 7 | 11–12 |
| `fase-6-pipeline.md` | 6 · Pipeline e segredos no código | §8 | 8 | 13 |
| `fase-7-operacao.md` | 7 · Operação e resposta a incidente | §9 | 9 | 14 |
| `fase-8-rastreabilidade.md` | 8 · Matriz, métricas e preparação da defesa | §10–11 | 10 | 15–16 |

---

## 2. Por onde começar

1. **Todos os integrantes**, na primeira aula:
   1. [`comum/00-antes-de-comecar.md`](comum/00-antes-de-comecar.md) — confere as ferramentas e
      clona o repositório do grupo;
   2. [`comum/01-subir-o-vidaplus.md`](comum/01-subir-o-vidaplus.md) — liga o sistema;
   3. [`comum/02-observar-o-sistema.md`](comum/02-observar-o-sistema.md) — aprende a olhar o
      sistema por dentro;
   4. [`comum/06-pipeline-do-zero.md`](comum/06-pipeline-do-zero.md) §1–§3 e
      [`comum/07-ferramentas-de-seguranca.md`](comum/07-ferramentas-de-seguranca.md) §1 — o que é o
      pipeline e as ferramentas de segurança (o resto destes guias, nas Fases 4 a 6);
   5. o `README.md` da pasta do **seu** grupo — entende o pacote de funcionalidades de vocês.
2. **A cada fase**, abra o roteiro da fase na pasta do seu grupo e siga do começo ao fim.
3. **Sempre que for gravar algo no repositório**, use
   [`comum/03-git-e-github.md`](comum/03-git-e-github.md).

| Grupo | Pasta | Pacote |
|---|---|---|
| **G1** | [`G1-identidade-e-trilha/`](G1-identidade-e-trilha/README.md) | Identidade & Trilha de Acesso |
| **G2** | [`G2-prontuario-alheio-x-proprio/`](G2-prontuario-alheio-x-proprio/README.md) | Prontuário: dado alheio × dado próprio |
| **G3** | [`G3-papeis-e-consentimento/`](G3-papeis-e-consentimento/README.md) | Papéis & Consentimento (DPO) |
| **G4** | [`G4-vazamento-e-ciclo-de-vida/`](G4-vazamento-e-ciclo-de-vida/README.md) | Vazamento & Ciclo de Vida do Dado |

---

## 3. Como os roteiros se relacionam com o resto do material

- **O dossiê de exemplo** — [`docs/12`](../docs/12-exemplo-dossie-preenchido.md) — é o dossiê
  completo de um grupo fictício (pacote 5). Os roteiros mandam você olhar a seção correspondente
  dele a cada fase, para ver **o nível de detalhe** esperado. Use como modelo de qualidade, nunca
  como resposta: o pacote dele é outro.
- **As aulas e a apostila** ([`docs/11`](../docs/11-apostila.md)) explicam a teoria. Os roteiros
  trazem só o mínimo de teoria para você conseguir fazer a prática; quando quiser entender mais,
  volte à aula.
- **O glossário** ([`docs/10`](../docs/10-glossario.md)) explica os termos. Na dúvida sobre uma
  palavra, procure lá (**Ctrl+F** no navegador ou no VS Code).
- **A rubrica** ([`docs/07`](../docs/07-rubrica-avaliacao.md)) diz como cada seção é avaliada.

---

## 4. Convenções dos roteiros

- **Todo comando é digitado na raiz do repositório** (a pasta onde o `ls` mostra `README.md  aulas
  docs  entregaveis  legacy-app  roteiros  security`). O terminal do VS Code já abre nela.
- **Todo comando aparece completo**, num bloco cinza. Copie o bloco inteiro (no Ubuntu, colar no
  terminal é **Ctrl+Shift+V**) e aperte **Enter**.
- Três comandos aparecem o tempo todo:

  | Comando | O que faz |
  |---|---|
  | `bash roteiros/comum/zerar-banco.sh` | religa o VidaPlus com o banco original (e recompila o código, se ele mudou) |
  | `source roteiros/comum/tokens.sh` | faz login com os 6 usuários de teste e guarda os tokens em `TOKEN_ANA`, `TOKEN_HOUSE`… |
  | `bash roteiros/comum/rodar-testes.sh "<filtro>"` | roda os testes de segurança no seu computador |

- Cada fase começa num **Passo 0** (preparar terminal, branch e VidaPlus) e termina num **Último
  passo** (gravar no Git e abrir o Pull Request).

## 5. Regras do jogo

1. **O que vale nota é o dossiê e a defesa**, não o pipeline verde. O pipeline é **evidência**.
2. **Todas** as funcionalidades do pacote são **analisadas**. Só o **P0** é **implementado** (na
   aula de laboratório guiada da Fase 4). A funcionalidade de criação (✨) é **especificada**, não
   implementada.
3. **Toda afirmação do dossiê precisa ter sido conferida no VidaPlus.** Se você escreveu "um
   paciente consegue ver o exame de outro", foi porque fez isso e viu acontecer. Os roteiros
   mostram como.
4. **Liderar uma fase não é fazê-la sozinho.** Quem lidera coordena e responde pela entrega; todos
   participam de todas as fases. Na defesa, o professor pergunta **a qualquer um sobre qualquer
   parte**.
5. **Não copie** o dossiê de exemplo nem o de outro grupo. Os roteiros dão o caminho; as
   conclusões são de vocês.
6. **Os ataques são feitos só no VidaPlus, no seu computador ou no do laboratório.** Nunca
   aponte os comandos destes roteiros para outro sistema: isso é crime (Lei 12.737/2012).
