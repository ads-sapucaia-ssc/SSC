# 00 · Antes de começar — ferramentas e repositório

**Objetivo:** ao final, você terá o repositório do grupo no computador, aberto no VS Code, com um
terminal funcionando.

**Tempo:** 20–40 minutos na primeira vez.

> **Onde rodar.** O caminho principal é o **computador Ubuntu do laboratório**, que já tem tudo
> instalado. Quer trabalhar no seu computador de casa? Veja o
> [Apêndice — Windows em casa](#apêndice--windows-em-casa) no fim deste arquivo.

---

## 1. As quatro ferramentas que você vai usar

| Ferramenta | Para que serve | Como abrir no Ubuntu do laboratório |
|---|---|---|
| **Terminal** | onde você digita comandos | **Ctrl+Alt+T**, ou o terminal de dentro do VS Code |
| **Git** | guarda o histórico do projeto e conversa com o GitHub | comando `git` no terminal |
| **Docker** | liga o VidaPlus (banco, API e site) sem instalar Java, Node ou MySQL | comando `docker` no terminal |
| **VS Code** | editor onde você lê o código e escreve o dossiê | menu de aplicativos → "Visual Studio Code" |

E um **navegador** (Firefox ou Chrome) para usar o VidaPlus.

### 1.1 Confira se tudo está instalado

Abra o terminal (**Ctrl+Alt+T**) e digite **um comando por vez**, apertando **Enter** depois de
cada um:

```bash
git --version
```

Resposta esperada: algo como `git version 2.43.0`. O número pode ser outro.

```bash
docker --version
```

Resposta esperada: algo como `Docker version 27.3.1`.

```bash
docker compose version
```

Resposta esperada: algo como `Docker Compose version v2.29.7`. Repare: é `docker compose`
(com espaço), não `docker-compose`.

```bash
docker run --rm hello-world
```

Este baixa e roda um container de teste. Resposta esperada: um texto que começa com
**`Hello from Docker!`**.

```bash
python3 --version
```

Resposta esperada: algo como `Python 3.12.3`. Os roteiros usam o Python para **formatar** respostas
da API (`python3 -m json.tool`) e em um script de demonstração; você não vai programar em Python.

**Deu erro?**

| Mensagem | O que significa | O que fazer |
|---|---|---|
| `command not found` | a ferramenta não está instalada | avise o professor |
| `permission denied while trying to connect to the Docker daemon socket` | seu usuário não tem permissão para usar o Docker | avise o professor (o usuário precisa estar no grupo `docker`). **Não** use `sudo` como atalho: os arquivos criados ficariam com dono `root` e você não conseguiria apagá-los depois |

---

## 2. Conta no GitHub e acesso ao repositório do grupo

O repositório do grupo fica no **GitHub**, um site que guarda projetos com Git. Na turma SSC
2026/2, **ele já foi criado**: o representante de cada grupo aceitou o assignment
**"VidaPlus — SSDLC + LGPD"** no Classroom 50 e adicionou os colegas. Confira o seu acesso:

1. Entre em `github.com` com a sua conta.
2. Se você **não** é o representante, aceite os convites pendentes: abra
   `github.com/settings/organizations` (convite da organização `ads-sapucaia-ssc`) e o sino de
   **notificações** ou o seu e-mail (convite para o repositório do grupo). Clique em **Accept**.
3. Abra `github.com/ads-sapucaia-ssc` → aba **Repositories**. O repositório do seu grupo deve
   aparecer na lista. **Não aparece?** Fale com o representante do grupo: ele confere em
   **Manage collaborators** se você foi adicionado com o usuário certo
   ([`docs/08` §1](../../docs/08-setup-grupos.md), com imagens).

> ⚠️ **Não aceite o link do assignment de novo** se você não é o representante: isso criaria um
> segundo repositório, separado do grupo.

### 2.1 Crie um token de acesso (a "senha" do Git)

O GitHub **não aceita a senha do site** quando o Git pede para entrar pelo terminal. Ele pede um
**token**: uma senha comprida, gerada pelo site, que você pode apagar quando quiser.

1. No GitHub, clique na **sua foto** (canto superior direito) → **Settings**.
2. No menu da esquerda, lá embaixo: **Developer settings**.
3. **Personal access tokens** → **Tokens (classic)** → **Generate new token** →
   **Generate new token (classic)**.
4. **Note:** escreva `SSC laboratório`. **Expiration:** escolha uma data no fim do semestre.
5. Marque a caixa **`repo`** (a primeira da lista). Não marque mais nada.
6. Clique em **Generate token** (fim da página).
7. **Copie o token agora** (começa com `ghp_`). O GitHub nunca mais vai mostrá-lo. Guarde num
   lugar seguro, como o gerenciador de senhas do seu celular.

> 🔒 **Isto já é segurança.** O token dá acesso a todos os seus repositórios. Nunca o cole no
> código, no dossiê, num print ou no grupo de mensagens. Se ele vazar, apague-o em
> **Settings → Developer settings → Personal access tokens** e gere outro.

---

## 3. Configure o Git (uma vez por computador)

O Git anota **quem** fez cada mudança. Diga a ele o seu nome e o seu e-mail — use o mesmo e-mail
da sua conta do GitHub:

```bash
git config --global user.name "Seu Nome Completo"
git config --global user.email "seu.email@exemplo.com"
```

Esses comandos não mostram resposta. Para conferir:

```bash
git config --global --list
```

Deve aparecer `user.name=...` e `user.email=...`.

### 3.1 No computador do laboratório: não deixe o token gravado

Os computadores do laboratório são **compartilhados**. Configure o Git para lembrar o token só por
**duas horas** e depois esquecer:

```bash
git config --global credential.helper 'cache --timeout=7200'
```

Assim você digita o token uma vez por aula, e o próximo aluno que usar a máquina não entra no
repositório do seu grupo com a sua identidade.

> No seu computador pessoal, você pode trocar `cache --timeout=7200` por `store`, que grava o
> token para sempre (num arquivo do seu usuário).

---

## 4. Clone o repositório do grupo

**Clonar** é baixar uma cópia do repositório, com todo o histórico, para o seu computador.

1. No GitHub, abra a página do repositório do grupo.
2. Clique no botão verde **`<> Code`** → aba **HTTPS** → clique no ícone de **copiar** ao lado do
   endereço. Ele tem o formato `https://github.com/ads-sapucaia-ssc/<nome-do-repositorio>.git`.
3. No terminal, vá para a sua pasta pessoal e crie uma pasta para a disciplina:

   ```bash
   cd ~
   mkdir -p ssc
   cd ssc
   ```

   - `cd ~` = vai para a sua pasta pessoal (`~` é o atalho dela);
   - `mkdir -p ssc` = cria a pasta `ssc` (o `-p` evita erro se ela já existir);
   - `cd ssc` = entra nela.

4. Clone (cole o endereço copiado no lugar de `<endereço>`; no terminal do Ubuntu, colar é
   **Ctrl+Shift+V**):

   ```bash
   git clone <endereço>
   ```

5. O Git pede **Username**: digite o seu usuário do GitHub. Depois pede **Password**: **cole o
   token** (o terminal não mostra nada enquanto você cola — é normal) e aperte **Enter**.

Resposta esperada: linhas como `Cloning into '...'` e `Receiving objects: 100%`. Aparece uma
pasta nova com o nome do repositório.

6. Entre na pasta do repositório (troque pelo nome real):

   ```bash
   cd <nome-do-repositorio>
   ls
   ```

   O `ls` lista o conteúdo. Você deve ver: `README.md`, `docs`, `entregaveis`, `legacy-app`,
   `roteiros`, `security`.

---

## 5. Abra o projeto no VS Code

```bash
code .
```

O `.` significa "esta pasta". O VS Code abre com o projeto. Se aparecer a pergunta
**"Do you trust the authors of the files in this folder?"**, clique em **Yes, I trust the
authors** (o repositório é do seu grupo).

### 5.1 Conheça a janela

- **Explorer** (ícone de folhas, à esquerda, ou **Ctrl+Shift+E**): a árvore de pastas e arquivos.
  Clique numa pasta para abrir; clique num arquivo para lê-lo.
- **Terminal integrado:** menu **Terminal → New Terminal** (ou **Ctrl+'**). Ele abre na parte de
  baixo, **já dentro da pasta do repositório**. A partir de agora, use este terminal.
- **Pré-visualização de Markdown:** com um arquivo `.md` aberto, aperte **Ctrl+Shift+V** para ver
  o texto formatado (tabelas, títulos, imagens). É assim que os roteiros e o dossiê ficam mais
  fáceis de ler.
- **Abrir arquivo pelo nome:** **Ctrl+P** e digite parte do nome (ex.: `ExameController`).
- **Procurar um texto em todo o projeto:** **Ctrl+Shift+F**.

---

## 6. Checklist

- [ ] `git`, `docker`, `docker compose` e `hello-world` funcionaram.
- [ ] Tenho token do GitHub guardado em lugar seguro.
- [ ] `git config --global --list` mostra meu nome e e-mail.
- [ ] O repositório do grupo está clonado e aberto no VS Code.
- [ ] Sei abrir o terminal integrado e a pré-visualização de Markdown.

Próximo: [`01-subir-o-vidaplus.md`](01-subir-o-vidaplus.md).

---

## Apêndice — Windows em casa

Use isto só se quiser trabalhar no seu computador pessoal com Windows 10 ou 11.

1. **Git for Windows** (`git-scm.com/download/win`): instale com as opções padrão. Ele traz o
   **Git Bash**, um terminal igual ao do Ubuntu. **Use sempre o Git Bash** para os comandos dos
   roteiros: o PowerShell trata aspas e o comando `curl` de outro jeito, e os comandos não
   funcionariam como estão escritos.
2. **Docker Desktop** (`docker.com/products/docker-desktop`): instale, reinicie o computador e
   abra o Docker Desktop. Ele precisa estar **aberto** (ícone da baleia na bandeja) sempre que
   você for usar o VidaPlus. Se pedir para instalar ou atualizar o **WSL**, aceite.
3. **VS Code** (`code.visualstudio.com`). Para usar o Git Bash dentro dele: no terminal integrado,
   clique na setinha ao lado do **+** → **Git Bash**.
4. No passo 3.1, você pode usar `git config --global credential.helper manager`: o Windows guarda
   o token no cofre de senhas dele, e o login abre no navegador.
5. Colar no Git Bash é **Shift+Insert** ou clique com o botão direito.
6. O script [`rodar-testes.sh`](rodar-testes.sh) já sabe lidar com o Windows; rode-o pelo Git Bash.
7. **Python** (`python.org/downloads`): na primeira tela do instalador, marque **"Add python.exe to
   PATH"**. No Windows o comando costuma se chamar `python`, e não `python3`; para os comandos dos
   roteiros funcionarem como estão escritos, rode **uma vez** no Git Bash:

   ```bash
   echo "alias python3='python'" >> ~/.bashrc
   source ~/.bashrc
   python3 --version
   ```

   Esperado: `Python 3.` e o resto do número. Se aparecer uma janela da Microsoft Store, o
   Python não foi instalado com a opção "Add python.exe to PATH": reinstale marcando-a.
