# G4 · Fase 5 — Verificação e evidências

> **Seção do dossiê:** §7 · **Lidera:** 💻 Dev e ⚙️ DevSecOps · **Aula:** 7 · **Semanas:** 11–12

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G4**.

## O que você entrega no fim

1. A tabela de verificação da §7: requisito → teste → ferramenta → antes/depois → evidência.
2. O registro dos **ataques manuais por outros canais** — e do que eles revelaram.
3. A leitura dos **scanners**, com as limitações de cada um.

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

| Tipo de prova | O que prova | Limite |
|---|---|---|
| **Teste automático** | que **aquele** ataque falha | só testa o que alguém escreveu |
| **Scanner** (SAST/DAST) | que um **padrão** conhecido sumiu | não sabe de quem é o laudo |
| **Ataque manual** | que **outros canais** estão fechados | depende de quem testa |

**Teste verde não é o fim** — e, no G4, **canal fechado não é dado protegido**, se houver outro
canal aberto.

---

## Passo 1 — Mostre o 🔴 → 🟢 no pipeline

> 🔎 **Como o Grupo 5 fez:** na aba **Actions**, abriu uma execução **antes** do PR e a **do PR**;
> procurou o nome do cenário no log: antes, `FAILURE!`; depois, fora da lista de falhas. Guardou os
> links.

**▶ Agora no G4:** encontre o seu cenário no log do pipeline, **antes** e **depois** da
correção. (Nunca usou o GitHub Actions? Leia antes [`comum/06`](../comum/06-pipeline-do-zero.md)
§1 a §5.)

**A execução de "antes"** (o código ainda vulnerável):

1. No GitHub, abra o repositório do grupo → aba **Actions** → à esquerda, **SSDLC - VidaPlus**.
2. Clique no filtro **Branch** (acima da lista) e escolha `main`.
3. Ache a execução cujo título começa com **"Merge pull request #"** e termina com
   `fix/F-02-canais-do-laudo` (o merge da correção da Fase 4). A execução **logo abaixo** dela (mais antiga)
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
   título da execução **"Merge pull request #… fix/F-02-canais-do-laudo"**.
9. Clique no job **Gate - Testes de seguranca** → etapa **Testes de aceite de seguranca (@Tag
   security)** → **Search logs** → `paciente não acessa o laudo`. Agora o cenário **não aparece** na lista de
   falhas. Procure também `Tests run:` e compare a última linha (`Tests run: X, Failures: Y`) com a
   de antes: o número de falhas diminuiu.
10. Print em `entregaveis/evidencias/fase5-ci-depois.png` e a URL no rascunho.

- Junte as saídas locais da Fase 4: `fase4-teste-antes.txt`, `fase4-teste-depois.txt`, `fase4-canais-antes.txt` e `fase4-canais-depois.txt`.


## Passo 2 — Ataque de novo, por outros canais

> 🔎 **Como o Grupo 5 fez:** com os testes verdes, tentou **chegar ao mesmo dado por outro
> caminho**: `q=` e `q=%` sem token devolveram **todos os pacientes**. A injeção estava corrigida,
> mas a busca não pedia login. Criou um requisito novo e revisou árvore, RIPD e matriz. Leia a 💡
> "Teste verde não é o fim" em [`docs/12` §7](../../docs/12-exemplo-dossie-preenchido.md#7-fase-5--verificação--evidências).

**▶ Agora no G4:** o objetivo continua o da sua árvore — **colher laudos**. Tente **cada canal** de
novo, com a correção no ar. Antes, prepare o terminal:

```bash
bash roteiros/comum/zerar-banco.sh
source roteiros/comum/tokens.sh
mkdir -p entregaveis/evidencias
```

(A branch desta fase foi criada a partir da `main` no Passo 0, então já tem a correção da Fase 4.
O `zerar-banco.sh` recompila esse código e volta o banco ao original.)

1. **O arquivo:**

   ```bash
   for ID in 102 104 105; do
     curl -s -o /dev/null -w "laudo-$ID.pdf -> %{http_code} %{content_type}\n" localhost:4200/laudos/laudo-$ID.pdf
   done
   ```

   O 102 e o 104 continuam saindo sem login? O 105 continua revelando que não existe? Registre como
   **aberto** (está no plano).
2. **Outro dado de saúde, outro canal:** `curl -s localhost:8080/api/pacientes/1/notas` — sem token
   nenhum. O que sai? Isso é o laudo? Não — mas é **dado de saúde** saindo por um canal que o
   recorte de vocês não cobria. De quem é? (V-19.) Registre como **interface**.
3. **O dado em que a regra confia.** A regra do médico depende da tabela `atendimento`:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s -o /dev/null -w 'House -> 102: %{http_code}\n' localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_HOUSE"
   curl -s -X POST localhost:8080/api/atendimentos -H "Authorization: Bearer $TOKEN_ANA" \
     -H "Content-Type: application/json" -d '{"medicoId":1,"pacienteId":2}'; echo
   curl -s -o /dev/null -w 'House -> 102: %{http_code}\n' localhost:8080/api/exames/102 -H "Authorization: Bearer $TOKEN_HOUSE"
   ```

   A Ana (paciente) criou um atendimento — e a médica passou a ver o exame do Bruno. Quem abriu o
   canal? (Controle por papel, F-03, do G3.) Registrem como **interface documentada**. **Zere o banco**
   (rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`).
4. **O efeito colateral bom.** A configuração de erros é global:
   `curl -s -G localhost:8080/api/pacientes/busca --data-urlencode "q='"`. O SQL ainda aparece? A
   correção de vocês fechou um canal **de outro pacote** também. Registrem.
5. **Uma ideia de vocês:** que outro canal a árvore não tinha? Tente.

Guarde cada saída em `entregaveis/evidencias/fase5-...txt`.

**Achou algo novo?** Faça como o Grupo 5: requisito na §2 (com `.feature @analise`), linha no
STRIDE e na árvore (§3), no plano (§6.2) e, se for de privacidade, no RIPD (§4.3).

## Passo 3 — Leia os scanners como evidência

> 🔎 **Como o Grupo 5 fez:** escreveu o resultado **e a limitação** de cada scanner — por exemplo,
> que o ZAP do pipeline é **passivo** (só observa as respostas, não ataca) e por isso não pegava a
> injeção.

**▶ Agora no G4** — para vocês, o fato de o ZAP **observar as respostas** é uma vantagem:

1. **ZAP (DAST):** abra a execução de "depois" (aba **Actions** → **SSDLC - VidaPlus** → filtro
   **Branch** = `main` → execução **"Merge pull request #… fix/F-02-canais-do-laudo"**) → job
   **Gate - DAST autenticado (OWASP ZAP)** → etapa **ZAP Baseline autenticado (injeta header
   Authorization)** → **Search logs**. Depois, volte à lista de execuções, clique na execução de "antes"
   (a que fica logo abaixo dessa) e abra o mesmo job, a mesma etapa e o **Search logs**. Como ler cada linha: [`comum/07` §5](../comum/07-ferramentas-de-seguranca.md#5-owasp-zap--examina-o-sistema-ligado-de-fora).
   Procure o alerta **"Application Error Disclosure"** (regra 90022 — está no
   `security/zap/rules.tsv`) e os alertas de **cabeçalhos ausentes** (CSP, X-Frame-Options). Ele
   aparecia **antes** da correção? E **depois**? (O ZAP só vê as páginas que ele visita — se o
   alerta não aparecer nem antes, explique por quê, em vez de concluir que está tudo bem.)

   Para ver os cabeçalhos ausentes no seu computador (examina o site, na porta 4200):

   ```bash
   docker run --rm --network host ghcr.io/zaproxy/zaproxy:stable \
     zap-baseline.py -t http://localhost:4200 -I | tee entregaveis/evidencias/fase5-zap-site.txt
   ```
2. **Semgrep/CodeQL:** acusavam o IDOR? Rode o Semgrep no seu computador:

   ```bash
   docker run --rm -v "$PWD":/src -w /src returntocorp/semgrep \
     semgrep scan --config security/semgrep/vidaplus.yml --metrics=off --json --quiet legacy-app \
     | grep -o '"check_id": *"[^"]*"' | sed 's/.*semgrep\.//; s/"$//' | sort | uniq -c
   ```

   Ele roda as regras do projeto e conta os achados de cada uma (como ler:
   [`comum/07` §2](../comum/07-ferramentas-de-seguranca.md#2-semgrep--procura-padrões-perigosos-no-código)).
   Alguma regra fala de dono de exame?

   Para o CodeQL: no GitHub, aba **Security** → **Code scanning** → filtro **Tool** = `CodeQL`
   (se a aba não mostrar alertas, diga isso no dossiê). Por que não acusam? (O código não tem nada de "errado" no texto.)
   E a configuração `include-stacktrace=always`? (Eles olham arquivos Java; o `.properties` ficou
   de fora.)
3. **Conclusão para o dossiê:** cada canal tem a sua melhor prova — o teste para a API, o ZAP para
   as respostas de erro, o `curl` para o arquivo.

## Passo 4 — Monte a tabela da §7

> 🔎 **Como o Grupo 5 fez:** uma linha por verificação, inclusive as **manuais** e as que deram
> **🔴** (achados). Veja: [`docs/12` §7](../../docs/12-exemplo-dossie-preenchido.md#7-fase-5--verificação--evidências).

**▶ Agora no G4:** colunas: requisito | teste | ferramenta | antes → depois | evidência. Uma linha
**por canal**:

- API: `idor.feature` e `SegurancaAceiteTest.idor_...` (🔴 → 🟢) + tabela de canais;
- erro: o cenário da F-16 (se virou `@security`) e/ou a tabela de canais (`exception`/`trace`
  sumiram) + o ZAP;
- arquivo: `curl` (🔴 → 🔴, no plano);
- notificação e site: "não verificável — planejado";
- e os achados do Passo 2.

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

- [ ] Tenho os links do pipeline antes e depois, e a tabela de canais antes e depois.
- [ ] Testei de novo **cada canal** e registrei o que segue aberto.
- [ ] Registrei as interfaces (V-19, F-03) e o efeito colateral na busca.
- [ ] Usei o ZAP como evidência do canal de erro — ou expliquei por que ele não mostrou.
