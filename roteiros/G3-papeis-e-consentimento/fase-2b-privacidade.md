# G3 · Fase 2b — Privacidade: LINDDUN, Privacy by Design e RIPD

> **Seção do dossiê:** §4 · **Lidera:** 📋 DPO · **Aula:** 4 · **Semana:** 5
> Para o G3, esta é **a fase central**: três das quatro funcionalidades são de privacidade.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G3**.

## O que você entrega no fim

1. **§4.1** — tabela LINDDUN por elemento do DFD.
2. **§4.2** — as decisões de **Privacy by Design**.
3. **§4.3** — o esboço do **RIPD** (LGPD art. 38).
4. A frase de **minimização** — que, no G3, é uma funcionalidade inteira (F-15).

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
ataque nenhum. Pergunta-chave:

> *Se ninguém invadir nada e o sistema fizer exatamente o que foi programado, ainda assim a
> privacidade de alguém é atingida?*

| Letra | Categoria | Pergunta |
|---|---|---|
| **L** | Vinculação (*Linking*) | dá para **ligar** registros como sendo da mesma pessoa? |
| **I** | Identificação (*Identifying*) | dá para descobrir **quem é** a pessoa por trás de um dado? |
| **N** | Não-repúdio (*Non-repudiation*) | o sistema **prova demais** o que a pessoa fez, contra ela? |
| **D** | Detecção (*Detecting*) | dá para **saber que algo existe**, sem ver o conteúdo? |
| **D** | Divulgação (*Data disclosure*) | o sistema coleta, guarda ou mostra **mais do que precisa**? |
| **U** | Desconhecimento (*Unawareness*) | a pessoa **não sabe** o que fazem com os dados dela, ou **não consegue intervir** (consentir, revogar, corrigir)? |
| **N** | Não-conformidade (*Non-compliance*) | o tratamento **descumpre a lei**? |

**O "U" tem duas metades** — e o G3 tem as duas:

- **Desconhecimento:** a pessoa **não sabe** (F-15: ninguém diz para que a religião é coletada);
- **Incapacidade de intervir:** a pessoa **sabe, mas não consegue agir** (F-04: não há como dizer
  não nem voltar atrás; F-19: não há como corrigir o próprio cadastro).

Material: [`docs/03`](../../docs/03-privacy-by-design-linddun.md).

---

## Passo 1 — Procure ameaças que acontecem **sem ataque**

> 🔎 **Como o Grupo 5 fez:** usou a busca do jeito normal e viu que uma busca específica devolvia
> **tudo de uma pessoa** (I) — sem injeção. E que pesquisar um CPF revela **se a pessoa é paciente**
> (D).

**▶ Agora no G3:**

1. **Minimização (F-15) — divulgação por volume.** Entre como **Recepção** e faça uma busca
   qualquer em **Pacientes**. As colunas **Religião** e **Renda mensal** aparecem na tela. A
   recepção precisa delas para **localizar** um paciente? E para agendar? Use a sua lista coluna |
   quem usa | finalidade (Fase 0): cada coluna **sem finalidade** é uma ameaça de **D**
   (divulgação), **U** (o paciente não sabe para que é) e **N** (descumpre o art. 6º, III).
2. **Consentimento (F-04) — incapacidade de intervir.** Escolham **uma finalidade opcional**
   plausível para a clínica (ex.: usar os dados em pesquisa; enviar campanhas de saúde por e-mail;
   compartilhar com um laboratório parceiro). Hoje, o paciente consegue dizer **não**? Consegue
   **mudar de ideia**? Sabe **qual versão** do termo aceitou?
3. **Perfil (F-19) — incapacidade de intervir.** A Ana consegue **ver** o próprio cadastro
   completo? **Corrigir** o telefone? (Fase 0, Passo 6.)
4. **Agenda — divulgação.** A agenda mostra quem se trata com qual médico. A médica, quando abre
   "Meus Pacientes", vê o **CPF** dos pacientes: ela precisa?
5. **O papel no token — divulgação.** O token mostra o papel e o número do usuário a quem o ler.
   Isso importa?

## Passo 2 — Analise o consentimento (F-04) como desenho de privacidade

> 🔎 **Como o Grupo 5 fez:** no pacote dele, a criação era a anonimização; analisou **cada
> atributo** e descobriu que o dado "escapa" por onde ninguém lembra. Leia a 💡 "Supressão não é
> anonimato" em [`docs/12` §4.1](../../docs/12-exemplo-dossie-preenchido.md#41-linddun-per-element).

**▶ Agora no G3:** para a finalidade escolhida no Passo 1, responda:

1. **O que precisa ficar registrado** para a clínica **provar** o consentimento (art. 8º, §2º)?
   (Partam de `entity/Consentimento.java`: paciente, finalidade, base legal, versão do termo,
   quando concedeu, quando revogou.)
2. **E se o termo mudar** (versão 2.0)? O consentimento dado na 1.0 vale para a 2.0?
3. **Revogar** precisa ser tão fácil quanto consentir. Onde fica o botão?
4. **Depois da revogação**, o que acontece com os dados já usados? E com os próximos usos?
5. **Ameaças ao próprio registro:** quem pode alterá-lo (T)? Se ele for alterado, como se prova o
   que valia (N, I)?
6. **"Roda atrás do RBAC":** primeiro o sistema confere **quem** pode (o papel), depois **sob qual
   base** (o consentimento). Desenhe essa ordem.

## Passo 3 — Monte a tabela LINDDUN

> 🔎 **Como o Grupo 5 fez:** uma linha por **ameaça**: elemento, letra(s), ameaça concreta,
> controle/PET e requisito — 11 linhas. Veja: [`docs/12` §4.1](../../docs/12-exemplo-dossie-preenchido.md#41-linddun-per-element).

**▶ Agora no G3:** use os elementos do **seu DFD**. Para cada ameaça: concreta (com o que você
viu), **controle/PET** (ex.: remover a coluna; resposta mínima; registro de consentimento por
versão; tela de autosserviço) e **requisito** (crie, se faltar, e volte à §2 com o `.feature
@analise`). Inclua também o **controle por papel** (F-03) na visão de privacidade: sem ele, quem
vê os dados é qualquer um (`Nc.3`, no [`docs/04`](../../docs/04-catalogo-funcionalidades.md)).

## Passo 4 — Escreva as decisões de Privacy by Design

> 🔎 **Como o Grupo 5 fez:** **decisão concreta** e **evidência** por princípio; "(plano)" quando
> ainda não havia evidência; **lacunas assumidas** escritas. Veja:
> [`docs/12` §4.2](../../docs/12-exemplo-dossie-preenchido.md#42-privacy-by-design-decisões).

**▶ Agora no G3:**

| Princípio | Pergunta para o G3 |
|---|---|
| Proativo | que coluna sem finalidade vocês removem antes de ela vazar? |
| Privacidade como padrão | o padrão é **negar** (RBAC) e **não consentido** (F-04): a caixa de consentimento vem desmarcada? |
| Embarcada no design | a regra de papel está num lugar só, usada por todas as funções, ou espalhada? |
| Soma positiva | cada papel continua fazendo o próprio trabalho depois da correção? A recepção ainda localiza pacientes sem ver a religião? |
| Ponta a ponta | o consentimento é registrado, consultado em cada uso, revogável e guardado como prova |
| Transparência | o paciente sabe para que cada dado é coletado e o que consentiu? |
| Respeito ao usuário | o paciente vê e corrige o próprio cadastro (F-19) e revoga o consentimento sozinho |

## Passo 5 — Esboce o RIPD

> 🔎 **Como o Grupo 5 fez:** **descrição do tratamento** (dados, titulares, finalidade, prazo),
> **escalas**, e uma linha por ameaça com **residual honesto**. Veja:
> [`docs/12` §4.3](../../docs/12-exemplo-dossie-preenchido.md#43-ripddpia-esboço--lgpd-art-38).

**▶ Agora no G3:**

1. **Descrição:** dados (cadastro completo, inclusive religião/renda; agenda; papéis); titulares
   (pacientes **e** funcionários); finalidades (atendimento; a finalidade opcional da F-04);
   prazo.
2. **Escalas** definidas.
3. **Tabela:** pelo menos três ameaças. Pensem em: vazamento de religião/renda que nem deviam
   existir (impacto ao titular: discriminação); uso para finalidade opcional sem consentimento;
   paciente sem conseguir corrigir um dado errado; qualquer pessoa com conta vendo a agenda.

## Passo 6 — Minimização (a F-15)

> 🔎 **Como o Grupo 5 fez:** *"religião, estado civil e renda são coletados sem finalidade —
> devem sair; a remoção é do F-15, de outro pacote."* — o F-15 que ele transferiu é **de vocês**.

**▶ Agora no G3:** aqui a frase vira uma **decisão por coluna**: remover, manter (com qual
finalidade?) ou manter só para uma finalidade com **consentimento** (F-04). Escrevam a tabela
coluna | decisão | por quê | onde mais ela aparece (busca, seed, tela) — tudo o que precisa mudar
junto.

## Deu errado?

Os problemas que aparecem em **qualquer** fase (VidaPlus desligado, token vazio num terminal
novo, comando fora da raiz, dados mudados por um teste anterior) estão na tabela de
[`comum/02` §6](../comum/02-observar-o-sistema.md#6-deu-errado-problemas-comuns-a-todas-as-fases).

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

- [ ] Achei ameaças **sem ataque** nas quatro funcionalidades.
- [ ] Sei explicar as duas metades do U e onde cada uma aparece no G3.
- [ ] A finalidade da F-04 é opcional (não é o atendimento) e foi analisada nos 6 itens do Passo 2.
- [ ] O RIPD tem descrição, escalas e residual honesto.
- [ ] A minimização tem uma decisão **por coluna**.
