# G3 · Fase 5 — Verificação e evidências

> **Seção do dossiê:** §7 · **Lidera:** 💻 Dev e ⚙️ DevSecOps · **Aula:** 7 · **Semanas:** 11–12

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G3**.

## O que você entrega no fim

1. A tabela de verificação da §7: requisito → teste → ferramenta → antes/depois → evidência.
2. O registro dos **ataques manuais por outros caminhos** — e do que eles revelaram.
3. A leitura dos **scanners**, com as limitações de cada um.

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
   git checkout -b dossie/fase-5
   git push -u origin dossie/fase-5
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-5
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

| Tipo de prova | O que prova | Limite |
|---|---|---|
| **Teste automático** | que **aquele** ataque falha | só testa o que alguém escreveu |
| **Scanner** (SAST/DAST) | que um **padrão** conhecido sumiu | não sabe que um paciente **não deveria** publicar laudo |
| **Ataque manual** | que **outros caminhos** estão fechados | depende de quem testa |

**Teste verde não é o fim.**

---

## Passo 1 — Mostre o 🔴 → 🟢 no pipeline

> 🔎 **Como o Grupo 5 fez:** na aba **Actions**, abriu uma execução **antes** do PR e a **do PR**;
> procurou o nome do cenário no log: antes, `FAILURE!`; depois, fora da lista de falhas. Guardou os
> links.

**▶ Agora no G3:** encontre o seu cenário no log do pipeline, **antes** e **depois** da
correção. (Nunca usou o GitHub Actions? Leia antes [`comum/06`](../comum/06-pipeline-do-zero.md)
§1 a §5.)

**A execução de "antes"** (o código ainda vulnerável):

1. No GitHub, abra o repositório do grupo → aba **Actions** → à esquerda, **SSDLC - VidaPlus**.
2. Clique no filtro **Branch** (acima da lista) e escolha `main`.
3. Ache a execução cujo título começa com **"Merge pull request #"** e termina com
   `fix/F-03-rbac` (o merge da correção da Fase 4). A execução **logo abaixo** dela (mais antiga)
   é a de **antes**. Clique no título dela.
4. Na lista de jobs da esquerda, clique em **Gate - Testes de seguranca**.
5. Clique na etapa **Testes de aceite de seguranca (@Tag security)** para abri-la.
6. No canto superior direito do log, clique na caixa **Search logs**, digite
   `paciente não pode publicar laudo` e aperte **Enter**. O cenário aparece numa linha com `FAILURE!` e, logo abaixo,
   `expected: <403> but was: <200>`. Procure também `rbac_paciente` (o teste equivalente em JUnit).
7. Tire um print (**Shift+Print Screen**, selecione a área do log) e salve como
   `entregaveis/evidencias/fase5-ci-antes.png`. Copie a URL da barra do navegador para o rascunho.

**A execução de "depois"** (com a correção):

8. Volte à lista: aba **Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main`. Clique no
   título da execução **"Merge pull request #… fix/F-03-rbac"**.
9. Clique no job **Gate - Testes de seguranca** → etapa **Testes de aceite de seguranca (@Tag
   security)** → **Search logs** → `paciente não pode publicar laudo`. Agora o cenário **não aparece** na lista de
   falhas. Procure também `Tests run:` e compare a última linha (`Tests run: X, Failures: Y`) com a
   de antes: o número de falhas diminuiu.
10. Print em `entregaveis/evidencias/fase5-ci-depois.png` e a URL no rascunho.

- Procure também, um de cada vez, os nomes dos cenários que vocês acrescentaram ao `rbac.feature` na Fase 1.
- Junte as saídas locais da Fase 4: `fase4-teste-antes.txt`, `fase4-teste-depois.txt`, `fase4-matriz-antes.txt` e `fase4-matriz-depois.txt` — a matriz é a evidência mais forte do G3.


## Passo 2 — Ataque de novo, por outros caminhos

> 🔎 **Como o Grupo 5 fez:** com os testes verdes, tentou **chegar ao mesmo dado por outro
> caminho**: `q=` e `q=%` sem token devolveram **todos os pacientes**. A injeção estava corrigida,
> mas a busca não pedia login. Criou um requisito novo e revisou árvore, RIPD e matriz. Leia a 💡
> "Teste verde não é o fim" em [`docs/12` §7](../../docs/12-exemplo-dossie-preenchido.md#7-fase-5--verificação--evidências).

**▶ Agora no G3:** o objetivo continua o da sua árvore: **fazer uma ação de admin sem ser
admin**. A correção confia **no papel que vem dentro do token**. Ataque essa confiança. Antes,
prepare o terminal:

```bash
bash roteiros/comum/zerar-banco.sh
source roteiros/comum/tokens.sh
mkdir -p entregaveis/evidencias
```

(A branch desta fase foi criada a partir da `main` no Passo 0, então já tem a correção da Fase 4.
O `zerar-banco.sh` recompila esse código e volta o banco ao original.)

**1. Fabricar um token com o papel que eu quiser.** O token é assinado com uma chave — e a chave
está no código. O script `fabricar-token.sh` lê a chave do `JwtUtil.java` e monta um token "da
Ana" (usuário 5) dizendo que ela é **ADMIN**. Depois, o `curl` usa esse token para pedir a lista de
usuários, que só o ADMIN pode ver:

```bash
FORJADO=$(bash roteiros/comum/fabricar-token.sh 5 ADMIN)
echo "$FORJADO" | cut -d. -f2 | base64 -d; echo
curl -s -o /dev/null -w 'token fabricado -> lista de usuarios: %{http_code}
' localhost:8080/api/usuarios -H "Authorization: Bearer $FORJADO" \
  | tee entregaveis/evidencias/fase5-token-fabricado.txt
```

A segunda linha mostra o conteúdo do token fabricado: `{"sub":"5","papel":"ADMIN"}`. Abra o script
(**Ctrl+P** → `fabricar-token.sh`) e leia os comentários: sem precisar saber Python, dá para ver
que ele monta à mão as três partes do token (cabeçalho, conteúdo **com o papel que eu escolhi** e
**assinatura**, calculada com a chave). Como a assinatura bate, o servidor acredita. Qual o
resultado? A correção de vocês está **errada**? (Não: ela faz o que deve. O problema está **em
outro elo** — qual?)

**2. Papel mudado no banco, token antigo.** A recepção entra e guarda o token; depois, a clínica
muda o papel dela (simulamos direto no banco, porque não há tela para isso):

```bash
source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
TOKEN_REC=$(curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"recepcao@vidaplus.local","senha":"Recep123"}' | sed -E 's/.*"access_token":"([^"]+)".*/\1/')
docker compose -f legacy-app/docker-compose.yml exec db mysql -uvidaplus -pvidaplus123 vidaplus -e "UPDATE usuario SET papel='PACIENTE' WHERE id=3;"
curl -s -o /dev/null -w 'token antigo da recepcao -> agenda: %{http_code}\n' localhost:8080/api/atendimentos -H "Authorization: Bearer $TOKEN_REC" \
  | tee entregaveis/evidencias/fase5-token-antigo.txt
```

No banco ela já não é recepção. E o token? Por quanto tempo isso vale? (O token expira?) O mesmo
vale para uma conta **desativada**. Teste: zere o banco, pegue um token novo da recepção, desative a
conta dela com o token do admin e use o token antigo:

```bash
bash roteiros/comum/zerar-banco.sh
source roteiros/comum/tokens.sh
curl -s -X POST localhost:8080/api/usuarios/3/alternar-ativo -H "Authorization: Bearer $TOKEN_ADMIN"; echo
curl -s -o /dev/null -w 'token da recepcao desativada -> agenda: %{http_code}\n' localhost:8080/api/atendimentos -H "Authorization: Bearer $TOKEN_REC"
```

**3. A carteira de outro médico.** O papel MEDICO pode ver `/api/atendimentos/medico/{id}` — mas
de **qualquer** `{id}`? Hoje só há um médico, então pense: se houvesse dois, a regra de papel
impediria um de ver a carteira do outro? Que tipo de regra faltaria?

**4. Funções sem login.** `curl -s "localhost:8080/api/pacientes/busca?q="` — a lista de pacientes
(com religião e renda: a F-15 de vocês) sai sem token nenhum. Está no plano?

**Zere o banco** ao terminar (rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`). Guarde cada saída em `entregaveis/evidencias/fase5-...txt`.

**Para o dossiê:** os itens 1 e 2 mostram que o RBAC de vocês é **tão forte quanto o token**.
Registrem como **interface documentada**: o controle supõe que a chave do token é secreta (F-10 —
vocês **fecham** esse elo na Fase 6, na aula guiada dos segredos, e repetem este mesmo ataque)
e que o token tem vida curta e é invalidado quando o papel muda (F-11, G1). Acrescentem a ameaça
no STRIDE (§3) e na árvore, e a linha no plano (§6.2).

> 💡 **Para a defesa:** o token fabricado é uma demonstração de 30 segundos que mostra que vocês
> entenderam a **cadeia**: o controle está certo, mas a confiança dele vem de outro lugar.

## Passo 3 — Leia os scanners como evidência

> 🔎 **Como o Grupo 5 fez:** escreveu o resultado **e a limitação** de cada scanner (por exemplo,
> que o ZAP do pipeline é **passivo**).

**▶ Agora no G3:**

1. **Semgrep e CodeQL** acusavam a falta de controle por papel antes da correção? Rode o
   Semgrep no seu computador:

   ```bash
   docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
     semgrep scan --config security/semgrep/vidaplus.yml --metrics=off --json --quiet legacy-app \
     | grep -o '"check_id": *"[^"]*"' | sed 's/.*semgrep\.//; s/"$//' | sort | uniq -c
   ```

   Ele roda as regras do projeto e conta os achados de cada uma (como ler:
   [`comum/07` §2](../comum/07-ferramentas-de-seguranca.md#2-semgrep--procura-padrões-perigosos-no-código)).
   Alguma regra fala de dono de exame ou de papel?

   Para o CodeQL: no GitHub, aba **Security** → **Code scanning** → filtro **Tool** = `CodeQL`
   (se a aba não mostrar alertas, diga isso no dossiê). Explique **por que não**: nenhum padrão de texto diz "este método deveria ser só do laboratório" — essa
   informação está na **matriz de permissões**, não no código.
2. **A regra `vidaplus-hardcoded-secret` do Semgrep** acusa a chave do `JwtUtil.java`. Ligue esse
   achado ao item 1 do Passo 2: um scanner **apontou** o elo fraco que derruba o RBAC.
3. **ZAP:** ele entra com **um** usuário. Conseguiria testar a matriz de cinco papéis?

## Passo 4 — Monte a tabela da §7

> 🔎 **Como o Grupo 5 fez:** uma linha por verificação, inclusive as **manuais** e as que deram
> **🔴** (achados). Veja: [`docs/12` §7](../../docs/12-exemplo-dossie-preenchido.md#7-fase-5--verificação--evidências).

**▶ Agora no G3:** colunas: requisito | teste | ferramenta | antes → depois | evidência. Inclua:

- `rbac.feature` (todos os cenários) e o `SegurancaAceiteTest.rbac_...` (🔴 → 🟢);
- a **matriz de acesso** (script, antes → depois);
- o uso legítimo de cada papel (pela tela);
- os ataques do Passo 2: token fabricado e token antigo (🔴, interface com F-10/F-11), carteira de
  outro médico, funções sem login.

E os parágrafos dos **scanners** e do **gate de testes**.

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-5`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 5 - verificacao"
git push -u origin dossie/fase-5
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
   **compare** escolha `dossie/fase-5` → **Create pull request**.
2. Título: "Dossiê — Fase 5: verificação". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] Tenho os links do pipeline antes e depois, e a matriz antes e depois.
- [ ] Fabriquei um token e usei um token antigo, e registrei os resultados.
- [ ] Registrei a dependência da chave e da vida do token como interface.
- [ ] Expliquei por que os scanners não pegam RBAC — e qual achado deles importa para o RBAC.
