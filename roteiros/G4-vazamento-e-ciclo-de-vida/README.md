# G4 · Vazamento & Ciclo de Vida do Dado — apresentação do pacote

Este é o ponto de partida do **Grupo 4**. Leia inteiro antes da Fase 0: ele explica **o que é o
seu pacote**, **onde cada funcionalidade está no VidaPlus** e **o que vocês entregam**.

> Ainda não preparou o ambiente? Faça antes
> [`comum/00`](../comum/00-antes-de-comecar.md), [`01`](../comum/01-subir-o-vidaplus.md) e
> [`02`](../comum/02-observar-o-sistema.md).

---

## 1. O tema do pacote, em linguagem simples

O seu pacote olha o dado de dois jeitos:

- **Por onde ele escapa.** Um laudo pode vazar pelo caminho óbvio (alguém pede o exame de outra
  pessoa), mas também por caminhos que ninguém vigia: o **arquivo** direto na pasta do site, uma
  **mensagem de erro** que conta demais, um **link** mandado por e-mail, o próprio **site** da
  clínica adulterado. Cada caminho é um **canal de vazamento**.
- **Quanto tempo ele vive.** Hoje, nenhum dado do VidaPlus é apagado — nem de quem deixou a
  clínica há décadas. A lei diz que o dado deve ser **eliminado** quando acaba a finalidade, mas
  também obriga a **guardar** o prontuário por um prazo. Equilibrar as duas coisas é o **ciclo de
  vida**.

## 2. As funcionalidades do pacote

| Func. | O que é | Criticidade | Ação | Nível de entrega |
|---|---|:-:|:-:|---|
| **F-02** Visualização/download de laudo | abrir um exame e baixar o laudo. Recorte do G4: **canal de vazamento** — por onde o laudo escapa (a API, o arquivo direto, a mensagem de erro) | 🔴 P0 | 🛠️ corrigir | **implementada** na aula guiada (o teste já vem pronto) |
| **F-06** Eliminação / anonimização | o que fazer com o dado no fim da vida dele. Recorte do G4: **retenção → eliminação** — o prazo legal e o que se apaga, o que se guarda e por quê | 🔴 P0 | ✨⚖️ criar | **especificada** (não implementada) |
| **F-16** Tratamento de erros | o que o sistema mostra quando algo dá errado | 🟡 P2 | 🛠️ corrigir | analisada + plano (parte entra no PR do P0 — veja a Fase 4) |
| **F-18** Notificação (e-mail/SMS) com link | avisar o paciente de que o laudo está pronto | 🟠 P1 | 🛠️⚖️ corrigir/adequar | analisada + plano — **não há código no legado**: vocês analisam o desenho |
| **F-20** Conteúdo institucional (site) | as páginas públicas da clínica | 🟢 P2 | 🛠️ corrigir | analisada + plano — **não há código específico**: vocês analisam o servidor do site |
| **F-10** Segredos no código | higiene contínua (todos os grupos) | 🟠 P1 | 🛠️ corrigir | **implementada** na aula guiada da Fase 6 ([`comum/08`](../comum/08-segredos-fora-do-codigo.md)) |
| **F-21** Dependências (SCA/SBOM) | higiene contínua (todos os grupos) | 🟢 P2 | — | leitura do pipeline (Fase 6) |

**Teste pronto do P0:** `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/idor.feature`
— cenário "paciente não acessa o laudo de outro paciente" (`REQ-AC-IDOR-01`). Filtro:
`"security & F-02"`. Hoje ele **falha** de propósito.

> **O G2 tem o mesmo P0 (F-02), com outro recorte** (quem é o dono). Cada grupo trabalha no
> próprio repositório; comparar as duas soluções na defesa é parte do valor.

> **O dossiê de exemplo também tem uma F-06** — com outro recorte: o Grupo 5 trabalhou **como
> transformar** o dado (a técnica de anonimização). Vocês trabalham **quando** e **o quê**: o
> prazo, o que a lei obriga a guardar, o que se elimina, o pedido do titular. Usem o exemplo como
> referência de nível, **não** como resposta.

**Escopo mínimo da criação (F-06),** segundo o [`docs/05`](../../docs/05-pacotes-trabalho-estudantes.md):
**1 entidade**, com regra de **retenção → anonimização/eliminação**, **respeitando a retenção
legal**. Vocês **especificam**; não programam.

## 3. Onde cada funcionalidade está no VidaPlus

| Func. | Tela / onde aparece | Endpoint / recurso | Backend / configuração | Banco / arquivos |
|---|---|---|---|---|
| F-02 (API) | `pages/ExameDetalhe.jsx` (`/exames/:id`); "Meus exames" em `Dashboard.jsx` | `GET /api/exames/{id}`, `/{id}/pdf`, `/paciente/{pacienteId}` | `ExameController.getExame`, `getPdf`, `porPaciente` | `exame` |
| F-02 (arquivo) | botão **Baixar PDF** | `http://localhost:4200/laudos/laudo-{id}.pdf` — **não passa pela API** | o **nginx** do frontend (`frontend/nginx.conf`) | `frontend/public/laudos/` |
| F-16 | qualquer erro | qualquer endpoint | `backend/src/main/resources/application.properties` (linhas `server.error.*`); os `throw new RuntimeException(...)` dos controllers e DAOs | — |
| F-18 | **não existe** | — | — | o link que existiria é o do botão **Baixar PDF** |
| F-20 | o site é a própria aplicação React | `http://localhost:4200/` | `frontend/nginx.conf`, `frontend/index.html`, pasta `frontend/public/` | — |
| F-06 | **não existe** | — | modelo pronto, sem uso: `entity/SolicitacaoTitular.java` (tipo `ELIMINACAO`) | coluna `ativo` (exclusão só "esconde"); comentário `V-16` em `db/schema.sql` |

(Front em `legacy-app/frontend/src/`; backend em `legacy-app/backend/src/main/java/br/ifsul/vidaplus/`.)

## 4. Mapa de ameaças e leis que mais aparecem no seu pacote

Do [`docs/04`](../../docs/04-catalogo-funcionalidades.md):

| Func. | STRIDE dominante | LINDDUN dominante | LGPD |
|---|---|---|---|
| F-02 Laudo | Divulgação de informação / Elevação de privilégio | Divulgação + Vinculação (`L.1.1` — número sequencial) | arts. 11, 46 |
| F-06 Eliminação | — | Não-conformidade (`Nc.1.1.4` — retenção além do necessário) | arts. 15, 16, 18 |

E, das "candidatas a versão futura" do mesmo documento, duas são do seu tema: o **oráculo de
existência** (`D.3`) e o **conteúdo da notificação** (`DD.4.2`).

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

A cada fase, um integrante **lidera** (★) — quadro em
[`docs/00`](../../docs/00-visao-geral.md#papéis-na-equipe-responsabilidade-individual-definida-por-fase).
Registrem na §0. No G4, o 📋 DPO tem papel forte na F-06 (prazos legais) e na F-18.
