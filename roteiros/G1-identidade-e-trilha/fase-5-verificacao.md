# G1 · Fase 5 — Verificação e evidências

> **Seção do dossiê:** §7 · **Lidera:** 💻 Dev e ⚙️ DevSecOps · **Aula:** 7 · **Semanas:** 11–12

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. A tabela de verificação da §7: requisito → teste → ferramenta → antes/depois → evidência.
2. O registro de **ataques manuais por outros caminhos** — e do que eles revelaram.
3. A leitura dos **scanners** (Semgrep, CodeQL, ZAP) como evidência, com as limitações de cada um.

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  aulas  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório (se você seguiu o [`comum/00`](../comum/00-antes-de-comecar.md#4-clone-o-repositório-do-grupo), é a `vidaplus`, dentro da pasta `ssc` da sua pasta pessoal).
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

**Verificar** é mostrar, com prova, que o controle funciona. Três tipos de prova se completam:

| Tipo | O que prova | Limite |
|---|---|---|
| **Teste automático** (`.feature`, JUnit) | que **aquele** ataque, com **aqueles** dados, falha | só testa o que alguém escreveu |
| **Scanner** (SAST: Semgrep, CodeQL; DAST: ZAP) | que um **padrão** conhecido sumiu do código ou da aplicação | não entende a regra de negócio; tem falsos negativos |
| **Ataque manual** | que **outros caminhos** até o mesmo dado também estão fechados | depende da criatividade de quem testa |

A lição mais importante desta fase: **teste verde não é o fim**.

---

## Passo 1 — Mostre o 🔴 → 🟢 no pipeline

> 🔎 **Como o Grupo 5 fez:** abriu, na aba **Actions**, a execução **anterior** ao PR (na `main`)
> e a execução **do PR**. No job "Gate - Testes de seguranca", procurou o nome do cenário: antes,
> ele aparecia com `FAILURE!`; depois, não aparecia mais, e o total de falhas caiu. Guardou os links
> das duas execuções.

**▶ Agora no G1:** encontre o seu cenário no log do pipeline, **antes** e **depois** da
correção. (Nunca usou o GitHub Actions? Leia antes [`comum/06`](../comum/06-pipeline-do-zero.md)
§1 a §5.)

**A execução de "antes"** (o código ainda vulnerável):

1. No GitHub, abra o repositório do grupo → aba **Actions** → à esquerda, **SSDLC - VidaPlus**.
2. Clique no filtro **Branch** (acima da lista) e escolha `main`.
3. Ache a execução cujo título começa com **"Merge pull request #"** e termina com
   `fix/F-01-sqli-login` (o merge da correção da Fase 4). A execução **logo abaixo** dela (mais antiga)
   é a de **antes**. Clique no título dela.
4. Na lista de jobs da esquerda, clique em **Gate - Testes de seguranca**.
5. Clique na etapa **Testes de aceite de seguranca (@Tag security)** para abri-la.
6. No canto superior direito do log, clique na caixa **Search logs**, digite
   `payload de injeção no login` e aperte **Enter**. O cenário aparece numa linha com `FAILURE!` e, logo abaixo,
   `expected: <401> but was: <200>`. Procure também `sqli_no_login` (o teste equivalente em JUnit).
7. Tire um print (**Shift+Print Screen**, selecione a área do log) e salve como
   `entregaveis/evidencias/fase5-ci-antes.png`. Copie a URL da barra do navegador para o rascunho.

**A execução de "depois"** (com a correção):

8. Volte à lista: aba **Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main`. Clique no
   título da execução **"Merge pull request #… fix/F-01-sqli-login"**.
9. Clique no job **Gate - Testes de seguranca** → etapa **Testes de aceite de seguranca (@Tag
   security)** → **Search logs** → `payload de injeção no login`. Agora o cenário **não aparece** na lista de
   falhas. Procure também `Tests run:` e compare a última linha (`Tests run: X, Failures: Y`) com a
   de antes: o número de falhas diminuiu.
10. Print em `entregaveis/evidencias/fase5-ci-depois.png` e a URL no rascunho.

- Junte as saídas locais da Fase 4: `fase4-teste-antes.txt` e `fase4-teste-depois.txt`.


## Passo 2 — Ataque de novo, por outros caminhos

> 🔎 **Como o Grupo 5 fez:** com os testes verdes, fez o papel do atacante e tentou **chegar ao
> mesmo dado por outro caminho**:
>
> ```bash
> curl -s "localhost:8080/api/pacientes/busca?q="                       # termo vazio, sem token
> curl -s -G localhost:8080/api/pacientes/busca --data-urlencode "q=%"  # curinga, sem token
> ```
>
> As duas devolveram **todos os pacientes**. A injeção estava corrigida, mas a busca não pedia
> login e o `%` funcionava como "qualquer coisa". O grupo criou um requisito novo
> (REQ-AUTH-BUSCA-01), registrou o achado, revisou a árvore, o RIPD e a matriz. Leia a 💡 "Teste
> verde não é o fim" em [`docs/12` §7](../../docs/12-exemplo-dossie-preenchido.md#7-fase-5--verificação--evidências).

**▶ Agora no G1:** o objetivo do atacante continua o mesmo da sua árvore (Fase 2a): **entrar como
outra pessoa**. Com a correção no ar, tente **cada ramo** da árvore de novo. Antes, religue o VidaPlus com o
código desta branch (criada a partir da `main` no Passo 0, já com a correção da Fase 4) e o banco
original:

```bash
bash roteiros/comum/zerar-banco.sh
source roteiros/comum/tokens.sh
mkdir -p entregaveis/evidencias
```

Roteiro mínimo:

1. **Outras formas de injeção no login** (a correção tem que fechar todas, não só a do teste):

   ```bash
   for EMAIL in "admin@vidaplus.local'#" "' OR '1'='1' -- " "admin@vidaplus.local' OR '1'='1"; do
     curl -s -o /dev/null -w "%{http_code}  <- $EMAIL\n" -X POST localhost:8080/api/auth/login \
       -H "Content-Type: application/json" -d "{\"email\":\"$EMAIL\",\"senha\":\"x\"}"
   done | tee entregaveis/evidencias/fase5-variacoes-injecao.txt
   ```

   O `for` tenta o login três vezes, cada uma com uma forma diferente de injeção no e-mail, e
   mostra o código de cada tentativa. Esperado: **401** nas três.
2. **Injeção no reset** (o `porEmail` também foi corrigido?):

   ```bash
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" \
     -d "{\"email\":\"x' OR '1'='1\"}" | tee entregaveis/evidencias/fase5-injecao-reset.txt; echo
   ```

   Esperado: uma linha JSON com `"mensagem"`. Antes, ele "encontrava" o usuário 1 (o ADMIN). E
   agora?
3. **Os ramos que não eram injeção.** Eles **continuam funcionando**? (Devem continuar: estão no
   plano.) Teste cada um e registre que o risco segue aberto.

   **Força bruta** — 10 senhas erradas seguidas para a Ana (alguma é bloqueada?):

   ```bash
   for i in 1 2 3 4 5 6 7 8 9 10; do
     curl -s -o /dev/null -w '%{http_code} ' -X POST localhost:8080/api/auth/login \
       -H "Content-Type: application/json" -d "{\"email\":\"ana.paciente@email.com\",\"senha\":\"errada$i\"}"
   done | tee entregaveis/evidencias/fase5-forca-bruta.txt; echo
   ```

   (Depois da correção, cada tentativa deve dar **401** — e nenhuma é bloqueada.)

   **Token depois do Sair** — entre como Ana na tela, copie o `access_token` (**F12 →
   Armazenamento/Aplicativo → Armazenamento local → `http://localhost:4200`**), clique em **Sair**
   e teste o token copiado:

   ```bash
   TOKEN_VELHO='cole-aqui-o-token'
   echo "${TOKEN_VELHO:0:10}"
   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN_VELHO" \
     | tee entregaveis/evidencias/fase5-token-depois-do-sair.txt
   ```

   Troque `cole-aqui-o-token` pelo token copiado, mantendo as aspas. Esperado: a segunda linha
   mostra `eyJhbGciOi` (se mostrar `cole-aqui-`, a colagem falhou) e a terceira, um código de
   três dígitos.

   **Código de recuperação previsível** — peça duas vezes e compare os códigos:

   ```bash
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" -d '{"email":"ana.paciente@email.com"}'; echo
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" -d '{"email":"ana.paciente@email.com"}'; echo
   ```

   Esperado: duas linhas com `"reset_token":"5-...`. Compare os números depois do `5-`.

4. **Uma ideia de vocês:** que outro caminho a árvore não tinha? Tente, e grave a saída com
   `| tee entregaveis/evidencias/fase5-NOME-DO-TESTE.txt` no fim do comando (troque `NOME-DO-TESTE`
   por um nome curto, sem espaços).

**Achou algo novo?** Faça o que o Grupo 5 fez: novo requisito na §2 (com `.feature @analise`),
linha no STRIDE e na árvore (§3), linha no plano (§6.2) e, se for de privacidade, no RIPD (§4.3).

## Passo 3 — Leia os scanners como evidência

> 🔎 **Como o Grupo 5 fez:**
>
> - **Semgrep:** `vidaplus-sql-concat` caiu de 7 para 0. Mas o grupo escreveu duas ressalvas:
>   *"zero achados não prova que a classe acabou"* (a regra é um padrão de texto) e *"os números
>   são só das regras próprias do projeto"*.
> - **CodeQL:** uma segunda ferramenta, que segue o caminho do dado. Ela **não** acusava os
>   métodos que recebem um número (`long`), e o grupo explicou por quê.
> - **ZAP:** o modo do pipeline é **passivo** (só observa, não ataca), então não detecta injeção
>   nem antes nem depois. O grupo disse isso, em vez de fingir que o ZAP provava algo.

**▶ Agora no G1:**

1. **Semgrep:** mostre, lado a lado, a contagem de achados por regra antes e depois da correção
   (os arquivos que você gravou na Fase 4):

   ```bash
   echo "== ANTES";  cat entregaveis/evidencias/fase4-semgrep-contagem-antes.txt
   echo "== DEPOIS"; cat entregaveis/evidencias/fase4-semgrep-contagem-depois.txt
   ```

   Esperado: as duas contagens, uma embaixo da outra (como a da Fase 4: `vidaplus-sql-concat` com
   7 antes e 4 depois). Quantos `vidaplus-sql-concat` havia antes e depois? Para ver **em quais arquivos** ficaram, abra
   `entregaveis/evidencias/fase4-semgrep-depois.txt` no VS Code e procure (**Ctrl+F**)
   `sql-concat`. E a regra
   `vidaplus-weak-hash-md5`, que aponta o MD5 do seu pacote: continua lá? Por quê?
2. **CodeQL:** no repositório do grupo no GitHub, aba **Security** → menu da esquerda **Code
   scanning** → filtro **Tool** = `CodeQL` → na caixa de busca, digite `sql`. Os alertas do `UsuarioDao` sumiram depois do merge? (Se a aba não mostrar alertas, diga
   isso no dossiê e use o Semgrep.)
3. **ZAP:** abra a execução de "depois" (aba **Actions** → **SSDLC - VidaPlus** → filtro **Branch** = `main` → execução **"Merge pull request #… fix/F-01-sqli-login"**) → job **Gate - DAST autenticado
   (OWASP ZAP)** → etapa **ZAP Baseline autenticado (injeta header Authorization)** → **Search
   logs** → digite `WARN-NEW`. Cada linha encontrada é um alerta (como ler:
   [`comum/07` §5](../comum/07-ferramentas-de-seguranca.md#5-owasp-zap--examina-o-sistema-ligado-de-fora)).
   Procure os **cabeçalhos ausentes** da F-17: `Content Security Policy`, `Strict-Transport-Security`,
   `X-Frame-Options`. Para ver o mesmo no seu computador (contra o site):

   ```bash
   docker run --rm --network host ghcr.io/zaproxy/zaproxy:stable \
     zap-baseline.py -t http://localhost:4200 -I | tee entregaveis/evidencias/fase5-zap-site.txt
   ```

   A primeira vez baixa uma imagem grande (cerca de 1,2 GB) e demora alguns minutos; depois, cada
   execução leva 1–2 minutos. Esperado: muitas linhas `PASS: ...`, algumas `WARN-NEW: ...` e, no
   fim, uma linha de resumo como `FAIL-NEW: 0  ...  WARN-NEW: 8  ...  PASS: 59`. Procure nas linhas
   `WARN-NEW` os cabeçalhos da F-17. Os cabeçalhos ausentes são evidência do **plano** da F-17 — o ZAP
   vai continuar acusando até alguém corrigir.

## Passo 4 — Monte a tabela da §7

> 🔎 **Como o Grupo 5 fez:** uma linha por verificação, inclusive as **manuais** e as que deram
> **🔴** (achados). Veja: [`docs/12` §7](../../docs/12-exemplo-dossie-preenchido.md#7-fase-5--verificação--evidências).

**▶ Agora no G1:** colunas: requisito | teste | ferramenta | antes → depois | evidência (link ou
arquivo). Inclua:

- `sqli.feature` e o `SegurancaAceiteTest.sqli_no_login_nao_autentica` (🔴 → 🟢);
- o `login_valido_funciona` (🟢 → 🟢: o uso legítimo não quebrou);
- as variações manuais do Passo 2;
- o Semgrep;
- os testes manuais dos ramos que continuam abertos (🔴 → 🔴, "no plano").

Escreva também o parágrafo dos **scanners** (Passo 3) e uma frase sobre o **gate de testes**: ele
segue vermelho por causa dos testes de outros pacotes.

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| As variações de injeção deram `200` | a correção da Fase 4 não está nesta branch, ou o VidaPlus não foi remontado | `git log --oneline -3` deve mostrar o merge do `fix/F-01-sqli-login`; depois, `zerar-banco.sh` |
| O ZAP demora muito na primeira vez | ele baixa uma imagem de ~1,2 GB | espere; das próximas vezes leva 1–2 minutos |
| No Windows, o ZAP diz que não alcança `localhost:4200` | no Docker Desktop, o `--network host` pode não funcionar | troque `--network host` por nada e `http://localhost:4200` por `http://host.docker.internal:4200` |

---

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

- [ ] Tenho os links das execuções do pipeline antes e depois, com prints.
- [ ] Ataquei de novo por pelo menos três caminhos diferentes e registrei o resultado.
- [ ] O que continua aberto está registrado como aberto (e está no plano).
- [ ] Cada scanner tem resultado **e** limitação escritos.
