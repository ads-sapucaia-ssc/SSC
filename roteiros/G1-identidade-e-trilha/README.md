# G1 · Identidade & Trilha de Acesso — apresentação do pacote

Este é o ponto de partida do **Grupo 1**. Leia inteiro antes da Fase 0: ele explica **o que é o
seu pacote**, **onde cada funcionalidade está no VidaPlus** e **o que vocês entregam**.

> Ainda não preparou o ambiente? Faça antes
> [`comum/00`](../comum/00-antes-de-comecar.md), [`01`](../comum/01-subir-o-vidaplus.md) e
> [`02`](../comum/02-observar-o-sistema.md).

---

## 1. O tema do pacote, em linguagem simples

O seu pacote cuida da **porta de entrada** do VidaPlus e do **registro de quem entrou e o que
fez**:

- **Identidade:** como o sistema sabe **quem** é a pessoa (login, senha, recuperação de senha) e
  como ele **lembra** disso entre um clique e outro (a sessão, o token).
- **Trilha de acesso:** como o sistema **registra** quem fez o quê, de forma que ninguém consiga
  apagar ou alterar esse registro depois — e sem expor dados pessoais nele.

Se a porta de entrada falha, **todo o resto falha junto**: quem entra como outra pessoa vê tudo o
que ela vê.

## 2. As funcionalidades do pacote

| Func. | O que é | Criticidade | Ação | Nível de entrega |
|---|---|:-:|:-:|---|
| **F-01** Autenticação (login) | entrar com e-mail e senha. Recorte do G1: o **fluxo de autenticação** — como a senha é guardada, tentativas repetidas, o que a resposta revela | 🔴 P0 | 🛠️ corrigir | **implementada** na aula guiada (o teste já vem pronto) |
| **F-05** Trilha de auditoria | registro, à prova de adulteração, dos eventos de autenticação e acesso | 🔴 P0 | ✨ criar | **especificada** (não implementada) |
| **F-11** Sessão / token | o "crachá" digital que a pessoa recebe ao entrar | 🟠 P1 | 🛠️ corrigir | analisada + plano |
| **F-12** Recuperação de senha | o "esqueci minha senha" | 🟠 P1 | 🛠️ corrigir | analisada + plano |
| **F-17** CORS / cabeçalhos de segurança | regras que o servidor manda ao navegador sobre quem pode chamá-lo e o que pode executar | 🟡 P1 | 🛠️ corrigir | analisada + plano |
| **F-10** Segredos no código | higiene contínua (todos os grupos) | 🟠 P1 | — | analisada + plano (Fase 6) |
| **F-21** Dependências (SCA/SBOM) | higiene contínua (todos os grupos) | 🟢 P2 | — | leitura do pipeline (Fase 6) |

**Teste pronto do P0:** `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/sqli.feature`
— cenário "payload de injeção no login não autentica" (requisito `REQ-AUTH-SQLI-01`). Filtro para
rodar: `"security & F-01"`. Hoje ele **falha** de propósito.

**Escopo mínimo da criação (F-05),** segundo o [`docs/05`](../../docs/05-pacotes-trabalho-estudantes.md):
**1 ação sensível** instrumentada + **verificação de integridade** (append-only) daquele registro.
Vocês **especificam** isso; não programam.

## 3. Onde cada funcionalidade está no VidaPlus

A "trilha" de cada uma, da tela até o banco (como em [`comum/02`](../comum/02-observar-o-sistema.md) §4.3):

| Func. | Tela | Serviço do front | Endpoint da API | Backend | Banco / outros |
|---|---|---|---|---|---|
| F-01 | `pages/Login.jsx` | `services/auth.js` → `login()` | `POST /api/auth/login` | `AuthController.login` → `UsuarioDao.autenticar`; senha via `config/SenhaConfig.java` e `security/LegacyHash.java` | tabela `usuario` (e `paciente`, `medico` para o nome) |
| F-11 | toda tela depois do login; botão **Sair** em `components/Layout.jsx` | `services/auth.js` → `salvarSessao()`, `getToken()`, `logout()` | todos os que recebem `Authorization: Bearer ...` | `security/JwtUtil.java` (`gerarToken`, `usuarioIdDoToken`, `papelDoToken`) | `localStorage` do navegador |
| F-12 | botão **Esqueci minha senha** em `pages/Login.jsx` | `services/auth.js` → `solicitarReset()` | `POST /api/auth/reset` | `AuthController.reset` → `UsuarioDao.porEmail` | tabela `usuario` (coluna `reset_token`) |
| F-17 | — (é configuração) | — | todas as respostas | `config/WebConfig.java` (CORS) | `frontend/nginx.conf` (cabeçalhos do site) |
| F-05 | **não existe** | — | — | modelo pronto, sem uso: `entity/Auditoria.java`, `repository/AuditoriaRepository.java` | **tabela não existe**; modelo comentado no fim de `db/schema.sql` |

(Os caminhos de front começam em `legacy-app/frontend/src/`; os de backend, em
`legacy-app/backend/src/main/java/br/ifsul/vidaplus/`.)

## 4. Mapa de ameaças e leis que mais aparecem no seu pacote

Do [`docs/04`](../../docs/04-catalogo-funcionalidades.md):

| Func. | STRIDE dominante | LINDDUN dominante | LGPD |
|---|---|---|---|
| F-01 Login | Falsificação de identidade (*Spoofing*) | Identificabilidade (`I.1.1` — e-mail como identificador único) | art. 46 |
| F-05 Auditoria | Repúdio | Não-repúdio (`Nr.2`) — o registro pode virar ameaça à privacidade de quem é registrado | arts. 37, 46 |

Os demais (F-11, F-12, F-17) vocês descobrem nas Fases 2a e 2b.

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

A cada fase, um integrante **lidera** (★) e os outros apoiam. Quem lidera o quê está no quadro do
[`docs/00`](../../docs/00-visao-geral.md#papéis-na-equipe-responsabilidade-individual-definida-por-fase).
Registrem na §0 do dossiê. No G1, a Fase 4 costuma ser liderada por quem quer mexer no código, e
a Fase 7 pesa mais que nos outros grupos, porque a trilha de auditoria é de vocês.
