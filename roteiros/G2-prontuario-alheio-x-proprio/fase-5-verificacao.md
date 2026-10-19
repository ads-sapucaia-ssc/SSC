# G2 · Fase 5 — Verificação e evidências

> **Seção do dossiê:** §7 · **Lidera:** 💻 Dev e ⚙️ DevSecOps · **Aula:** 7 · **Semanas:** 11–12

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G2**.

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
| **Teste automático** | que **aquele** ataque, com **aqueles** dados, falha | só testa o que alguém escreveu |
| **Scanner** (SAST/DAST) | que um **padrão** conhecido sumiu | não entende regra de negócio — e o IDOR **é** regra de negócio |
| **Ataque manual** | que **outros caminhos** até o mesmo dado estão fechados | depende de quem testa |

**Teste verde não é o fim.**

---

## Passo 1 — Mostre o 🔴 → 🟢 no pipeline

> 🔎 **Como o Grupo 5 fez:** abriu, na aba **Actions**, uma execução **antes** do PR e a **do PR**;
> no job "Gate - Testes de seguranca", procurou o nome do cenário: antes, `FAILURE!`; depois, fora da
> lista de falhas. Guardou os links.

**▶ Agora no G2:** encontre o seu cenário no log do pipeline, **antes** e **depois** da
correção. (Nunca usou o GitHub Actions? Leia antes [`comum/06`](../comum/06-pipeline-do-zero.md)
§1 a §5.)

**A execução de "antes"** (o código ainda vulnerável):

1. No GitHub, abra o repositório do grupo → aba **Actions** → à esquerda, **SSDLC - VidaPlus**.
2. Clique no filtro **Branch** (acima da lista) e escolha `main`.
3. Ache a execução cujo título começa com **"Merge pull request #"** e termina com
   `fix/F-02-idor` (o merge da correção da Fase 4). A execução **logo abaixo** dela (mais antiga)
   é a de **antes**. Clique no título dela.
4. Na lista de jobs da esquerda, clique em **Gate - Testes de seguranca**.
5. Clique na etapa **Testes de aceite de seguranca (@Tag security)** para abri-la.
6. No canto superior direito do log, clique na caixa **Search logs**, digite
   `paciente não acessa o laudo` e aperte **Enter**. O cenário aparece numa linha com `FAILURE!` e, logo abaixo,
   `expected: <403> but was: <200>`. Procure também `idor_paciente` (o teste equivalente em JUnit).
7. Tire um print (**Shift+Print Screen**, selecione a área do log) e salve como
   `entregaveis/evidencias/fase5-ci-antes.png`. Copie a URL da barra do navegador para o rascunho.

**A execução de "depois"** (com a correção):

8. Volte à lista: aba **Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main`. Clique no
   título da execução **"Merge pull request #… fix/F-02-idor"**.
9. Clique no job **Gate - Testes de seguranca** → etapa **Testes de aceite de seguranca (@Tag
   security)** → **Search logs** → `paciente não acessa o laudo`. Agora o cenário **não aparece** na lista de
   falhas. Procure também `Tests run:` e compare a última linha (`Tests run: X, Failures: Y`) com a
   de antes: o número de falhas diminuiu.
10. Print em `entregaveis/evidencias/fase5-ci-depois.png` e a URL no rascunho.

- Junte as saídas locais da Fase 4: `fase4-teste-antes.txt`, `fase4-teste-depois.txt`, `fase4-acessos-antes.txt` e `fase4-acessos-depois.txt`.


## Passo 2 — Ataque de novo, por outros caminhos

> 🔎 **Como o Grupo 5 fez:** com os testes verdes, tentou **chegar ao mesmo dado por outro
> caminho**: `q=` e `q=%` sem token devolveram **todos os pacientes**. A injeção estava corrigida,
> mas a busca não pedia login. Criou um requisito novo (REQ-AUTH-BUSCA-01) e revisou árvore, RIPD
> e matriz. Leia a 💡 "Teste verde não é o fim" em [`docs/12` §7](../../docs/12-exemplo-dossie-preenchido.md#7-fase-5--verificação--evidências).

**▶ Agora no G2:** o objetivo continua o da sua árvore — **ler um exame que não é seu**. Tente cada ramo da
árvore de novo, com a correção no ar. Antes, prepare o terminal:

```bash
bash roteiros/comum/zerar-banco.sh
source roteiros/comum/tokens.sh
mkdir -p entregaveis/evidencias
```

(A branch desta fase foi criada a partir da `main` no Passo 0, então já tem a correção da Fase 4.
O `zerar-banco.sh` recompila esse código e volta o banco ao original.)

1. **O arquivo, fora da API:**

   ```bash
   curl -s -o /dev/null -w '%{http_code}\n' localhost:4200/laudos/laudo-102.pdf
   ```

   Esperado: **continua 200** — a correção foi na API, e o arquivo nem passa por ela. Está no
   plano? Registre como **aberto**.
2. **Criar um exame em nome de outra pessoa:**

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -X POST localhost:8080/api/exames/solicitar -H "Authorization: Bearer $TOKEN_ANA" \
     -H "Content-Type: application/json" -d '{"pacienteId":2,"tipo":"teste"}'; echo
   ```

   A Ana (paciente 1) criou um exame para o Bruno (paciente 2)? Que requisito isso viola?
3. **Mudar o dado em que a regra confia.** A regra do médico diz: "pode ver se **atende** o
   paciente" — e "atende" está na tabela `atendimento`. Quem consegue **escrever** nessa tabela?
   Teste:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -o /dev/null -w 'House -> 102: %{http_code}\n' localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_HOUSE"
   curl -s -X POST localhost:8080/api/atendimentos -H "Authorization: Bearer $TOKEN_ANA" \
     -H "Content-Type: application/json" -d '{"medicoId":1,"pacienteId":2}'; echo
   curl -s -o /dev/null -w 'House -> 102: %{http_code}\n' localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_HOUSE"
   ```

   O que aconteceu entre o primeiro e o terceiro comando? **Quem** abriu o caminho? Explique com
   as suas palavras por que uma regra de autorização é tão forte quanto **o dado em que ela
   confia**. Esse buraco é de qual pacote? (Pista: quem pode agendar é controle por papel — F-03,
   do G3.) Registrem como **interface documentada**: o controle de vocês **supõe** que só a
   recepção cria atendimentos. **Depois, zere o banco** (rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`).
4. **Sem login:**
   `curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/101` — 401 ou 500?
5. **Uma ideia de vocês:** que outro caminho a árvore não tinha? Tente.

Guarde cada saída em `entregaveis/evidencias/fase5-...txt`.

**Achou algo novo?** Faça como o Grupo 5: requisito na §2 (com `.feature @analise`), linha no
STRIDE e na árvore (§3), no plano (§6.2) e, se for de privacidade, no RIPD (§4.3).

> 💡 **Para a defesa:** o achado do item 3 é do tipo que impressiona a banca. Ele mostra que vocês
> entenderam que segurança é uma **cadeia**: o elo de vocês está certo, mas depende de um elo de
> outro grupo.

## Passo 3 — Leia os scanners como evidência

> 🔎 **Como o Grupo 5 fez:** escreveu o resultado **e a limitação** de cada scanner — por exemplo,
> que o ZAP do pipeline é **passivo** e não detecta injeção nem antes nem depois.

**▶ Agora no G2:**

1. **Semgrep e CodeQL:** procuram padrões no código. Algum deles acusava o IDOR antes da correção?
   Rode o Semgrep no seu computador:

   ```bash
   docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
     semgrep scan --config security/semgrep/vidaplus.yml --metrics=off --json --quiet legacy-app \
     | grep -o '"check_id": *"[^"]*"' | sed 's/.*semgrep\.//; s/"$//' | sort | uniq -c
   ```

   Ele roda as regras do projeto e conta os achados de cada uma (como ler:
   [`comum/07` §2](../comum/07-ferramentas-de-seguranca.md#2-semgrep--procura-padrões-perigosos-no-código)).
   Alguma regra fala de dono de exame ou de papel?

   Para o CodeQL: no GitHub, aba **Security** → **Code scanning** → filtro **Tool** = `CodeQL`
   (se a aba não mostrar alertas, diga isso no dossiê). Explique **por que não**: o código vulnerável não
   tem nada de "errado" no texto — falta uma **pergunta** que só quem conhece a regra de negócio
   sabe fazer.
2. **ZAP (DAST autenticado):** o job entra no sistema com um usuário de teste. Ele conseguiria
   perceber que a Ana não deveria ver o 102? Por quê?
3. Conclusão para o dossiê: para falhas de autorização, **o teste escrito pelo grupo é a prova
   principal**.

## Passo 4 — Monte a tabela da §7

> 🔎 **Como o Grupo 5 fez:** uma linha por verificação, inclusive as **manuais** e as que deram
> **🔴** (achados). Veja: [`docs/12` §7](../../docs/12-exemplo-dossie-preenchido.md#7-fase-5--verificação--evidências).

**▶ Agora no G2:** colunas: requisito | teste | ferramenta | antes → depois | evidência. Inclua:

- `idor.feature` e o `SegurancaAceiteTest.idor_...` (🔴 → 🟢);
- a tabela de acessos por papel da Fase 4 (manual, antes → depois);
- o uso legítimo (Ana vê os dela; House, os dos pacientes dela; Bio publica);
- os ataques do Passo 2, inclusive os que continuam **abertos** (🔴 → 🔴, "no plano" ou
  "interface com o G3").

E os parágrafos dos **scanners** e do **gate de testes** (vermelho pelos testes dos outros
pacotes).

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

- [ ] Tenho os links das execuções antes e depois, com prints.
- [ ] Ataquei de novo por pelo menos três caminhos e registrei o resultado.
- [ ] Registrei a dependência da tabela `atendimento` (e de quem a escreve) como interface.
- [ ] Expliquei por que os scanners não pegam IDOR.
