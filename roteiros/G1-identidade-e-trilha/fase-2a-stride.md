# G1 · Fase 2a — DFD, STRIDE e árvore de ataque

> **Seção do dossiê:** §3 · **Lidera:** 🛡️ Segurança · **Aula:** 3 · **Semana:** 4
> Junto com a 2b, é **a fase de maior peso** na avaliação.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. Um **DFD** do recorte do G1, com fronteiras de confiança.
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

**DFD** — desenho de por onde os dados passam. Quatro elementos (como desenhar:
[`comum/05` §3](../comum/05-escrever-o-dossie.md#3-desenhar-o-dfd-fase-2a)):

| Elemento | O que é | No G1, por exemplo |
|---|---|---|
| **Entidade externa** | quem está fora e usa o sistema | o usuário que faz login |
| **Processo** | algo que o sistema faz com os dados | "Autenticar" |
| **Depósito** | onde o dado fica guardado | tabela `usuario`; `localStorage` |
| **Fluxo** | o dado que passa entre dois elementos | "e-mail e senha"; "token" |

**Fronteira de confiança** — a linha onde o dado sai de um lugar em que você **não** pode confiar
(o navegador, nas mãos do usuário) para outro em que confia mais (o servidor) e daí para o banco.

**STRIDE** — seis perguntas para **cada elemento**:

| Letra | Ameaça | Pergunta | Atributo violado |
|---|---|---|---|
| **S** | falsificação de identidade (*Spoofing*) | alguém consegue **se passar** por outro? | Autenticidade |
| **T** | adulteração (*Tampering*) | alguém consegue **alterar** o dado ou o comportamento? | Integridade |
| **R** | repúdio (*Repudiation*) | alguém consegue **negar** o que fez, por falta de registro? | Não-repúdio |
| **I** | divulgação (*Information disclosure*) | alguém consegue **ler** o que não devia? | Confidencialidade |
| **D** | negação de serviço (*Denial of service*) | alguém consegue **impedir** o uso? | Disponibilidade |
| **E** | elevação de privilégio (*Elevation of privilege*) | alguém consegue fazer **mais do que pode**? | Autorização |

| Tipo de elemento | Letras analisadas |
|---|---|
| Entidade externa | S, R |
| Processo | S, T, R, I, D, E |
| Depósito | T, R, I, D |
| Fluxo | T, I, D |

**Árvore de ataque** — o **objetivo** do atacante no topo; os **caminhos** abaixo, em ramos **OR**
(qualquer um basta) ou **AND** (precisa de todos). Em cada folha: **custo** e **requisito** que
bloqueia.

---

## Passo 1 — Liste os elementos do recorte

> 🔎 **Como o Grupo 5 fez:** usou as trilhas da Fase 0. Cada método de backend virou um
> **processo**; cada tabela ou armazenamento, um **depósito**; cada papel que usa o fluxo, uma
> **entidade**:
>
> - processos: **1.0** Autenticar (`UsuarioDao.autenticar`), **2.0** Buscar pacientes
>   (`PacienteDao.buscar`), **3.0** Obter nota clínica (`PacienteDao.notaDoPaciente`), **4.0**
>   Exibir prontuário (`Prontuario.jsx`);
> - depósitos: **D1** `usuario`, **D2** `paciente`, **D3** `nota_clinica`, **D4** `localStorage`;
> - entidades: Usuário, Recepção, Médico.
>
> Na Fase 4 achou um ponto de injeção no reset de senha e **voltou** para acrescentar o processo
> **5.0** Recuperar senha. O threat model é vivo.

**▶ Agora no G1:** pegue as trilhas que você escreveu na Fase 0 (Passo 2). O recorte precisa
cobrir:

- o **login** (F-01);
- o **uso do token** num pedido depois do login (F-11) — escolha um, por exemplo abrir um exame
  (`GET /api/exames/{id}`), e desenhe o token indo do `localStorage` até a API;
- a **recuperação de senha** (F-12);
- a **trilha de auditoria** (F-05): como não existe, desenhe-a como **depósito planejado** (anote
  "a criar") e os fluxos que a alimentariam (de quais processos?).

A F-17 (cabeçalhos/CORS) não é um processo: é uma propriedade das **respostas** que voltam ao
navegador. Ela aparece no STRIDE desses **fluxos**.

Numere: processos 1.0, 2.0…; depósitos D1, D2…

---

## Passo 2 — Desenhe o DFD

> 🔎 **Como o Grupo 5 fez:** desenhou no draw.io com três zonas (navegador | servidor | banco),
> separadas por fronteiras tracejadas vermelhas, e colocou perto de cada processo uma nota amarela
> com o requisito que o protege: [`docs/img/dfd-exemplo-pacote5.svg`](../../docs/img/dfd-exemplo-pacote5.svg).

**▶ Agora no G1:**

1. Desenhe no draw.io os elementos do Passo 1 ([`comum/05` §3](../comum/05-escrever-o-dossie.md#3-desenhar-o-dfd-fase-2a)).
2. Trace as fronteiras de confiança.
3. Dê nome a cada seta com **o dado** que passa ("e-mail e senha", "token", "código de
   recuperação"…).
4. Exporte e coloque na §3 ([`comum/05` §3.2](../comum/05-escrever-o-dossie.md#32-gravar-e-colocar-no-dossiê)).

---

## Passo 3 — Faça o STRIDE de cada elemento, testando

> 🔎 **Como o Grupo 5 fez:** para cada letra, **tentou** o ataque antes de escrever. Quatro
> exemplos do que ele rodou e do que concluiu:
>
> **S/E no 1.0 (Autenticar)** — o ataque do teste-semente:
>
> ```bash
> curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
>   -d "{\"email\":\"admin@vidaplus.local' -- \",\"senha\":\"qualquer\"}"
> ```
>
> Recebeu um token com `"papel":"ADMIN"`. Escreveu: *"com `' -- ` no fim do e-mail, entra como
> qualquer usuário sem a senha (S), inclusive como ADMIN (E)"*.
>
> **I no D1 (`usuario`)** — usou a injeção da busca para ler outra tabela (o comando `UNION`
> "cola" o resultado de uma segunda consulta na primeira):
>
> ```bash
> curl -s -G localhost:8080/api/pacientes/busca --data-urlencode \
>   "q=x') UNION SELECT id,email,senha_hash,NULL,papel,NULL,NULL,NULL,NULL,NULL,NULL,NULL FROM usuario -- "
> ```
>
> Recebeu e-mail e hash de senha de **todos** os usuários.
>
> **T/D no D2 (`paciente`)** — achava que a injeção "apagava o banco". **Testou** com
> `'; UPDATE paciente SET nome='HACK' WHERE id=3; -- ` e recebeu **erro de sintaxe**: o banco não
> executa dois comandos de uma vez. **Corrigiu a tabela.** Mas descobriu outro D, real:
> `' OR SLEEP(3) -- ` no login fez a resposta demorar **18 segundos**.
>
> **T/I no 3.0 (`notaDoPaciente`)** — o SQL também concatena o id, mas testou
> `curl -s localhost:8080/api/pacientes/1abc/notas` e recebeu **400**: hoje só chega número.
> Escreveu que a proteção é **acidental** (se o id virar texto, a falha aparece).

**▶ Agora no G1:** monte a tabela com **uma linha por elemento**. Para cada letra aplicável,
pergunte e — sempre que der — **teste**. Experimentos do G1 (rode no terminal, na raiz do repositório; observe,
anote o resultado e a letra):

**Processo Autenticar (F-01)**

1. O ataque do teste-semente — um e-mail que termina com `' -- ` e uma senha qualquer:

   ```bash
   curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d "{\"email\":\"admin@vidaplus.local' -- \",\"senha\":\"qualquer\"}"; echo
   ```

   Esperado: uma linha JSON que começa com `{"access_token":"eyJ`. O que veio no campo `papel` da
   resposta? O sistema conferiu a senha?
2. Tentativas repetidas:

   ```bash
   for i in 1 2 3 4 5 6 7 8 9 10; do
     curl -s -o /dev/null -w '%{http_code} ' -X POST localhost:8080/api/auth/login \
       -H "Content-Type: application/json" -d "{\"email\":\"ana.paciente@email.com\",\"senha\":\"errada$i\"}"
   done; echo
   ```

   O `for ... do ... done` repete o comando 10 vezes (com as senhas `errada1`, `errada2`…) e mostra o
   código de status de cada tentativa, numa linha só. Esperado: dez números de três dígitos,
   separados por espaço. Alguma foi bloqueada? E se
   fossem 10 mil, com uma lista das senhas mais comuns?
3. O `SLEEP`: um e-mail com `' OR SLEEP(1) -- ` pede ao banco que "durma" 1 segundo. O `time` na
   frente do comando mede quanto ele demorou:

   ```bash
   time curl -s -o /dev/null -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d "{\"email\":\"x' OR SLEEP(1) -- \",\"senha\":\"x\"}"
   ```

   Esperado: três linhas, `real`, `user` e `sys`, cada uma com um tempo (`0m6.013s` = 6
   segundos). Olhe a linha `real`. Por que demorou bem mais que 1 segundo? (Pista: quantos usuários há na
   tabela?) Que letra é essa?

> **Atenção à letra D.** A força bruta ameaça a **autenticidade** (S): o atacante acaba acertando
> a senha. Bloquear a conta depois de N erros protege contra S, mas cria um **D**: o atacante pode
> **travar a conta de outra pessoa** de propósito. Registrar esse efeito colateral é o nível de
> análise esperado.

**Uso do token (F-11)**

1. Entre como Ana na tela (`http://localhost:4200`). Aperte **F12** → aba **Armazenamento**
   (Firefox) ou **Aplicativo** (Chrome) → **Armazenamento local** → `http://localhost:4200`. Clique
   duas vezes no valor de `access_token` e copie (**Ctrl+C**).
2. No terminal, guarde o token copiado numa variável (cole entre as aspas simples, com
   **Ctrl+Shift+V**):

   ```bash
   TOKEN_VELHO='cole-aqui-o-token'
   echo "${TOKEN_VELHO:0:10}"
   ```

   Troque `cole-aqui-o-token` pelo que você copiou, **mantendo as aspas simples**. Esperado: a
   segunda linha mostra `eyJhbGciOi` (o começo de todo token). Se mostrar `cole-aqui-` ou nada,
   a colagem não funcionou: repita o item 1.

3. Na tela, clique em **Sair**.
4. Teste o token "velho", gravando a saída como evidência:

   ```bash
   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN_VELHO" \
     | tee entregaveis/evidencias/fase2a-token-depois-do-sair.txt
   ```

   Esperado: um número de três dígitos (`200` = o token ainda vale; `401`/`403` = foi recusado).
   O que deveria acontecer depois do "Sair"? O que aconteceu?
5. Em `JwtUtil.java`, a chave que assina o token está no código. Quem tem o código consegue
   **fabricar** um token com `"papel":"ADMIN"`? Que letra é essa, e em qual elemento? (Não precisa
   fabricar: basta explicar. Se quiser ver, o script de demonstração faz isso só com o código —
   `FORJADO=$(bash roteiros/comum/fabricar-token.sh 5 ADMIN)` e depois
   `curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/usuarios -H "Authorization: Bearer $FORJADO"`;
   esperado: `200`. Na Fase 6 vocês fecham essa porta.)

**Recuperar senha (F-12)**

1. Peça a recuperação de senha para um e-mail que existe e para um que não existe:

   ```bash
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" -d '{"email":"ana.paciente@email.com"}'; echo
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" -d '{"email":"naoexiste@teste.com"}'; echo
   ```

   A diferença entre "encontrado" e "não cadastrado": que letra? E o código previsível (o número
   antes do `-` é o id do usuário; o depois, a hora em milissegundos)?
2. O mesmo padrão de injeção do login, no reset:

   ```bash
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" \
     -d "{\"email\":\"x' OR '1'='1\"}"; echo
   ```

   Esperado: uma linha JSON com `"mensagem"`. A resposta diz "encontrado"? Para qual usuário (olhe o número antes do `-` no `reset_token`)?
   Abra `UsuarioDao.porEmail` e compare com `autenticar`: é o mesmo padrão de SQL?

**Fluxos que voltam ao navegador (F-17)**

- Veja de novo o que a API responde a um pedido vindo de **outro site** (o cabeçalho `Origin`
  finge a origem):

  ```bash
  curl -s -i -H "Origin: http://site-qualquer.example" "localhost:8080/api/pacientes/busca?q=Silva" | grep -i "access-control"
  ```

  Esperado: duas linhas `Vary: Access-Control-...` e uma `Access-Control-Allow-Origin: ...`.
  `Access-Control-Allow-Origin: *` quer dizer, em palavras, que **qualquer site** pode pedir dados
  à API a partir do navegador de quem está logado. Que letra, em que fluxo?
- A falta de `Content-Security-Policy` não é um ataque em si: é a **ausência de uma segunda
  barreira**. Escreva qual ataque ela deixaria passar (pense no `localStorage`).

**Depósitos**

- `usuario`: hash MD5 sem sal, igual para Ana e Bruno — o que acontece se a tabela vazar (I)? Quem
  consegue mudar a coluna `ativo` (T)? Teste: a Ana (paciente) tenta desativar a conta da
  recepção (usuário 3), e depois você confere no banco:

  ```bash
  source roteiros/comum/tokens.sh
  curl -s -X POST localhost:8080/api/usuarios/3/alternar-ativo -H "Authorization: Bearer $TOKEN_ANA"; echo
  docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, email, ativo FROM usuario;"
  ```

  Esperado: `{"alterado":true,"id":3}` e, na tabela, a linha do `recepcao@vidaplus.local` com
  `ativo` = `0`. De que pacote é essa falha? Ao terminar, **volte o banco ao estado original**:

  ```bash
  bash roteiros/comum/zerar-banco.sh
  source roteiros/comum/tokens.sh
  ```
- `localStorage`: qualquer script da página lê (I).
- Trilha de auditoria (a criar): quem poderia **apagar ou alterar** uma linha para esconder o que
  fez (T, R)? É exatamente o que a F-05 precisa impedir.

**Entidades:** S e R para cada papel.

---

## Passo 4 — Ligue cada ameaça a uma resposta

> 🔎 **Como o Grupo 5 fez:** na última coluna, citou o requisito do pacote que mitiga a ameaça ou
> escreveu **"outro pacote (…) — transferida"**. Ex.: o fluxo "e-mail e senha" em HTTP, sem TLS →
> *"TLS + HSTS — outro pacote (V-13)"*.

**▶ Agora no G1:** para cada letra marcada:

- **um requisito do G1** da Fase 1. Se a ameaça não tem requisito, **crie um** e volte à §2 (é
  normal);
- ou **"outro pacote (F-xx) — transferida"**. Ex.: desativar a conta de outro usuário sem ser
  ADMIN é controle por papel → F-03 (G3).

**Toda letra marcada tem resposta.** Letra sem resposta é ameaça esquecida.

---

## Passo 5 — Monte a árvore de ataque

> 🔎 **Como o Grupo 5 fez:** objetivo *"copiar nome, CPF e dados sensíveis de todos os
> pacientes"*. A primeira versão começava pela injeção de SQL ("porque o pacote é de injeção"). Só
> na Fase 5 o grupo achou o caminho **mais barato** — usar a busca sem login, sem técnica nenhuma —
> e o pôs como ramo 1. Veja a árvore final e a 💡 depois dela:
> [`docs/12` §3](../../docs/12-exemplo-dossie-preenchido.md#3-fase-2a--threat-modeling-stride).

**▶ Agora no G1:** objetivo sugerido: **"entrar no VidaPlus como ADMIN (ou como outra pessoa) sem
saber a senha"**. Pergunte: **de quantos jeitos diferentes** dá para chegar lá? Pense em caminhos
pela **credencial**, pela **sessão**, pela **recuperação de senha** e pelo **código do sistema**
— e comece pelo **mais barato**. Para cada folha: custo (trivial/baixo/médio/alto), se precisa de
conta, e o requisito que bloqueia.

```
OBJETIVO: <...>                                              (OR)
├── 1. <caminho mais barato>       custo <...>               → REQ-...
├── 2. <caminho composto>                                    (AND)
│      ├── <parte a>               custo <...>
│      └── <parte b>               custo <...>               → REQ-...
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
| O `time` do `SLEEP` mostrou menos de 1 segundo | o e-mail do ataque foi digitado diferente (faltou a aspa ou o `-- ` com espaço no fim) | copie o bloco inteiro de novo, sem editar |
| `echo "${TOKEN_VELHO:0:10}"` mostra `cole-aqui-` ou nada | o token não foi colado entre as aspas | repita a cópia do `access_token` no DevTools e cole com **Ctrl+Shift+V** |
| Depois do teste de desativar a recepção, `TOKEN_REC FALHOU` | a conta da recepção ficou desativada | rode o `zerar-banco.sh` e o `source` do `tokens.sh`, como o passo pede |

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

- [ ] O DFD tem entidades, processos e depósitos numerados, fluxos com nome do dado e fronteiras.
- [ ] O recorte cobre login, uso do token, recuperação de senha e a trilha (planejada).
- [ ] Cada elemento tem uma linha no STRIDE, com as letras aplicáveis analisadas.
- [ ] Cada X tem **ameaça concreta** e **resposta**.
- [ ] O que eu disse que acontece, **eu testei** (ou expliquei por que não dá).
- [ ] A árvore começa pelo caminho mais barato e tem pelo menos um ramo AND.
- [ ] O DFD (`.drawio` + `.svg`) está em `entregaveis/img/` e entrou no PR.

## Erros comuns

- **X sem ameaça escrita.** A coluna "Ameaça concreta" é o que vale.
- **Impacto sem teste.** Leia a 💡 "Teste a sua afirmação" do [`docs/12` §3](../../docs/12-exemplo-dossie-preenchido.md#3-fase-2a--threat-modeling-stride).
- **Confundir fluxo com processo.** O que o processo faz com o dado é do processo; o que acontece
  com o dado **no caminho** é do fluxo.
