# G3 · Papéis & Consentimento (DPO) — apresentação do pacote

Este é o ponto de partida do **Grupo 3**. Leia inteiro antes da Fase 0: ele explica **o que é o
seu pacote**, **onde cada funcionalidade está no VidaPlus** e **o que vocês entregam**.

> Ainda não preparou o ambiente? Faça antes
> [`comum/00`](../comum/00-antes-de-comecar.md), [`01`](../comum/01-subir-o-vidaplus.md) e
> [`02`](../comum/02-observar-o-sistema.md).

---

## 1. O tema do pacote, em linguagem simples

O seu pacote responde a duas perguntas que vêm **antes** de qualquer acesso a dado:

- **Quem pode fazer isto?** Cada pessoa tem um **papel** (paciente, médico, recepção, laboratório,
  administrador), e cada papel só pode fazer o que a função dele exige. Hoje, o VidaPlus
  **esconde** os menus de cada papel na tela — mas o servidor **não confere** nada: um paciente
  consegue publicar laudos e desativar contas.
- **Sob qual base legal?** Para algumas finalidades, a clínica só pode usar o dado se o paciente
  **consentir** — e ele pode **retirar** o consentimento depois. Hoje, isso não existe.

Junto, duas funcionalidades de privacidade: **coletar só o necessário** (minimização) e deixar o
paciente **ver e corrigir os próprios dados** (autosserviço do titular).

## 2. As funcionalidades do pacote

| Func. | O que é | Criticidade | Ação | Nível de entrega |
|---|---|:-:|:-:|---|
| **F-03** Controle de acesso por papel (RBAC) | o servidor nega, por padrão, tudo o que o papel de quem pede não permite (*deny-by-default*) | 🔴 P0 | 🛠️ corrigir | **implementada** na aula guiada (o teste já vem pronto) |
| **F-04** Gestão de consentimento | registrar, por finalidade e por versão do termo, o consentimento do paciente — e a revogação | 🔴 P0 | ✨⚖️ criar | **especificada** (não implementada) |
| **F-15** Minimização no cadastro | o cadastro coleta dados sem finalidade | 🟠 P1 | ⚖️ adequar | analisada + plano |
| **F-19** Perfil do usuário (autosserviço do titular) | o paciente ver e corrigir os próprios dados; hoje, a edição de cadastro não valida nada | 🟢 P2 | 🛠️ corrigir | analisada + plano |
| **F-10** Segredos no código | higiene contínua (todos os grupos) | 🟠 P1 | 🛠️ corrigir | **implementada** na aula guiada da Fase 6 ([`comum/08`](../comum/08-segredos-fora-do-codigo.md)) |
| **F-21** Dependências (SCA/SBOM) | higiene contínua (todos os grupos) | 🟢 P2 | — | leitura do pipeline (Fase 6) |

**Teste pronto do P0:** `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/rbac.feature`
— cenário "paciente não pode publicar laudo" (requisito `REQ-AC-RBAC-01`). Filtro:
`"security & F-03"`. Hoje ele **falha** de propósito.

**Escopo mínimo da criação (F-04),** segundo o [`docs/05`](../../docs/05-pacotes-trabalho-estudantes.md):
**1 finalidade**, **1 versão** do termo e **revogação**, aplicada em **1 fluxo**. Vocês
**especificam**; não programam.

## 3. Onde cada funcionalidade está no VidaPlus

| Func. | Tela | Serviço do front | Endpoint | Backend | Banco |
|---|---|---|---|---|---|
| F-03 (menus) | `components/Layout.jsx` — a lista `NAV_POR_PAPEL` decide **que menu aparece** | — | — | — | — |
| F-03 (gestão de usuários, ADMIN) | `pages/Usuarios.jsx` (`/usuarios`) | `services/usuario.js` | `GET /api/usuarios`, `POST /api/usuarios/{id}/alternar-ativo` | `UsuarioController` → `UsuarioDao.listarTodos`, `alternarAtivo` | `usuario` |
| F-03 (agenda, RECEPÇÃO) | `pages/Agenda.jsx` (`/agenda`) | `services/atendimento.js` | `GET` e `POST /api/atendimentos` | `AtendimentoController.listar`, `agendar` | `atendimento` |
| F-03 (meus pacientes, MÉDICO) | `pages/MeusPacientes.jsx` | `services/atendimento.js` | `GET /api/atendimentos/medico/{medicoId}` | `AtendimentoController.pacientesDoMedico` | `atendimento`, `paciente` |
| F-03 (laudos, LABORATÓRIO) | `pages/FilaLaudos.jsx`; botão **Publicar laudo** em `ExameDetalhe.jsx` | `services/exame.js` | `GET /api/exames/pendentes`, `POST /api/exames/{id}/publicar` | `ExameController.pendentes`, `publicar` | `exame` |
| F-03 (o papel) | — | — | — | `security/JwtUtil.java` → `papelDoToken` (existe; repare **quem** o chama) | `usuario.papel` |
| F-15 | `pages/BuscaPacientes.jsx` (colunas Religião, Renda) | `services/paciente.js` | `GET /api/pacientes/busca` | `PacienteDao.buscar` | colunas `religiao`, `estado_civil`, `renda_mensal` da tabela `paciente` (`db/schema.sql`, `db/seed.sql`) |
| F-19 | botão **Editar cadastro** em `pages/PacienteFicha.jsx` | `services/paciente.js` → `editar` | `PUT /api/pacientes/{id}` | `PacienteController.editar` | `paciente` |
| F-04 | **não existe** | — | — | modelo pronto, sem uso: `entity/Consentimento.java`, `repository/ConsentimentoRepository.java` | **tabela não existe**; modelo comentado no fim de `db/schema.sql` |

(Front em `legacy-app/frontend/src/`; backend em `legacy-app/backend/src/main/java/br/ifsul/vidaplus/`.)

## 4. Mapa de ameaças e leis que mais aparecem no seu pacote

Do [`docs/04`](../../docs/04-catalogo-funcionalidades.md):

| Func. | STRIDE dominante | LINDDUN dominante | LGPD |
|---|---|---|---|
| F-03 RBAC | Elevação de privilégio | Não-conformidade (`Nc.3` — sem controle de acesso interno) | art. 46 |
| F-04 Consentimento | — | Incapacidade de intervir (`U.2.1` — sem mecanismo de consentimento) | arts. 8, 9 |
| F-15 Cadastro | — | Divulgação (`DD.2.1`, volume) + Desconhecimento + Não-conformidade | art. 6º, III (minimização) |
| F-19 Perfil | — | Incapacidade de intervir (`U.2.2`/`U.2.3` — não consegue ver nem corrigir) | art. 18 |

Repare: **três das quatro** funcionalidades são de **privacidade**, não de ataque. O 📋 DPO tem
trabalho em todas as fases.

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
Registrem na §0 do dossiê.
