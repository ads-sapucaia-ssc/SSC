# G3 · Fase 2a — DFD, STRIDE e árvore de ataque

> **Seção do dossiê:** §3 · **Lidera:** 🛡️ Segurança · **Aula:** 3 · **Semana:** 4
> Junto com a 2b, é **a fase de maior peso** na avaliação.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G3**.

## O que você entrega no fim

1. Um **DFD** do recorte do G3, com fronteiras de confiança.
2. A tabela **STRIDE por elemento**.
3. Uma **árvore de ataque** do objetivo mais grave do pacote.

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório.
   **Todos os comandos deste roteiro são digitados nesta pasta.**
2. **Entre na branch da fase.** Quem **lidera** a fase cria a branch e a envia ao GitHub:

   ```bash
   git checkout main
   git pull
   git checkout -b dossie/fase-2a
   git push -u origin dossie/fase-2a
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-2a
   ```

   Antes de começar a editar, a cada aula: `git pull` (traz o que os colegas já enviaram).
3. **Ligue o VidaPlus com o banco original e carregue os tokens** dos usuários de teste:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   source roteiros/comum/tokens.sh
   mkdir -p entregaveis/evidencias
   ```

   O primeiro termina com `PRONTO: VidaPlus no ar...` (a primeira vez demora alguns minutos); o
   segundo mostra seis linhas `TOKEN_... ok`. O terceiro cria a pasta das evidências, se ela ainda não existir. Os tokens valem **só neste terminal**: se abrir
   outro, rode o `source` de novo.

---

## Conceitos em 5 minutos

**DFD** — desenho de por onde os dados passam ([`comum/05` §3](../comum/05-escrever-o-dossie.md#3-desenhar-o-dfd-fase-2a)):

| Elemento | O que é | No G3, por exemplo |
|---|---|---|
| **Entidade externa** | quem está fora e usa o sistema | os cinco papéis |
| **Processo** | algo que o sistema faz | "Gerir usuários", "Publicar laudo" |
| **Depósito** | onde o dado fica | tabela `usuario`; tabela `atendimento` |
| **Fluxo** | o dado que passa | "token (com o papel)"; "id do usuário a desativar" |

**Fronteira de confiança:** navegador (não confiável) | servidor | banco.

**STRIDE** — seis perguntas para cada elemento:

| Letra | Ameaça | Pergunta | Atributo violado |
|---|---|---|---|
| **S** | falsificação de identidade | alguém consegue **se passar** por outro? | Autenticidade |
| **T** | adulteração | alguém consegue **alterar** o dado? | Integridade |
| **R** | repúdio | alguém consegue **negar** o que fez? | Não-repúdio |
| **I** | divulgação | alguém consegue **ler** o que não devia? | Confidencialidade |
| **D** | negação de serviço | alguém consegue **impedir** o uso? | Disponibilidade |
| **E** | elevação de privilégio | alguém consegue fazer **mais do que pode**? | Autorização |

| Tipo de elemento | Letras analisadas |
|---|---|
| Entidade externa | S, R |
| Processo | S, T, R, I, D, E |
| Depósito | T, R, I, D |
| Fluxo | T, I, D |

**A letra do G3 é o E.** Mas não pare nele: quando um paciente consegue usar uma função de admin,
o que ele **faz** com ela aparece nas outras letras (desativar contas = D; publicar laudo = T;
ver a lista de usuários = I).

**Árvore de ataque** — objetivo no topo; caminhos em **OR** / **AND**; custo e requisito em cada
folha.

---

## Passo 1 — Liste os elementos do recorte

> 🔎 **Como o Grupo 5 fez:** cada método de backend virou um **processo**; cada tabela, um
> **depósito**; cada papel, uma **entidade**. Na Fase 4, achou um ponto novo e **voltou** para
> acrescentar um processo — o threat model é vivo.

**▶ Agora no G3:** a partir das trilhas da Fase 0:

- **processos:** um por **função restrita** — gerir usuários (listar e desativar), agenda (listar
  e agendar), carteira do médico, fila de laudos, publicar laudo, editar cadastro do paciente;
- **um processo especial: "decidir o que a pessoa pode fazer"** — hoje ele **não existe** (o
  `papelDoToken` ninguém chama). Desenhe-o como **processo planejado**, entre a entrada da API e
  as funções. É ali que a correção do P0 vai morar;
- **a verificação de consentimento** (F-04): processo **planejado**, com o seu depósito
  (`consentimento`, "a criar");
- **depósitos:** `usuario` (com o papel), `atendimento`, `exame`, `paciente`, `localStorage`
  (o token, **com o papel dentro**);
- **entidades:** os cinco papéis.

O DFD vai ficar grande. Tudo bem: agrupe as funções de um mesmo papel num processo só, se ficar
mais claro (ex.: "2.0 Funções da recepção").

---

## Passo 2 — Desenhe o DFD

> 🔎 **Como o Grupo 5 fez:** três zonas, fronteiras tracejadas e uma nota com o requisito perto de
> cada processo: [`docs/img/dfd-exemplo-pacote5.svg`](../../docs/img/dfd-exemplo-pacote5.svg).

**▶ Agora no G3:** desenhe ([`comum/05` §3](../comum/05-escrever-o-dossie.md#3-desenhar-o-dfd-fase-2a)).
Deixe visível o fluxo **"token (com o papel)"** saindo do `localStorage` (zona do navegador) e
entrando na API: o papel **atravessa a fronteira de confiança vindo do lado não confiável**.
Guarde essa observação para o Passo 3.

---

## Passo 3 — Faça o STRIDE de cada elemento, testando

> 🔎 **Como o Grupo 5 fez:** para cada letra, **tentou** o ataque antes de escrever. Exemplos:
>
> - **S/E no 1.0 Autenticar:** o login com `admin@vidaplus.local' -- ` devolveu token de ADMIN;
> - **T/D no D2:** achava que a injeção "apagava o banco"; **testou** `'; UPDATE ...`, recebeu erro e
>   **corrigiu a tabela**;
> - **3.0:** testou `/api/pacientes/1abc/notas` → **400**: proteção acidental.

**▶ Agora no G3:** uma linha por elemento. Experimentos (no terminal, na raiz do repositório). Os tokens dos usuários (`TOKEN_ANA`, `TOKEN_HOUSE`, `TOKEN_REC`, `TOKEN_BIO`, `TOKEN_ADMIN`) vêm do `source roteiros/comum/tokens.sh`, que já está no começo de cada bloco abaixo. **Estes comandos mudam o banco: zere ao final** (rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`)
([`comum/01` §5](../comum/01-subir-o-vidaplus.md#7-zerar-o-banco--e-por-que-isso-importa)).

**Gerir usuários**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s -X POST localhost:8080/api/usuarios/3/alternar-ativo -H "Authorization: Bearer $TOKEN_ANA"; echo
curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"recepcao@vidaplus.local","senha":"Recep123"}'; echo
```

A Ana (paciente) desativou a conta da recepção. A recepção consegue entrar? Que letras são essas
(E por fazer; **D** pelo efeito)? E se ela fizer isso com **todas** as contas — inclusive a do
admin (`alternar-ativo` no usuário 1)? Alguém registrou **quem** desativou (R)?

**Agenda**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s localhost:8080/api/atendimentos -H "Authorization: Bearer $TOKEN_ANA" | python3 -m json.tool | head -20
curl -s -X POST localhost:8080/api/atendimentos -H "Authorization: Bearer $TOKEN_ANA" \
  -H "Content-Type: application/json" -d '{"medicoId":1,"pacienteId":2}'; echo
```

O primeiro: a Ana vê **quem se trata com qual médico** — que letra, e que tipo de dado é esse? O
segundo: a Ana **cria** um atendimento entre a médica e o Bruno (T). Pense no efeito: em vários
sistemas, "o médico atende o paciente" é o que **libera** o acesso aos exames. Adulterar a agenda
pode virar **acesso indevido** em outro lugar. (É exatamente o que o G2 descobre na Fase 5 dele.)

**Carteira do médico e fila de laudos**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s localhost:8080/api/atendimentos/medico/1 -H "Authorization: Bearer $TOKEN_ANA"; echo
curl -s localhost:8080/api/exames/pendentes -H "Authorization: Bearer $TOKEN_ANA"; echo
```

Que dado de saúde de outras pessoas aparece (I)? Na fila, que tipo de exame está pendente, e de
quem?

**Publicar laudo** (o P0)

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s -X POST localhost:8080/api/exames/104/publicar -H "Authorization: Bearer $TOKEN_ANA"; echo
```

E (o que o teste-semente prova), T (o status do laudo mudou sem conferência do laboratório).

**Editar cadastro**

A atribuição em massa da Fase 0 (Passo 6): o servidor aceitou um campo `papel`. Hoje ele não
grava nada — mas **aceitar** sem recusar é o padrão perigoso. Que letra seria, se um dia o método
passasse a gravar?

**O fluxo "token (com o papel)"**

O papel vem **dentro** do token, do lado não confiável. O token é **assinado** (não dá para mudar
um caractere sem a chave) — mas a chave está no código (`JwtUtil.java`). Em palavras: quem tem o
código pode **escrever o papel que quiser**. Que letras, em que elemento? (Na Fase 5 vocês vão ver
isso funcionando.) E o token, depois de emitido, continua dizendo o papel antigo mesmo se o admin
mudar o papel no banco — por quanto tempo?

**Depósito `usuario`:** quem consegue mudar a coluna `ativo`? E a coluna `papel` (há algum
endpoint que a mude)?

**Entidades:** S e R de cada papel.

**Consentimento (planejado):** como ainda não existe, analise o **projeto**: quem poderia alterar
o registro de um consentimento (T)? E se a clínica disser que o paciente consentiu e ele negar
(R — e de quem é o ônus da prova, art. 8º, §2º)?

Ao terminar: **zere o banco** (rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`).

---

## Passo 4 — Ligue cada ameaça a uma resposta

> 🔎 **Como o Grupo 5 fez:** cada ameaça com o requisito do pacote, ou **"outro pacote (…) —
> transferida"**.

**▶ Agora no G3:** cada letra marcada → requisito do G3 (crie, se faltar, e volte à §2) ou
"outro pacote — transferida". Exemplos de transferência: o registro de **quem** desativou (F-05,
G1); a chave do token no código (F-10 — de todos; corrigida na Fase 6); o token que não expira (F-11,
G1); o paciente vendo exame de outro **paciente** (F-02, G2/G4).

---

## Passo 5 — Monte a árvore de ataque

> 🔎 **Como o Grupo 5 fez:** a primeira árvore começava pela falha famosa (injeção); só depois o
> grupo achou o caminho **mais barato** — usar a busca sem login — e o pôs como ramo 1. Veja a
> árvore e a 💡: [`docs/12` §3](../../docs/12-exemplo-dossie-preenchido.md#3-fase-2a--threat-modeling-stride).

**▶ Agora no G3:** objetivo sugerido: **"fazer uma ação de administrador (ex.: desativar todas as
contas) sem ser administrador"**. Ramos para pensar, **começando pelo mais barato**: chamar a
função direto; escrever o próprio papel no token; reaproveitar o token de um admin; entrar como
admin pelo login (G1). Em cada folha: custo, se precisa de conta e o requisito que bloqueia.

```
OBJETIVO: <...>                                              (OR)
├── 1. <caminho mais barato>       custo <...>               → REQ-...
├── 2. <caminho composto>                                    (AND)
│      ├── <parte a>               custo <...>
│      └── <parte b>               custo <...>               → REQ-... / outro pacote
└── 3. <...>
```

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-2a`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 2a - stride"
git push -u origin dossie/fase-2a
```

- `git status` lista o que mudou: confira que só aparecem os arquivos que você quer gravar;
- `git add` escolhe o que entra; `git commit` grava; `git push` envia ao GitHub;
- se o `push` pedir usuário e senha: o seu usuário do GitHub e, na senha, o **token** do GitHub
  ([`comum/00` §2.1](../comum/00-antes-de-comecar.md#21-crie-um-token-de-acesso-a-senha-do-git));
- se o `push` for recusado (`rejected`), um colega enviou algo antes: rode `git pull` e repita o
  `git push`.

Depois, no navegador:

1. Abra o repositório do grupo no GitHub e clique em **Compare & pull request** (faixa amarela).
   Se a faixa não aparecer: **Pull requests** → **New pull request** → em **base** deixe `main`, em
   **compare** escolha `dossie/fase-2a` → **Create pull request**.
2. Título: "Dossiê — Fase 2a: STRIDE". Na descrição: o que foi feito e quem participou. Clique em
   **Create pull request**.
3. **Outro integrante** (não quem abriu) abre o PR → aba **Files changed** → lê → **Review
   changes** → **Approve** → **Submit review**.
4. Com a aprovação: **Merge pull request** → **Confirm merge** → **Delete branch**.
5. No terminal de cada integrante:

   ```bash
   git checkout main
   git pull
   ```

Conflito ou erro? Veja [`comum/03`](../comum/03-git-e-github.md) §4.2 e §5.

---

## Confira antes de seguir

- [ ] O DFD tem o processo planejado "decidir o que a pessoa pode fazer" e o fluxo do token com o
      papel atravessando a fronteira.
- [ ] Testei funções de **pelo menos quatro** papéis com o token da paciente.
- [ ] Escrevi o **efeito** de cada E nas outras letras (D, T, I).
- [ ] A árvore começa pelo caminho mais barato e tem pelo menos um ramo AND.
- [ ] Zerei o banco.

## Erros comuns

- **Parar no E.** "Paciente acessa função de admin" é o começo; o dossiê precisa dizer **o que**
  ela faz com isso.
- **Esquecer o uso legítimo.** Para cada função, quem **deve** poder usar? (A Fase 4 precisa
  dessa lista.)
