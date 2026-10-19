# G1 · Fase 2b — Privacidade: LINDDUN, Privacy by Design e RIPD

> **Seção do dossiê:** §4 · **Lidera:** 📋 DPO · **Aula:** 4 · **Semana:** 5
> Junto com a 2a, é **a fase de maior peso** na avaliação.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G1**.

## O que você entrega no fim

1. **§4.1** — tabela LINDDUN por elemento do DFD.
2. **§4.2** — as decisões de **Privacy by Design** (7 princípios).
3. **§4.3** — o esboço do **RIPD** (Relatório de Impacto à Proteção de Dados, LGPD art. 38).
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

**A diferença entre STRIDE e LINDDUN.** O STRIDE procura **ataques**: alguém fazendo o que não
devia. O **LINDDUN** procura ameaças à **privacidade** — e muitas acontecem **sem ataque nenhum**,
com o sistema funcionando exatamente como foi feito. A pergunta-chave do LINDDUN é:

> *Se ninguém invadir nada e o sistema fizer exatamente o que foi programado para fazer, ainda
> assim a privacidade de alguém é atingida?*

As sete categorias (a sigla vem do inglês):

| Letra | Categoria | Pergunta | Exemplo genérico |
|---|---|---|---|
| **L** | Vinculação (*Linking*) | dá para **ligar** dois registros como sendo da mesma pessoa? | o mesmo e-mail usado em vários sistemas |
| **I** | Identificação (*Identifying*) | dá para descobrir **quem é** a pessoa por trás de um dado? | um "anônimo" com data de nascimento e bairro |
| **N** | Não-repúdio (*Non-repudiation*) | o sistema **prova demais** o que a pessoa fez, contra o interesse dela? | um registro que mostra "fulano abriu o resultado de HIV às 22h" |
| **D** | Detecção (*Detecting*) | dá para **saber que algo existe**, mesmo sem ver o conteúdo? | uma mensagem "e-mail não cadastrado" |
| **D** | Divulgação (*Data disclosure*) | o sistema coleta, guarda ou mostra **mais dados do que precisa**? | uma tela que mostra renda sem precisar |
| **U** | Desconhecimento (*Unawareness*) | a pessoa **não sabe** o que é feito com os dados dela, ou **não consegue intervir**? | ninguém avisa por quanto tempo os dados ficam |
| **N** | Não-conformidade (*Non-compliance*) | o tratamento **descumpre a lei** ou a política? | guardar dados para sempre |

Repare no **N de não-repúdio**: no STRIDE, não-repúdio é sempre **bom** (quem fez não consegue
negar). No LINDDUN, ele pode ser **ruim** para o titular. **Isso é central para o G1**, porque a
trilha de auditoria (F-05) é de vocês.

**Privacy by Design (PbD)** — sete princípios para pôr a privacidade **no projeto**, e não no
fim: proativo; privacidade como padrão; embarcada no design; soma positiva; ponta a ponta;
transparência; respeito ao usuário. Material: [`docs/03`](../../docs/03-privacy-by-design-linddun.md).

**RIPD** — documento que a LGPD (art. 38) pede para tratamentos de risco: descreve o tratamento,
os riscos ao titular e as medidas.

---

## Passo 1 — Procure ameaças que acontecem **sem ataque**

> 🔎 **Como o Grupo 5 fez:** usou a busca **do jeito normal**, como a recepção usaria, e perguntou
> o que aquilo revelava:
>
> ```bash
> curl -s "localhost:8080/api/pacientes/busca?q=Silva" | python3 -m json.tool
> ```
>
> Uma busca bem específica (nome ou CPF) devolveu **o registro completo de uma única pessoa**,
> com religião e renda — sem injeção nenhuma. Escreveu: *"parametrizar não resolve"* (categoria
> **I**). E percebeu um segundo efeito: pesquisar um CPF e receber 1 resultado (ou nenhum) revela
> **se a pessoa é paciente da clínica** — categoria **D** (detecção).

**▶ Agora no G1:** use cada funcionalidade **normalmente** e pergunte o que ela revela.

1. **Recuperação de senha (F-12):**

   ```bash
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" -d '{"email":"bruno.paciente@email.com"}'; echo
   curl -s -X POST localhost:8080/api/auth/reset -H "Content-Type: application/json" -d '{"email":"vizinho@teste.com"}'; echo
   ```

   Esperado: duas linhas JSON, cada uma com um campo `"mensagem"`. Imagine que você conhece o
   e-mail do seu vizinho. O que você descobre sobre ele, **sem invadir
   nada**? Numa **clínica**, "ter conta" significa o quê? Que categoria é essa?
2. **Login (F-01):** compare as respostas de "senha errada" e de "e-mail inexistente":

   ```bash
   curl -s -i -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"ana.paciente@email.com","senha":"errada"}' | head -1
   curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"ana.paciente@email.com","senha":"errada"}'; echo
   curl -s -i -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"naoexiste@teste.com","senha":"errada"}' | head -1
   curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"naoexiste@teste.com","senha":"errada"}'; echo
   ```

   Esperado: quatro linhas — para cada e-mail, uma `HTTP/1.1 ...` e um JSON. São iguais? Se
   fossem diferentes, seria a mesma ameaça do item anterior.
3. **Token (F-11):** decodifique de novo o conteúdo do token da Ana:

   ```bash
   source roteiros/comum/tokens.sh
   echo "$TOKEN_ANA" | cut -d. -f2 | base64 -d; echo
   ```

   Esperado: as seis linhas `TOKEN_... ok` e, depois, um JSON curto com `"sub"` e `"papel"`.
   Ele é **legível por qualquer
   um** que o veja (não é cifrado, só assinado). O que ele revela? E onde ele fica guardado num
   **computador compartilhado** (como os do laboratório), depois que a pessoa fecha o navegador sem
   clicar em Sair?
4. **E-mail como login:** a pessoa usa o mesmo e-mail em outros serviços. Que categoria (pense em
   ligar bases diferentes)?

## Passo 2 — Analise a trilha de auditoria (F-05) como ameaça à privacidade

> 🔎 **Como o Grupo 5 fez:** no pacote dele, a auditoria era de outro grupo, mas ele registrou a
> ameaça: *"log com CPF identificaria mesmo após anonimizar o cadastro"* (categoria **I**), com o
> controle *"`user_id` opaco (pseudônimo)"* (REQ-PRIV-PSEUDO-01).

**▶ Agora no G1** — aqui a trilha é **de vocês**, e ela é ao mesmo tempo **controle de segurança**
e **risco de privacidade**. Imagine a tabela de auditoria já funcionando e responda:

1. **N (não-repúdio):** que eventos registrados poderiam **expor o titular** (horário em que ele
   abriu um exame, de onde acessou)? Quem deveria poder ler esses registros?
2. **I (identificação):** o modelo em `entity/Auditoria.java` guarda `user_id` e `recurso`. Que
   dados **nunca** podem ir para lá (releia o comentário da classe)? Por quê?
3. **L (vinculação):** juntando os registros de vários dias, dá para montar a **rotina** de uma
   pessoa?
4. **N (não-conformidade):** por quanto tempo a trilha deve ser guardada? Guardar para sempre é
   problema?
5. **U (desconhecimento):** o usuário sabe que os acessos dele são registrados? Ele pode consultar
   os próprios registros?

Cada resposta vira uma linha da tabela com um **controle** (ex.: pseudônimo, acesso restrito ao
log, prazo de guarda, aviso de privacidade).

## Passo 3 — Monte a tabela LINDDUN por elemento

> 🔎 **Como o Grupo 5 fez:** uma linha por **ameaça** (não por elemento): elemento, letra(s),
> ameaça concreta, controle/PET (*Privacy Enhancing Technology*) e requisito. Chegou a 11 linhas.
> Veja: [`docs/12` §4.1](../../docs/12-exemplo-dossie-preenchido.md#41-linddun-per-element).

**▶ Agora no G1:** use os elementos do **seu DFD** (Fase 2a). Para cada um, passe pelas sete
categorias e anote as que se aplicam. Para cada ameaça:

- **Ameaça concreta:** o que acontece, com quem, a partir do que você observou;
- **Controle/PET:** a medida (ex.: resposta igual para e-mail existente ou não; pseudônimo no log;
  prazo de guarda; aviso de privacidade);
- **Requisito:** o ID da Fase 1. **Ameaça de privacidade sem requisito → crie o requisito** e
  volte à §2 (com o `.feature @analise`).

## Passo 4 — Escreva as decisões de Privacy by Design

> 🔎 **Como o Grupo 5 fez:** para cada princípio, uma **decisão concreta** e a **evidência** (onde
> ela aparece). Ex.: *"Privacidade como padrão — `?` sempre; busca só com login (plano)"*;
> *"Ponta a ponta — ... Lacunas assumidas: o arquivo PDF depende do F-08"*. Quando ainda não havia
> evidência, escreveu "—" ou "(plano)". Veja: [`docs/12` §4.2](../../docs/12-exemplo-dossie-preenchido.md#42-privacy-by-design-decisões).

**▶ Agora no G1:** uma linha por princípio. Perguntas para cada um:

| Princípio | Pergunta para o G1 |
|---|---|
| Proativo | que problema do login/sessão vocês vão prevenir **antes** de ele acontecer? |
| Privacidade como padrão | o que a trilha registra **por padrão**? (O mínimo, sem dado pessoal.) |
| Embarcada no design | onde, no fluxo de login, a proteção entra (não "depois")? |
| Soma positiva | a correção não pode piorar o uso: o login legítimo continua funcionando? A trava contra força bruta não tranca o usuário honesto para sempre? |
| Ponta a ponta | o ciclo de vida da credencial e do registro: criação, uso, guarda, **descarte** |
| Transparência | o usuário é avisado de que os acessos são registrados? |
| Respeito ao usuário | o usuário consegue ver os últimos acessos à **própria** conta (como nos bancos)? |

## Passo 5 — Esboce o RIPD

> 🔎 **Como o Grupo 5 fez:** começou pela **descrição do tratamento** (dados, titulares,
> finalidade, prazo) e pelas **escalas** (Baixa/Média/Alta; Risco = combinação). Depois, uma linha
> por ameaça de privacidade, com o **risco residual** depois da medida — e foi honesto: onde a
> medida ainda é plano, o residual continua alto (*"Crítico até o REQ-AUTH-BUSCA-01"*). Veja:
> [`docs/12` §4.3](../../docs/12-exemplo-dossie-preenchido.md#43-ripddpia-esboço--lgpd-art-38).

**▶ Agora no G1:**

1. **Descrição do tratamento** — quatro itens:
   - **Dados:** os do inventário do G1 (credencial, e-mail, papel, token, registro de acessos);
   - **Titulares:** quem tem conta (pacientes **e** funcionários);
   - **Finalidade:** autenticar, manter a sessão, recuperar o acesso, registrar acessos;
   - **Por quanto tempo:** hoje (o que você observou) e o proposto.
2. **Escalas:** copie a do exemplo ou defina a de vocês — mas **defina**.
3. **Tabela:** ameaça → probabilidade → impacto **ao titular** → risco → medida → risco residual.
   Pelo menos três linhas. Ideias de ameaça para avaliar: tomada de conta de um paciente; registro
   de auditoria expondo hábitos do paciente; descoberta de quem é paciente pela recuperação de
   senha.

## Passo 6 — Minimização

> 🔎 **Como o Grupo 5 fez:** *"religião, estado civil e renda são coletados sem finalidade —
> devem sair; a remoção é do F-15, de outro pacote."*

**▶ Agora no G1:** que dado **do seu pacote** é coletado, guardado ou devolvido **sem
necessidade**? Pistas: o que a resposta do login devolve além do token (Fase 0, Passo 3)? O que vai
dentro do token? O que o reset devolve na resposta?

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

Os desta fase:

| O que aparece | Causa provável | O que fazer |
|---|---|---|
| As respostas de "senha errada" e "e-mail inexistente" parecem diferentes só na hora | a linha `Date:` muda a cada segundo | compare só a primeira linha (`HTTP/1.1 ...`) e o corpo JSON |

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

- [ ] Achei ao menos uma ameaça que acontece **sem ataque** (o sistema funcionando normalmente).
- [ ] A trilha de auditoria (F-05) aparece como **controle** e também como **risco** de privacidade.
- [ ] Toda ameaça LINDDUN tem controle e requisito (ou "outro pacote").
- [ ] Os 7 princípios têm decisão concreta (ou "plano"/"—" com justificativa).
- [ ] O RIPD tem descrição do tratamento, escalas e risco residual honesto.

## Erros comuns

- **Repetir o STRIDE com outro nome.** Se a ameaça precisa de um atacante invadindo, ela é STRIDE.
  LINDDUN é sobre o que o sistema **faz** com os dados.
- **Esquecer que o registro de auditoria também trata dados pessoais.**
- **Risco residual "Baixo" para medida que ainda é só plano.**
