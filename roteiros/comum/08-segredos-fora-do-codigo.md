# Comum 08 · Segredos fora do código (F-10) — aula guiada

Este roteiro é **igual para os quatro grupos**. Vocês vão **tirar do código** a chave que assina
os tokens e as senhas do banco, **trocar os valores** e **provar** que os antigos não servem
mais. É a parte prática da Fase 6: o roteiro da fase do seu grupo diz **quando** chegar aqui
(Passo 5) e o que escrever no dossiê depois.

**Tempo:** uma aula (cerca de 90 minutos). **Quem faz:** um integrante digita (de preferência o
⚙️ DevSecOps, que lidera a Fase 6); os outros acompanham e conferem cada resultado. No fim, **outro**
integrante revisa o Pull Request na máquina dele.

> **Antes de começar:**
> - o Passo 4 da Fase 6 do seu grupo (a **análise** dos segredos) está feito;
> - a correção do P0 (Fase 4) já está na `main`;
> - o VS Code está aberto na pasta do repositório, com o terminal **na raiz** (o comando `ls`
>   mostra `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`).

---

## 1. Conceitos em 10 minutos

### 1.1 O problema, em uma frase

Hoje, **quem lê o repositório tem a chave de tudo**. A chave que assina os tokens está escrita no
`JwtUtil.java`; com ela, qualquer pessoa fabrica um token de ADMIN sem saber senha nenhuma. As
senhas do banco estão no `docker-compose.yml` e no `application.properties`. O repositório é
copiado para o computador de cada aluno, de cada professor, de cada servidor de CI…

### 1.2 Variável de ambiente

Uma **variável de ambiente** é um valor com nome que o sistema operacional entrega a um programa
quando ele **começa a rodar** — o programa não traz o valor dentro dele, **recebe** de fora.
Experimente no terminal:

```bash
echo "$HOME"
```

Aparece a sua pasta pessoal (por exemplo `/home/aluno`). `HOME` é uma variável de ambiente: o
programa `echo` não sabe onde fica a sua pasta; o sistema contou para ele.

Analogia: a senha do cofre **não** é pintada na porta do cofre (isso é o segredo no código). Ela
fica com o gerente, que a informa **na hora** a quem tem direito (isso é a variável de ambiente).

No Java, um programa lê uma variável de ambiente assim:

```java
String chave = System.getenv("JWT_SECRET");   // null, se ninguém definiu
```

### 1.3 O arquivo `.env` e o `.gitignore`

Digitar as variáveis a cada vez seria chato. O **Docker Compose** lê, sozinho, um arquivo chamado
`.env` que fica **na mesma pasta** do `docker-compose.yml` (no VidaPlus: `legacy-app/.env`). Cada
linha é `NOME=valor`.

O `.env` tem os **valores de verdade** — então ele **nunca** pode ir para o Git. Quem impede isso
é o arquivo `.gitignore` (na raiz do repositório): ele lista o que o Git deve **ignorar**. O do
VidaPlus já tem uma linha `.env`.

Para que os colegas saibam **quais** variáveis criar, existe um **modelo** sem os valores:
`legacy-app/.env.example`. O modelo **vai** para o Git; o `.env` **não**.

| Arquivo | Tem os valores? | Vai para o Git? |
|---|:-:|:-:|
| `legacy-app/.env.example` | não (só os nomes) | **sim** |
| `legacy-app/.env` | **sim** | **nunca** |

### 1.4 O caminho do segredo até o Java

```
legacy-app/.env              JWT_SECRET=4f9c...          (só no seu computador)
      │  o Docker Compose lê o .env e preenche o ${JWT_SECRET} do docker-compose.yml
      ▼
docker-compose.yml           JWT_SECRET: ${JWT_SECRET:?falta no arquivo legacy-app/.env}
      │  e entrega como variável de ambiente ao container do backend
      ▼
JwtUtil.java                 System.getenv("JWT_SECRET")
```

O `${JWT_SECRET:?mensagem}` quer dizer: "use o valor de `JWT_SECRET`; **se não existir, pare
tudo** e mostre a mensagem". Isso é **fail-secure** (falhar fechado): sem o segredo, o sistema
**não sobe** — em vez de subir com uma senha qualquer.

### 1.5 Apagar não basta: é preciso **trocar**

O Git guarda **todas** as versões de todos os arquivos. Se vocês só apagarem a chave do
`JwtUtil.java`, ela continua no **histórico** — qualquer um a recupera com `git log -p`. Por isso
a correção tem duas metades:

1. **tirar** os segredos do código (para não vazarem de novo);
2. **trocar** os valores (para que os que já vazaram **não sirvam mais**).

Vocês vão **provar** a segunda metade: um token fabricado com a chave antiga, tirada do histórico,
vai ser **recusado**.

---

## Passo 1 — Crie a branch da correção

```bash
git checkout main
git pull
git checkout -b fix/F-10-segredos
```

(`git checkout main` volta para a `main`; `git pull` traz o que os colegas já gravaram;
`git checkout -b ...` cria a branch nova e entra nela. Mais em
[`comum/03` §3](03-git-e-github.md#3-o-ciclo-completo-passo-a-passo).)

## Passo 2 — Registre o "antes"

Sem o "antes", não há como mostrar o 🔴→🟢. Ligue o VidaPlus **com o código atual** e guarde
quatro provas.

1. Ligue o VidaPlus e carregue os tokens dos usuários de teste:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   source roteiros/comum/tokens.sh
   mkdir -p entregaveis/evidencias
   ```

   Esperado: `PRONTO: VidaPlus no ar...` e seis linhas `TOKEN_... ok`.

2. **O Gitleaks** (a varredura de segredos — como ler a saída:
   [`comum/07` §4](07-ferramentas-de-seguranca.md#4-gitleaks--procura-segredos-inclusive-no-passado)):

   ```bash
   docker run --rm -v "$PWD":/repo zricethezav/gitleaks:v8.21.2 \
     detect --source /repo --config /repo/security/gitleaks/gitleaks.toml \
     --log-opts="--all -- legacy-app" --no-banner --redact -v \
     2>&1 | tee entregaveis/evidencias/fase6-f10-gitleaks-antes.txt
   ```

   Esperado, na última linha: `leaks found: 8`.

3. **A verificação automática do professor** (é a mesma que o autograding roda; ela confere se
   ainda há chave escrita no `application.properties` ou no `JwtUtil.java`):

   ```bash
   if ! grep -qE 'jwt.secret=[^$]' legacy-app/backend/src/main/resources/application.properties \
      && ! grep -qE 'SECRET *= *"' legacy-app/backend/src/main/java/br/ifsul/vidaplus/security/JwtUtil.java
   then echo "V-05: PASSOU"; else echo "V-05: FALHOU"; fi \
     | tee entregaveis/evidencias/fase6-f10-v05-antes.txt
   ```

   Esperado: `V-05: FALHOU`.

4. **O ataque: fabricar um token de ADMIN só com o código.** O script lê a chave do
   `JwtUtil.java` e monta um token dizendo que a Ana (usuário 5) é ADMIN; o `curl` usa esse token
   para pedir a lista de usuários, que só o ADMIN pode ver:

   ```bash
   FORJADO=$(bash roteiros/comum/fabricar-token.sh 5 ADMIN)
   curl -s -o /dev/null -w 'token fabricado com a chave do codigo -> lista de usuarios: %{http_code}\n' \
     localhost:8080/api/usuarios -H "Authorization: Bearer $FORJADO" \
     | tee entregaveis/evidencias/fase6-f10-token-fabricado-antes.txt
   ```

   Esperado: `Chave lida do codigo atual` e depois `... lista de usuarios: 200`. **Ninguém digitou
   senha nenhuma** — e o servidor entregou a lista.

5. **Guarde um token "legítimo" de hoje**, para testar depois da troca. Ele fica **fora** do
   repositório (na sua pasta pessoal), porque um token é uma credencial:

   ```bash
   echo "$TOKEN_ADMIN" > ~/token-admin-antigo.txt
   ```

   Esperado: nada aparece na tela — o token foi para o arquivo. Para conferir, sem mostrá-lo
   inteiro: `cut -c1-10 ~/token-admin-antigo.txt` deve mostrar `eyJhbGciOi`.

## Passo 3 — Confira que o Git vai ignorar o `.env`

Antes de criar um arquivo com segredos, confirme que ele **não** vai para o Git:

```bash
git check-ignore -v legacy-app/.env
```

Esperado: `.gitignore:23:.env	legacy-app/.env` (o número da linha pode variar). Isso quer dizer:
"a linha 23 do `.gitignore` manda ignorar esse arquivo".

> ⚠️ Se **nada** aparecer, **pare** e chame o professor: o `.env` iria para o Git.

## Passo 4 — Crie o modelo (`.env.example`) e o seu `.env`

**4a. O modelo**, que vai para o Git. No VS Code: **File → New File…**, digite o nome
`legacy-app/.env.example` e cole:

```
# Modelo do arquivo legacy-app/.env (F-10: segredos fora do codigo).
#
# Este arquivo (.env.example) VAI para o Git: ele so diz QUAIS segredos existem, sem os valores.
# O arquivo de verdade, legacy-app/.env, NAO vai para o Git (esta no .gitignore): cada integrante
# cria o seu, com valores gerados na hora. Como criar: roteiros/comum/08-segredos-fora-do-codigo.md
#
# Senha do usuario root do MySQL (so o banco usa).
MYSQL_ROOT_PASSWORD=
# Senha do usuario "vidaplus" do MySQL (o banco e o backend usam).
DB_PASSWORD=
# Chave que assina os tokens de login (o backend usa). Minimo de 32 caracteres.
JWT_SECRET=
```

Salve (**Ctrl+S**). Repare: depois do `=` **não há nada**.

**4b. O seu `.env`, com valores novos.** Em vez de inventar senhas, peça ao computador valores
**aleatórios**. Rode, de uma vez, no terminal:

```bash
cat > legacy-app/.env <<EOF
MYSQL_ROOT_PASSWORD=$(openssl rand -hex 16)
DB_PASSWORD=$(openssl rand -hex 16)
JWT_SECRET=$(openssl rand -hex 32)
EOF
```

O que aconteceu:

- `openssl rand -hex 16` sorteia 16 bytes e os escreve como 32 caracteres (`0-9a-f`). Para a chave
  do token, 32 bytes = 64 caracteres (a chave precisa de pelo menos 32);
- `cat > arquivo <<EOF ... EOF` escreve as linhas entre os dois `EOF` dentro do arquivo;
- `$( ... )` executa o comando e põe o resultado no lugar.

Confira **sem mostrar os valores na tela** (pense no colega ao lado e no projetor):

```bash
sed 's/=.*/=(escondido)/' legacy-app/.env
git status --short
```

Esperado: as três linhas com `=(escondido)`; e, no `git status`, **só**
`?? legacy-app/.env.example` — o `.env` **não aparece**, porque o Git o ignora.

> **Estes valores são só do seu computador.** Cada integrante gera os seus (Passo 13) e eles são
> diferentes em cada máquina. Tudo bem: é o mesmo que acontece numa empresa, onde o ambiente de
> cada desenvolvedor, o de testes e o de produção têm segredos diferentes.

## Passo 5 — `docker-compose.yml`: as senhas vêm do `.env`

Abra o arquivo (**Ctrl+P** → digite `docker-compose.yml` → **Enter**). São **três** mudanças.

**5a.** No serviço `db`, procure (**Ctrl+F**) `MYSQL_ROOT_PASSWORD`. Troque estas quatro linhas:

```yaml
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: vidaplus
      MYSQL_USER: vidaplus
      MYSQL_PASSWORD: vidaplus123   # V-05
```

por estas cinco:

```yaml
      # F-10: as senhas vem do arquivo legacy-app/.env (fora do Git). Sem ele, nada sobe.
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:?falta no arquivo legacy-app/.env}
      MYSQL_DATABASE: vidaplus
      MYSQL_USER: vidaplus
      MYSQL_PASSWORD: ${DB_PASSWORD:?falta no arquivo legacy-app/.env}
```

**5b.** Logo abaixo, no `healthcheck` (a verificação "o banco já está pronto?"), a senha também
estava escrita. Troque a linha:

```yaml
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-proot123"]
```

por:

```yaml
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -uroot -p\"$$MYSQL_ROOT_PASSWORD\""]
```

(O `$$` é proposital: diz ao Compose "não troque isto agora; deixe o `$MYSQL_ROOT_PASSWORD` para
ser lido **dentro** do container, na hora da verificação".)

**5c.** No serviço `backend`, procure `SPRING_DATASOURCE_PASSWORD`. Troque a linha:

```yaml
      SPRING_DATASOURCE_PASSWORD: vidaplus123
```

por estas duas:

```yaml
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:?falta no arquivo legacy-app/.env}
      JWT_SECRET: ${JWT_SECRET:?falta no arquivo legacy-app/.env}
```

Salve (**Ctrl+S**). A indentação (os espaços no começo da linha) precisa ficar **igual** à das
linhas vizinhas: no YAML, os espaços fazem parte da regra.

Confira que o Compose entende o arquivo e acha os três valores (o comando mostra o arquivo
"resolvido"; o `grep -c` só **conta** as linhas, sem mostrar os valores):

```bash
docker compose -f legacy-app/docker-compose.yml config | grep -cE 'MYSQL_ROOT_PASSWORD|DB_PASSWORD|MYSQL_PASSWORD|SPRING_DATASOURCE_PASSWORD|JWT_SECRET'
```

Esperado: um número (4 ou mais), **sem** mensagem de erro. Se aparecer `required variable ... is
missing a value`, o `.env` do Passo 4b não foi criado na pasta `legacy-app/`.

## Passo 6 — `application.properties`: nenhuma senha no arquivo

Abra **Ctrl+P** → `main/resources/application.properties` (atenção: o de `main`, **não** o de
`test`). São **duas** mudanças.

**6a.** Troque a linha:

```properties
spring.datasource.password=vidaplus123
```

por:

```properties
# F-10: a senha vem do ambiente (variavel SPRING_DATASOURCE_PASSWORD, que o docker-compose
# preenche com o DB_PASSWORD do arquivo legacy-app/.env). Sem ela, a aplicacao nao sobe.
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

`${...}` no Spring quer dizer "busque este valor no ambiente". Se a variável não existir, o
Spring **para** na hora de subir, com a mensagem `Could not resolve placeholder` — fail-secure de
novo.

**6b.** **Apague** estas três linhas (é uma cópia da chave do token que ninguém usava — mas que
vazava do mesmo jeito):

```properties
# V-05: segredo do JWT fixo no arquivo
vidaplus.jwt.secret=vidaplus-secret-123

```

Salve (**Ctrl+S**).

> O `application.properties` de **teste** (`src/test/resources`) continua com uma senha e uma
> chave **só de teste**: elas valem para um banco que existe por alguns minutos, dentro do
> pipeline, sem dado real. Isso é um **risco aceito** — e vocês o registram no dossiê com essa
> justificativa.

## Passo 7 — `JwtUtil.java`: a chave vem do ambiente

Abra **Ctrl+P** → `JwtUtil.java`. São **duas** mudanças.

**7a.** No topo, logo abaixo da linha `import javax.crypto.SecretKey;`, acrescente:

```java
import java.nio.charset.StandardCharsets;
```

**7b.** Troque estas três linhas:

```java
    // V-05: segredo hardcoded (>=256 bits apenas para o HS256 nao recusar a chave)
    private static final String SECRET = "vidaplus-secret-123-inseguro-de-proposito-ssc!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
```

por:

```java
    // F-10: a chave vem do ambiente (variavel JWT_SECRET, definida no legacy-app/.env), nunca do codigo.
    private static final SecretKey KEY = carregarChave();

    private static SecretKey carregarChave() {
        String chave = System.getenv("JWT_SECRET");
        if (chave == null || chave.length() < 32) {
            // fail-secure: sem uma chave forte, nenhum token e emitido nem aceito
            throw new IllegalStateException(
                    "JWT_SECRET ausente ou curta (minimo 32 caracteres): defina no arquivo legacy-app/.env");
        }
        return Keys.hmacShaKeyFor(chave.getBytes(StandardCharsets.UTF_8));
    }
```

Salve (**Ctrl+S**). O que mudou, linha a linha:

- **sumiu** a constante `SECRET` com o texto da chave — o segredo não está mais no código;
- `carregarChave()` pede a chave ao ambiente (`System.getenv`);
- se ela **não existir** ou for **curta demais**, o método **lança um erro** em vez de seguir com
  uma chave fraca: sem chave, nenhum token é emitido e nenhum é aceito (fail-secure);
- o resto do arquivo (`gerarToken`, `usuarioIdDoToken`, `papelDoToken`) **não muda**: continua
  usando `KEY`.

## Passo 8 — Religue e confira que o uso legítimo continua funcionando

1. Religue o VidaPlus com o código novo. O banco é **recriado** com as senhas novas do `.env`
   (por isso o `zerar-banco.sh` apaga o banco antes):

   ```bash
   bash roteiros/comum/zerar-banco.sh
   ```

   Esperado: `PRONTO: VidaPlus no ar...`. Se terminar com `ERRO ao ligar`, veja a tabela
   **Deu errado?** no fim deste roteiro.
2. Os tokens **novos** (assinados com a chave nova):

   ```bash
   source roteiros/comum/tokens.sh
   curl -s -o /dev/null -w 'admin com token novo -> lista de usuarios: %{http_code}\n' \
     localhost:8080/api/usuarios -H "Authorization: Bearer $TOKEN_ADMIN"
   ```

   Esperado: seis `TOKEN_... ok` e `... lista de usuarios: 200`.
3. Pela tela: abra `http://localhost:4200`, entre como `ana.paciente@email.com` / `Paciente123` e
   abra um exame. Tudo como antes.
4. **Abrir o banco agora é diferente.** A senha antiga não entra mais:

   ```bash
   docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT 1;"
   ```

   Esperado: `ERROR 1045 (28000): Access denied`. A forma nova lê a senha **de dentro** do
   container do banco (que a recebeu do `.env`), sem você precisar digitá-la:

   ```bash
   docker compose -f legacy-app/docker-compose.yml exec db sh -c 'mysql -uvidaplus -p"$MYSQL_PASSWORD" vidaplus -e "SELECT COUNT(*) AS usuarios FROM usuario;"'
   ```

   Esperado: `usuarios` e o número `6`. **Daqui em diante, use sempre esta forma** quando um
   roteiro mandar abrir o banco: troque `mysql -uvidaplus -pvidaplus123 vidaplus` por
   `sh -c 'mysql -uvidaplus -p"$MYSQL_PASSWORD" vidaplus ...'`.

## Passo 9 — Prove o "depois"

1. **O token antigo** (do Passo 2.5, assinado com a chave antiga) **não vale mais**:

   ```bash
   curl -s -o /dev/null -w 'admin com token ANTIGO -> lista de usuarios: %{http_code}\n' \
     localhost:8080/api/usuarios -H "Authorization: Bearer $(cat ~/token-admin-antigo.txt)" \
     | tee entregaveis/evidencias/fase6-f10-token-antigo-depois.txt
   rm ~/token-admin-antigo.txt
   ```

   Esperado: **não** é 200. Aparece **500** (o erro "nao autenticado" do legado; o certo seria 401,
   mas a resposta de erro é assunto da F-16) ou **401**, se o grupo já trocou essa resposta na
   Fase 4 (o G3 trocou, com o `Papeis.exigir`). Os dois querem dizer o que importa aqui: o pedido
   **não foi atendido**. Como os tokens do VidaPlus **não expiram** (F-11), trocar a chave é a
   **única** forma de invalidar todos os que já foram emitidos — inclusive os que vazaram.

2. **O ataque de novo.** Agora a chave não está no código; o script faz o que um atacante faria:
   procura a chave **antiga** no **histórico do Git**:

   ```bash
   FORJADO=$(bash roteiros/comum/fabricar-token.sh 5 ADMIN)
   curl -s -o /dev/null -w 'token fabricado com a chave ANTIGA (historico) -> lista de usuarios: %{http_code}\n' \
     localhost:8080/api/usuarios -H "Authorization: Bearer $FORJADO" \
     | tee entregaveis/evidencias/fase6-f10-token-fabricado-depois.txt
   ```

   Esperado: a mensagem `A chave nao esta mais no codigo, mas o historico do Git guarda a ANTIGA`
   e um código que **não** é 200. **Leia de novo a seção 1.5**: o atacante **achou** a chave (no
   histórico) — e ela não serviu, porque vocês a **trocaram**. Se vocês só tivessem apagado a
   linha e colocado no `.env` o **mesmo** valor antigo, este token teria dado 200.

3. **Sem o `.env`, nada sobe** (fail-secure). Tire o `.env` do lugar por um instante, tente
   religar e devolva:

   ```bash
   mv legacy-app/.env legacy-app/.env.guardado
   bash roteiros/comum/zerar-banco.sh 2>&1 | tee entregaveis/evidencias/fase6-f10-sem-env.txt
   docker compose -f legacy-app/docker-compose.yml config 2>&1 | tail -1 | tee -a entregaveis/evidencias/fase6-f10-sem-env.txt
   mv legacy-app/.env.guardado legacy-app/.env
   ```

   Esperado: `Falta o arquivo legacy-app/.env...` e
   `required variable ... is missing a value: falta no arquivo legacy-app/.env`. O VidaPlus que
   já estava no ar **continua** no ar (o script para antes de desligar).

4. **Os testes de segurança** continuam como estavam (a correção não pode quebrar nada):

   ```bash
   bash roteiros/comum/rodar-testes.sh 2>&1 | tee entregaveis/evidencias/fase6-f10-testes-depois.txt
   ```

   O resultado geral pode continuar **vermelho** por causa dos P0 dos **outros** pacotes (no
   repositório de vocês, só o P0 de vocês foi corrigido). Confira que o `login_valido_funciona`
   **não** está entre as falhas e que o cenário do **seu** P0 continua verde. (O script entrega aos
   testes a senha nova do banco e a chave do `.env` — ele lê o arquivo sozinho.)

5. **A verificação automática do professor:**

   ```bash
   if ! grep -qE 'jwt.secret=[^$]' legacy-app/backend/src/main/resources/application.properties \
      && ! grep -qE 'SECRET *= *"' legacy-app/backend/src/main/java/br/ifsul/vidaplus/security/JwtUtil.java
   then echo "V-05: PASSOU"; else echo "V-05: FALHOU"; fi \
     | tee entregaveis/evidencias/fase6-f10-v05-depois.txt
   ```

   Esperado: `V-05: PASSOU`.

## Passo 10 — Escreva o requisito (as duas formas)

Como todo requisito de segurança do projeto, este também tem duas formas
([`comum/04`](04-testes-e-pipeline.md)):

1. **A forma "deve" (shall), no dossiê** — vocês a escrevem na §8 (o roteiro da Fase 6 do grupo
   diz onde):

   > **REQ-SEGREDO-01** — O sistema **deve** obter senhas e chaves do ambiente de execução, e
   > **nunca** do código-fonte ou do repositório; sem elas, **não deve** iniciar. Segredos que já
   > estiveram no repositório **devem** ser trocados. (LGPD art. 46; ASVS V2.10.)

2. **A forma Gherkin, num `.feature`.** No VS Code: **File → New File…** e digite o nome
   `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/segredos-analise.feature`.
   Cole:

   ```gherkin
   # language: pt
   @F-10 @REQ-SEGREDO-01 @P1 @lgpd-art46 @asvs-V2.10 @analise
   Funcionalidade: F-10 · Segredos fora do código (REQ-SEGREDO-01)
     Declaração shall (agnóstica): ver dossiê §8 — REQ-SEGREDO-01.
     Base legal: LGPD art. 46.
     # Verificado por FERRAMENTA (gate do Gitleaks e autograding V-05) e pelos comandos do
     # roteiro comum/08 (Passo 9) - nao por um teste do Spring. Por isso @analise: nao roda
     # no gate de testes.

     Cenário: token assinado com a chave antiga é recusado
       Dado que a chave que assina os tokens foi trocada
       E um token de ADMIN assinado com a chave antiga, recuperada do histórico do Git
       Quando faço GET em "/api/usuarios" com esse token
       Então o pedido não é atendido

     Cenário: sem os segredos, o sistema não inicia
       Dado que o arquivo legacy-app/.env não existe
       Quando o ambiente é iniciado
       Então nenhum serviço sobe
       E a mensagem diz qual segredo falta
   ```

   Salve (**Ctrl+S**).

**Por que `@analise`, se a correção foi feita?** A tag `@analise` diz "este arquivo **não** roda
no gate de testes". Aqui a verificação não é um teste do Spring: é feita por **ferramenta** (o
Gitleaks e o V-05, que rodam no pipeline a cada push) e pelos comandos do Passo 9, cujas saídas
estão nas evidências. Na matriz de rastreabilidade (Fase 8), a coluna "Teste" desta linha diz
exatamente isso. Nem todo requisito vira um cenário executável — o importante é dizer **como**
ele é verificado.

## Passo 11 — Grave a correção e trate o histórico no Gitleaks

O Gitleaks lê os **commits**, então primeiro grave a correção num commit (ainda **sem** enviar).

1. Confira o que vai entrar e grave:

   ```bash
   git status --short
   ```

   Esperado: `M` nos três arquivos (`docker-compose.yml`, `application.properties`,
   `JwtUtil.java`), `??` no `legacy-app/.env.example`, no `segredos-analise.feature` e nas
   evidências. **O `legacy-app/.env` não
   pode aparecer.** Se aparecer, **pare** e volte ao Passo 3.

   ```bash
   git add legacy-app/.env.example legacy-app/docker-compose.yml \
     legacy-app/backend/src/main/resources/application.properties \
     legacy-app/backend/src/main/java/br/ifsul/vidaplus/security/JwtUtil.java \
     legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/segredos-analise.feature
   git commit -m "fix(F-10): segredos fora do codigo (variaveis de ambiente) e valores trocados"
   ```

2. Rode o Gitleaks de novo:

   ```bash
   docker run --rm -v "$PWD":/repo zricethezav/gitleaks:v8.21.2 \
     detect --source /repo --config /repo/security/gitleaks/gitleaks.toml \
     --log-opts="--all -- legacy-app" --no-banner --redact -v \
     2>&1 | tee entregaveis/evidencias/fase6-f10-gitleaks-depois.txt
   ```

   Ainda `leaks found: 8`! Abra o arquivo de evidência e olhe o campo **`Commit`** de cada achado:
   é sempre o **primeiro** commit do repositório. O commit de vocês **não** trouxe nenhum segredo
   novo — os 8 são do **histórico** (seção 1.5). Eles nunca vão sumir sozinhos.

3. **A lista de exceções.** Agora que os valores foram **trocados**, aqueles 8 achados apontam para
   segredos que **não abrem mais nada** (ou são de teste — risco aceito). O jeito profissional de
   registrar isso é o arquivo `.gitleaksignore`, na **raiz** do repositório: cada linha é a
   "impressão digital" (`Fingerprint`) de um achado que foi analisado e resolvido. Crie o arquivo
   com as impressões digitais de hoje:

   ```bash
   {
     echo "# Excecoes do Gitleaks (F-10). Cada linha e a impressao digital de um achado do HISTORICO:"
     echo "# o valor foi TROCADO no commit da correcao (agora vem do legacy-app/.env) ou e de teste"
     echo "# (risco aceito, ver o dossie §8). Segredo NOVO nao entra aqui: tira do codigo e troca."
     docker run --rm -v "$PWD":/repo zricethezav/gitleaks:v8.21.2 \
       detect --source /repo --config /repo/security/gitleaks/gitleaks.toml \
       --log-opts="--all -- legacy-app" --no-banner --redact \
       --report-format json --report-path /dev/stdout 2>/dev/null \
       | grep -o '"Fingerprint": *"[^"]*"' | cut -d'"' -f4
   } > .gitleaksignore
   cat .gitleaksignore
   ```

   Esperado: as três linhas de comentário (começam com `#`) e **8** linhas no formato
   `commit:arquivo:regra:linha`.

4. O Gitleaks pela última vez:

   ```bash
   docker run --rm -v "$PWD":/repo zricethezav/gitleaks:v8.21.2 \
     detect --source /repo --config /repo/security/gitleaks/gitleaks.toml \
     --log-opts="--all -- legacy-app" --no-banner --redact -v \
     2>&1 | tee entregaveis/evidencias/fase6-f10-gitleaks-final.txt
   ```

   Esperado: `no leaks found`. 🔴 → 🟢

> **Isto não é "esconder o problema".** A exceção vale só para aqueles 8 achados, **daquele**
> commit. Se alguém gravar uma senha nova amanhã, em qualquer arquivo do `legacy-app`, o Gitleaks
> acusa de novo — e o gate fica vermelho. E cada exceção tem uma justificativa: o valor foi trocado
> ou é de teste.

## Passo 12 — Envie, abra o PR e peça a revisão

1. Grave a lista de exceções e as evidências, e envie:

   ```bash
   git add .gitleaksignore entregaveis/evidencias
   git status --short
   git commit -m "fix(F-10): excecoes do Gitleaks para o historico e evidencias"
   git push -u origin fix/F-10-segredos
   ```

   No `git status --short`, de novo: **o `legacy-app/.env` não pode aparecer.**

2. Abra o PR ([`comum/03` §3, passo 7](03-git-e-github.md#passo-7--abra-o-pull-request)), com
   **base** `main` e **compare** `fix/F-10-segredos`. Título: "F-10: segredos fora do código". Na
   descrição:
   - **o que muda:** chave do token e senhas do banco saem do código e passam a vir do
     `legacy-app/.env` (fora do Git); valores trocados; sem `.env`, nada sobe;
   - **como testar:** criar o próprio `.env` (Passo 4b deste roteiro) e rodar os Passos 8 e 9;
   - **o que não fazer:** **nunca** colar o conteúdo do `.env` no PR, no chat ou no dossiê.

3. **Quem revisa** (outro integrante, na máquina **dele**):

   ```bash
   git fetch
   git checkout fix/F-10-segredos
   ```

   e depois **cria o próprio `.env`** com o comando do Passo 4b (o `.env` do colega **não** veio
   pelo Git — e esse é o ponto). Em seguida roda os Passos 8 e 9 (pode pular o 9.1, que precisa do
   token antigo de quem fez). Usa o checklist do
   [`docs/07`](../../docs/07-rubrica-avaliacao.md#checklist-de-secure-code-review-para-os-prs-da-poc):
   o item que mais importa aqui é "sem segredo no código; config por ambiente". Se tudo deu
   certo: **Review changes → Approve → Submit review**.

4. Com a aprovação: **Merge pull request → Confirm merge → Delete branch**.

## Passo 13 — Depois do merge: todos criam o seu `.env` e conferem o pipeline

1. **Cada integrante**, no computador dele:

   ```bash
   git checkout main
   git pull
   ```

   Quem ainda não tem `legacy-app/.env` cria o seu com o comando do Passo 4b e roda
   `bash roteiros/comum/zerar-banco.sh`. A partir de agora, **sem o `.env` o VidaPlus não liga**
   — o `zerar-banco.sh` avisa e diz o que fazer. Isso vale também para o computador de casa.

2. **O pipeline** (na aba **Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` →
   execução **"Merge pull request #… fix/F-10-segredos"**):
   - job **Gate - Secret Scanning (Gitleaks)**: agora **verde** — na etapa **Gitleaks**, a última
     linha diz `no leaks found`. Tire um print: é a evidência do 🔴→🟢 no pipeline;
   - **como o pipeline consegue subir o VidaPlus sem o `.env` de ninguém?** Abra o job
     **Gate - DAST autenticado (OWASP ZAP)** e clique na etapa **Segredos descartaveis deste
     ambiente (legacy-app/.env)**: a cada execução, o pipeline **sorteia** senhas e uma chave novas,
     usa e joga fora. Nos jobs de teste, a etapa **Chave do token so para este teste** faz o mesmo.
     Nenhum valor fica gravado no repositório. (No log, onde estaria o valor aparece `***`: o
     GitHub esconde.)
3. **O autograding:** na aba **Actions**, a execução mais recente de **Autograding (Classroom)** →
   o teste **"F-10 / V-05 JWT via variavel de ambiente"** agora pontua.

---

## Deu errado?

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| `zerar-banco.sh` diz `Falta o arquivo legacy-app/.env` | o `.env` não existe (ou foi criado em outra pasta) | rode o comando do Passo 4b **na raiz** do repositório; confira com `ls -a legacy-app` |
| `required variable ... is missing a value` | falta uma das três linhas no `.env`, ou o nome está diferente | `sed 's/=.*/=(escondido)/' legacy-app/.env` deve mostrar `MYSQL_ROOT_PASSWORD`, `DB_PASSWORD`, `JWT_SECRET` |
| `ERRO ao ligar` e `cannot find symbol ... StandardCharsets` | faltou o `import` do Passo 7a | acrescente a linha e rode `zerar-banco.sh` de novo |
| O backend não responde; no log (`docker compose -f legacy-app/docker-compose.yml logs backend --tail 40`) aparece `Could not resolve placeholder 'SPRING_DATASOURCE_PASSWORD'` | o Passo 5c não foi feito (o backend não recebeu a senha) | refaça o 5c |
| `Access denied for user 'vidaplus'` no log do backend | o banco foi criado **antes** com outra senha | rode `bash roteiros/comum/zerar-banco.sh` (ele apaga e recria o banco) |
| O login devolve 500 e o log diz `JWT_SECRET ausente ou curta` | a linha `JWT_SECRET` do Passo 5c faltou, ou o valor do `.env` tem menos de 32 caracteres | refaça o 5c; gere a chave de novo com `openssl rand -hex 32` |
| `yaml: line ...` ao rodar o Compose | indentação errada no `docker-compose.yml` | compare os espaços com as linhas vizinhas |
| `openssl: command not found` (Windows) | o terminal não é o Git Bash | abra o **Git Bash** ([`comum/00`](00-antes-de-comecar.md)) |
| O `.env` apareceu no `git status` | o `.gitignore` foi alterado | **não** faça commit; chame o professor |
| Fiz commit do `.env` por engano | o segredo foi para o histórico | **não** basta apagar: gere valores novos (Passo 4b), zere o banco e avise o professor. O Gitleaks do pipeline vai acusar |

## Confira antes de seguir

- [ ] Tenho as provas do **antes**: Gitleaks com 8, V-05 falhando, token fabricado com 200.
- [ ] O `.env` **não** está no Git (`git check-ignore` confirmou) e o `.env.example` está.
- [ ] Depois da correção: uso legítimo funcionando, token antigo recusado, token fabricado com a
      chave do histórico recusado, sem `.env` nada sobe, testes como antes, V-05 passando.
- [ ] O REQ-SEGREDO-01 tem as duas formas: "deve" no dossiê e o `segredos-analise.feature`.
- [ ] Sei explicar por que **apagar** a chave não bastava e por que os 8 achados do histórico
      viraram exceção só **depois** de trocar os valores.
- [ ] O PR foi revisado por outro integrante, que criou o **próprio** `.env`.
- [ ] Vi no pipeline o Gitleaks verde e de onde vêm os segredos do pipeline.
