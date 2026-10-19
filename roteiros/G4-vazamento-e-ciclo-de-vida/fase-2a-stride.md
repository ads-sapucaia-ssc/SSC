# G4 · Fase 2a — DFD, STRIDE e árvore de ataque

> **Seção do dossiê:** §3 · **Lidera:** 🛡️ Segurança · **Aula:** 3 · **Semana:** 4
> Junto com a 2b, é **a fase de maior peso** na avaliação.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G4**.

## O que você entrega no fim

1. Um **DFD** do recorte do G4, com fronteiras de confiança — **com todos os canais**.
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

| Elemento | O que é | No G4, por exemplo |
|---|---|---|
| **Entidade externa** | quem está fora e usa (ou recebe dados do) sistema | Paciente; **provedor de e-mail/SMS**; visitante do site |
| **Processo** | algo que o sistema faz | "Obter exame"; "Servir arquivo"; "Enviar notificação" |
| **Depósito** | onde o dado fica | tabela `exame`; a pasta de PDFs |
| **Fluxo** | o dado que passa | "laudo em PDF"; **"mensagem de erro"**; "texto da notificação" |

Repare: no G4, uma **mensagem de erro** é um **fluxo de dados** — ela sai do servidor e chega ao
navegador do atacante. E um **provedor de e-mail** é uma **entidade externa** que recebe dados da
clínica.

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

**A letra do G4 é o I** — mas cada canal tem as suas outras letras (o site adulterado é T; a
notificação falsa é S).

---

## Passo 1 — Liste os elementos do recorte

> 🔎 **Como o Grupo 5 fez:** cada método de backend virou um **processo**; cada tabela, um
> **depósito**; cada papel, uma **entidade**. Na Fase 4, achou um ponto novo e **voltou** para
> acrescentar um processo — o threat model é vivo.

**▶ Agora no G4:** o recorte cobre **os canais** e **o fim de vida**:

- **API:** "Obter exame" (`getExame`, `getPdf`, `porPaciente`);
- **arquivo:** "Servir arquivo" — o **nginx**, processo separado da API, com o depósito "pasta
  pública de laudos";
- **erro:** o fluxo "mensagem de erro" saindo da API (e do nginx) para o navegador;
- **notificação (planejada):** processo "Enviar notificação", a entidade externa "Provedor de
  e-mail/SMS" e o fluxo "texto + link" até o paciente;
- **site:** o nginx servindo `index.html` e a pasta `public/` (depósito "conteúdo do site") — e
  quem **altera** esse conteúdo (o pipeline, a partir do repositório);
- **fim de vida (planejado):** processo "Rotina de retenção", lendo e alterando as tabelas de
  paciente e os arquivos.

---

## Passo 2 — Desenhe o DFD

> 🔎 **Como o Grupo 5 fez:** três zonas, fronteiras tracejadas, e uma nota com o requisito perto de
> cada processo: [`docs/img/dfd-exemplo-pacote5.svg`](../../docs/img/dfd-exemplo-pacote5.svg).

**▶ Agora no G4:** desenhe ([`comum/05` §3](../comum/05-escrever-o-dossie.md#3-desenhar-o-dfd-fase-2a)).
Duas particularidades:

1. o **nginx** e a **API** são processos diferentes na zona do servidor — e o fluxo do PDF **não
   passa** pela API;
2. o **provedor de e-mail/SMS** fica **fora** da clínica: desenhe uma **fronteira** entre a
   clínica e ele. Tudo o que cruza essa fronteira sai do controle da clínica.

---

## Passo 3 — Faça o STRIDE de cada elemento, testando

> 🔎 **Como o Grupo 5 fez:** para cada letra, **tentou** o ataque antes de escrever. Exemplos:
>
> - **S/E no 1.0:** o login com `admin@vidaplus.local' -- ` devolveu token de ADMIN;
> - **T/D no D2:** achava que a injeção "apagava o banco"; **testou** `'; UPDATE ...`, recebeu erro e
>   **corrigiu a tabela**;
> - **3.0:** testou `/api/pacientes/1abc/notas` → **400**: proteção acidental.

**▶ Agora no G4:** uma linha por elemento. Experimentos (no terminal, na raiz do repositório). Os tokens dos usuários (`TOKEN_ANA`, `TOKEN_HOUSE`, `TOKEN_REC`, `TOKEN_BIO`, `TOKEN_ADMIN`) vêm do `source roteiros/comum/tokens.sh`, que já está no começo de cada bloco abaixo:

**Processo "Obter exame" — percorrer números**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
for ID in 100 101 102 103 104 105; do
  echo "exame $ID: $(curl -s -w ' [%{http_code}]' localhost:8080/api/exames/$ID -H "Authorization: Bearer $TOKEN_ANA" | head -c 70)"
done
```

Que exames a Ana consegue? E os números que **não existem** (100, 105): a resposta é diferente de
um que existe? (I — guarde para a Fase 2b.)

**Fluxo "mensagem de erro"**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
for U in /api/exames/999/pdf /api/exames/abc /api/exames/101; do
  echo "== $U"; curl -s localhost:8080$U -H "Authorization: Bearer $TOKEN_ANA" | head -c 300; echo
done
curl -s localhost:8080/api/exames/101 | head -c 300; echo     # sem token
```

Para cada resposta: que informação interna ela entrega (nomes de classes Java, nomes de métodos,
o SQL)? Que letra? Que outra falha esse detalhe ajuda a explorar? (Veja a busca com uma aspa:
`curl -s -G localhost:8080/api/pacientes/busca --data-urlencode "q='" | head -c 600; echo` — a resposta
mostra o comando SQL.) E repare no código de status **sem token**: é o certo? (Compare com a
tabela de [`comum/02` §1.2](../comum/02-observar-o-sistema.md#12-códigos-de-status-http-que-você-vai-encontrar).)

**Processo "Servir arquivo" (nginx) e depósito "pasta de laudos"**

```bash
for ID in 101 102 103 104 105; do
  curl -s -o /dev/null -w "laudo-$ID.pdf -> %{http_code} %{content_type}\n" localhost:4200/laudos/laudo-$ID.pdf
done
```

Sem login, quais baixam? E o 105, que não existe — o código é 200, mas o **tipo de conteúdo** é
outro. Por quê? (Abra `frontend/nginx.conf` — **Ctrl+P** → `nginx.conf` — e leia a linha `try_files`.) Isso permite descobrir **quais laudos
existem** só olhando o tipo da resposta.

**Processo "Enviar notificação" (planejado)** — analise o **desenho** da Fase 0:

- I: o que o texto e o link revelam a quem lê a mensagem (provedor, tela de bloqueio, encaminhamento)?
- S: um golpista manda uma mensagem **igual** à da clínica, com um link falso. O paciente
  distingue? (Essa é a razão de o link não pedir senha numa página "parecida".)
- E: o link funciona para quem **não** é o paciente?

**Depósito "conteúdo do site" e processo do nginx (F-20)**

- T: quem consegue trocar o `index.html`? Pense no caminho: repositório → pipeline → imagem →
  servidor. Em qual ponto um atacante (ou um integrante descuidado) mudaria o site?
- I/T no navegador: sem `Content-Security-Policy`, o navegador executa qualquer script que a página
  carregue (`curl -s -I localhost:4200/` — os cabeçalhos de proteção estão lá?).
- R: se a página for trocada, existe registro de **quem** trocou? (Pista: o histórico do Git.)

**Processo "Rotina de retenção" (planejado)** — analise o **desenho**:

- T/D: uma regra de prazo errada apaga dados **antes** da hora — o que acontece com um paciente em
  tratamento? (Eliminação **não tem volta**.)
- R: depois de eliminar, como a clínica **prova** à ANPD que eliminou (e o quê)?
- I: a eliminação alcança os **arquivos** PDF? E os **backups**?

**Depósito `paciente` (inativos):** os dados de quem deixou a clínica continuam lá, para sempre
(I, pelo tempo de exposição).

**Entidades:** S e R.

---

## Passo 4 — Ligue cada ameaça a uma resposta

> 🔎 **Como o Grupo 5 fez:** cada ameaça com o requisito do pacote, ou **"outro pacote (…) —
> transferida"** (ex.: HTTP sem TLS → *"TLS + HSTS — outro pacote (V-13)"*).

**▶ Agora no G4:** cada letra marcada → requisito do G4 (crie, se faltar, e volte à §2) ou "outro
pacote — transferida". Exemplos de transferência: o armazenamento de PDFs fora da pasta pública é
também a F-08 (G2) — registrem a **interface**: o canal "arquivo" é do recorte de vocês, a
reestruturação do armazenamento é deles; CORS e cabeçalhos da API são F-17 (G1).

---

## Passo 5 — Monte a árvore de ataque

> 🔎 **Como o Grupo 5 fez:** a primeira árvore começava pela falha famosa (injeção); só depois o
> grupo achou o caminho **mais barato** — usar a busca sem login — e o pôs como ramo 1. Veja a
> árvore e a 💡: [`docs/12` §3](../../docs/12-exemplo-dossie-preenchido.md#3-fase-2a--threat-modeling-stride).

**▶ Agora no G4:** objetivo sugerido: **"colher o máximo de laudos de outros pacientes"**. Os
ramos são **os canais** — e a pergunta é qual é **o mais barato**. Pense em: com conta e sem
conta; adivinhando números ou descobrindo-os pela resposta (o oráculo); pelo arquivo, pela API,
por uma notificação encaminhada. Em cada folha: custo, se precisa de conta e o requisito que
bloqueia.

```
OBJETIVO: <...>                                              (OR)
├── 1. <canal mais barato>         custo <...>               → REQ-...
├── 2. <caminho composto>                                    (AND)
│      ├── <descobrir quais números existem>   custo <...>
│      └── <baixar cada um>                    custo <...>   → REQ-...
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

- [ ] O DFD tem os canais: API, arquivo (nginx), erro, notificação (planejada), site — e a
      rotina de retenção (planejada).
- [ ] O provedor de e-mail/SMS está fora de uma fronteira de confiança.
- [ ] Testei o oráculo pela API **e** pelo arquivo.
- [ ] Cada X tem ameaça concreta e resposta.
- [ ] A árvore começa pelo canal mais barato e tem pelo menos um ramo AND.

## Erros comuns

- **Ignorar o erro como canal.** "É só uma mensagem de erro" — que contém o SQL da consulta.
- **Analisar a notificação como se fosse código.** Ela não existe: analise o **desenho** proposto
  e as ameaças dele.
- **Esquecer que eliminar também é risco.** Apagar cedo demais é um incidente.
