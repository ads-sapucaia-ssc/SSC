# G4 · Fase 2b — Privacidade: LINDDUN, Privacy by Design e RIPD

> **Seção do dossiê:** §4 · **Lidera:** 📋 DPO · **Aula:** 4 · **Semana:** 5
> Junto com a 2a, é **a fase de maior peso** na avaliação.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G4**.

## O que você entrega no fim

1. **§4.1** — tabela LINDDUN por elemento do DFD.
2. **§4.2** — as decisões de **Privacy by Design**.
3. **§4.3** — o esboço do **RIPD** (LGPD art. 38).
4. A frase de **minimização** — no G4, ela inclui **minimização no tempo**.

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
   ```

   O primeiro termina com `PRONTO: VidaPlus no ar...` (a primeira vez demora alguns minutos); o
   segundo mostra seis linhas `TOKEN_... ok`. Os tokens valem **só neste terminal**: se abrir
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
| **D** | Divulgação (*Data disclosure*) | o sistema coleta, guarda, mostra ou **envia** mais do que precisa? |
| **U** | Desconhecimento (*Unawareness*) | a pessoa **não sabe** o que fazem com os dados dela, ou **não consegue intervir**? |
| **N** | Não-conformidade (*Non-compliance*) | o tratamento **descumpre a lei**? |

**Duas categorias são do G4 por excelência:**

- **D de Detecção:** o **oráculo de existência** — saber que um laudo existe sem ver o conteúdo;
- **N de Não-conformidade:** guardar o dado **além do necessário** (`Nc.1.1.4`).

E a ideia de **minimização no tempo**: tão importante quanto coletar pouco é **guardar pelo menor
tempo**. Cada dia a mais é um dia a mais de exposição. Material: [`docs/03`](../../docs/03-privacy-by-design-linddun.md).

---

## Passo 1 — Procure ameaças que acontecem **sem ataque**

> 🔎 **Como o Grupo 5 fez:** usou a busca do jeito normal e viu que uma busca específica devolvia
> **tudo de uma pessoa** (I) — sem injeção. E que pesquisar um CPF revela **se a pessoa é
> paciente** (D).

**▶ Agora no G4:**

1. **O oráculo de existência (D).** Junte o que você viu na Fase 2a:
   - pela API: um exame que **não existe** responde diferente de um que existe;
   - pelo arquivo: `laudo-105.pdf` (inexistente) responde **200** com tipo `text/html`, e
     `laudo-104.pdf`, **200** com `application/pdf`.

   Sem ver conteúdo nenhum, o que alguém descobre? (Quantos exames a clínica fez? Os números
   são sequenciais — dá para estimar o movimento da clínica?) Leia a linha "Detectabilidade —
   oráculo de existência" no [`docs/04`](../../docs/04-catalogo-funcionalidades.md#ameaças-de-privacidade-linddun-candidatas-a-versão-futura).
2. **A notificação (D, divulgação).** Leia a linha "Divulgação de informação — payload da
   notificação" do mesmo documento. A mensagem *"Seu exame de sorologia está pronto"* aparece na
   **tela de bloqueio** do celular, passa pelo **provedor**, fica na **caixa de entrada**. Mesmo
   com o link protegido, o **texto** já revelou. O que a mensagem **precisa** dizer?
3. **A mensagem de erro (D, divulgação).** O SQL na resposta da busca com aspa contém nomes de
   colunas e de tabelas. Isso é dado pessoal? Não — mas é **divulgação de informação** sobre o
   sistema. (Está no LINDDUN? Ou é STRIDE puro? Discutam e justifiquem.)
4. **O tempo (N).** Os dados de um paciente que esteve na clínica uma vez, há 30 anos, continuam
   expostos a cada um dos canais acima. A clínica **precisa** deles? Por quanto tempo a lei
   **obriga** a guardar o prontuário?

## Passo 2 — Analise o fim de vida (F-06) como privacidade

> 🔎 **Como o Grupo 5 fez:** analisou **a técnica** de anonimização atributo por atributo e
> descobriu que o dado "escapa" por onde ninguém lembra (texto livre, arquivo PDF, datas, backups).
> Leia a 💡 "Supressão não é anonimato" em [`docs/12` §4.1](../../docs/12-exemplo-dossie-preenchido.md#41-linddun-per-element).

**▶ Agora no G4** — vocês olham **o quando e o quê**. Monte, no rascunho, a **tabela de
retenção**:

| Categoria de dado | Onde está | Por que existe | Prazo de guarda | Base legal do prazo | No fim do prazo |
|---|---|---|---|---|---|
| Prontuário (notas, exames, laudos) | `nota_clinica`, `exame`, PDFs | tutela da saúde | ? | Lei 13.787/2018 | eliminar? anonimizar? |
| Cadastro do paciente | `paciente` | | | | |
| Conta de acesso | `usuario` | | | | |
| Agenda | `atendimento` | | | | |
| Registros de acesso (quando existirem) | — | | | | |

Para cada linha, perguntem:

1. **O prazo começa quando?** (Pista: a Lei 13.787 conta "a partir do **último** registro".)
   Existe hoje alguma coluna que diga quando foi o último registro de um paciente?
2. **No fim, eliminar ou anonimizar?** Eliminar some com o dado; anonimizar mantém algo útil
   (estatística) sem identificar ninguém. O exemplo do Grupo 5 mostra **como** anonimizar; vocês
   decidem **quando** cada um se aplica.
3. **O titular pede a eliminação (art. 18, VI) antes do prazo.** O que a clínica responde sobre o
   prontuário (art. 16, I)? E sobre o cadastro de quem **nunca** foi atendido?
4. **Onde mais o dado vive?** Os PDFs na pasta pública; os backups; os logs; as notificações já
   enviadas. A eliminação alcança cada um?

## Passo 3 — Monte a tabela LINDDUN

> 🔎 **Como o Grupo 5 fez:** uma linha por **ameaça**: elemento, letra(s), ameaça concreta,
> controle/PET e requisito — 11 linhas. Veja: [`docs/12` §4.1](../../docs/12-exemplo-dossie-preenchido.md#41-linddun-per-element).

**▶ Agora no G4:** use os elementos do **seu DFD**. Controles/PETs típicos do G4: **resposta
uniforme** (a mesma para "não existe" e "não é seu"); identificadores **não sequenciais**;
**mensagem mínima** na notificação ("há um resultado disponível no portal"); link **autenticado e
com prazo**; **prazo de retenção** por categoria; **eliminação ou anonimização** no fim. Cada
ameaça → requisito (crie, se faltar, e volte à §2 com o `.feature @analise`).

## Passo 4 — Escreva as decisões de Privacy by Design

> 🔎 **Como o Grupo 5 fez:** **decisão concreta** e **evidência** por princípio; "(plano)" quando
> ainda não havia evidência; **lacunas assumidas** escritas. Veja:
> [`docs/12` §4.2](../../docs/12-exemplo-dossie-preenchido.md#42-privacy-by-design-decisões).

**▶ Agora no G4:**

| Princípio | Pergunta para o G4 |
|---|---|
| Proativo | qual canal vocês fecham antes de alguém usá-lo? |
| Privacidade como padrão | a notificação **padrão** diz o mínimo? O dado tem **prazo** desde que é criado? |
| Embarcada no design | o prazo de guarda é uma regra do sistema, ou depende de alguém lembrar de apagar? |
| Soma positiva | o paciente continua sendo avisado do resultado — sem expor o resultado? |
| Ponta a ponta | do nascimento (coleta) à morte (eliminação) — inclusive arquivos e backups |
| Transparência | o paciente sabe por quanto tempo os dados dele ficam? |
| Respeito ao usuário | o paciente pede a eliminação e recebe uma resposta clara (o que foi eliminado, o que a lei obriga a manter e até quando) |

## Passo 5 — Esboce o RIPD

> 🔎 **Como o Grupo 5 fez:** **descrição do tratamento** (dados, titulares, finalidade, prazo),
> **escalas**, e uma linha por ameaça com **residual honesto**. Veja:
> [`docs/12` §4.3](../../docs/12-exemplo-dossie-preenchido.md#43-ripddpia-esboço--lgpd-art-38).

**▶ Agora no G4:**

1. **Descrição:** dados (laudos, resultados, cadastro); titulares (pacientes, **inclusive
   ex-pacientes**); finalidades (entregar resultado; avisar; guardar por obrigação legal); **prazos
   — a tabela de retenção do Passo 2**.
2. **Escalas** definidas.
3. **Tabela:** pelo menos três ameaças. Pensem em: laudo baixado direto da pasta; o resultado
   revelado no texto da notificação; dados de ex-paciente expostos décadas depois; eliminação
   **indevida** de um prontuário que ainda precisava ser guardado.

## Passo 6 — Minimização (no conteúdo e no tempo)

> 🔎 **Como o Grupo 5 fez:** *"religião, estado civil e renda são coletados sem finalidade —
> devem sair; a remoção é do F-15, de outro pacote."*

**▶ Agora no G4:** duas frases: o que os canais do G4 entregam **além do necessário** (o conteúdo
dos erros; o texto da notificação; os campos do exame), e o que fica guardado **além do tempo
necessário** (a tabela de retenção).

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

- [ ] Analisei o oráculo pela API e pelo arquivo.
- [ ] Analisei o texto da notificação separado do link.
- [ ] Tenho a tabela de retenção por categoria, com prazo, base legal e destino no fim.
- [ ] Sei responder ao pedido de eliminação do titular para o prontuário e para o cadastro.
- [ ] O RIPD inclui o risco de eliminar **demais** (ou cedo demais).
