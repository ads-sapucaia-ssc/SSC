# G4 · Fase 1 — Requisitos de segurança e LGPD

> **Seção do dossiê:** §2 (e a coluna "Base legal" da §1) · **Lidera:** 🛡️ Segurança e 📋 DPO ·
> **Aula:** 2 · **Semanas:** 2–3

Cada passo mostra **🔎 como o Grupo 5 fez** (o grupo do [`docs/12`](../../docs/12-exemplo-dossie-preenchido.md))
e **▶ o mesmo passo no G4**.

## O que você entrega no fim

1. A coluna **Base legal** da §1 preenchida.
2. Na §2, **Forma 1:** requisitos *shall* — pelo menos **um por funcionalidade** (F-02 e cada
   canal dela, F-06, F-16, F-18, F-20).
3. O **nível ASVS** alvo, com justificativa.
4. Na §2, **Forma 2:** os `.feature` — o do P0 já existe; os outros com `@analise`.

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
   git checkout -b dossie/fase-1
   git push -u origin dossie/fase-1
   ```

   Os **demais** integrantes entram nela (depois que o líder rodou os comandos acima):

   ```bash
   git checkout main
   git pull
   git fetch
   git checkout dossie/fase-1
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

**Requisito de segurança:** uma frase que diz **o que o sistema deve garantir**, de forma
**verificável**. Duas formas: a **declaração *shall*** (no dossiê, **sem tecnologia**) e o
**cenário Gherkin** (`.feature`, **com tecnologia**). Só existe com as duas.

> **Frase-síntese:** requisito bem-feito = **ativo + ameaça + atributo + critério verificável +
> rastreabilidade**.

**Base legal (LGPD).** A hipótese do art. 7º (dado comum) ou do art. 11 (sensível) que permite
tratar o dado. O **art. 46** é o dever de proteger, não base legal. **Para o G4, três artigos sobre
o fim da vida do dado são centrais:**

- **art. 15:** o tratamento **termina** quando a finalidade é alcançada ou o prazo acaba;
- **art. 16:** terminado o tratamento, os dados devem ser **eliminados** — **exceto**, entre
  outros, para **cumprir obrigação legal** (inciso I) ou se forem **anonimizados** (inciso IV);
- **art. 18, VI:** o titular pode pedir a eliminação — mas a clínica pode recusar se houver
  obrigação legal de guardar, **explicando por quê**.

E a lei que cria a obrigação de guardar: **Lei 13.787/2018** — o prontuário do paciente deve ser
guardado por **20 anos** a partir do último registro. Material: [`docs/02`](../../docs/02-lgpd.md).

**ASVS** (OWASP, **versão 4.0.3**). Capítulos que servem ao G4:

| Capítulo | Assunto | Funcionalidades |
|---|---|---|
| **V4** Access Control | autorização no servidor; IDOR (V4.2.1) | F-02 (P0) |
| **V7** Error Handling and Logging | mensagem genérica ao usuário, sem detalhes internos (V7.4.1) | F-16 |
| **V8** Data Protection | classificação de retenção e eliminação de dados vencidos (V8.3.8) | F-06, F-18 |
| **V12** Files and Resources | arquivos entregues com controle | F-02 (canal arquivo) |
| **V14** Configuration | cabeçalhos de segurança; sem informação de depuração em produção (V14.3) | F-20, F-16 |

Níveis: **L1**, **L2** (recomendado para dados sensíveis), **L3**.

---

## Passo 1 — Preencha a base legal da §1

> 🔎 **Como o Grupo 5 fez:** nota clínica → **art. 11, II, "f"**; credenciais → **art. 7º, V**;
> religião sem uso → **"— (sem finalidade)"**; segredos → **"— (não é dado pessoal)"**.

**▶ Agora no G4:** para cada ativo, **para que** a clínica o trata. E, para o G4, **acrescente
mentalmente uma pergunta: até quando?** Ex.: o laudo é tratado para a tutela da saúde (art. 11,
II, "f") e **guardado** por obrigação legal (art. 7º, II / art. 11, II, "a" + Lei 13.787/2018)
depois que o tratamento ativo termina. As mensagens de erro e o conteúdo do site não são dados
pessoais — "—" (mas são ativos).

---

## Passo 2 — Escreva as ameaças "em palavras"

> 🔎 **Como o Grupo 5 fez:** *"copiar o cadastro de todos os pacientes pela busca, sem login"*.

**▶ Agora no G4:** formato *"alguém [quem] consegue [o quê], porque [o que você observou]"*. **Uma
ameaça por canal**, no mínimo:

- **API (F-02):** a Ana viu o exame do Bruno…
- **Arquivo (F-02):** qualquer pessoa, sem login…
- **Erro (F-16):** um atacante que manda uma aspa na busca recebe…
- **Notificação (F-18):** quem olha a tela de bloqueio do celular da Ana…
- **Site (F-20):** quem conseguir trocar a página inicial…
- **Fim de vida (F-06):** os dados de quem deixou a clínica há 30 anos…

---

## Passo 3 — Transforme cada ameaça num requisito *shall*

> 🔎 **Como o Grupo 5 fez** (com a busca):
>
> - 1º rascunho: *"A busca deve usar PreparedStatement."* → **rejeitado** (é solução, cita
>   tecnologia).
> - 2º: *"A busca deve ser segura contra injeção."* → **rejeitado** ("segura" não se testa).
> - Final: *"A busca de pacientes **deve** devolver somente cadastros que correspondam ao termo
>   informado, qualquer que seja o conteúdo do termo."* → **aceito**.
>
> E, para a anonimização (a F-06 dele, outro recorte): *"Ao fim do prazo de guarda, o sistema
> **deve** tornar os dados do titular anonimizados de forma que ele não possa ser reidentificado
> por meios razoáveis..."*

**▶ Agora no G4:** receita e **teste de qualidade** (diz **o que**, não **como**? sem tecnologia?
testável? combate ameaça concreta?):

```
O sistema deve <garantia>, <em que situação/escopo>, <sem que / de forma que ...>.
```

**O requisito do P0 já está pronto:**

| ID | *shall* | Prior. | Origem | Ameaça |
|---|---|:-:|---|---|
| REQ-AC-IDOR-01 | O sistema **deve** validar a titularidade do recurso no servidor e negar o acesso ao laudo — sem revelar metadados — quando o solicitante não for o dono. | P0 | arts. 11, 46 · ASVS V4.2.1 | um paciente ver o laudo de outro trocando o número na URL |

Repare em **"sem revelar metadados"**: é a parte do P0 que pertence ao **seu recorte**. Uma
negação que conta "esse exame existe, é uma glicemia" ainda vaza. Pense também: e uma negação
**diferente** para "não existe" e "não é seu"?

Escrevam os outros. IDs sugeridos:

| Func. | Prefixo | Pense em |
|---|---|---|
| F-02 (canal arquivo) | `REQ-CANAL-ARQ-...` | o arquivo só sai para quem pode ver o exame — inclusive o **não publicado** |
| F-16 | `REQ-ERR-...` | o usuário recebe uma mensagem **genérica**; o detalhe fica **só** no registro interno; a resposta não revela se um recurso **existe** |
| F-18 | `REQ-NOTIF-...` | o **texto** não carrega dado de saúde; o **link** exige que a pessoa se identifique e/ou expira |
| F-20 | `REQ-SITE-...` | o conteúdo publicado só muda por um processo controlado; o navegador recebe instruções de proteção |
| F-06 | `REQ-RET-...` | cada tipo de dado tem prazo; no fim do prazo, eliminar ou anonimizar; **guardar** o que a lei obriga, só pelo tempo que ela obriga; responder ao pedido do titular |

---

## Passo 4 — Escolha o nível ASVS e justifique

> 🔎 **Como o Grupo 5 fez:** *"L2 — dado de saúde e religião (sensíveis); L1 não basta. O L3 fica
> para os módulos mais críticos, como o acesso ao laudo, que não estão no nosso pacote."*

**▶ Agora no G4:** o Grupo 5 citou o **acesso ao laudo** como candidato a **L3** — e é o P0 de
vocês. Decidam: L2 ou L3? Justifiquem. Digam que usam a **versão 4.0.3**.

---

## Passo 5 — Rode o teste do P0 e veja-o falhar

> 🔎 **Como o Grupo 5 fez:** leu o cenário em voz alta, rodou, viu `expected: <401> but was: <200>`
> e guardou a saída do "antes".

**▶ Agora no G4:**

1. Abra `legacy-app/backend/src/test/resources/br/ifsul/vidaplus/features/idor.feature` e leia.
   Repare na última linha: `E a resposta não contém "Glicemia"` — é o "sem revelar metadados".
2. No terminal (na raiz do repositório), religue o VidaPlus com o banco original e rode o teste
   do P0. O `rodar-testes.sh` roda os testes dentro de um container, sem precisar instalar Java (o
   que ele faz, passo a passo: [`comum/04` §2](../comum/04-testes-e-pipeline.md#2-rodar-os-testes-no-seu-computador)):

   ```bash
   bash roteiros/comum/zerar-banco.sh
   bash roteiros/comum/rodar-testes.sh "security & F-02" 2>&1 | tee entregaveis/evidencias/fase1-idor-vermelho.txt
   ```

3. Ache `expected: <403> but was: <200>` e explique o que prova.

---

## Passo 6 — Crie os `.feature` dos requisitos só analisados

> 🔎 **Como o Grupo 5 fez:** criou `busca-minima-analise.feature`, com **`@analise`** e as frases
> prontas:
>
> ```gherkin
> # language: pt
> @F-13 @REQ-PRIV-BUSCA-01 @P1 @lgpd-art6 @linddun-identificabilidade @analise
> Funcionalidade: F-13 · Busca mínima (REQ-PRIV-BUSCA-01)
>   Cenário: termo curto demais é recusado
>     Dado que estou autenticado como "recepcao@vidaplus.local" com senha "Recep123"
>     Quando faço GET em "/api/pacientes/busca?q=Si"
>     Então o status HTTP é 400
> ```

**▶ Agora no G4:** um arquivo `<assunto>-analise.feature` por requisito que **não** é o P0.
Vários cabem nas **frases prontas** ([`comum/04` §1.2](../comum/04-testes-e-pipeline.md#12-frases-que-o-computador-já-sabe-executar)).
Por exemplo, para a F-16:

```gherkin
# language: pt
@F-16 @REQ-ERR-01 @P2 @lgpd-art46 @asvs-V7 @stride-info-disclosure @analise
Funcionalidade: F-16 · Erro sem detalhes internos (REQ-ERR-01)
  Declaração shall (agnóstica): ver dossiê §2, Forma 1 — REQ-ERR-01.

  Cenário: erro ao pedir um exame inexistente não revela o funcionamento interno
    Dado que estou autenticado como "ana.paciente@email.com" com senha "Paciente123"
    Quando faço GET em "/api/exames/999/pdf"
    Então a resposta não contém "exception"
    E a resposta não contém "trace"
```

Para F-18, F-20 e F-06, escrevam frases novas em português claro (o arquivo não roda):
`Quando o laudo do paciente é publicado` / `Então a mensagem enviada não contém o tipo do exame`;
`Dado um paciente inativo cuja última atividade foi há mais de 20 anos` / `Quando a rotina de
retenção roda` / `Então ...`.

> **Atenção:** se o grupo decidir, na Fase 4, incluir no PR do P0 a parte da F-16 (veja lá), o
> cenário acima pode virar `@security` — ele passa a rodar no gate e precisa ficar verde.

## Passo 7 — Monte a Forma 2 no dossiê

> 🔎 **Como o Grupo 5 fez:** tabela "requisito → arquivo → estado" ([`docs/12` §2, Forma 2](../../docs/12-exemplo-dossie-preenchido.md#forma-2--cenários-gherkin-feature-vinculados-à-tecnologia)).

**▶ Agora no G4:** `idor.feature` com `@security`; os outros com `@analise`.

---

## Último passo — Grave no Git e abra o Pull Request

Com a seção escrita e salva (**Ctrl+S**), no terminal, na branch `dossie/fase-1`:

```bash
git status
git add entregaveis/ legacy-app/backend/src/test/resources/
git commit -m "docs(dossie): fase 1 - requisitos"
git push -u origin dossie/fase-1
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
   **compare** escolha `dossie/fase-1` → **Create pull request**.
2. Título: "Dossiê — Fase 1: requisitos". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] Toda linha da §1 tem base legal — e, para os dados de saúde, sei dizer **até quando**.
- [ ] Sei explicar a diferença entre o art. 16, I (guardar por obrigação legal) e o art. 18, VI
      (pedido de eliminação).
- [ ] Há requisito *shall* para cada canal e para F-06, F-16, F-18 e F-20.
- [ ] Cada requisito tem um `.feature`.
- [ ] Rodei o teste do P0 e guardei a saída vermelha.

## Erros comuns

- **"Eliminar tudo ao fim do tratamento".** O prontuário tem guarda obrigatória de 20 anos.
- **"Eliminar" = "desativar".** Desativar só esconde (Fase 0).
- **Esquecer `@analise`.**
