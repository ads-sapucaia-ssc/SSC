# G3 · Fase 0 — Inventário e classificação de dados

> **Seção do dossiê:** §1 · **Lidera:** 🛡️ Segurança e 📋 DPO · **Aula:** 1 · **Semana:** 1
> **Antes:** faça o **Passo 0**, logo abaixo.

## Como usar este roteiro

Cada passo tem duas partes:

1. **🔎 Como o Grupo 5 fez** — o que o grupo do dossiê de exemplo
   ([`docs/12`](../../docs/12-exemplo-dossie-preenchido.md)) executou: o comando, o que apareceu e
   o que escreveu. Leia para entender **o raciocínio**.
2. **▶ Agora no G3** — o mesmo passo, nas funcionalidades do **seu** pacote. É isto que você faz.

O Grupo 5 trabalhou a busca de pacientes e o prontuário; vocês trabalham **os papéis** (quem pode
o quê), o **consentimento**, a **minimização** do cadastro e o **perfil** do paciente.

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

- **Ativo:** tudo o que tem valor — um dado, um arquivo, um segredo do sistema, um registro — e
  também uma **função** do sistema (publicar laudo, desativar conta): quem a usa indevidamente
  causa dano.
- **Fluxo crítico:** um caminho do sistema por onde passa um ativo importante.
- **Nível:** **Crítico → Confidencial → Interno → Público** (vale o mais alto).
- **Atributo prioritário:** **C**onfidencialidade, **I**ntegridade, **D**isponibilidade,
  **Autenticidade**, **Não-repúdio**.
- **Quem demanda/define:** negócio, jurídico/DPO, segurança, operações ou o titular.

Crie um rascunho: `entregaveis/evidencias/fase0-anotacoes.txt`.

---

## Passo 1 — Percorra as telas com cada papel

> 🔎 **Como o Grupo 5 fez:** entrou com os seis usuários e anotou as telas de cada papel. Viu que
> Recepção, Médico, Bioquímico e Admin têm o menu **Pacientes** e que a ficha mostra o
> **prontuário**.

**▶ Agora no G3** — para vocês, **esta é a investigação principal da fase**. Entre com os seis
usuários e monte, no rascunho, a **matriz de menus**:

| Tela (menu) | Endereço | ADMIN | MEDICO | RECEPCAO | BIOQUIMICO | PACIENTE |
|---|---|:-:|:-:|:-:|:-:|:-:|
| Pacientes | `/pacientes` | | | | | |
| Usuários | `/usuarios` | | | | | |
| Agenda | `/agenda` | | | | | |
| Meus Pacientes | `/meus-pacientes` | | | | | |
| Fila de Laudos | `/fila-laudos` | | | | | |

Marque ✓ onde o menu aparece. Depois abra `components/Layout.jsx` e compare com a lista
`NAV_POR_PAPEL`: é ela que decide os menus.

**Agora o teste que importa:** entre como **Ana (paciente)** e digite **na barra de endereços**
`http://localhost:4200/usuarios`. A tela abre? Mostra a lista de usuários? Depois, ainda como Ana,
digite `http://localhost:4200/agenda` e, em seguida, `http://localhost:4200/fila-laudos`. Anote o
que acontece em cada uma. **Esconder o menu protege alguma coisa?**

---

## Passo 2 — Siga a trilha de cada funcionalidade, da tela ao banco

> 🔎 **Como o Grupo 5 fez:** seguiu a trilha da busca:
> `BuscaPacientes.jsx → services/paciente.js (buscar) → GET /api/pacientes/busca?q= → PacienteController.buscar → PacienteDao.buscar → tabela paciente`.

**▶ Agora no G3:** confira, abrindo os arquivos, as trilhas do [`README` do G3](README.md#3-onde-cada-funcionalidade-está-no-vidaplus):

1. **Gestão de usuários:** `Usuarios.jsx` → `services/usuario.js` → `UsuarioController` →
   `UsuarioDao`. Que coluna o `alternarAtivo` muda?
2. **Agenda, Meus Pacientes, Fila de Laudos:** a mesma coisa para cada uma.
3. **Em cada método de controller**, leia as primeiras linhas: o que ele confere antes de agir?
   Ele olha **quem** é a pessoa? Olha **o papel** dela?
4. **Procure quem confere o papel:** **Ctrl+Shift+F** → `papelDoToken`. O método existe em
   `JwtUtil.java`. Quantos lugares do backend o **chamam** de verdade (fora de comentários)?
5. **Editar cadastro (F-19):** `PacienteFicha.jsx` → `editar` → `PUT /api/pacientes/{id}` →
   `PacienteController.editar`. Leia o método inteiro: ele **grava** alguma coisa no banco?

---

## Passo 3 — Observe a funcionalidade principal por dentro

> 🔎 **Como o Grupo 5 fez:** com o **F12 → Rede** aberto, viu que a busca devolvia religião e
> renda; repetiu pelo terminal **sem token** e recebeu o cadastro completo. Escreveu: *"A busca não
> pede login e devolve CPF, religião e renda."*

**▶ Agora no G3 — funções de outros papéis, usadas por um paciente.** No terminal, o bloco abaixo
carrega os tokens (o da Ana fica em `TOKEN_ANA`) e pede, como Ana, quatro endereços de outros
papéis:

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
for E in /api/usuarios /api/atendimentos /api/atendimentos/medico/1 /api/exames/pendentes; do
  echo "$E -> $(curl -s -o /dev/null -w '%{http_code}' localhost:8080$E -H "Authorization: Bearer $TOKEN_ANA")"
done | tee entregaveis/evidencias/fase0-ana-funcoes-alheias.txt
```

O `for` pede quatro endereços, um de cada papel. Anote os códigos. Depois veja o **conteúdo** de
um deles:

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
curl -s localhost:8080/api/usuarios -H "Authorization: Bearer $TOKEN_ANA" | python3 -m json.tool
```

O que uma paciente fica sabendo? (E-mails de funcionários; quem é admin; quais contas estão
ativas.) Que dado de saúde aparece em `/api/exames/pendentes`?

> **Não rode ainda** os endereços que **mudam** dados (publicar, desativar, agendar): você testa
> na Fase 2a, com o banco zerado depois.

---

## Passo 4 — Olhe no banco o que o sistema guarda

> 🔎 **Como o Grupo 5 fez:** `DESCRIBE paciente;` achou religião, estado civil e renda; procurou no
> código quem usava (só a busca) e escreveu *"coletados, mas nenhum fluxo usa"*.

**▶ Agora no G3** — isto **é** a F-15 de vocês:

```bash
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "DESCRIBE paciente;"
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, nome, religiao, estado_civil, renda_mensal FROM paciente;"
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, email, papel, ativo FROM usuario;"
```

1. Para **cada coluna** de `paciente`, procure no código (**Ctrl+Shift+F**) quem a usa e **para
   quê**. Monte no rascunho: coluna | quem usa | finalidade | precisa? Leia os comentários `V-14`
   no `schema.sql`.
2. Que coluna de `usuario` decide o papel? Quem consegue mudá-la?
3. A coluna `ativo`: o que "desativar" faz? (Leia o comentário em `UsuarioDao.alternarAtivo`.) Os
   dados da pessoa continuam lá?

---

## Passo 5 — Olhe o "crachá" da sessão

> 🔎 **Como o Grupo 5 fez:** registrou o token no `localStorage` como ativo: *"não expira: quem
> copia o token usa a conta"*.

**▶ Agora no G3:**

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
echo "$TOKEN_ANA" | cut -d. -f2 | base64 -d; echo
```

Resposta esperada: `{"sub":"5","papel":"PACIENTE"}`. **O papel está dentro do token.** Pense:

1. Quando o servidor (depois de corrigido) quiser saber o papel da pessoa, de onde vai ler — do
   token ou do banco?
2. Se o administrador **mudar** o papel de alguém no banco, o token antigo continua dizendo o papel
   antigo? Por quanto tempo? (O token expira?)
3. Quem tem a **chave** que assina o token (`JwtUtil.java`) consegue escrever qualquer papel ali?

Guarde as respostas: elas voltam na Fase 5.

---

## Passo 6 — Teste as funcionalidades secundárias

> 🔎 **Como o Grupo 5 fez:** testou a nota clínica **sem token** e recebeu a nota da Ana: *"a API
> entrega a nota sem login"*.

**▶ Agora no G3 — a edição de cadastro (F-19):**

1. Entre como **Recepção**, abra **Pacientes** → busque "Bruno" → abra a ficha → **Editar
   cadastro**. Mude o telefone e salve. O que a tela mostra depois? (Repare que a ficha nem exibe
   o telefone.)
2. Confira no banco:
   `docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "SELECT id, telefone FROM paciente WHERE id = 2;"`.
   O telefone mudou? (Releia o `PacienteController.editar` do Passo 2.)
3. Pelo terminal, com o token da **Ana**, mande campos que **não existem** na tela:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -X PUT localhost:8080/api/pacientes/2 -H "Authorization: Bearer $TOKEN_ANA" \
     -H "Content-Type: application/json" -d '{"telefone":"<b>x</b>","papel":"ADMIN","religiao":"nenhuma"}'; echo
   ```

   A resposta lista os campos recebidos. O servidor **recusou** algum? Quem é a Ana para editar o
   cadastro do Bruno? E ela consegue editar **o próprio** cadastro pela tela?

---

## Passo 7 — Procure o que deveria existir e não existe

> 🔎 **Como o Grupo 5 fez:** procurou `DELETE FROM` e "anonim" no código — nada. Escreveu:
> *"(Ausente) Fim de vida do dado"*.

**▶ Agora no G3:**

1. **Consentimento (F-04):** `SHOW TABLES;` — existe tabela de consentimento? **Ctrl+Shift+F** →
   `consent`. Abra `entity/Consentimento.java` e o fim do `db/schema.sql`: o que estava previsto?
   Em algum lugar o paciente aceita ou recusa alguma coisa?
2. **Autosserviço (F-19):** existe alguma tela em que o **próprio paciente** vê e corrige os
   **seus** dados cadastrais?

Escreva os fluxos ausentes: "(Ausente) **Consentimento** — ..." e "(Ausente) **Autosserviço do
titular** — ...".

---

## Passo 8 — Procure os segredos do sistema

> 🔎 **Como o Grupo 5 fez:** viu a senha do banco e a chave do token em `application.properties`
> e `docker-compose.yml` e criou a linha *"Segredos do sistema"*, base legal *"— (não é dado
> pessoal)"*.

**▶ Agora no G3:** abra os três arquivos (**Ctrl+P** e digite o nome): `application.properties`,
`docker-compose.yml` e `JwtUtil.java`. Em cada um, procure (**Ctrl+F**) as palavras `password`,
`senha`, `secret` e `SECRET`. Para o G3, a **chave do token** é especialmente grave: o papel
está dentro do token (Passo 5), então quem tem a chave **escolhe o próprio papel**. Anote **o que**
é cada segredo e **onde** está, sem copiar valores.

---

## Passo 9 — Monte a §1 do dossiê

> 🔎 **Como o Grupo 5 fez:** 5 fluxos críticos e 9 ativos; cada observação diz **o que foi
> visto**. Veja: [`docs/12` §1](../../docs/12-exemplo-dossie-preenchido.md#1-fase-0--inventário--classificação-de-dados).

**▶ Agora no G3:**

**Fluxos críticos** — 3 a 5 (ex.: gestão de usuários, publicação de laudo, agenda, edição de
cadastro, e os ausentes).

**Tabela de ativos:**

| Coluna | Como preencher |
|---|---|
| **Ativo / Dado** | nome + **onde está**. Ex.: "Papel do usuário (`usuario.papel` e dentro do token)"; "Função de desativar contas (`POST /api/usuarios/{id}/alternar-ativo`)" |
| **Fluxo** | em qual fluxo crítico aparece |
| **Nível** | Crítico, Confidencial, Interno ou Público |
| **Atributo** | C, I, D, Autenticidade, Não-repúdio |
| **Quem demanda** | negócio, jurídico/DPO, segurança, operações, titular |
| **Base legal** | **em branco agora** (Fase 1) |
| **Observação** | **o que você viu**. "A Ana abriu `/usuarios` pela barra de endereços e viu os e-mails de todos os funcionários" é bom; "RBAC ausente" é pouco |

Ativos que um G3 atento encontra (confira os seus): o papel de cada usuário, as **funções
restritas** de cada papel, a lista de usuários, a agenda (quem se trata com qual médico), os
campos do cadastro (inclusive os sem finalidade), o token, os segredos — e o que **falta**
(consentimento, autosserviço).

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

- [ ] Montei a matriz de menus e testei abrir telas de outro papel pela barra de endereços.
- [ ] Testei, com o token da paciente, endereços de **quatro** papéis diferentes.
- [ ] Tenho a lista coluna | quem usa | finalidade do cadastro do paciente.
- [ ] Incluí o que **falta** (consentimento, autosserviço) como fluxo ausente.

## Erros comuns

- **Confundir menu escondido com controle.** Teste pela barra de endereços e pelo curl.
- **Esquecer que funções também são ativos.** "Publicar laudo" e "desativar conta" valem tanto
  quanto um dado.
- **Pôr artigo de lei na base legal agora.** É na Fase 1.
