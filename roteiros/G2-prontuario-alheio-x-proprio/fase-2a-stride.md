# G2 · Fase 2a — DFD, STRIDE e árvore de ataque

> **Seção do dossiê:** §3 · **Lidera:** 🛡️ Segurança · **Aula:** 3 · **Semana:** 4
> Junto com a 2b, é **a fase de maior peso** na avaliação.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G2**.

## O que você entrega no fim

1. Um **DFD** do recorte do G2, com fronteiras de confiança.
2. A tabela **STRIDE por elemento**.
3. Uma **árvore de ataque** do objetivo mais grave do pacote.

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório (se você seguiu o [`comum/00`](../comum/00-antes-de-comecar.md#4-clone-o-repositório-do-grupo), é a `vidaplus`, dentro da pasta `ssc` da sua pasta pessoal).
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

**DFD** — desenho de por onde os dados passam (como desenhar:
[`comum/05` §3](../comum/05-escrever-o-dossie.md#3-desenhar-o-dfd-fase-2a)):

| Elemento | O que é | No G2, por exemplo |
|---|---|---|
| **Entidade externa** | quem está fora e usa o sistema | Paciente, Médico, Laboratório |
| **Processo** | algo que o sistema faz com os dados | "Obter exame" |
| **Depósito** | onde o dado fica guardado | tabela `exame`; a pasta de PDFs |
| **Fluxo** | o dado que passa entre dois elementos | "número do exame"; "laudo em PDF" |

**Fronteira de confiança** — onde o dado sai de um lugar não confiável (o navegador) para um mais
confiável (o servidor) e daí para o banco.

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

**Árvore de ataque** — objetivo no topo; caminhos abaixo, em ramos **OR** (qualquer um basta) ou
**AND** (precisa de todos); em cada folha, **custo** e **requisito**.

> **IDOR** (*Insecure Direct Object Reference*) é o nome da falha do seu P0: o sistema usa um
> **número** que vem do usuário (o id do exame) para buscar um objeto, **sem conferir se ele pode
> vê-lo**. No STRIDE, ela aparece como **I** (lê o que não devia) e **E** (faz mais do que pode).

---

## Passo 1 — Liste os elementos do recorte

> 🔎 **Como o Grupo 5 fez:** usou as trilhas da Fase 0. Cada método de backend virou um
> **processo** (1.0 Autenticar, 2.0 Buscar, 3.0 Obter nota, 4.0 Exibir prontuário); cada tabela ou
> armazenamento, um **depósito** (D1 `usuario`, D2 `paciente`, D3 `nota_clinica`, D4
> `localStorage`); cada papel, uma **entidade**. Na Fase 4, achou um ponto novo e **voltou** para
> acrescentar o processo 5.0.

**▶ Agora no G2:** a partir das trilhas da Fase 0, o recorte precisa cobrir:

- **obter um exame** (`ExameController.getExame` e `getPdf`);
- **listar os exames de um paciente** (`porPaciente`);
- **entregar o arquivo PDF** — atenção: quem faz isso é o **nginx**, não a API. Desenhe-o como um
  **processo separado**, com o seu próprio depósito (a pasta `public/laudos`). Repare que esse
  caminho **não cruza** a API: é aqui que mora a falha da F-08;
- **publicar um laudo** (`publicar`);
- **exportar os dados do titular** (F-09): como não existe, desenhe como **processo planejado**
  (anote "a criar") e os depósitos que ele precisaria ler.

Depósitos: `exame`, `atendimento` (o vínculo médico–paciente), `usuario` (a ligação usuário →
paciente), a pasta de PDFs, o `localStorage` (token). Entidades: Paciente, Médico, Laboratório e
— se entrarem no fluxo — Recepção/Admin.

Numere: processos 1.0, 2.0…; depósitos D1, D2…

---

## Passo 2 — Desenhe o DFD

> 🔎 **Como o Grupo 5 fez:** três zonas (navegador | servidor | banco), fronteiras tracejadas
> vermelhas e uma nota amarela com o requisito perto de cada processo:
> [`docs/img/dfd-exemplo-pacote5.svg`](../../docs/img/dfd-exemplo-pacote5.svg).

**▶ Agora no G2:** desenhe ([`comum/05` §3](../comum/05-escrever-o-dossie.md#3-desenhar-o-dfd-fase-2a)),
com uma particularidade: o **servidor web (nginx)** e a **API** são dois processos diferentes na
zona do servidor. O fluxo "laudo em PDF" sai da pasta pública direto para o navegador, **sem
passar** pela API. Deixe isso visível no desenho: é o tipo de detalhe que a defesa pergunta.

---

## Passo 3 — Faça o STRIDE de cada elemento, testando

> 🔎 **Como o Grupo 5 fez:** para cada letra, **tentou** o ataque antes de escrever. Exemplos:
>
> - **S/E no 1.0:** o login com `admin@vidaplus.local' -- ` devolveu token de ADMIN;
> - **I no D1:** a injeção `UNION` na busca devolveu e-mail e hash de senha de todos;
> - **T/D no D2:** achava que a injeção "apagava o banco"; **testou** `'; UPDATE ...`, recebeu erro e
>   **corrigiu a tabela**;
> - **3.0:** testou `/api/pacientes/1abc/notas` → **400**: proteção acidental.

**▶ Agora no G2:** uma linha por elemento. Experimentos (no terminal, na raiz do
repositório). Os tokens dos usuários (`TOKEN_ANA`, `TOKEN_HOUSE`, `TOKEN_REC`, `TOKEN_BIO`, `TOKEN_ADMIN`) vêm do `source roteiros/comum/tokens.sh`, que já está no começo de cada bloco abaixo:

**Processo "Obter exame" — quem vê o quê?**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
for QUEM in ANA HOUSE REC; do
  T=$(eval echo \$TOKEN_$QUEM)
  for ID in 101 102 104; do
    echo "$QUEM -> exame $ID: $(curl -s -o /dev/null -w '%{http_code}' localhost:8080/api/exames/$ID -H "Authorization: Bearer $T")"
  done
done
```

O `for` de fora troca a pessoa; o de dentro, o exame. Esperado: as linhas `TOKEN_... ok` e nove
linhas `QUEM -> exame NNN: CÓDIGO` (três pessoas × três exames). Monte uma tabela no rascunho:

| Quem | 101 (Ana) | 102 (Bruno) | 104 (Carla, não publicado) | O que **deveria** acontecer? |
|---|---|---|---|---|

Pense: a Dra. House atende a Ana e a Carla, mas **não** o Bruno. A recepção cuida de cadastro e
agenda, **não** de conteúdo clínico ([`docs/00`](../../docs/00-visao-geral.md)). Que letras
STRIDE aparecem?

**Processo "Obter exame" — números sequenciais**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
for ID in 100 101 102 103 104 105; do
  echo "exame $ID: $(curl -s -w ' [%{http_code}]' localhost:8080/api/exames/$ID -H "Authorization: Bearer $TOKEN_ANA" | head -c 80)"
done
```

Esperado: seis linhas `exame NNN: ...`, cada uma com o começo da resposta e o código entre
colchetes, como `[200]`. Um atacante percorre os números e "colhe" todos os exames. O que acontece com os números que
**não existem** (100, 105)? A resposta é diferente da de um exame que existe? Guarde essa
observação para a Fase 2b (ela tem nome: oráculo de existência).

**Processo "Listar exames do paciente"**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s localhost:8080/api/exames/paciente/3 -H "Authorization: Bearer $TOKEN_ANA"; echo
```

Esperado: as linhas `TOKEN_... ok` e uma lista entre colchetes, `[{"id":...}]`. A Ana consegue
a lista de exames da Carla — inclusive o número do exame de sorologia. Que letra?
E como isso ajuda o ataque do processo anterior?

**Processo "Entregar PDF" (nginx) e o depósito "pasta de laudos"**

Peça os arquivos de laudo **sem login** (o `for` repete o pedido para quatro números):

```bash
for ID in 101 102 103 104; do
  curl -s -o /dev/null -w "laudo-$ID.pdf -> %{http_code} %{content_type}\n" localhost:4200/laudos/laudo-$ID.pdf
done
```

Esperado: quatro linhas `laudo-NNN.pdf -> CÓDIGO TIPO`. Que letra? Que **controle** existe entre
o navegador e o arquivo? E o laudo 104, ainda **não publicado**?

**Processo "Publicar laudo"**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s -X POST localhost:8080/api/exames/104/publicar -H "Authorization: Bearer $TOKEN_ANA"; echo
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, publicado, publicado_por, hash_laudo FROM exame WHERE id = 104;"
```

Esperado: uma linha JSON da API e, depois, a linha do exame `104` com as colunas `publicado`,
`publicado_por` e `hash_laudo`. A Ana (paciente) publicou um laudo — que letra? O sistema guardou **quem** publicou
(`publicado_por`)? Isso ajuda em qual letra (R)? E o `hash_laudo` continua vazio: se alguém
mudar o resultado no banco depois de publicado, o que prova que mudou (T)? **Depois, zere o banco**
(rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`) ([`comum/01` §7](../comum/01-subir-o-vidaplus.md#7-zerar-o-banco--e-por-que-isso-importa)).

**Depósitos `atendimento` e `usuario`:** eles têm a informação necessária para decidir quem pode
ver o quê (o vínculo médico–paciente; a ligação usuário–paciente). Alguém consulta hoje?

**Entidades:** S e R de cada papel. Ex.: alguém pode se passar pela Ana? (Pense no token, Fase 0.)

**Exportação (planejada):** como ela ainda não existe, analise o **projeto**: que ameaças ela vai
ter? (Pista: é o **mesmo** problema do P0 — se a Ana pedir a exportação com o número de outra
pessoa…)

---

## Passo 4 — Ligue cada ameaça a uma resposta

> 🔎 **Como o Grupo 5 fez:** cada ameaça com o requisito do pacote, ou **"outro pacote (…) —
> transferida"** (ex.: HTTP sem TLS → *"TLS + HSTS — outro pacote (V-13)"*).

**▶ Agora no G2:** cada letra marcada → um requisito do G2 (crie se faltar e volte à §2) ou
"outro pacote (F-xx) — transferida". Exemplos de transferência no G2: a fila de laudos pendentes
visível a qualquer papel é controle por papel (F-03, G3); o registro de **quem viu** cada laudo é
trilha de auditoria (F-05, G1).

---

## Passo 5 — Monte a árvore de ataque

> 🔎 **Como o Grupo 5 fez:** objetivo *"copiar dados sensíveis de todos os pacientes"*. A primeira
> versão começava pela falha famosa (injeção); só depois o grupo achou o caminho **mais barato** —
> usar a busca sem login — e o pôs como ramo 1. Veja a árvore e a 💡:
> [`docs/12` §3](../../docs/12-exemplo-dossie-preenchido.md#3-fase-2a--threat-modeling-stride).

**▶ Agora no G2:** objetivo sugerido: **"ler o resultado da sorologia da Carla (exame 104), que
ainda nem foi publicado"**. Pergunte **de quantos jeitos** dá para chegar lá, **começando pelo
mais barato**: com conta e sem conta; pela API e fora dela; descobrindo o número ou adivinhando-o.
Em cada folha: custo, se precisa de conta e o requisito que bloqueia.

```
OBJETIVO: <...>                                              (OR)
├── 1. <caminho mais barato>       custo <...>               → REQ-...
├── 2. <caminho composto>                                    (AND)
│      ├── <descobrir o número do exame>   custo <...>
│      └── <pedir o exame com esse número> custo <...>       → REQ-...
└── 3. <...>
```

---

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| Os experimentos mostram resultados diferentes dos da tabela que você esperava (ex.: o 104 já aparece publicado) | o experimento de publicar o 104 já foi feito e o banco não foi zerado | `bash roteiros/comum/zerar-banco.sh` e `source roteiros/comum/tokens.sh`, e repita |
| O `for` mostra `-> exame NNN: 500` para todo mundo | os tokens estão vazios | rode o bloco inteiro de novo, **com** a linha do `source` no começo |

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

- [ ] O DFD separa a **API** do **servidor de arquivos**, e o fluxo do PDF não passa pela API.
- [ ] O recorte cobre obter, listar, baixar, publicar e (planejado) exportar.
- [ ] Testei quem vê o quê para **pelo menos três papéis**, e registrei a tabela.
- [ ] Cada X tem ameaça concreta e resposta.
- [ ] A árvore começa pelo caminho mais barato e tem pelo menos um ramo AND.
- [ ] Zerei o banco depois de publicar o 104.

## Erros comuns

- **Pensar só no paciente.** O IDOR do médico (vendo quem não atende) e da recepção (vendo
  conteúdo clínico) são o mesmo problema.
- **Esquecer o arquivo.** Proteger a API e deixar o PDF na pasta pública é proteger a porta da
  frente e deixar a janela aberta.
- **Impacto sem teste.** Leia a 💡 "Teste a sua afirmação" do [`docs/12` §3](../../docs/12-exemplo-dossie-preenchido.md#3-fase-2a--threat-modeling-stride).
