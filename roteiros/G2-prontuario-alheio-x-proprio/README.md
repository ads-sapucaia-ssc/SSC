# G2 · Prontuário: dado alheio × dado próprio — apresentação do pacote

Este é o ponto de partida do **Grupo 2**. Leia inteiro antes da Fase 0: ele explica **o que é o
seu pacote**, **onde cada funcionalidade está no VidaPlus** e **o que vocês entregam**.

> Ainda não preparou o ambiente? Faça antes
> [`comum/00`](../comum/00-antes-de-comecar.md), [`01`](../comum/01-subir-o-vidaplus.md) e
> [`02`](../comum/02-observar-o-sistema.md).

---

## 1. O tema do pacote, em linguagem simples

O seu pacote cuida do **dado mais sensível do VidaPlus — o laudo de exame** — visto de dois
lados que são espelho um do outro:

- **Dado alheio:** um paciente **não pode** ver o exame de outro paciente; um médico não pode ver
  o exame de quem ele não atende. Hoje, basta **trocar um número na barra de endereços**.
- **Dado próprio:** o paciente **tem o direito** de receber os **próprios** dados (LGPD, art. 18,
  portabilidade). Hoje, isso não existe.

Negar o alheio e entregar o próprio usam **a mesma pergunta**: *"este dado é desta pessoa?"*. Por
isso as duas coisas estão no mesmo pacote.

## 2. As funcionalidades do pacote

| Func. | O que é | Criticidade | Ação | Nível de entrega |
|---|---|:-:|:-:|---|
| **F-02** Visualização/download de laudo | abrir um exame e baixar o laudo. Recorte do G2: **autorização** — o servidor confere se quem pede é o dono (ou o médico dele), e nega o resto (*deny-by-default*) | 🔴 P0 | 🛠️ corrigir | **implementada** na aula guiada (o teste já vem pronto) |
| **F-09** Exportação do titular (portabilidade) | o paciente baixa **todos os seus dados**, num formato estruturado | 🟠 P1 | ✨⚖️ criar | **especificada** (não implementada) |
| **F-07** Publicação de laudo | o laboratório libera o laudo para o paciente | 🟠 P1 | 🛠️ corrigir | analisada + plano |
| **F-08** Armazenamento de PDFs | onde e como os arquivos de laudo ficam guardados e são entregues | 🟠 P1 | 🛠️⚖️ corrigir/adequar | analisada + plano — **teste com um exame não publicado** (veja a nota do [`docs/04`](../../docs/04-catalogo-funcionalidades.md)) |
| **F-10** Segredos no código | higiene contínua (todos os grupos) | 🟠 P1 | — | analisada + plano (Fase 6) |
| **F-21** Dependências (SCA/SBOM) | higiene contínua (todos os grupos) | 🟢 P2 | — | leitura do pipeline (Fase 6) |

**Teste pronto do P0:** `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/idor.feature`
— cenário "paciente não acessa o laudo de outro paciente" (requisito `REQ-AC-IDOR-01`). Filtro:
`"security & F-02"`. Hoje ele **falha** de propósito.

> **O G4 tem o mesmo P0 (F-02), com outro recorte** (por onde o laudo vaza). Cada grupo trabalha
> no próprio repositório; comparar as duas soluções na defesa é parte do valor.

**Escopo mínimo da criação (F-09),** segundo o [`docs/05`](../../docs/05-pacotes-trabalho-estudantes.md):
exportação dos dados de **1 titular** em formato estruturado (JSON ou CSV), **autenticada**. Vocês
**especificam**; não programam.

## 3. Onde cada funcionalidade está no VidaPlus

| Func. | Tela | Serviço do front | Endpoint | Backend | Banco / arquivos |
|---|---|---|---|---|---|
| F-02 (ver exame) | `pages/Dashboard.jsx` ("Meus exames") → `pages/ExameDetalhe.jsx` (`/exames/:id`) | `services/exame.js` → `getExame`, `getPdf`, `listarExamesDoPaciente` | `GET /api/exames/{id}`, `GET /api/exames/{id}/pdf`, `GET /api/exames/paciente/{pacienteId}` | `ExameController.getExame`, `getPdf`, `porPaciente` → `ExameDao.porId`, `doPaciente` (e `ExameDao.medicoAtende`, que existe e **ninguém chama**) | tabelas `exame`, `atendimento`, `usuario` |
| F-07 (publicar) | botão **Publicar laudo** em `ExameDetalhe.jsx`; `pages/FilaLaudos.jsx` (Bioquímico) | `publicar`, `listarPendentes` | `POST /api/exames/{id}/publicar`, `GET /api/exames/pendentes` | `ExameController.publicar`, `pendentes` → `ExameDao.publicar`, `pendentes` | colunas `exame.publicado`, `publicado_por`, `hash_laudo` |
| F-08 (PDFs) | botão **Baixar PDF** em `ExameDetalhe.jsx` | — (link direto) | **não passa pela API**: `http://localhost:4200/laudos/laudo-{id}.pdf` | nenhum: quem entrega é o **nginx** do frontend | arquivos em `frontend/public/laudos/`; coluna `exame.laudo_pdf` |
| F-09 (exportar) | **não existe** | — | — | modelo pronto, sem uso: `entity/SolicitacaoTitular.java`, `repository/SolicitacaoTitularRepository.java` | os dados do titular estão em `paciente`, `usuario`, `exame`, `nota_clinica`, `atendimento` |

(Front em `legacy-app/frontend/src/`; backend em `legacy-app/backend/src/main/java/br/ifsul/vidaplus/`.)

## 4. Mapa de ameaças e leis que mais aparecem no seu pacote

Do [`docs/04`](../../docs/04-catalogo-funcionalidades.md):

| Func. | STRIDE dominante | LINDDUN dominante | LGPD |
|---|---|---|---|
| F-02 Laudo (IDOR) | Divulgação de informação / Elevação de privilégio | Divulgação + Vinculação (`L.1.1` — número sequencial do exame) | arts. 11, 46 |
| F-09 Portabilidade | — | Incapacidade de intervir (o titular não consegue exercer o direito) | art. 18 |

## 5. Os roteiros, na ordem

| Fase | Roteiro | Seção do dossiê |
|---|---|---|
| 0 | [`fase-0-inventario.md`](fase-0-inventario.md) | §1 |
| 1 | [`fase-1-requisitos.md`](fase-1-requisitos.md) | §2 |
| 2a | [`fase-2a-stride.md`](fase-2a-stride.md) | §3 |
| 2b | [`fase-2b-privacidade.md`](fase-2b-privacidade.md) | §4 |
| 3 | [`fase-3-priorizacao.md`](fase-3-priorizacao.md) | §5 |
| 4 | [`fase-4-remediacao.md`](fase-4-remediacao.md) | §6 |
| 5 | [`fase-5-verificacao.md`](fase-5-verificacao.md) | §7 |
| 6 | [`fase-6-pipeline.md`](fase-6-pipeline.md) | §8 |
| 7 | [`fase-7-operacao.md`](fase-7-operacao.md) | §9 |
| 8 | [`fase-8-rastreabilidade.md`](fase-8-rastreabilidade.md) | §10–11 |

## 6. Combinem os papéis

A cada fase, um integrante **lidera** (★) e os outros apoiam — quadro em
[`docs/00`](../../docs/00-visao-geral.md#papéis-na-equipe-responsabilidade-individual-definida-por-fase).
Registrem na §0 do dossiê. No G2, o 📋 DPO tem papel forte na F-09 (direito do titular) e o
💻 Dev, na Fase 4 (a regra de quem pode ver o quê).
