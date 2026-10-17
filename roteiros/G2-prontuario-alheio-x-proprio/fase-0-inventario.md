# G2 · Fase 0 — Inventário e classificação de dados

> **Seção do dossiê:** §1 · **Lidera:** 🛡️ Segurança e 📋 DPO · **Aula:** 1 · **Semana:** 1
> **Antes:** faça o **Passo 0**, logo abaixo.

## Como usar este roteiro

Cada passo tem duas partes:

1. **🔎 Como o Grupo 5 fez** — o que o grupo do dossiê de exemplo
   ([`docs/12`](../../docs/12-exemplo-dossie-preenchido.md)) executou: o comando, o que apareceu e
   o que escreveu. Leia para entender **o raciocínio**.
2. **▶ Agora no G2** — o mesmo passo, nas funcionalidades do **seu** pacote. É isto que você faz.

O Grupo 5 trabalhou a busca de pacientes e o prontuário; vocês trabalham o **laudo**: abrir,
listar, baixar, publicar e (no futuro) exportar. O **caminho** é o mesmo; os **achados** são
outros.

## O que você entrega no fim

Na §1 do `DOSSIE.md`: os **fluxos críticos** (3 a 5) e a **tabela de ativos** com nível,
atributo e quem demanda. A coluna **Base legal** fica para a Fase 1.

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  docs  entregaveis  legacy-app  roteiros  security`. Se não
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
   ```

   O primeiro termina com `PRONTO: VidaPlus no ar...` (a primeira vez demora alguns minutos); o
   segundo mostra seis linhas `TOKEN_... ok`. Os tokens valem **só neste terminal**: se abrir
   outro, rode o `source` de novo.

---

## Conceitos em 5 minutos

- **Ativo:** tudo o que tem valor e que alguém poderia querer roubar, alterar ou destruir — um
  dado, um arquivo, um segredo do sistema, um registro.
- **Fluxo crítico:** um caminho do sistema por onde passa um ativo importante.
- **Nível:** **Crítico → Confidencial → Interno → Público**. Um conjunto que junta níveis
  diferentes fica com **o mais alto**.
- **Atributo prioritário:** **C**onfidencialidade, **I**ntegridade, **D**isponibilidade,
  **Autenticidade**, **Não-repúdio**.
- **Quem demanda/define:** negócio, jurídico/DPO, segurança, operações ou o titular.

Crie um rascunho: `entregaveis/evidencias/fase0-anotacoes.txt`.

---

## Passo 1 — Percorra as telas com cada papel

> 🔎 **Como o Grupo 5 fez:** entrou com os seis usuários e anotou as telas de cada papel. Viu que
> Recepção, Médico, Bioquímico e Admin têm o menu **Pacientes** e que a ficha mostra o
> **prontuário**. Essas duas telas eram o centro do pacote dele.

**▶ Agora no G2:**

1. Entre com os seis usuários ([`comum/01` §3](../comum/01-subir-o-vidaplus.md#4-entrar-no-sistema)).
   Para cada um, anote **por onde ele chega a um exame**:
   - **Ana / Bruno (pacientes):** a lista "Meus exames" no Início;
   - **Dra. House (médica):** **Meus Pacientes** → ficha → histórico de exames; e o campo de
     **protocolo** no Início;
   - **Bio (laboratório):** **Fila de Laudos** → abre o exame → botão **Publicar laudo**;
   - **Recepção e Admin:** o campo de protocolo no Início.
2. Abra um exame da Ana e clique em **Baixar PDF**. Repare no **endereço** que abre na nova aba.

O centro do seu pacote é a **tela do exame** (`/exames/:id`) e o **arquivo do laudo**.

---

## Passo 2 — Siga a trilha de cada funcionalidade, da tela ao banco

> 🔎 **Como o Grupo 5 fez:** seguiu a trilha da busca:
> `BuscaPacientes.jsx → services/paciente.js (buscar) → GET /api/pacientes/busca?q= → PacienteController.buscar → PacienteDao.buscar → tabela paciente`.

**▶ Agora no G2:** confira, abrindo cada arquivo, as trilhas do [`README` do G2](README.md#3-onde-cada-funcionalidade-está-no-vidaplus):

1. **Abrir um exame:** em `pages/ExameDetalhe.jsx`, ache `getExame(id)` e `getPdf(id)`. Em
   `services/exame.js`, que endereços elas chamam? Em `controller/ExameController.java`, que
   métodos atendem? Leia os comentários `FALHA` de cada um. Em `dao/ExameDao.java`, que SQL roda?
2. **Listar os exames de um paciente:** `Dashboard.jsx` e `PacienteFicha.jsx` →
   `listarExamesDoPaciente` → ... até a tabela.
3. **Publicar:** `ExameDetalhe.jsx` → `publicar` → ... Que colunas o `ExameDao.publicar` altera?
4. **Baixar o PDF:** o botão aponta direto para `/laudos/laudo-{id}.pdf`. **Quem** entrega esse
   arquivo? (Abra `frontend/nginx.conf` e liste a pasta `frontend/public/laudos/`.) Ele passa pela
   API, onde ficaria uma verificação de permissão?
5. **Procure o que existe e ninguém usa:** em `ExameDao.java`, ache o método `medicoAtende`. Use
   **Ctrl+Shift+F** → `medicoAtende(`: quem o chama?

Escreva as trilhas no rascunho: elas viram o DFD na Fase 2a.

---

## Passo 3 — Observe a funcionalidade principal por dentro

> 🔎 **Como o Grupo 5 fez:** com o **F12 → Rede** aberto, viu que a busca devolvia religião e
> renda. Depois repetiu pelo terminal **sem token** e recebeu o cadastro completo. Escreveu: *"A
> busca não pede login e devolve CPF, religião e renda."*

**▶ Agora no G2 — o exame (F-02):**

1. Entre como **Ana** e abra o exame **101** (é dela). Com o **F12 → Rede → XHR**, clique no
   pedido `101`: o que a API devolveu? Liste os campos.
2. Agora, **na barra de endereços**, troque `/exames/101` por `/exames/102` e aperte Enter. O
   exame 102 é do **Bruno**. O que aparece na tela?
3. Faça o mesmo pedido pelo terminal. O bloco abaixo carrega os tokens dos usuários de teste
   (o da Ana fica em `TOKEN_ANA`), pede o exame 102 como Ana e grava a resposta como evidência:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -i localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_ANA" \
     | tee entregaveis/evidencias/fase0-ana-exame-102.txt
   ```

   Anote o status e o conteúdo. Que dado de saúde de outra pessoa a Ana viu?
4. Teste os outros dois endereços do exame com o mesmo token:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s localhost:8080/api/exames/102/pdf -H "Authorization: Bearer $TOKEN_ANA"; echo
   curl -s localhost:8080/api/exames/paciente/3 -H "Authorization: Bearer $TOKEN_ANA"; echo
   ```

   O segundo lista os exames da **Carla** (paciente 3). Repare no exame 104: que tipo é, e está
   publicado?

---

## Passo 4 — Olhe no banco o que o sistema guarda

> 🔎 **Como o Grupo 5 fez:** rodou `DESCRIBE paciente;`, achou religião/estado civil/renda,
> procurou no código quem usava (só a busca) e escreveu *"coletados, mas nenhum fluxo usa"*.

**▶ Agora no G2:**

```bash
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "DESCRIBE exame;"
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT * FROM exame;"
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT * FROM atendimento;"
```

Anote e pense:

1. A coluna `resultado` guarda o quê? Que nível?
2. Os números dos exames (`id`) são **sequenciais** (101, 102, 103, 104). O que isso facilita para
   quem quer ver exames alheios?
3. A coluna `hash_laudo` está **vazia** (`NULL`). Leia o comentário dela no `db/schema.sql`. Para
   que serviria? (Pista: como provar que um laudo não foi alterado depois de publicado?)
4. A tabela `atendimento` diz quais pacientes cada médico atende. Ela é usada em algum controle de
   acesso hoje? (Lembre do `medicoAtende` do Passo 2.)
5. Que tabelas têm dados **da Ana**? (Pista: procure as colunas `paciente_id`.) Essa lista é o
   ponto de partida da F-09: é tudo o que ela teria o direito de receber.

---

## Passo 5 — Olhe o "crachá" da sessão

> 🔎 **Como o Grupo 5 fez:** registrou o **token no `localStorage`** como ativo, com a observação
> *"não expira: quem copia o token usa a conta"*.

**▶ Agora no G2:** veja o que há dentro do token da Ana:

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
echo "$TOKEN_ANA" | cut -d. -f2 | base64 -d; echo
```

Resposta esperada: `{"sub":"5","papel":"PACIENTE"}`. Repare: o token diz que ela é o **usuário
5** com papel **PACIENTE** — mas **não** diz que ela é a **paciente 1**. Onde essa ligação está
guardada? (Pista: `SELECT id, email, paciente_id FROM usuario;`.) Isso vai importar na Fase 4:
para conferir se o exame é dela, o servidor precisa fazer essa ligação.

---

## Passo 6 — Teste as funcionalidades secundárias

> 🔎 **Como o Grupo 5 fez:** testou a nota clínica **sem token** e recebeu a nota da Ana. Escreveu:
> *"a API entrega a nota sem login"*.

**▶ Agora no G2 — os arquivos de laudo (F-08), sem login nenhum:**

```bash
for ID in 101 102 103 104; do
  curl -s -o /dev/null -w "laudo-$ID.pdf -> %{http_code} %{content_type}\n" localhost:4200/laudos/laudo-$ID.pdf
done
```

O `for` repete o pedido para os quatro números. Depois abra no navegador
`http://localhost:4200/laudos/laudo-104.pdf` **numa janela anônima** (**Ctrl+Shift+P** no Firefox,
**Ctrl+Shift+N** no Chrome), ou seja, sem login. O que está escrito no laudo 104? Ele está
publicado? Leia a nota sobre F-07 × F-08 no [`docs/04`](../../docs/04-catalogo-funcionalidades.md):
"publicado" protege o arquivo?

**▶ Agora no G2 — a publicação (F-07):** quem pode publicar um laudo? Leia o comentário de
`ExameController.publicar`. (Não publique agora: isso altera o banco. Você testa na Fase 2a.)

---

## Passo 7 — Procure o que deveria existir e não existe

> 🔎 **Como o Grupo 5 fez:** procurou `DELETE FROM` e "anonim" no código — nada. Escreveu como
> fluxo crítico: *"(Ausente) Fim de vida do dado"*.

**▶ Agora no G2:**

1. **Portabilidade (F-09):** procure no código (**Ctrl+Shift+F**) por `export`, `portabil`,
   `SolicitacaoTitular`. Existe alguma tela ou endpoint para o paciente baixar os próprios dados?
   Abra `entity/SolicitacaoTitular.java`: o que estava previsto?
2. **Integridade do laudo:** o `hash_laudo` vazio (Passo 4). Existe algum código que o preencha?
   (**Ctrl+Shift+F** → `hash_laudo`.)

Escreva os fluxos ausentes: "(Ausente) **Exportação dos dados pelo titular** — ..." e o que mais
achar.

---

## Passo 8 — Procure os segredos do sistema

> 🔎 **Como o Grupo 5 fez:** abriu `application.properties` e `docker-compose.yml`, viu a senha do
> banco e a chave do token, e criou a linha *"Segredos do sistema"* com base legal *"— (não é dado
> pessoal)"*.

**▶ Agora no G2:** abra os três arquivos (**Ctrl+P** e digite o nome): `application.properties`,
`docker-compose.yml` e `JwtUtil.java`. Em cada um, procure (**Ctrl+F**) as palavras `password`,
`senha`, `secret` e `SECRET`. Para o G2, pense: com a chave que assina o token, alguém pode
fabricar um token de **qualquer** usuário — e então ver os exames dele "legitimamente". Anote
**o que** é cada segredo e **onde** está, sem copiar os valores.

---

## Passo 9 — Monte a §1 do dossiê

> 🔎 **Como o Grupo 5 fez:** 5 fluxos críticos e 9 ativos; cada observação diz **o que foi
> visto**. Veja: [`docs/12` §1](../../docs/12-exemplo-dossie-preenchido.md#1-fase-0--inventário--classificação-de-dados).

**▶ Agora no G2:**

**Fluxos críticos** — 3 a 5: **nome** — o que acontece — **por que é crítico**. Inclua os
ausentes.

**Tabela de ativos** — uma linha por ativo:

| Coluna | Como preencher |
|---|---|
| **Ativo / Dado** | nome + **onde está**. Ex.: "Resultado de exame (`exame.resultado`)"; "Arquivo do laudo (`frontend/public/laudos/laudo-{id}.pdf`)" |
| **Fluxo** | em qual fluxo crítico aparece |
| **Nível** | Crítico, Confidencial, Interno ou Público |
| **Atributo** | C, I, D, Autenticidade, Não-repúdio |
| **Quem demanda** | negócio, jurídico/DPO, segurança, operações, titular |
| **Base legal** | **em branco agora** (Fase 1) |
| **Observação** | **o que você viu**. "A Ana abriu o exame 102 do Bruno trocando o número na URL" é bom; "IDOR" é pouco |

Ativos que um G2 atento encontra (confira os seus): o resultado do exame, o tipo do exame, o
**arquivo PDF**, o status "publicado" e quem publicou, o vínculo médico–paciente, o número do
exame, o token, os segredos do sistema — e o que **falta** (hash do laudo, exportação).

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

- [ ] Reproduzi a Ana vendo o exame do Bruno, pela tela **e** pelo curl, e guardei a saída.
- [ ] Baixei um laudo **sem login** e vi o laudo **não publicado** (104).
- [ ] Cada linha da tabela foi **observada** por mim.
- [ ] Incluí o que **falta** (exportação, integridade do laudo) como fluxo ausente.

## Erros comuns

- **Analisar só `GET /api/exames/{id}`.** O mesmo laudo chega por **três** endpoints e por **um
  arquivo** direto. Cada caminho é um ativo exposto.
- **Achar que "não publicado" protege.** Teste.
- **Pôr artigo de lei na base legal agora.** É na Fase 1.
