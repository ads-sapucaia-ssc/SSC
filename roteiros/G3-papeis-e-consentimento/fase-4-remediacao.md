# G3 · Fase 4 — Correção do P0, plano de remediação e especificação do consentimento

> **Seção do dossiê:** §6 · **Lidera:** 💻 Dev (correção) e 🛡️ Segurança (revisão e plano) ·
> **Aula:** 6 · **Semanas:** 7–10
> A **Parte A** é feita na **aula de laboratório guiada**, com o professor. Não precisa saber Java:
> o roteiro explica cada linha.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G3**.

## O que você entrega no fim

1. **Parte A — Prova de conceito:** o controle por papel (RBAC) corrigido num PR revisado, com o
   `rbac.feature` passando de 🔴 para 🟢, e a §6.1 escrita.
2. **Parte B — Plano de remediação:** a §6.2.
3. **Parte C — Especificação da criação (F-04):** o consentimento **projetado**.

---

# Parte A — Corrigir o P0 (aula guiada)

## Passo 0 — Prepare o terminal, a branch e o VidaPlus

1. Abra o VS Code na pasta do repositório do grupo e o terminal (**Terminal → New Terminal**).
   Confira que o terminal está na **raiz** do repositório:

   ```bash
   ls
   ```

   Deve aparecer `README.md  docs  entregaveis  legacy-app  roteiros  security`. Se não
   aparecer, a pasta está errada: **File → Open Folder…** e escolha a pasta do repositório.
   **Todos os comandos deste roteiro são digitados nesta pasta.**
2. **Branches desta fase:** a correção do código tem a sua própria branch (Passo 2 da Parte A); a
   escrita da §6 no dossiê é gravada no fim, numa branch `dossie/fase-4` (último passo).
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

**A falha.** Cada função restrita do VidaPlus confere, no máximo, se **existe um token válido**.
Nenhuma pergunta **qual é o papel** de quem pede. O método `JwtUtil.papelDoToken`, que responde
essa pergunta, existe — e ninguém o chama.

**A correção: uma regra só, chamada por todas as funções.** Vocês vão criar uma classe pequena,
`Papeis`, com um único método:

```java
Papeis.exigir(auth, "BIOQUIMICO");              // só o laboratório
Papeis.exigir(auth, "RECEPCAO", "ADMIN");       // recepção ou admin
```

Ele lê o papel do token e:

- **sem token, ou token inválido → 401** ("não sei quem você é");
- **papel fora da lista → 403** ("sei quem você é, mas não pode");
- **papel na lista → segue** para a função.

Repare: a lista diz **quem pode**. Quem não está nela — inclusive um papel que vier a ser criado
no futuro — é **negado**. Isso é *deny-by-default*.

**Por que uma classe só?** Se cada método tivesse a sua própria checagem, escrita à mão, bastaria
um esquecimento num deles para abrir um buraco. Com uma regra central, a revisão do PR confere uma
coisa: "toda função restrita chama `Papeis.exigir` na primeira linha?".

---

## Passo 1 — Faça o inventário da classe de falha

> 🔎 **Como o Grupo 5 fez:** antes de corrigir **um** ponto, procurou **todos** (Semgrep +
> **Ctrl+Shift+F**), montou a tabela e corrigiu a classe no mesmo PR. Veja:
> [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G3:** a falha de vocês é **lógica**: nenhum scanner a encontra. O inventário é à mão,
e vocês já o têm: é a **matriz de permissões** da Fase 3.

1. Confira se ela está completa: **Ctrl+Shift+F** → `@GetMapping`, depois `@PostMapping`, depois
   `@PutMapping`. Cada resultado num `controller` é uma função. Alguma ficou fora da matriz?
2. Vocês vão achar endpoints que **nem pedem login** (a busca, a ficha e a nota do paciente, no
   `PacienteController`). Eles também são funções. A decisão padrão é registrá-los no **plano**
   (Parte B), porque o problema principal deles é de outro pacote (falta de login e de dono). Se o
   professor liberar na aula, vocês podem incluí-los: será preciso acrescentar ao método o
   parâmetro `@RequestHeader(value = "Authorization", required = false) String auth`.
3. **Decisão do G3:** todas as funções da matriz entram no mesmo PR. Corrigir só a publicação (o
   que o teste cobre) deixaria um paciente desativando contas.

## Passo 2 — Crie a branch da correção

```bash
git checkout main
git pull
git checkout -b fix/F-03-rbac
```

## Passo 3 — Registre o "antes"

> 🔎 **Como o Grupo 5 fez:** guardou a saída do ataque e o teste vermelho **antes** de mexer no
> código.

**▶ Agora no G3:** religue o VidaPlus com o banco original e rode o script da matriz:

```bash
bash roteiros/comum/zerar-banco.sh
bash roteiros/G3-papeis-e-consentimento/matriz-de-acesso.sh | tee entregaveis/evidencias/fase4-matriz-antes.txt
```

O script entra com os cinco papéis e testa cada função da matriz, mostrando o código HTTP de cada
combinação. Hoje, **tudo** dá 200. Compare com a matriz de permissões de vocês: cada 200 onde
deveria haver um "não" é uma falha.

O script publica um laudo e cria um atendimento. Então **zere o banco de novo** e rode o teste:

```bash
bash roteiros/comum/zerar-banco.sh
bash roteiros/comum/rodar-testes.sh "security & F-03" 2>&1 | tee entregaveis/evidencias/fase4-teste-antes.txt
```

Esperado: **vermelho** (`expected: <403> but was: <200>`).

## Passo 4 — Crie a classe `Papeis`

No VS Code, clique com o botão direito na pasta
`legacy-app/backend/src/main/java/br/ifsul/vidaplus/security` → **New File** → `Papeis.java`, e
cole:

```java
package br.ifsul.vidaplus.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

/**
 * Controle de acesso por papel (RBAC), deny-by-default.
 * Uso: Papeis.exigir(auth, "BIOQUIMICO");  ou  Papeis.exigir(auth, "RECEPCAO", "ADMIN");
 *  - sem token, ou token invalido  -> 401 (nao sei quem voce e)
 *  - papel fora da lista           -> 403 (sei quem voce e, mas nao pode)
 */
public final class Papeis {

    private Papeis() {
    }

    public static void exigir(String auth, String... permitidos) {
        String papel = (auth != null && auth.startsWith("Bearer "))
                ? JwtUtil.papelDoToken(auth.substring(7)) : null;
        if (papel == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!Arrays.asList(permitidos).contains(papel)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
```

Lendo devagar:

| Trecho | O que faz |
|---|---|
| `String... permitidos` | aceita **um ou mais** papéis: `exigir(auth, "ADMIN")` ou `exigir(auth, "RECEPCAO", "ADMIN")` |
| `auth.startsWith("Bearer ")` e `auth.substring(7)` | tira o `"Bearer "` do cabeçalho e fica só com o token |
| `JwtUtil.papelDoToken(...)` | lê o papel de dentro do token. Devolve `null` se o token for inválido |
| `papel == null` → `UNAUTHORIZED` | **401** |
| `!Arrays.asList(permitidos).contains(papel)` → `FORBIDDEN` | **403** se o papel não estiver na lista |

## Passo 5 — Chame a regra em cada função restrita

Em **cada** controller que você mexer, acrescente no topo, logo abaixo da linha
`import br.ifsul.vidaplus.security.JwtUtil;`:

```java
import br.ifsul.vidaplus.security.Papeis;
```

E, em cada método, ponha `Papeis.exigir(...)` como **primeira linha**, com os papéis da **matriz
de vocês**. Os papéis abaixo são os do quadro de atores do [`docs/00`](../../docs/00-visao-geral.md);
se a matriz de vocês decidiu diferente, use a de vocês.

**5.1 — `ExameController.publicar` (o P0).** Troque:

```java
        Long uid = usuarioId(auth);
        if (uid == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica JwtUtil.papelDoToken(...) == "BIOQUIMICO"
        exameDao.publicar(id, uid);
```

por:

```java
        Papeis.exigir(auth, "BIOQUIMICO");
        Long uid = usuarioId(auth);
        exameDao.publicar(id, uid);
```

(A linha `if (uid == null) ...` sai: o `Papeis.exigir` já respondeu 401 se não havia token. Se
ela ficasse **antes** do `exigir`, quem chega sem token receberia 500 em vez de 401.)

**5.2 — `ExameController.pendentes`.** Troque as duas linhas

```java
        if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica papel
```

por `Papeis.exigir(auth, "BIOQUIMICO");`.

**5.3 — `UsuarioController.listar` e `alternarAtivo`.** Em cada um, troque a linha
`if (usuarioId(auth) == null) throw new RuntimeException("nao autenticado");` e o comentário
`// FALHA` logo abaixo dela por `Papeis.exigir(auth, "ADMIN");`.

**5.4 — `AtendimentoController`.**

- `listar` e `agendar`: troque a linha `if (usuarioId(auth) == null) ...` (e, no `listar`, o
  comentário `// FALHA: nao verifica papel`) por `Papeis.exigir(auth, "RECEPCAO", "ADMIN");`. No
  `agendar`, **mantenha** o comentário sobre validar os ids: isso é outra falha (vai para o plano).
- `pacientesDoMedico`: troque a linha `if (usuarioId(auth) == null) ...` e o comentário
  `// FALHA` por `Papeis.exigir(auth, "MEDICO");`.

**5.5 — `PacienteController.editar`.** Troque as linhas

```java
        Long uid = auth != null && auth.startsWith("Bearer ")
                ? JwtUtil.usuarioIdDoToken(auth.substring(7)) : null;
        if (uid == null) throw new RuntimeException("nao autenticado");
        // FALHA: nao verifica papel; nao registra quem alterou o que
```

por `Papeis.exigir(auth, "RECEPCAO", "ADMIN");`.

**Confira:** **Ctrl+Shift+F** → `Papeis.exigir`. Deve aparecer **uma vez em cada função da sua
matriz** (8 vezes, se a matriz for a do quadro acima).

## Passo 6 — Remonte, ataque de novo e rode os testes

> 🔎 **Como o Grupo 5 fez:** repetiu **o mesmo ataque** do "antes", conferiu o **uso legítimo**,
> rodou os testes e guardou cada saída como "depois".

**▶ Agora no G3:**

1. Remonte e zere: `bash roteiros/comum/zerar-banco.sh`
   (espere 30 s).
2. **A matriz, depois:**

   ```bash
   bash roteiros/G3-papeis-e-consentimento/matriz-de-acesso.sh | tee entregaveis/evidencias/fase4-matriz-depois.txt
   ```

   Esperado (com a matriz do quadro acima): **200 só onde a matriz de vocês tem ✓**, **403** em
   todo o resto, e **401** na última linha (sem login). Compare célula por célula com a matriz de
   permissões. Alguma diferença? Ou o código está errado, ou a matriz está — decidam qual.
3. **Pela tela, com cada papel:** entre como Ana e digite `/usuarios` na barra de endereços: a
   tela abre, mas fica em **"Carregando…"** para sempre — a API negou (veja o 403 no **F12 →
   Rede**). A tela continua acessível: a proteção está no **dado**, não na tela. Entre como **Admin**: a lista aparece. Como
   **Recepção**: a Agenda funciona. Como **Bio**: a Fila de Laudos funciona e o botão Publicar
   funciona. Como **Dra. House**: Meus Pacientes funciona.
4. **Os testes:** zere o banco de novo (o script da matriz e os cliques mudaram dados) e rode:

   ```bash
   bash roteiros/comum/zerar-banco.sh
   bash roteiros/comum/rodar-testes.sh "security & F-03" 2>&1 | tee entregaveis/evidencias/fase4-teste-depois.txt
   bash roteiros/comum/rodar-testes.sh 2>&1 | tee entregaveis/evidencias/fase4-todos-depois.txt
   ```

   O primeiro: **verde** (inclusive os cenários que vocês acrescentaram na Fase 1). O segundo:
   continua vermelho pelos testes de SQLi e IDOR (outros grupos) — confira que **nenhuma** falha
   fala de `rbac`.

**Deu errado?**

| Sintoma | Causa provável |
|---|---|
| `cannot find symbol: class Papeis` | faltou o `import br.ifsul.vidaplus.security.Papeis;` naquele controller |
| `cannot find symbol: variable uid` | você apagou a linha `Long uid = usuarioId(auth);` num método que ainda usa o `uid` (o `publicar` usa) |
| O Bio não consegue publicar | o papel escrito errado: é `"BIOQUIMICO"`, sem acento, em maiúsculas |
| Sem login dá 500, não 401 | sobrou um `if (uid == null) throw ...` **antes** do `Papeis.exigir` |

## Passo 7 — Grave, abra o PR e peça a revisão

```bash
git add legacy-app/backend/src entregaveis/evidencias
git commit -m "fix(F-03): controle de acesso por papel, deny-by-default, em todas as funcoes restritas"
git push -u origin fix/F-03-rbac
```

Abra o PR ([`comum/03` §3, passo 7](../comum/03-git-e-github.md#passo-7--abra-o-pull-request)).
Na descrição: **a matriz de permissões** (cole a tabela), **como testar** (o script, antes e
depois) e **o teste que deve passar** (`rbac.feature`).

**Quem revisa** usa o checklist do [`docs/07`](../../docs/07-rubrica-avaliacao.md#checklist-de-secure-code-review-para-os-prs-da-poc)
— "autorização server-side (RBAC deny-by-default)" — e confere **célula por célula** a matriz do
PR contra a saída do script.

## Passo 8 — Escreva a §6.1

> 🔎 **Como o Grupo 5 fez:** **Antes**, **Depois**, **O que rejeitamos e por quê**, **O que esta
> correção NÃO resolve**, **PR**, **Teste**. Veja: [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G3:** os mesmos blocos. Para **"O que rejeitamos"**: por que não bastava esconder os
menus (como o `Layout.jsx` já faz)? Por que não uma checagem escrita à mão em cada método? Para
**"O que esta correção NÃO resolve"**:

- o papel vem **de dentro do token**: e se o token for fabricado (a chave está no código)?
- e se o papel da pessoa mudar no banco — o token antigo continua com o papel antigo?
- a médica só pode ver **a própria** carteira, ou qualquer `{id}` de médico?
- os endpoints que nem pedem login (busca, ficha, nota)?

Cada um vai para a Parte B — e alguns vocês vão **testar** na Fase 5.

---

# Parte B — Plano de remediação

> 🔎 **Como o Grupo 5 fez:** tabela **funcionalidade | falha | controle proposto | esforço (P/M/G)
> | prioridade | observação**, com a escala de esforço definida; o que dependia de outro pacote,
> marcado como "transferido". Veja: [`docs/12` §6.2](../../docs/12-exemplo-dossie-preenchido.md#62-plano-de-remediação-demais-falhas-do-pacote--não-implementadas).

**▶ Agora no G3:** uma linha para **cada falha não corrigida**. Pontos de partida:

| Falha | Perguntas para chegar ao controle |
|---|---|
| Coluna sem finalidade (F-15) | a decisão por coluna da Fase 2b: o que muda no `schema.sql`, no `seed.sql`, no `PacienteDao.buscar` e na tela `BuscaPacientes.jsx`? E os dados que **já estão** no banco? |
| Atribuição em massa e falta de validação (F-19) | o servidor deve aceitar **só** os campos permitidos (lista de permitidos) e validar o formato de cada um (telefone, endereço). E o autosserviço: o paciente edita **o próprio** cadastro — por qual endereço, com que regra? |
| Papel dentro do token | conferir o papel no banco a cada pedido? Token de vida curta? (Interface com o G1: F-11) |
| Carteira de **outro** médico | o `{id}` deve ser o do médico logado — é regra de dono (interface com o G2) |
| Endpoints sem login (busca, ficha, nota) | exigir login e papel — outros pacotes (F-13, V-19) |
| Quem desativou quem | registro de auditoria das ações do admin (F-05, G1) |

---

# Parte C — Especificar o consentimento (F-04)

Nesta turma, a criação é **especificada**, não implementada. O nível esperado é o do Controle 2
do Grupo 5 — **sem** a implementação.

> 🔎 **Como o Grupo 5 fez** (a criação dele era a anonimização): **entidade**, **gatilho** com
> colunas que existem, **tratamento atributo por atributo** (tabela), **limitações** assumidas e o
> `.feature @analise`. Veja o Controle 2 em [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G3** — escopo mínimo ([`docs/05`](../../docs/05-pacotes-trabalho-estudantes.md#escopo-mínimo-da-poc--criação--para-equiparar-o-esforço)):
**1 finalidade**, **1 versão** do termo e **revogação**, aplicada em **1 fluxo**. Escrevam:

1. **A finalidade** (opcional — não o atendimento) e **por que ela depende de consentimento**
   (art. 7º, I ou art. 11, I).
2. **O texto do termo, versão 1.0** — curto, em linguagem que o paciente entende (art. 8º, §4º:
   finalidade determinada; termos genéricos são nulos).
3. **O registro:** a tabela, campo por campo — partam da `entity/Consentimento.java` e do modelo
   comentado no fim do `db/schema.sql`. Para cada campo: o que guarda e **por que** (o que ele
   prova).
4. **Os fluxos:** como o paciente **consente** (onde, quando), como **revoga** (tão fácil
   quanto consentir) e como **consulta** o que consentiu.
5. **Onde a verificação entra:** em que ponto do **fluxo escolhido** o sistema consulta o registro
   antes de tratar o dado — **depois** do `Papeis.exigir` ("roda atrás do RBAC").
6. **A nova versão do termo:** o que acontece com quem consentiu na 1.0 quando surgir a 2.0?
7. **Comportamento sob falha** (Fase 3): sem conseguir consultar o registro, trata ou não trata?
8. **Limitações assumidas.**
9. **O `.feature @analise`** da F-04 (Fase 1), com pelo menos: "sem consentimento, o uso é
   negado"; "depois da revogação, o uso é negado"; "o consentimento de uma versão não vale para
   outra".

## Último passo — Grave no Git e abra o Pull Request

A correção já entrou pelo PR da Parte A. Agora grave a **§6 do dossiê** (Partes A, B e C)
numa branch própria, criada a partir da `main` atualizada:

```bash
git checkout main
git pull
git checkout -b dossie/fase-4
```

Escreva a §6 no `entregaveis/DOSSIE.md`, salve (**Ctrl+S**) e grave:

```bash
git status
git add entregaveis/
git commit -m "docs(dossie): fase 4 - remediacao"
git push -u origin dossie/fase-4
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
   **compare** escolha `dossie/fase-4` → **Create pull request**.
2. Título: "Dossiê — Fase 4: remediação". Na descrição: o que foi feito e quem participou. Clique em
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

- [ ] PR da correção revisado por outro integrante e mergeado.
- [ ] Tenho a matriz **antes** (tudo 200) e **depois** (igual à matriz de permissões).
- [ ] Cada papel continua fazendo o próprio trabalho (testei pela tela).
- [ ] O `rbac.feature` está verde, inclusive os cenários novos.
- [ ] A §6.1 tem antes, depois, o que rejeitamos, o que **não** resolve, PR e teste.
- [ ] Toda falha não corrigida está na §6.2.
- [ ] A F-04 está especificada nos 9 itens da Parte C.
