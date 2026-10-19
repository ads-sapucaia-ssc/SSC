# G4 · Fase 0 — Inventário e classificação de dados

> **Seção do dossiê:** §1 · **Lidera:** 🛡️ Segurança e 📋 DPO · **Aula:** 1 · **Semana:** 1
> **Antes:** faça o **Passo 0**, logo abaixo.

## Como usar este roteiro

Cada passo tem duas partes:

1. **🔎 Como o Grupo 5 fez** — o que o grupo do dossiê de exemplo
   ([`docs/12`](../../docs/12-exemplo-dossie-preenchido.md)) executou: o comando, o que apareceu e
   o que escreveu. Leia para entender **o raciocínio**.
2. **▶ Agora no G4** — o mesmo passo, nas funcionalidades do **seu** pacote. É isto que você faz.

O Grupo 5 trabalhou a busca de pacientes e o prontuário; vocês trabalham **os canais por onde o
laudo vaza** e **o fim da vida do dado**.

## O que você entrega no fim

Na §1 do `DOSSIE.md`: os **fluxos críticos** (3 a 5) e a **tabela de ativos** com nível,
atributo e quem demanda. A coluna **Base legal** fica para a Fase 1.

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
   git checkout -b dossie/fase-0
   git push -u origin dossie/fase-0
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-0
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

- **Ativo:** tudo o que tem valor — um dado, um arquivo, um segredo, um registro — e também
  **uma informação sobre o sistema** (a mensagem de erro que revela o nome de uma tabela ajuda o
  atacante).
- **Fluxo crítico:** um caminho por onde passa um ativo importante.
- **Canal de vazamento:** qualquer caminho por onde o ativo sai do sistema — inclusive os que
  ninguém desenhou para isso.
- **Nível:** **Crítico → Confidencial → Interno → Público** (vale o mais alto).
- **Atributo prioritário:** **C**onfidencialidade, **I**ntegridade, **D**isponibilidade,
  **Autenticidade**, **Não-repúdio**.

Crie um rascunho: `entregaveis/evidencias/fase0-anotacoes.txt`.

---

## Passo 1 — Percorra as telas com cada papel

> 🔎 **Como o Grupo 5 fez:** entrou com os seis usuários e anotou as telas de cada papel; o centro
> do pacote dele era a busca e o prontuário.

**▶ Agora no G4:** entre com os seis usuários e anote **todos os lugares onde aparece um laudo ou
um exame**: a lista "Meus exames", a tela do exame, o botão **Baixar PDF**, a Fila de Laudos, a
ficha do paciente. Para cada um, anote **o endereço** (a URL na barra do navegador). Abra um
laudo e clique em **Baixar PDF**: que endereço abre na nova aba?

---

## Passo 2 — Siga a trilha de cada funcionalidade

> 🔎 **Como o Grupo 5 fez:** seguiu a trilha da busca:
> `BuscaPacientes.jsx → services/paciente.js (buscar) → GET /api/pacientes/busca?q= → PacienteController.buscar → PacienteDao.buscar → tabela paciente`.

**▶ Agora no G4:** confira as trilhas do [`README` do G4](README.md#3-onde-cada-funcionalidade-está-no-vidaplus):

1. **O laudo pela API:** `ExameDetalhe.jsx` → `services/exame.js` → `ExameController` →
   `ExameDao` → `exame`.
2. **O laudo pelo arquivo:** o botão aponta para `/laudos/laudo-{id}.pdf`. Abra
   `frontend/nginx.conf`: quem serve esse arquivo? Abra a pasta `frontend/public/laudos/`: quais
   arquivos há? O pedido passa pela API, onde ficaria alguma verificação?
3. **As mensagens de erro (F-16):** abra `backend/src/main/resources/application.properties` e
   leia as linhas que começam com `server.error` e o comentário `V-11` acima delas. Depois procure
   (**Ctrl+Shift+F**) por `throw new RuntimeException`: quantas há, e o que elas carregam (veja
   o `e.getMessage()` em alguns DAOs)?
4. **O site (F-20):** abra `frontend/index.html` e `frontend/nginx.conf`. O que o servidor do site
   faz quando alguém pede um endereço que não existe? (Leia a linha `try_files`.)
5. **A notificação (F-18):** procure (**Ctrl+Shift+F**) `email`, `sms`, `notific`. Existe algum
   código que envie mensagem? (Não existe: anote isso.)
6. **O fim da vida do dado (F-06):** procure `DELETE FROM`, `anonim`, `elimin`. O que o
   `UsuarioDao.alternarAtivo` faz com os dados da pessoa?

---

## Passo 3 — Observe a funcionalidade principal por dentro: os canais

> 🔎 **Como o Grupo 5 fez:** com o **F12 → Rede** aberto, viu que a busca devolvia religião e
> renda; repetiu pelo terminal **sem token** e recebeu o cadastro completo. Escreveu: *"A busca não
> pede login e devolve CPF, religião e renda."*

**▶ Agora no G4** — teste **cada canal**, no terminal. Os blocos abaixo carregam os tokens dos
usuários de teste (o da Ana fica em `TOKEN_ANA`) com `source roteiros/comum/tokens.sh`.

**Canal 1 — a API (o caminho óbvio).**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s -i localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_ANA" \
  | tee entregaveis/evidencias/fase0-canal-api.txt
```

O exame 102 é do Bruno. O que a Ana recebeu?

**Canal 2 — o arquivo direto, sem login nenhum.**

```bash
for ID in 101 102 103 104; do
  curl -s -o /dev/null -w "laudo-$ID.pdf -> %{http_code} %{content_type}\n" localhost:4200/laudos/laudo-$ID.pdf
done | tee entregaveis/evidencias/fase0-canal-arquivo.txt
```

Abra `http://localhost:4200/laudos/laudo-104.pdf` numa **janela anônima** (sem login). O que está
escrito? Ele foi publicado?

**Canal 3 — a mensagem de erro.** Peça coisas que **dão errado** e leia a resposta inteira:

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s localhost:8080/api/exames/999/pdf -H "Authorization: Bearer $TOKEN_ANA" | head -c 600; echo
curl -s localhost:8080/api/exames/101 | head -c 600; echo
curl -s -G localhost:8080/api/pacientes/busca --data-urlencode "q='" | head -c 600; echo
```

O primeiro pede o PDF de um exame que não existe; o segundo, um exame **sem login**; o terceiro
manda uma aspa na busca. Em cada resposta, procure `exception`, `trace` e `message`. O que elas
contam sobre o **funcionamento interno** do sistema? Repare no terceiro: a mensagem mostra **o
comando SQL**. Que ajuda isso dá a um atacante?

Guarde as três saídas: `fase0-canal-erro.txt`.

---

## Passo 4 — Olhe no banco o que o sistema guarda — e há quanto tempo

> 🔎 **Como o Grupo 5 fez:** `DESCRIBE paciente;` achou religião, estado civil e renda; procurou no
> código quem usava e escreveu *"coletados, mas nenhum fluxo usa"*.

**▶ Agora no G4:**

```bash
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "DESCRIBE paciente;"
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, nome, ativo, criado_em FROM paciente;"
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, paciente_id, tipo, publicado, criado_em FROM exame;"
```

Para a F-06, pense no **ciclo de vida**:

1. Que colunas dizem **quando** um dado nasceu? Existe alguma que diga **até quando** ele deve
   ficar?
2. A coluna `ativo`: leia o comentário dela no `db/schema.sql`. "Desativar" um paciente apaga
   alguma coisa?
3. Que tabelas guardam dados de um paciente? (Procure as colunas `paciente_id`.) Se um paciente
   pedisse para ser "apagado", quantos lugares teriam de ser tratados? E os **arquivos** PDF?
4. Os números dos exames são **sequenciais**. Isso importa para os canais do Passo 3?

---

## Passo 5 — Olhe o "crachá" da sessão

> 🔎 **Como o Grupo 5 fez:** registrou o token no `localStorage` como ativo: *"não expira: quem
> copia o token usa a conta"*.

**▶ Agora no G4:** `echo "$TOKEN_ANA" | cut -d. -f2 | base64 -d; echo`. O token diz quem é a Ana,
mas **não** diz que ela é a paciente 1 (`SELECT id, email, paciente_id FROM usuario;`). Guarde para
a Fase 4. E pense no canal: onde o token fica guardado no navegador, e quem mais consegue lê-lo?

---

## Passo 6 — Analise as funcionalidades que ainda não têm código

> 🔎 **Como o Grupo 5 fez:** testou a nota clínica **sem token** e recebeu a nota da Ana: *"a API
> entrega a nota sem login"*.

**▶ Agora no G4** — F-18 e F-20 não têm código, mas têm **ativos**:

1. **Notificação (F-18).** O catálogo diz: "link direto ao laudo, sem autenticação". Escreva, no
   rascunho, como seria a mensagem hoje, com o link **real** do botão Baixar PDF. Ex.: *"Olá, Ana!
   Seu exame de sorologia está pronto: http://.../laudos/laudo-104.pdf"*. Agora responda: quem
   mais lê essa mensagem (o provedor de e-mail, a operadora de SMS, quem olha a tela de bloqueio do
   celular, quem recebe um encaminhamento)? Com o link, o que essa pessoa consegue? Que **dado** já
   está no **texto**, mesmo sem clicar?
2. **Site institucional (F-20).** O "site" é o que o nginx serve. Veja os cabeçalhos:
   `curl -s -I localhost:4200/`. E pergunte: quem consegue **trocar** os arquivos da pasta
   `public/` ou o `index.html`? (Pense no repositório e no pipeline.) Se alguém trocasse a página
   inicial por uma falsa pedindo a senha, como a clínica perceberia?
3. Um teste curioso: `curl -s -o /dev/null -w '%{http_code} %{content_type}\n' localhost:4200/laudos/laudo-105.pdf`.
   O laudo 105 não existe. O que o servidor respondeu? Compare com o tipo de conteúdo do
   `laudo-104.pdf`. Guarde para a Fase 2b.

---

## Passo 7 — Procure o que deveria existir e não existe

> 🔎 **Como o Grupo 5 fez:** procurou `DELETE FROM` e "anonim" no código — nada. Escreveu como
> fluxo crítico: *"(Ausente) Fim de vida do dado — nenhum dado de paciente é descartado ou
> anonimizado, nem de quem já deixou a clínica."*

**▶ Agora no G4:** o "fim de vida" é **a sua** F-06. O Grupo 5 constatou a ausência; vocês vão
além e anotam **o que a lei exige** que falta: um **prazo** de guarda definido por tipo de dado,
uma **rotina** de eliminação, um jeito de o titular **pedir** a eliminação (abra
`entity/SolicitacaoTitular.java`). E as ausências dos canais: nenhuma mensagem de erro genérica;
nenhum cabeçalho de segurança no site.

---

## Passo 8 — Procure os segredos do sistema

> 🔎 **Como o Grupo 5 fez:** viu a senha do banco e a chave do token em `application.properties` e
> `docker-compose.yml`; criou a linha *"Segredos do sistema"*, base legal *"— (não é dado
> pessoal)"*.

**▶ Agora no G4:** abra os três arquivos (**Ctrl+P** e digite o nome): `application.properties`,
`docker-compose.yml` e `JwtUtil.java`. Em cada um, procure (**Ctrl+F**) as palavras `password`,
`senha`, `secret` e `SECRET`. Para o G4, repare que **o repositório é um canal de vazamento**:
quem o lê leva a senha do banco — e o banco tem todos os laudos. Anote **o que** e **onde**, sem
copiar valores.

---

## Passo 9 — Monte a §1 do dossiê

> 🔎 **Como o Grupo 5 fez:** 5 fluxos críticos e 9 ativos; cada observação diz **o que foi
> visto**. Veja: [`docs/12` §1](../../docs/12-exemplo-dossie-preenchido.md#1-fase-0--inventário--classificação-de-dados).

**▶ Agora no G4:**

**Fluxos críticos** — 3 a 5 (ex.: laudo pela API; laudo pelo arquivo; resposta de erro;
notificação (planejada); fim de vida (ausente)).

**Tabela de ativos:**

| Coluna | Como preencher |
|---|---|
| **Ativo / Dado** | nome + **onde está**. Ex.: "Arquivo do laudo (`frontend/public/laudos/laudo-{id}.pdf`)"; "Detalhes internos nas mensagens de erro (`server.error.*`)" |
| **Fluxo** | em qual fluxo crítico aparece |
| **Nível** | Crítico, Confidencial, Interno ou Público |
| **Atributo** | C, I, D, Autenticidade, Não-repúdio |
| **Quem demanda** | negócio, jurídico/DPO, segurança, operações, titular |
| **Base legal** | **em branco agora** (Fase 1) |
| **Observação** | **o que você viu**. "O laudo 104, não publicado, baixa sem login pelo endereço `/laudos/laudo-104.pdf`" é bom; "pasta pública" é pouco |

Ativos que um G4 atento encontra: o resultado do exame, o **arquivo** do laudo, o **número** do
exame, o **conteúdo das mensagens de erro**, o **texto e o link** da notificação (planejados), o
**conteúdo do site**, os dados dos pacientes **inativos**, os segredos — e o que **falta** (prazo
de guarda, eliminação).

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-0`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 0 - inventario"
git push -u origin dossie/fase-0
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
   **compare** escolha `dossie/fase-0` → **Create pull request**.
2. Título: "Dossiê — Fase 0: inventário". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] Testei os três canais (API, arquivo, erro) e guardei as saídas.
- [ ] Escrevi como seria a notificação hoje, com o link real, e quem mais a leria.
- [ ] Sei quantos lugares guardam dados de um paciente (para a F-06).
- [ ] Incluí o que **falta** (prazo de guarda, eliminação) como fluxo ausente.

## Erros comuns

- **Analisar só a API.** O recorte do G4 é exatamente o resto.
- **Achar que mensagem de erro não é ativo.** Ela ensina o atacante.
- **Pôr artigo de lei na base legal agora.** É na Fase 1.
