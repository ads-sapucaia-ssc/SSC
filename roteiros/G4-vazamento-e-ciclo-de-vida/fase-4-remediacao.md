# G4 · Fase 4 — Correção do P0, plano de remediação e especificação da retenção

> **Seção do dossiê:** §6 · **Lidera:** 💻 Dev (correção) e 🛡️ Segurança (revisão e plano) ·
> **Aula:** 6 · **Semanas:** 7–10
> A **Parte A** é feita na **aula de laboratório guiada**, com o professor. Não precisa saber Java:
> o roteiro explica cada linha.

Cada passo mostra **🔎 como o Grupo 5 fez** e **▶ o mesmo passo no G4**.

## O que você entrega no fim

1. **Parte A — Prova de conceito:** o P0 (laudo de outra pessoa) corrigido num PR revisado — com
   o canal da **API** fechado por uma regra de dono, e o canal da **mensagem de erro** fechado —,
   o `idor.feature` passando de 🔴 para 🟢, e a §6.1 escrita.
2. **Parte B — Plano de remediação:** a §6.2 — com os canais que ficaram abertos.
3. **Parte C — Especificação da criação (F-06):** a regra de retenção → eliminação **projetada**.

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

**A falha.** O `ExameController.getExame` confere se **existe um token válido** (alguém logado) e
busca o exame pelo número. Ele **nunca pergunta**: *"este exame é desta pessoa?"*.

**A correção, em duas partes — porque o recorte de vocês é o canal:**

1. **Fechar o canal da API: autorização no servidor, negando por padrão.**

   | Papel de quem pede | Pode ver o exame se… |
   |---|---|
   | PACIENTE | o exame é **dele** |
   | MEDICO | ele **atende** o paciente do exame (`ExameDao.medicoAtende` já existe) |
   | BIOQUIMICO | o exame **ainda não foi publicado** |
   | qualquer outro | **não pode** (*deny-by-default*) |

2. **Fechar o canal da mensagem de erro:** a negação responde **403 sem nada dentro** — a mesma
   resposta para "não existe" e "não é seu" (o oráculo da Fase 2b) —, nenhuma resposta de erro
   carrega detalhes internos, e quem chega sem login recebe **401**, não um erro 500.

> **A regra da tabela é uma proposta**, baseada no quadro de atores do [`docs/00`](../../docs/00-visao-geral.md).
> Se o grupo decidiu diferente, ajuste — e justifique no dossiê.

> **O G2 corrige o mesmo P0** com a mesma regra de dono. O que é **só do G4** é a parte 2: vocês
> tratam o P0 como um **canal**, e fecham também o que a negação deixava escapar.

---

## Passo 1 — Faça o inventário da classe de falha: os canais

> 🔎 **Como o Grupo 5 fez:** antes de corrigir **um** ponto, procurou **todos** (Semgrep +
> **Ctrl+Shift+F**), montou a tabela arquivo/método/linha/entrada/endpoint e corrigiu a classe no
> mesmo PR. Veja: [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G4:** o inventário de vocês é **a lista de canais** até o laudo, com o que cada um
deixa escapar hoje (Fases 0 e 2a):

| Canal | Onde | Entra no PR do P0? |
|---|---|---|
| exame pela API | `ExameController.getExame` | **sim** (é o teste) |
| PDF pela API | `ExameController.getPdf` | **sim** |
| lista de exames pela API | `ExameController.porPaciente` | **sim** |
| mensagem de erro | `application.properties` (`server.error.*`) e os `RuntimeException("nao autenticado")` do `ExameController` | **sim** (parte 2) |
| arquivo direto | nginx, `frontend/public/laudos/` | não: muda a arquitetura — vai para o **plano** |
| notificação | não existe | não: **plano** |

Por que a mensagem de erro entra no PR do P0, se ela é a F-16? Porque o P0 exige negar **"sem
revelar metadados"** — e uma negação acompanhada de *stack trace* revela. São **três linhas** de
configuração e uma substituição. O resto da F-16 vai para o plano.

## Passo 2 — Crie a branch da correção

```bash
git checkout main
git pull
git checkout -b fix/F-02-canais-do-laudo
```

## Passo 3 — Registre o "antes"

> 🔎 **Como o Grupo 5 fez:** guardou a saída do ataque e o teste vermelho **antes** de mexer no
> código.

**▶ Agora no G4:** religue o VidaPlus com o banco original e registre os canais e o teste
vermelho:

```bash
bash roteiros/comum/zerar-banco.sh
bash roteiros/G4-vazamento-e-ciclo-de-vida/canais-do-laudo.sh | tee entregaveis/evidencias/fase4-canais-antes.txt
bash roteiros/comum/rodar-testes.sh "security & F-02" 2>&1 | tee entregaveis/evidencias/fase4-teste-antes.txt
```

O script `canais-do-laudo.sh` (abra-o para ler: são poucos comandos `curl`) testa onze
combinações de pessoa × endereço na API e três arquivos de laudo direto no servidor do site. Para
cada uma, mostra o código e **o que vazou** na resposta: o tipo do exame (`Glicemia`) ou detalhes
internos (`exception`, `trace`). Hoje, quase tudo dá 200 e várias respostas de erro trazem
`exception` e `trace`. O teste deve sair **vermelho**.

## Passo 4 — Ensine o `UsuarioDao` a responder "quem é este usuário"

O token diz que a Ana é o **usuário 5**, mas a regra precisa saber que ela é a **paciente 1**
(Fase 0, Passo 5). Abra `dao/UsuarioDao.java`. Logo **antes** do comentário
`/** Lista todos os usuarios`, cole:

```java
    /** Numero do paciente ligado a este usuario (ou null, se nao for paciente). */
    public Long pacienteIdDoUsuario(long usuarioId) {
        return umNumero("SELECT paciente_id FROM usuario WHERE id = ?", usuarioId);
    }

    /** Numero do medico ligado a este usuario (ou null, se nao for medico). */
    public Long medicoIdDoUsuario(long usuarioId) {
        return umNumero("SELECT id FROM medico WHERE usuario_id = ?", usuarioId);
    }

    /** Roda uma consulta que devolve um unico numero. O "?" recebe o parametro. */
    private Long umNumero(String sql, long parametro) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;          // nenhuma linha
                long valor = rs.getLong(1);
                return rs.wasNull() ? null : valor;   // coluna vazia (NULL) -> null
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar usuario", e);
        }
    }
```

As consultas novas usam `?` (consulta parametrizada), nunca `+` com o dado.

## Passo 5 — Feche o canal da API: a regra de acesso no `ExameController`

Abra `controller/ExameController.java`.

**5.1 — Imports.** Troque a linha `import org.springframework.web.bind.annotation.*;` por:

```java
import br.ifsul.vidaplus.dao.UsuarioDao;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
```

**5.2 — A regra.** Logo **depois** das linhas `@Autowired` / `private ExameDao exameDao;`, cole:

```java
    @Autowired
    private UsuarioDao usuarioDao;

    /**
     * Deny-by-default: so pode ver os dados clinicos de um paciente
     * o proprio paciente ou o medico que o atende. Qualquer outro papel: nao.
     */
    private boolean podeVerPaciente(Long uid, String papel, long pacienteId) {
        if ("PACIENTE".equals(papel)) {
            Long meuPaciente = usuarioDao.pacienteIdDoUsuario(uid);
            return meuPaciente != null && meuPaciente == pacienteId;
        }
        if ("MEDICO".equals(papel)) {
            Long meuMedico = usuarioDao.medicoIdDoUsuario(uid);
            return meuMedico != null && exameDao.medicoAtende(meuMedico, pacienteId);
        }
        return false;   // qualquer outro papel (ou papel desconhecido): nega
    }

    /** O laboratorio ve os exames ainda em processamento (nao publicados). */
    private boolean podeVerExame(Long uid, String papel, Map<String, Object> exame) {
        if ("BIOQUIMICO".equals(papel)) {
            return ((Number) exame.get("publicado")).intValue() == 0;
        }
        long pacienteId = ((Number) exame.get("paciente_id")).longValue();
        return podeVerPaciente(uid, papel, pacienteId);
    }
```

O `return false` no fim de `podeVerPaciente` é o *deny-by-default*: tudo o que não foi permitido
é negado.

**5.3 — `getExame`.** Troque

```java
        // FALHA: nenhuma verificacao de que 'id' pertence a 'uid'
        return exameDao.porId(id);
```

por

```java
        String papel = JwtUtil.papelDoToken(auth.substring(7));
        Map<String, Object> exame = exameDao.porId(id);
        if (exame == null || !podeVerExame(uid, papel, exame)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return exame;
```

Repare em `exame == null || ...`: o exame que **não existe** e o que **não é seu** recebem **a
mesma** resposta. É o fechamento do oráculo (Fase 2b).

**5.4 — `getPdf`.** Troque

```java
        Map<String, Object> exame = exameDao.porId(id);
        // FALHA: devolve o caminho publico do PDF sem checar ownership
```

por (o `return Map.of(...)` logo depois continua como está):

```java
        String papel = JwtUtil.papelDoToken(auth.substring(7));
        Map<String, Object> exame = exameDao.porId(id);
        if (exame == null || !podeVerExame(uid, papel, exame)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
```

Hoje, pedir o PDF de um exame inexistente dava **500 com stack trace** (o código tentava usar um
exame `null`). Com esta troca, dá 403, igual a todos.

**5.5 — `porPaciente`.** Antes do `return exameDao.doPaciente(pacienteId);`, apague o comentário
`// FALHA` e coloque:

```java
        String papel = JwtUtil.papelDoToken(auth.substring(7));
        if (!podeVerPaciente(uid, papel, pacienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
```

## Passo 6 — Feche o canal da mensagem de erro

**6.1 — Sem login = 401, não 500.** Ainda no `ExameController.java`, use o **Substituir** do VS
Code (**Ctrl+H**): no primeiro campo, `throw new RuntimeException("nao autenticado")`; no segundo,
`throw new ResponseStatusException(HttpStatus.UNAUTHORIZED)`. Clique em **Substituir tudo**
(o ícone à direita do segundo campo). Devem ser **6** substituições.

**6.2 — Nenhum detalhe interno nas respostas.** Abra
`legacy-app/backend/src/main/resources/application.properties` e troque as três linhas
`server.error.*` por:

```properties
server.error.include-message=never
server.error.include-stacktrace=never
server.error.include-exception=false
```

Isso vale para **toda** a API: qualquer erro passa a responder só o código e uma frase genérica
("Forbidden", "Internal Server Error"). O detalhe continua indo para o **log** do servidor, onde
só a equipe vê.

> **E o resto da F-16?** Os DAOs ainda montam mensagens com `e.getMessage()` — elas não chegam
> mais ao usuário, mas vão para o log com detalhes. E a busca ainda responde **500** a uma aspa
> (agora sem detalhes). Isso fica para o plano (Parte B).

## Passo 7 — Remonte, ataque de novo e rode os testes

> 🔎 **Como o Grupo 5 fez:** repetiu **o mesmo ataque** do "antes", conferiu o **uso legítimo**,
> rodou os testes e guardou cada saída como "depois".

**▶ Agora no G4:**

1. Remonte o backend com o código novo e o banco original (se houver erro de digitação, o script
   termina com `ERRO ao ligar` e mostra o arquivo e a linha):

   ```bash
   bash roteiros/comum/zerar-banco.sh
   ```

2. Rode de novo o script dos canais, agora gravando no arquivo do "depois":

   ```bash
   bash roteiros/G4-vazamento-e-ciclo-de-vida/canais-do-laudo.sh | tee entregaveis/evidencias/fase4-canais-depois.txt
   ```

   Esperado:

   | Pedido | Antes | Depois |
   |---|:-:|:-:|
   | Ana → 101 (dela) | 200 | **200** |
   | Ana → 102 (do Bruno) | 200 `Glicemia` | **403**, nada vazado |
   | Ana → 999 (não existe) | 200 (vazio) | **403** — igual ao 102 |
   | Ana → 102/pdf | 200 | **403** |
   | Ana → 999/pdf | 500 `exception` `trace` | **403**, nada vazado |
   | Ana → lista do paciente 2 | 200 | **403** |
   | House → 101 / 102 | 200 / 200 | **200 / 403** |
   | Bio → 104 / 101 | 200 / 200 | **200 / 403** |
   | sem login → 101 | 500 | **401** |

3. **A busca com aspa** (de outro pacote, mas a configuração é global):
   `curl -s -G localhost:8080/api/pacientes/busca --data-urlencode "q='"`. O SQL ainda aparece?
4. **Pela tela:** a Ana trocando `/exames/101` por `/exames/102` → "Exame não encontrado ou acesso
   negado."; o Bio abrindo a **Fila de Laudos** e publicando o 104.
5. **Os testes:**

   ```bash
   bash roteiros/comum/rodar-testes.sh "security & F-02" 2>&1 | tee entregaveis/evidencias/fase4-teste-depois.txt
   bash roteiros/comum/rodar-testes.sh 2>&1 | tee entregaveis/evidencias/fase4-todos-depois.txt
   ```

   O primeiro: **verde**. O segundo: vermelho só pelos testes de SQLi e RBAC (outros grupos).
   Se vocês transformaram o cenário da F-16 em `@security` (Fase 1), ele também deve estar verde.

   > ⚠️ **Os testes mudam o banco:** o teste de RBAC (do G3) **publica o laudo 104**. Depois de
   > rodá-los, o Bio deixa de ver o 104 (ele só vê os não publicados). Por isso o bloco de canais
   > roda **antes** dos testes — e, se quiser repeti-lo, **zere o banco** antes (rode `bash roteiros/comum/zerar-banco.sh` e depois `source roteiros/comum/tokens.sh`).

**Deu errado?**

| Sintoma | Causa provável |
|---|---|
| `cannot find symbol: class UsuarioDao` ou `HttpStatus` | faltou um `import` (5.1) |
| A Ana não vê nem o **próprio** exame | confira o SQL `SELECT paciente_id FROM usuario WHERE id = ?` (Passo 4) |
| Sem login ainda dá 500 | a substituição do 6.1 não foi feita, ou não foi salva |
| Ainda aparece `trace` nas respostas | o `application.properties` não foi salvo, ou o backend não foi remontado (`--build`) |

## Passo 8 — Grave, abra o PR e peça a revisão

```bash
git add legacy-app/backend/src entregaveis/evidencias
git commit -m "fix(F-02): fecha os canais do laudo na API (dono, deny-by-default) e o canal da mensagem de erro"
git push -u origin fix/F-02-canais-do-laudo
```

Abra o PR ([`comum/03` §3, passo 7](../comum/03-git-e-github.md#passo-7--abra-o-pull-request)).
Na descrição: **o que muda** (os três endpoints; a resposta uniforme; o 401; a configuração de
erros), **como testar** (o bloco do Passo 3 e a tabela esperada) e **o teste que deve passar**.

**Quem revisa** usa o checklist do [`docs/07`](../../docs/07-rubrica-avaliacao.md#checklist-de-secure-code-review-para-os-prs-da-poc)
— em especial "autorização server-side" e "erro genérico ao cliente; detalhe só no log" — e roda o
Passo 7 na própria máquina.

## Passo 9 — Escreva a §6.1

> 🔎 **Como o Grupo 5 fez:** **Antes**, **Depois**, **O que rejeitamos e por quê**, **O que esta
> correção NÃO resolve**, **PR**, **Teste**. Veja: [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).

**▶ Agora no G4:** os mesmos blocos. Para **"O que rejeitamos"**: por que não responder **404**
para "não existe" e **403** para "não é seu"? Por que não esconder o exame só na tela? Para **"O
que esta correção NÃO resolve"** — os canais que ficaram abertos:

- o **arquivo** na pasta pública continua baixável sem login? E o `laudo-105.pdf`, ainda revela
  quais laudos existem pelo tipo de conteúdo?
- os números continuam sequenciais?
- a notificação ainda não existe — quando existir, nasce segura?

---

# Parte B — Plano de remediação

> 🔎 **Como o Grupo 5 fez:** tabela **funcionalidade | falha | controle proposto | esforço (P/M/G)
> | prioridade | observação**, com a escala de esforço definida; o que dependia de outro pacote,
> "transferido". Veja: [`docs/12` §6.2](../../docs/12-exemplo-dossie-preenchido.md#62-plano-de-remediação-demais-falhas-do-pacote--não-implementadas).

**▶ Agora no G4:** uma linha para **cada canal ou falha não corrigida**. Pontos de partida:

| Falha | Perguntas para chegar ao controle |
|---|---|
| Arquivo direto na pasta pública | tirar os PDFs da pasta pública e entregar **pela API**, com a mesma regra? Interface com o G2 (F-08) |
| Oráculo pelo arquivo (`laudo-105.pdf` → 200 HTML) | o que muda no `nginx.conf` para `/laudos/` responder igual para tudo (ou deixar de existir)? |
| Resto da F-16 | as mensagens com `e.getMessage()` nos DAOs; a busca respondendo 500 a uma aspa (é falha de outro pacote, mas a resposta é de vocês) |
| Notificação (F-18) | o texto mínimo; o link que exige login e expira; o que **nunca** vai na mensagem |
| Site (F-20) | cabeçalhos de segurança no `nginx.conf` (`Content-Security-Policy`, `X-Frame-Options`, `X-Content-Type-Options`); quem pode mudar o conteúdo (só por PR aprovado — a proteção da `main`); como perceber uma troca |
| Números sequenciais | identificadores aleatórios |
| Criar atendimento por qualquer papel abre o acesso do médico | interface com o G3 (F-03): a regra de vocês confia na tabela `atendimento` |

---

# Parte C — Especificar a retenção → eliminação (F-06)

Nesta turma, a criação é **especificada**, não implementada.

> 🔎 **Como o Grupo 5 fez** (recorte **anonimização** — a técnica): **entidade**, **gatilho** com
> colunas que existem, **tratamento atributo por atributo**, **limitações** (o PDF fora do alcance;
> o teste que prova supressão, não anonimato) e o `.feature @analise`. Veja o Controle 2 em
> [`docs/12` §6.1](../../docs/12-exemplo-dossie-preenchido.md#61-prova-de-conceito-12-controles-implementados).
> **O recorte de vocês é outro:** **quando** e **o quê** — o Grupo 5 é referência de nível e de
> técnica, não de conteúdo.

**▶ Agora no G4** — escopo mínimo ([`docs/05`](../../docs/05-pacotes-trabalho-estudantes.md#escopo-mínimo-da-poc--criação--para-equiparar-o-esforço)):
**1 entidade**, regra de **retenção → eliminação/anonimização**, **respeitando a retenção legal**.
Escrevam:

1. **A entidade** (sugestão: o paciente e tudo o que pertence a ele) e **a tabela de retenção** da
   Fase 2b, finalizada.
2. **O gatilho**, com colunas que **existem** no banco. (Pista: o "último registro" da Lei 13.787
   pode ser a data mais recente entre `atendimento.data`, `exame.criado_em` e
   `nota_clinica.criado_em`. E o `ativo`?)
3. **O que acontece no fim do prazo, por categoria:** elimina, anonimiza ou mantém (por qual base
   legal, até quando).
4. **O pedido do titular** (art. 18, VI): o fluxo com a `entity/SolicitacaoTitular.java`
   (`tipo = ELIMINACAO`, `status`, datas); o que é atendido na hora, o que é recusado por obrigação
   legal, e **o texto da resposta** ao titular nos dois casos.
5. **Onde mais o dado vive:** arquivos PDF, backups, logs, notificações enviadas. O que a rotina
   alcança e o que não alcança (limitações assumidas).
6. **Salvaguardas contra eliminar demais:** a eliminação não tem volta. Simulação antes ("o que
   seria eliminado?"), aprovação do DPO, tudo numa transação.
7. **A prova:** depois de eliminar, o que fica registrado para mostrar à ANPD — **sem** guardar o
   dado eliminado.
8. **Comportamento sob falha** (Fase 3).
9. **O `.feature @analise`** da F-06 (Fase 1), com pelo menos: "o prontuário dentro do prazo legal
   é mantido mesmo com pedido do titular", "o dado vencido é eliminado" e "o titular recebe a
   resposta explicando o que foi mantido e por quê".

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
- [ ] Tenho a tabela de canais e o teste, **antes e depois**, em `entregaveis/evidencias/`.
- [ ] "Não existe" e "não é seu" dão a mesma resposta; nenhuma resposta traz `exception`/`trace`;
      sem login dá 401.
- [ ] Cada papel continua fazendo o trabalho legítimo (Ana, House, Bio).
- [ ] A §6.1 lista os canais que **ficaram abertos**.
- [ ] A F-06 está especificada nos 9 itens da Parte C.
