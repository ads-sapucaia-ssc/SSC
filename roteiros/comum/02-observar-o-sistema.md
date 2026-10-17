# 02 · Observar o sistema — navegador, curl, banco, código e logs

**Objetivo:** aprender os **cinco jeitos de olhar o VidaPlus por dentro**. Todos os roteiros das
fases usam estas técnicas; volte aqui sempre que um passo disser "faça uma requisição com curl" ou
"consulte o banco".

**Tempo:** 60–90 minutos, fazendo todos os exercícios. Vale a pena: é a base de tudo.

**Antes:** o VidaPlus precisa estar ligado ([`01`](01-subir-o-vidaplus.md)).

---

## 1. Pelo navegador, com as ferramentas do desenvolvedor (DevTools)

Toda tela do VidaPlus conversa com a API pelos bastidores. O navegador mostra essa conversa.

1. Abra `http://localhost:4200` e entre como **Ana** (`ana.paciente@email.com` / `Paciente123`).
2. Aperte **F12**. Abre um painel com várias abas.
3. Clique na aba **Rede** (em inglês, **Network**).
4. Recarregue a página (**F5**). Aparecem várias linhas: cada uma é um **pedido** (requisição)
   que a tela fez.
5. Para ver só os pedidos à API, clique no filtro **XHR** (Firefox) ou **Fetch/XHR** (Chrome).
6. Clique numa linha, por exemplo a que termina em `/exames/paciente/1`. À direita aparecem:
   - **Cabeçalhos (Headers):** o **endereço** pedido (URL), o **método** (`GET`, `POST`…), o
     **código de status** (`200` = deu certo) e os **cabeçalhos** enviados. Procure o cabeçalho
     **`Authorization: Bearer eyJ...`**: é o **token** que prova quem é a Ana;
   - **Resposta (Response):** o que a API devolveu, em formato **JSON** (texto com
     `{ "chave": "valor" }`).

### 1.1 Onde a tela guarda o login

1. Ainda no F12, abra a aba **Armazenamento** (Firefox) ou **Aplicativo/Application** (Chrome).
2. Abra **Armazenamento local (Local Storage)** → `http://localhost:4200`.
3. Você vê `access_token`, `papel`, `paciente_id`… É assim que a tela "lembra" quem entrou.

> **Pergunta para guardar:** qualquer código JavaScript que rode nesta página consegue ler esse
> armazenamento. Isso é bom ou ruim? (Você vai voltar a esta pergunta.)

### 1.2 Códigos de status HTTP que você vai encontrar

| Código | Significado | Em segurança, quer dizer… |
|---|---|---|
| **200** | OK | o pedido foi atendido |
| **400** | Bad Request | o pedido veio malformado (ex.: texto onde se esperava número) |
| **401** | Unauthorized | "não sei quem você é" — falta login ou o token é inválido |
| **403** | Forbidden | "sei quem você é, mas você não pode fazer isso" |
| **404** | Not Found | não existe |
| **500** | Internal Server Error | o servidor quebrou ao tentar atender |

A diferença entre **401** e **403** cai na prova e na defesa: 401 é sobre **identidade**
(autenticação); 403 é sobre **permissão** (autorização).

---

## 2. Pelo terminal, com o `curl`

A tela só faz os pedidos que o programador previu. Um atacante não usa a tela: ele **monta o
pedido que quiser**. O `curl` é o programa que faz isso pelo terminal.

### 2.1 Anatomia de um comando curl

```bash
curl -s "localhost:8080/api/pacientes/busca?q=Silva"
```

| Parte | Significado |
|---|---|
| `curl` | o programa |
| `-s` | "silencioso": não mostra a barra de progresso |
| `"localhost:8080/..."` | o endereço. Use **aspas** sempre: o `?` e o `&` têm significado especial no terminal |
| `?q=Silva` | um **parâmetro**: o nome `q` com o valor `Silva` |

Rode e veja o resultado: um JSON com os dados da Ana.

**Para ler melhor**, acrescente `| python3 -m json.tool` no fim. O `|` ("pipe") entrega a saída do
primeiro comando para o segundo, que a organiza em linhas:

```bash
curl -s "localhost:8080/api/pacientes/busca?q=Silva" | python3 -m json.tool
```

### 2.2 Ver o status e os cabeçalhos da resposta

```bash
curl -s -i "localhost:8080/api/pacientes/busca?q=Silva"
```

O `-i` mostra o **cabeçalho** da resposta antes do corpo. A primeira linha tem o status:
`HTTP/1.1 200`.

Só o código de status, no fim:

```bash
curl -s -o /dev/null -w '%{http_code}\n' "localhost:8080/api/pacientes/busca?q=Silva"
```

(`-o /dev/null` joga o corpo fora; `-w` escreve o código.)

### 2.3 Fazer login pelo terminal (método POST, com corpo JSON)

```bash
curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana.paciente@email.com","senha":"Paciente123"}'
```

| Parte | Significado |
|---|---|
| `-X POST` | o método. Sem ele, o curl usa `GET` |
| `-H "Content-Type: application/json"` | um **cabeçalho**: avisa que o corpo é JSON |
| `-d '{...}'` | o **corpo** do pedido. Aspas **simples** por fora, para as aspas duplas do JSON passarem intactas |
| `\` no fim da linha | "o comando continua na linha de baixo". Pode digitar tudo numa linha só, sem as barras |

A resposta traz `"access_token":"eyJ..."`.

### 2.4 Guardar o token numa variável e usá-lo

Copiar o token à mão é chato. Guarde-o numa **variável** do terminal:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana.paciente@email.com","senha":"Paciente123"}' \
  | sed -E 's/.*"access_token":"([^"]+)".*/\1/')
echo $TOKEN
```

- `TOKEN=$( ... )` guarda a saída do comando na variável `TOKEN`;
- o `sed` recorta só o valor do token de dentro do JSON;
- `echo $TOKEN` mostra o conteúdo. Deve começar com `eyJ`.

Agora use o token como a tela usa, no cabeçalho `Authorization`:

```bash
curl -s localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

> A variável só existe **naquele terminal**. Se abrir outro, ou fechar o VS Code, gere de novo.

### 2.4.1 O atalho dos roteiros: `tokens.sh`

Fazer o login acima para cada usuário é trabalhoso. Os roteiros usam um script que faz o login com
**os seis usuários de teste** de uma vez e guarda cada token numa variável com o nome da pessoa:

```bash
source roteiros/comum/tokens.sh
```

Ele cria `TOKEN_ANA`, `TOKEN_BRUNO`, `TOKEN_HOUSE`, `TOKEN_REC`, `TOKEN_BIO` e `TOKEN_ADMIN`, e
mostra uma linha `ok` para cada. Use **`source`** (não `bash`), para as variáveis ficarem no seu
terminal. Exemplo de uso — a Ana pedindo o exame 101:

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN_ANA" | python3 -m json.tool
```

### 2.5 Mandar caracteres especiais (aspas, espaços, `%`)

Vários testes de segurança usam aspas, espaços ou `%` no parâmetro. Na URL, eles precisam ser
**codificados** (o espaço vira `%20`, por exemplo). Deixe o curl fazer isso por você:

```bash
curl -s -G "localhost:8080/api/pacientes/busca" --data-urlencode "q=Ana Silva"
```

- `-G` diz "mande os dados na URL, como GET";
- `--data-urlencode "q=..."` codifica o valor.

No corpo JSON, uma aspa simples dentro do e-mail atrapalha as aspas simples de fora. Nesse caso,
use aspas **duplas** por fora e **escape** as duplas de dentro com `\"`:

```bash
curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d "{\"email\":\"teste'aspas@x.com\",\"senha\":\"x\"}"
```

### 2.6 Guardar a saída como evidência

Acrescente `| tee <arquivo>`: mostra na tela **e** grava no arquivo.

```bash
mkdir -p entregaveis/evidencias
curl -s -i "localhost:8080/api/pacientes/busca?q=Silva" | tee entregaveis/evidencias/fase0-busca-silva.txt
```

(O `mkdir -p` cria a pasta, se ela ainda não existir.) Mais sobre evidências em
[`05-escrever-o-dossie.md`](05-escrever-o-dossie.md).

---

## 3. Pelo banco de dados

O banco mostra **o que o sistema guarda** — inclusive o que nenhuma tela mostra.

No terminal (na raiz do repositório):

```bash
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus
```

- `docker compose -f legacy-app/docker-compose.yml` = "Docker Compose, use o arquivo
  `legacy-app/docker-compose.yml`" ([`01` §2.1](01-subir-o-vidaplus.md#21-os-comandos-do-docker-que-você-vai-ver));
- `exec db` = "execute dentro do container `db`" (o do banco);
- `mysql -uvidaplus -pvidaplus123 vidaplus` = abre o cliente do MySQL com o usuário `vidaplus`,
  a senha `vidaplus123` e o banco `vidaplus`.

> Repare: a senha do banco está escrita **no repositório** — qualquer um que o leia entra no
> banco. Na Fase 6 vocês corrigem isso (F-10, [`08`](08-segredos-fora-do-codigo.md)); **depois**
> dela, `-pvidaplus123` não entra mais, e o comando passa a ser
> `docker compose -f legacy-app/docker-compose.yml exec db sh -c 'mysql -uvidaplus -p"$MYSQL_PASSWORD" vidaplus'`.

O terminal muda para `mysql>`. Agora você digita **SQL**; cada comando termina com `;`.

```sql
SHOW TABLES;
```

Lista as tabelas.

```sql
DESCRIBE paciente;
```

Mostra as **colunas** da tabela `paciente`: nome, tipo, se aceita vazio (`NULL`).

```sql
SELECT * FROM paciente;
SELECT id, email, papel, ativo FROM usuario;
SELECT id, paciente_id, tipo, publicado FROM exame;
```

`SELECT colunas FROM tabela;` lê linhas. O `*` quer dizer "todas as colunas".

Para sair: `exit` (ou **Ctrl+D**).

**Atalho para um comando só**, sem entrar no `mysql>`:

```bash
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, email, papel FROM usuario;"
```

> Aparece o aviso `Using a password on the command line interface can be insecure`. É verdade: a
> senha fica no histórico do terminal. Repare também que você só sabe a senha porque ela **está
> escrita no `docker-compose.yml`, dentro do repositório**. Guarde isso para a Fase 6.

**Exercício:** abra também `legacy-app/db/schema.sql` no VS Code. Ele é o "projeto" do banco, com
comentários. Compare com o `DESCRIBE`.

---

## 4. Pelo código

Você não precisa saber programar em Java ou React para **seguir o caminho** de uma funcionalidade.
É como seguir uma trilha de placas.

### 4.1 Onde fica cada coisa

```
legacy-app/
├── frontend/src/
│   ├── pages/        ← as TELAS (Login.jsx, Dashboard.jsx, ExameDetalhe.jsx…)
│   ├── components/   ← pedaços de tela reaproveitados (Layout.jsx = menu do topo; Prontuario.jsx)
│   ├── services/     ← as funções que CHAMAM a API (auth.js, exame.js, paciente.js…)
│   └── config/api.js ← o endereço da API
├── backend/src/main/java/br/ifsul/vidaplus/
│   ├── controller/   ← as PORTAS DE ENTRADA da API: cada método atende um endereço
│   ├── dao/          ← quem FALA COM O BANCO (o SQL está aqui)
│   ├── security/     ← JwtUtil.java (token) e LegacyHash.java (hash da senha)
│   ├── config/       ← configurações (senha, CORS…)
│   └── entity/ e repository/ ← modelos prontos para funcionalidades que ainda não existem
├── backend/src/main/resources/application.properties ← configuração do backend
├── backend/src/test/ ← os TESTES de segurança (.feature e .java)
└── db/schema.sql e db/seed.sql ← estrutura e dados iniciais do banco
```

### 4.2 Lendo um controller (o mínimo de Java que você precisa)

Abra `ExameController.java` (**Ctrl+P** → `ExameController`). Procure estas marcações, chamadas
**anotações** (começam com `@`):

| Anotação | Significado |
|---|---|
| `@RestController` | esta classe responde pedidos HTTP |
| `@RequestMapping("/api/exames")` | todos os endereços desta classe começam com `/api/exames` |
| `@GetMapping("/{id}")` | o método logo abaixo atende `GET /api/exames/{id}` |
| `@PostMapping("/{id}/publicar")` | atende `POST /api/exames/{id}/publicar` |
| `@PathVariable long id` | pega o `{id}` do endereço |
| `@RequestParam("q") String q` | pega o parâmetro `?q=` |
| `@RequestBody Map<...> body` | pega o corpo JSON do pedido |
| `@RequestHeader("Authorization") String auth` | pega o cabeçalho `Authorization` (o token) |

E dois trechos que se repetem:

- `JwtUtil.usuarioIdDoToken(...)` — descobre **quem** está pedindo, a partir do token;
- `exameDao.porId(id)` — pede ao DAO que busque no banco.

### 4.3 Exercício: siga a trilha de "Meus exames" da Ana

1. **Tela:** `frontend/src/pages/Dashboard.jsx`. Procure (**Ctrl+F**) `listarExamesDoPaciente`.
2. **Serviço:** `frontend/src/services/exame.js`. A função `listarExamesDoPaciente` chama
   `` `${API_BASE}/exames/paciente/${pacienteId}` `` — ou seja, `GET /api/exames/paciente/1`.
3. **Controller:** `ExameController.java`, método com `@GetMapping("/paciente/{pacienteId}")`
   (chama-se `porPaciente`).
4. **DAO:** ele chama `exameDao.doPaciente(pacienteId)`. Abra `ExameDao.java` e ache o método.
5. **Banco:** o SQL é `SELECT id, tipo, publicado, criado_em FROM exame WHERE paciente_id = ?` — a
   tabela é `exame`.

Trilha completa: `Dashboard.jsx → exame.js → GET /api/exames/paciente/{id} → ExameController.porPaciente → ExameDao.doPaciente → tabela exame`.

> Você vai fazer este mesmo exercício para **cada funcionalidade do seu pacote** na Fase 0. É ele
> que dá os elementos do DFD na Fase 2a.

### 4.4 Os comentários do legado são pistas, não respostas

O código do VidaPlus tem comentários como `// FALHA: ...` e `V-03`. Eles foram deixados pelo
professor para ajudar, mas:

- **não apontam todas as falhas**;
- citar um comentário **não é evidência**: o dossiê só aceita o que você **reproduziu** (com
  curl, no navegador ou no banco).

### 4.5 Procurar no projeto inteiro

**Ctrl+Shift+F** abre a busca global. Exemplos úteis:

- `@PostMapping` — todas as ações que mudam algo;
- `papelDoToken` — onde o sistema confere o papel do usuário (repare **quantas vezes** aparece);
- `createStatement` — onde o SQL é montado na mão.

---

## 5. Pelos logs

O **log** é o diário do backend: o que ele escreve enquanto trabalha.

No terminal (na raiz do repositório):

```bash
docker compose -f legacy-app/docker-compose.yml logs backend --tail 30
```

Mostra as últimas 30 linhas. Para **acompanhar ao vivo** enquanto você usa o sistema:

```bash
docker compose -f legacy-app/docker-compose.yml logs backend -f --tail 0
```

Agora use o sistema no navegador ou com curl, e veja o que aparece. Para parar de acompanhar:
**Ctrl+C** (isso não desliga o VidaPlus).

> **Pergunta para guardar:** faça um login correto e um login errado. O que ficou registrado no
> log? Se alguém invadisse a conta de um paciente, daria para descobrir depois?

---

## 6. Checklist

- [ ] Vi, no F12, o pedido, o token no cabeçalho `Authorization` e a resposta JSON.
- [ ] Vi o que fica no Armazenamento local.
- [ ] Fiz login com curl, guardei o token em `TOKEN` e usei-o num pedido.
- [ ] Rodei `source roteiros/comum/tokens.sh` e usei o `$TOKEN_ANA`.
- [ ] Usei `-i`, `-G --data-urlencode` e `| tee`.
- [ ] Entrei no `mysql>`, rodei `SHOW TABLES`, `DESCRIBE` e `SELECT`.
- [ ] Segui a trilha de "Meus exames" da tela até a tabela.
- [ ] Acompanhei o log ao vivo com `-f`.

Próximo: o `README.md` da pasta do seu grupo.
