# 01 · Subir o VidaPlus — Docker do zero, ligar, desligar e zerar

**Objetivo:** entender o que é o Docker (o mínimo para usar), ligar o VidaPlus no seu computador,
entrar com cada usuário de teste e saber desligar e "zerar" o sistema.

**Tempo:** 30 minutos (a primeira vez demora mais, porque o Docker baixa e monta tudo).

**Onde digitar os comandos:** no terminal do VS Code, na **raiz do repositório**. Confira:

```bash
ls
```

Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. **Todos os comandos
deste guia (e dos roteiros) são digitados nesta pasta.**

---

## 1. O que é o VidaPlus, por dentro

O VidaPlus é o sistema de uma clínica. Ele tem **três partes**:

| Parte | O que faz | Tecnologia | Endereço no seu computador |
|---|---|---|---|
| **frontend** | as telas que você vê no navegador | React (JavaScript), servido pelo programa **nginx** | `http://localhost:4200` |
| **backend** (API) | recebe os pedidos das telas, aplica as regras e consulta o banco | Java + Spring Boot | `http://localhost:8080` |
| **db** | guarda os dados (usuários, pacientes, exames…) | MySQL | `localhost:3306` |

O caminho de um clique é sempre o mesmo:

```
Navegador (tela)  ──pedido HTTP──►  API (backend)  ──consulta SQL──►  Banco (MySQL)
                  ◄──resposta JSON──                ◄──linhas──────
```

Guarde este desenho: ele é a base do **DFD** da Fase 2a.

> **`localhost`** quer dizer "este próprio computador". O número depois dos dois-pontos é a
> **porta**: cada programa "escuta" numa porta diferente, como apartamentos num mesmo prédio.

---

## 2. Docker do zero, em 5 minutos

Para rodar o VidaPlus "do jeito tradicional", você teria de instalar Java, Node, nginx e MySQL,
nas versões certas, e configurar cada um. O **Docker** evita isso.

**Analogia:** pense numa **marmita congelada**. Alguém preparou a refeição completa, com todos os
ingredientes, e congelou numa embalagem. Você não precisa saber cozinhar: só aquece e come. E pode
aquecer quantas marmitas iguais quiser.

| Termo | Na analogia | No VidaPlus |
|---|---|---|
| **Imagem** | a marmita congelada: a receita pronta e embalada, que não muda | "o backend do VidaPlus com Java e tudo o que ele precisa"; "um MySQL 8.4" |
| **Container** | a marmita **aquecida, sendo comida**: uma imagem **rodando** | os três "computadorzinhos" isolados que rodam o VidaPlus: `db`, `backend`, `frontend` |
| **Volume** | a geladeira onde fica o que sobrou | o lugar onde o MySQL guarda os dados. Se você apaga o volume, o banco volta ao original |
| **Docker Compose** | o cardápio do dia: diz quais marmitas aquecer, e como elas se ligam | o arquivo `legacy-app/docker-compose.yml` |
| **Build** | preparar e congelar a marmita | montar as imagens do backend e do frontend **a partir do código** do repositório |

Abra o `legacy-app/docker-compose.yml` no VS Code (**Ctrl+P** → `docker-compose.yml`). Você vai
reconhecer os três serviços (`db`, `backend`, `frontend`), as **portas** (`"8080:8080"` = "a porta
8080 do seu computador leva à porta 8080 do container") e — repare — **senhas escritas em texto
claro**. Isso é uma falha de propósito (Fase 6).

### 2.1 Os comandos do Docker que você vai ver

Todos começam com `docker compose -f legacy-app/docker-compose.yml`, que quer dizer: "Docker
Compose, use o cardápio que está em `legacy-app/docker-compose.yml`". Depois vem a ação:

| Comando (depois do `-f ...`) | O que faz |
|---|---|
| `up --build -d` | monta as imagens a partir do código (`--build`) e liga os containers, devolvendo o terminal (`-d`, "em segundo plano") |
| `ps` | mostra os containers e se estão ligados |
| `stop` | desliga os containers, **mantendo** os dados |
| `down -v` | apaga os containers **e o volume** (`-v`): os dados voltam ao original na próxima vez |
| `logs backend` | mostra o "diário" (log) do backend |
| `exec db mysql ...` | roda um comando **dentro** do container do banco (ex.: o cliente do MySQL) |

Você não precisa decorar: os roteiros sempre dão o comando completo.

---

## 3. Ligar o VidaPlus

O repositório traz um script que faz tudo de uma vez. Rode:

```bash
bash roteiros/comum/zerar-banco.sh
```

**O que ele faz** (abra `roteiros/comum/zerar-banco.sh` no VS Code para ler):

1. `docker compose -f legacy-app/docker-compose.yml down -v` — desliga o que estiver ligado e
   apaga o banco;
2. `docker compose -f legacy-app/docker-compose.yml up --build -d` — monta as imagens com o código
   atual e liga os três containers. O banco é criado do zero a partir de `legacy-app/db/schema.sql`
   (as tabelas) e `legacy-app/db/seed.sql` (os dados de teste);
3. fica perguntando à API, a cada 2 segundos, se ela já responde — o backend leva uns 20–30
   segundos para ficar pronto depois de ligado.

**A primeira vez demora alguns minutos** (o Docker baixa as imagens do Java, do Node e do MySQL).
Resposta esperada, no fim:

```
PRONTO: VidaPlus no ar, com o banco original. Tela: http://localhost:4200
```

Confira os containers:

```bash
docker compose -f legacy-app/docker-compose.yml ps
```

A coluna **STATUS** deve mostrar `Up` nos três (o `db` mostra `Up ... (healthy)`).

---

## 4. Entrar no sistema

Abra o navegador em **`http://localhost:4200`**. Deve aparecer a tela de login do VidaPlus.

**Usuários de teste** (estão em `legacy-app/db/seed.sql`):

| E-mail | Senha | Papel | Observação |
|---|---|---|---|
| `admin@vidaplus.local` | `Admin123` | ADMIN | gestão de usuários |
| `dra.house@vidaplus.local` | `Medico123` | MEDICO | atende Ana e Carla, **não** atende Bruno |
| `recepcao@vidaplus.local` | `Recep123` | RECEPCAO | cadastro e agenda |
| `bio@vidaplus.local` | `Bio123` | BIOQUIMICO | laboratório: publica laudos |
| `ana.paciente@email.com` | `Paciente123` | PACIENTE | paciente nº 1; exames 101 e 103 |
| `bruno.paciente@email.com` | `Paciente123` | PACIENTE | paciente nº 2; exame 102 |

A Carla (paciente nº 3, exame 104 — uma sorologia **ainda não publicada**) não tem login.

**Faça agora:** entre com **cada um** dos seis usuários, olhe o menu do topo e clique em tudo.
Para trocar de usuário, clique em **Sair** (canto superior direito). Anote, para cada papel, quais
telas aparecem. Você vai usar isso na Fase 0.

---

## 5. Os tokens dos usuários, no terminal

Muitos passos dos roteiros fazem pedidos à API "como se fosse" um usuário. Para isso, é preciso
o **token** dele (o "crachá" que o sistema dá depois do login — explicado em [`02`](02-observar-o-sistema.md) §2.4).
O script abaixo faz login com os seis usuários e guarda o token de cada um numa **variável** do
terminal:

```bash
source roteiros/comum/tokens.sh
```

Resposta esperada:

```
TOKEN_ANA ok
TOKEN_BRUNO ok
TOKEN_HOUSE ok
TOKEN_REC ok
TOKEN_BIO ok
TOKEN_ADMIN ok
```

- Use **`source`** (e não `bash`): o `source` roda o script **dentro** do seu terminal, e assim as
  variáveis ficam disponíveis nele.
- As variáveis valem **só neste terminal**. Abriu outro terminal, ou fechou o VS Code? Rode o
  `source` de novo.
- Para ver um token: `echo $TOKEN_ANA`.

---

## 6. Desligar no fim da aula

```bash
docker compose -f legacy-app/docker-compose.yml stop
```

Esperado: três pares de linhas `Container legacy-app-... Stopping` / `Stopped`. Desliga os
containers, mas **mantém** os dados. Para religar na próxima aula, mantendo os dados:

```bash
docker compose -f legacy-app/docker-compose.yml up -d
```

Esperado: linhas `Container legacy-app-... Started` (e `Healthy` no `db`). A API leva uns 20–30
segundos a mais para responder: se um `curl` logo em seguida der `Connection refused`, espere e
repita. (Ou, para religar **com o banco original**: `bash roteiros/comum/zerar-banco.sh`.)

---

## 7. "Zerar" o banco — e por que isso importa

Vários testes dos roteiros **mudam os dados** — publicar um laudo, desativar um usuário, agendar
um atendimento. Quando um roteiro disser **"zere o banco"**, rode:

```bash
bash roteiros/comum/zerar-banco.sh
source roteiros/comum/tokens.sh
```

Esperado: `PRONTO: VidaPlus no ar, com o banco original...` e seis linhas `TOKEN_... ok`.
(O segundo comando é preciso porque, depois de zerar, os tokens antigos podem não servir mais —
por exemplo, se o usuário foi desativado. Gerar de novo é sempre seguro.)

> ⚠️ **Zere sempre antes de registrar uma evidência**, para que o resultado não dependa de algo que
> você mudou antes sem lembrar.

---

## 8. Depois de mudar o código (Fase 4)

O container não percebe sozinho que o código mudou: é preciso **montar a imagem de novo**. O mesmo
script faz isso (o `--build` dele recompila o que mudou):

```bash
bash roteiros/comum/zerar-banco.sh
```

Se a compilação falhar, o script termina com `ERRO ao ligar`. Role as mensagens para cima e
procure linhas com `ERROR` e o nome de um arquivo `.java` (com o número da linha).

---

## 9. Problemas comuns

| O que aparece | O que significa | O que fazer |
|---|---|---|
| `permission denied while trying to connect to the Docker daemon socket` | seu usuário não pode usar o Docker | avise o professor (não use `sudo`) |
| `port is already allocated` ou `address already in use` | outro programa já usa a porta 3306, 8080 ou 4200 — quase sempre um VidaPlus antigo ligado em outra pasta | `docker ps` mostra o que está rodando; para desligar **todos** os containers ligados, rode `docker stop $(docker ps -q)` e depois o `zerar-banco.sh` de novo |
| `no such file or directory: legacy-app/docker-compose.yml` | o terminal não está na raiz do repositório | rode `ls`: se não aparecer `legacy-app`, abra a pasta certa no VS Code (**File → Open Folder…**) |
| `A API nao respondeu em 2 minutos` | o backend não subiu | veja o log: `docker compose -f legacy-app/docker-compose.yml logs backend --tail 40` e procure a primeira linha com `ERROR` |
| A tela de login diz "Não foi possível entrar" | o backend ainda está subindo, ou caiu | espere 30 s; se continuar, veja o log (linha acima) |
| `TOKEN_... FALHOU` | o VidaPlus não está ligado, ou aquela conta foi desativada | `bash roteiros/comum/zerar-banco.sh` e `source roteiros/comum/tokens.sh` |
| A página do navegador não muda depois de alterar o frontend | o navegador guardou a versão antiga | **Ctrl+Shift+R** |

---

## 10. Checklist

- [ ] Sei explicar imagem, container, volume e Docker Compose com a analogia da marmita.
- [ ] `bash roteiros/comum/zerar-banco.sh` terminou com `PRONTO`.
- [ ] Entrei com os seis usuários e anotei as telas de cada papel.
- [ ] `source roteiros/comum/tokens.sh` mostrou seis linhas `ok`.
- [ ] Sei desligar (`stop`) e zerar (`zerar-banco.sh`).

Próximo: [`02-observar-o-sistema.md`](02-observar-o-sistema.md).
