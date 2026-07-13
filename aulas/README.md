# Slides das aulas — VidaPlus (SSC)

Decks `.pptx` no template institucional do IFSul, um por aula, cobrindo a **base teórica
que habilita cada etapa do projeto**. Cada deck segue: capa → teoria (curada dos materiais
da disciplina) → **ponte com o VidaPlus** (entregável da fase) → encerramento.

> **São material de estudo do aluno.** Cada slide de conteúdo tem **notas de apresentação**
> com a explicação aprofundada (uma apostila embutida): o **slide** é o resumo visual; as
> **notas** são o texto para estudar. Para ler as notas, abra o `.pptx` no **modo de
> anotações** (PowerPoint: *Exibir → Anotações*; LibreOffice/OnlyOffice: *Exibir → Notas*)
> ou exporte "Handout com anotações" em PDF. Vários decks incluem **exemplos trabalhados**
> (ex.: correção do IDOR, teste BDD anti-IDOR, STRIDE/LINDDUN-per-element).
>
> **Leitura paralela:** o mesmo conteúdo, em texto corrido, está em
> [`docs/12-apostila.md`](../docs/12-apostila.md) — uma seção por aula.

| Deck | Semana | Fase | Tema |
|------|:------:|:----:|------|
| `aula-01-contexto-e-dados.pptx` | 1 | 0 | Por que SSDLC · pilares CIA+AAN · classificação de dados |
| `aula-02-requisitos-e-lgpd.pptx` | 2 | 1 | RF×RNF · RFS×RGS · 4 fontes · LGPD · SMART · ASVS |
| `aula-03-dfd-e-stride.pptx` | 3 | 2a | Threat modeling · DFD+trust boundaries · STRIDE |
| `aula-04-privacidade-linddun.pptx` | 4 | 2b | Privacy by Design · LINDDUN · PETs · RIPD |
| `aula-05-projeto-seguro.pptx` | 5 | 3 | Saltzer & Schroeder · fail-secure · priorização · DoD |
| `aula-06-implementacao-segura.pptx` | 6–9 | 4 | OWASP Top 10:2025 · controles · módulos LGPD |
| `aula-07-verificacao.pptx` | 10–11 | 5 | SAST/DAST/IAST/SCA · CI/CD · V&V |
| `aula-08-devsecops.pptx` | 12 | 6 | DevSecOps · SBOM/SLSA/Sigstore · CVSS/EPSS/KEV |
| `aula-09-operacao-incidentes.pptx` | 13 | 7 | Auditoria · métricas · resposta a incidente · ANPD |
| `aula-10-rastreabilidade.pptx` | 14 | 8 | Matriz · frameworks de controle · métricas · defesa |

> Semanas 15–16: defesas e buffer — sem deck novo (usar o recap dos conceitos).

## Como foram gerados

Gerados por código com a skill `ifsul-template` (`python-pptx`), a partir dos materiais
existentes (`SSDLC.pdf`, `requisitos-seguranca.pdf`) e dos `docs/` do projeto. Para
regenerar/editar, ajuste o script `gera_aulas.py` (fora do repositório) e rode com o venv
do `python-pptx`.

## Complementos além dos materiais originais

- **Ponte com o VidaPlus** em cada aula (slide "Entregável da Fase X").
- **OWASP Top 10:2025**, **EPSS/KEV**, **SLSA/Sigstore** e **frameworks de controle**
  (NIST 800-53 / ISO 27001) integrados ao fluxo.
- Alinhamento ao plano de **16 semanas** e ao foco **SSDLC + LGPD + privacidade**.
