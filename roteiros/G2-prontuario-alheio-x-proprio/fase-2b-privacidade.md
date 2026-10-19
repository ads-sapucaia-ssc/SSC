# G2 · Fase 2b — Privacidade: LINDDUN, Privacy by Design e RIPD

> **Seção do dossiê:** §4 · **Lidera:** 📋 DPO · **Aula:** 4 · **Semana:** 5
> Junto com a 2a, é **a fase de maior peso** na avaliação.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G2**.

## O que você entrega no fim

1. **§4.1** — tabela LINDDUN por elemento do DFD.
2. **§4.2** — as decisões de **Privacy by Design** (7 princípios).
3. **§4.3** — o esboço do **RIPD** (LGPD art. 38).
4. A frase de **minimização**.

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
   git checkout -b dossie/fase-2b
   git push -u origin dossie/fase-2b
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-2b
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

O **STRIDE** procura **ataques**. O **LINDDUN** procura ameaças à **privacidade** — muitas sem
ataque nenhum, com o sistema funcionando como foi feito. Pergunta-chave:

> *Se ninguém invadir nada e o sistema fizer exatamente o que foi programado, ainda assim a
> privacidade de alguém é atingida?*

| Letra | Categoria | Pergunta |
|---|---|---|
| **L** | Vinculação (*Linking*) | dá para **ligar** registros como sendo da mesma pessoa? |
| **I** | Identificação (*Identifying*) | dá para descobrir **quem é** a pessoa por trás de um dado? |
| **N** | Não-repúdio (*Non-repudiation*) | o sistema **prova demais** o que a pessoa fez, contra o interesse dela? |
| **D** | Detecção (*Detecting*) | dá para **saber que algo existe**, mesmo sem ver o conteúdo? |
| **D** | Divulgação (*Data disclosure*) | o sistema coleta, guarda ou mostra **mais do que precisa**? |
| **U** | Desconhecimento (*Unawareness*) | a pessoa **não sabe** o que é feito com os dados dela, ou **não consegue intervir**? |
| **N** | Não-conformidade (*Non-compliance*) | o tratamento **descumpre a lei**? |

**Privacy by Design:** sete princípios (proativo; privacidade como padrão; embarcada no design;
soma positiva; ponta a ponta; transparência; respeito ao usuário). **RIPD:** o relatório de impacto
do art. 38. Material: [`docs/03`](../../docs/03-privacy-by-design-linddun.md).

**Para o G2, o "U" é central:** a F-09 existe porque hoje o titular **não consegue exercer** o
direito de receber os próprios dados (art. 18, V). E a correção do P0 pode criar, sem querer, um
"D" — veja o Passo 1.

---

## Passo 1 — Procure ameaças que acontecem **sem ataque**

> 🔎 **Como o Grupo 5 fez:** usou a busca do jeito normal e perguntou o que ela revelava. Uma busca
> específica devolveu **tudo de uma pessoa** (categoria **I**) — sem injeção. E pesquisar um CPF e
> receber 1 resultado (ou nenhum) revela **se a pessoa é paciente** (categoria **D**).

**▶ Agora no G2:**

1. **O oráculo de existência (D).** Na Fase 2a você viu que pedir um exame que **não existe** dá
   uma resposta diferente de pedir um que existe. Pense na **correção do P0**: se ela responder
   **403** para "exame de outra pessoa" e **404** para "exame que não existe", o atacante, sem ver
   conteúdo nenhum, descobre **quais números existem**. Leia a linha "Detectabilidade — oráculo de
   existência" no fim do [`docs/04`](../../docs/04-catalogo-funcionalidades.md#ameaças-de-privacidade-linddun-candidatas-a-versão-futura).
   Que resposta a correção de vocês deveria dar nos dois casos?
2. **Números sequenciais (L).** 101, 102, 103, 104: quem tem um número adivinha os outros. E o
   número do exame aparece na **URL** (`/exames/104`) — onde mais ele fica gravado? (Histórico do
   navegador num computador compartilhado; prints; links mandados por mensagem.)
3. **Excesso na resposta (D, divulgação).** Peça um exame da Ana com o token dela e veja **todos**
   os campos devolvidos:

   ```bash
   source roteiros/comum/tokens.sh   # carrega TOKEN_ANA, TOKEN_HOUSE... (rode de novo se abriu outro terminal)
   curl -s localhost:8080/api/exames/101 -H "Authorization: Bearer $TOKEN_ANA" | python3 -m json.tool
   ```

   Esperado: as linhas `TOKEN_... ok` e o exame 101 em JSON formatado, um campo por linha (`"id"`,
   `"paciente_id"`, `"tipo"`, `"resultado"`, `"laudo_pdf"`, `"publicado_por"`...). A tela usa
   todos? O paciente precisa saber o **número interno do funcionário** que publicou
   (`publicado_por`)? E o caminho do arquivo (`laudo_pdf`)?
4. **O arquivo (I).** Abra um PDF de laudo. O que nele identifica a pessoa?
5. **O médico vê tudo do paciente?** A Dra. House atende a Carla: ela deveria ver **todos** os
   exames da Carla, ou só os que ela pediu? (Pense em "necessidade de saber".)

## Passo 2 — Analise a exportação (F-09) como privacidade

> 🔎 **Como o Grupo 5 fez:** no pacote dele, a criação era a anonimização; ele analisou cada
> atributo (identificador direto, quase-identificador, texto livre, **arquivo**) e descobriu que o
> dado "escapa" por lugares que ninguém lembra (o PDF, as datas, os backups). Leia a 💡
> "Supressão não é anonimato" em [`docs/12` §4.1](../../docs/12-exemplo-dossie-preenchido.md#41-linddun-per-element).

**▶ Agora no G2** — a exportação é o **espelho**: em vez de esconder, **entregar**. Responda:

1. **U (intervenção):** sem a F-09, que direito a Ana não consegue exercer? Que artigo?
2. **O que entra** na exportação? Liste as tabelas com dados da Ana (Fase 0, Passo 4). Entram as
   **notas clínicas** escritas pelo médico sobre ela? E os **PDFs**?
3. **O que NÃO pode entrar:** dados de **outras** pessoas que aparecem junto (o nome do médico? o
   número do funcionário que publicou?). Se a exportação vazar dado de terceiro, que categoria é?
4. **I/D (entrega):** como o arquivo chega à Ana? Um link mandado por e-mail, que qualquer um com o
   link abre, seria seguro? Por quanto tempo o arquivo fica disponível?
5. **S:** como o sistema tem certeza de que quem pede é a Ana? (É o mesmo problema do P0.)

## Passo 3 — Monte a tabela LINDDUN

> 🔎 **Como o Grupo 5 fez:** uma linha por **ameaça**: elemento, letra(s), ameaça concreta,
> controle/PET e requisito. Chegou a 11 linhas. Veja: [`docs/12` §4.1](../../docs/12-exemplo-dossie-preenchido.md#41-linddun-per-element).

**▶ Agora no G2:** use os elementos do **seu DFD**. Para cada ameaça: **ameaça concreta** (com o
que você observou), **controle/PET** (ex.: resposta igual para "não existe" e "não é seu";
identificadores não sequenciais; resposta mínima; exportação autenticada com link de curta
duração) e **requisito** (crie, se faltar, e volte à §2 com o `.feature @analise`).

## Passo 4 — Escreva as decisões de Privacy by Design

> 🔎 **Como o Grupo 5 fez:** uma **decisão concreta** e a **evidência** por princípio; "(plano)"
> ou "—" quando ainda não havia evidência; e as **lacunas assumidas** escritas. Veja:
> [`docs/12` §4.2](../../docs/12-exemplo-dossie-preenchido.md#42-privacy-by-design-decisões).

**▶ Agora no G2:**

| Princípio | Pergunta para o G2 |
|---|---|
| Proativo | que regra de acesso vocês definem **antes** de alguém reclamar? |
| Privacidade como padrão | qual é a resposta **padrão** quando o servidor não sabe se a pessoa pode ver? (*deny-by-default*) |
| Embarcada no design | a verificação fica no **servidor**, em todos os caminhos (exame, PDF, listagem), ou só na tela? |
| Soma positiva | depois da correção, o paciente ainda vê os **próprios** exames? A médica, os dos pacientes dela? O laboratório ainda consegue publicar? |
| Ponta a ponta | o laudo protegido na API, mas e o **arquivo**? |
| Transparência | o paciente sabe quem viu o laudo dele? |
| Respeito ao usuário | a F-09: o paciente recebe os próprios dados sem precisar pedir por escrito |

## Passo 5 — Esboce o RIPD

> 🔎 **Como o Grupo 5 fez:** começou pela **descrição do tratamento** (dados, titulares,
> finalidade, prazo) e pelas **escalas**; depois, uma linha por ameaça, com risco residual
> **honesto** (*"Crítico até ..."* quando a medida era só plano). Veja:
> [`docs/12` §4.3](../../docs/12-exemplo-dossie-preenchido.md#43-ripddpia-esboço--lgpd-art-38).

**▶ Agora no G2:**

1. **Descrição:** dados (resultados, laudos, vínculo médico–paciente); titulares (pacientes);
   finalidade (entregar o resultado ao paciente e ao médico dele; publicar após conferência);
   prazo (hoje: para sempre?).
2. **Escalas** definidas.
3. **Tabela** com pelo menos três ameaças. Pense em: um paciente vendo o laudo de outro; um laudo
   **não publicado** baixado direto da pasta (o paciente pode saber de um resultado grave **antes**
   de o médico conversar com ele — que impacto isso tem?); o titular sem conseguir os próprios
   dados.

## Passo 6 — Minimização

> 🔎 **Como o Grupo 5 fez:** *"religião, estado civil e renda são coletados sem finalidade —
> devem sair; a remoção é do F-15, de outro pacote."*

**▶ Agora no G2:** o que as respostas do seu pacote devolvem **sem necessidade**? (Passo 1, item 3.)

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| `python3 -m json.tool` diz `Expecting value` | a resposta veio vazia (o VidaPlus caiu ou o token está vazio) | tire o `| python3 -m json.tool` para ver a resposta crua; veja a tabela comum |

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-2b`:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 2b - privacidade"
git push -u origin dossie/fase-2b
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
   **compare** escolha `dossie/fase-2b` → **Create pull request**.
2. Título: "Dossiê — Fase 2b: privacidade". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] Analisei o oráculo de existência e decidi como a correção deve responder.
- [ ] A F-09 foi analisada: o que entra, o que não entra, como é entregue, como se confirma quem
      pede.
- [ ] Toda ameaça LINDDUN tem controle e requisito (ou "outro pacote").
- [ ] Os 7 princípios têm decisão concreta.
- [ ] O RIPD tem descrição, escalas e residual honesto.
