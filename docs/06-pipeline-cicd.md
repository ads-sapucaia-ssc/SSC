# 06 · Pipeline CI/CD — gates de segurança do SSDLC

O arquivo [`.github/workflows/ssdlc-pipeline.yml`](../.github/workflows/ssdlc-pipeline.yml)
implementa o pipeline. A ideia pedagógica: **tornar a segurança visível e versionada**. O
pipeline nasce com quase todos os gates **vermelhos** (o legado é inseguro) e a **prova de
conceito** do grupo faz o **cenário do seu P0** (no job de testes) e, com o F-10, o **Gitleaks**
transitarem **🔴 → 🟢**.

> ⚠️ **O pipeline é gerador de EVIDÊNCIA e FEEDBACK — não é a nota.** Como o foco é o
> processo (SSDLC + LGPD + privacidade), a avaliação vem dos **artefatos** e da **defesa**
> (ver [07-rubrica-avaliacao.md](07-rubrica-avaliacao.md)). Um gate verde **sustenta** a
> evidência de um controle; ele não substitui o requisito, o threat model, o RIPD e a
> rastreabilidade por trás. Os gates cobrem o repositório **inteiro** e seguem vermelhos pelas
> falhas que ficaram só analisadas — isso é esperado; esses vermelhos sustentam o **plano de
> remediação**.

---

## 1. Gates e o que cada um verifica

| Gate | Ferramenta | Fase SSDLC | Falhas que acusa |
|------|-----------|:----------:|------------------|
| Build backend | Maven | 4 | compila/roda + testes |
| Build frontend | React (Vite) | 4 | compila |
| **Secret Scanning** | Gitleaks | 4/6 | segredos no código (F-10) |
| **SAST** | Semgrep + CodeQL | 4/5 | SQL concatenado (F-01, F-13), MD5 (F-01), token sem expiração (F-11), `dangerouslySetInnerHTML` (F-14) |
| **SCA** | Dependency-Check + Trivy (fs) | 5/6 | higiene de supply chain (CVE em dependências) |
| **Container Scanning** | Trivy (image) | 5/6 | CVE na imagem base/SO do backend/frontend |
| **SBOM** | Syft (CycloneDX) | 6 | inventário de componentes |
| **DAST autenticado** | OWASP ZAP baseline (com JWT) | 5 | cabeçalhos de segurança ausentes — CSP, HSTS, X-Content-Type-Options (F-17) — e erro que vaza detalhes (F-16) — varredura **passiva**: não acha IDOR |
| **Testes de segurança** | JUnit + Cucumber (`-Dgroups=security`) | 5 | IDOR (F-02), papel indevido (F-03), SQLi no login (F-01) — os testes-semente |
| Placar | summary job | 8 | consolida o estado dos gates |

> IDOR (F-02) e RBAC (F-03) são falhas **lógicas** — não aparecem em scanner genérico.
> São cobertas pelos **testes-semente** que já vêm no repositório (cenários `@security`; ex.:
> a paciente Ana pede `GET /api/exames/102`, que é do Bruno, e o teste exige **403 sem
> metadados**).

> **Por que não há IaC scanning (Checkov):** aplique o controle **proporcional ao contexto** —
> o VidaPlus roda **localmente** via Docker Compose, sem Terraform/Kubernetes a analisar.

---

## 1.1 Onde cada ferramenta vive — a pasta `security/`

A pasta [`security/`](../security/) **não guarda as ferramentas** do pipeline — guarda
apenas os **arquivos de configuração customizada** das ferramentas que precisam de ajuste
específico do projeto. As ferramentas em si **não ficam versionadas no repositório**: o
GitHub Actions **baixa cada uma em tempo de execução** (via `uses:` de uma *action* pronta,
ou `container:`/`run:` com uma imagem Docker).

Por isso `security/` tem só **4 arquivos** — são exatamente as 4 ferramentas que exigem
config própria. As demais rodam com **configuração padrão** (nada a versionar) ou têm a sua
config **junto do código** (`pom.xml`, `package.json`).

| Ferramenta | Como o pipeline a obtém | Config no repositório? |
|-----------|--------------------------|------------------------|
| **Gitleaks** | CLI em Docker (`zricethezav/gitleaks:v8.21.2`), só no histórico de `legacy-app/`, com `--redact` (a `gitleaks-action@v2` exige licença paga em organizações) | ✅ [security/gitleaks/gitleaks.toml](../security/gitleaks/gitleaks.toml) |
| **Semgrep** | `container: returntocorp/semgrep` + conjuntos públicos `p/owasp-top-ten`, `p/java` | ✅ [security/semgrep/vidaplus.yml](../security/semgrep/vidaplus.yml) (regras próprias) |
| **Dependency-Check** | `dependency-check/Dependency-Check_Action@main` | ✅ [security/dependency-check/suppression.xml](../security/dependency-check/suppression.xml) |
| **OWASP ZAP** | `zaproxy/action-baseline@v0.12.0` | ✅ [security/zap/rules.tsv](../security/zap/rules.tsv) |
| **CodeQL** | `github/codeql-action/{init,analyze}@v3` | ❌ usa as *queries* padrão |
| **Trivy** (fs + imagem) | `aquasecurity/trivy-action@master` | ❌ config padrão |
| **Syft** (SBOM) | `anchore/sbom-action@v0` | ❌ config padrão |
| **cosign / Sigstore** | `sigstore/cosign-installer@v3` | ❌ *keyless* via OIDC (sem segredo) |
| **Maven / JUnit** | `actions/setup-java@v4` + `mvn` | ✅ em [legacy-app/backend/pom.xml](../legacy-app/backend/pom.xml) |
| **React / Vite** | `actions/setup-node@v4` + build | ✅ em [legacy-app/frontend/](../legacy-app/frontend/) |

> **Os segredos do próprio pipeline.** Depois da correção da F-10 (Fase 6, `roteiros/comum/08`),
> o VidaPlus só sobe com as senhas e a chave do token vindas do ambiente. O pipeline já está
> preparado: nos jobs que sobem a aplicação (DAST, staging), a etapa **"Segredos descartaveis
> deste ambiente"** cria um `legacy-app/.env` com valores sorteados a cada execução; nos jobs de
> teste, a etapa **"Chave do token so para este teste"** faz o mesmo com a `JWT_SECRET`. Nada fica
> gravado no repositório, e antes da correção essas etapas não têm efeito.

> A quantidade de arquivos em `security/` **não** deveria bater com a quantidade de
> ferramentas do pipeline: ela reflete só **quantas precisam de configuração customizada**.
> Maven/JUnit e React/Vite são ferramentas de **build**, não de segurança — por isso a
> config delas vive junto do código, não em `security/`.

---

## 2. Informativo × bloqueante

Os gates usam `continue-on-error: true` (**informativo**): reportam vermelho, mas não travam o
merge — senão nada avançaria, porque o repositório de cada grupo ainda tem as falhas dos outros
pacotes. Um gate **bloqueante** seria o contrário: sem essa linha, e exigido na proteção da
`main`, qualquer regressão barraria o merge.

```yaml
secret-scan:
  name: "Gate - Secret Scanning (Gitleaks)"
  runs-on: ubuntu-latest
  continue-on-error: true      # <-- informativo: reporta, mas não trava o merge
  steps:
    - uses: actions/checkout@v4
    # ...
```

> **Neste projeto, os gates ficam informativos** e o grupo **não** precisa torná-los
> bloqueantes. O que se espera é **entender** a diferença e **explicar** no dossiê (§8) por que,
> num projeto real, o gate da classe corrigida deveria virar bloqueante.

---

## 3. Fluxo recomendado de PR

1. Grupo cria branch `fix/F-02-idor`.
2. Implementa a correção (o teste-semente do P0 já existe; o grupo o roda antes e depois).
3. Abre PR; o pipeline roda todos os gates.
4. **Revisão por pares intra-grupo** (regra dos "4 olhos"): **outro membro do grupo — não
   o autor do PR** — revisa com o [checklist de secure code review](07-rubrica-avaliacao.md#checklist-de-secure-code-review-para-os-prs-da-poc)
   e **aprova**.
5. Merge com a revisão **aprovada**; a evidência é o **cenário do P0 verde** no log do job de
   testes.
6. Atualiza a **matriz de rastreabilidade** (requisito → teste → evidência → status).

> **Quem revisa:** a revisão é **intra-grupo** e **bloqueante** — autor ≠ revisor. Isso
> mantém os grupos independentes (nenhum PR espera por outra equipe).
>
> **Num projeto real, "verde + aprovação" vira regra:** os passos 4–5 são **impostos** por
> *branch protection* na `main` — exigir os **status checks verdes** dos gates **e 1
> aprovação** antes do merge. É a prática **PS.1 do NIST SSDF** (proteger o código-fonte
> contra adulteração/bypass). *No VidaPlus (recorte do projeto):* o **professor** configura a
> exigência de PR + 1 aprovação; os gates ficam **informativos**, porque cobrem as falhas de
> todos os pacotes ([08-setup-grupos.md §3.1](08-setup-grupos.md#31-proteção-da-main-ruleset)).
> O grupo explica no dossiê.

---

## 4. Evidências

Cada execução do pipeline gera evidências reutilizáveis na defesa (Fase 8):
- SARIF de SAST na aba **Security** do repositório;
- relatório HTML do ZAP (artifact);
- relatório do Dependency-Check;
- **SBOM** (`sbom.cdx.json`);
- log dos testes de segurança;
- **placar de gates** no summary de cada run.

---

## 5. Entrega segura (já pré-fiada): assinatura, verificação e promoção

É **entregável da Fase 6**. **O pipeline já faz tudo isto** — você **não configura
ferramenta** (usa **OIDC + `GITHUB_TOKEN`**, sem segredos). Seu trabalho é **entender e
explicar no dossiê** com a evidência do run.

### O fluxo de CD (roda na `main`)

```
release-sign  →  deploy-staging  →  deploy-prod
```

1. **`release-sign`** — *build once*: builda e **publica** a imagem no **GHCR**, **assina**
   com **cosign** (keyless/OIDC, registrado no **Rekor**) e **atesta o SBOM** da imagem
   (anexa a lista de componentes, também assinada).
2. **`deploy-staging`** — "sobe" a app num **staging** efêmero e faz um *smoke* (confere que
   ela responde). O DAST roda em job próprio, sobre outra instância efêmera.
3. **`deploy-prod`** — **admission gate**: `cosign verify` + `verify-attestation` **recusam
   imagem não assinada/atestada**, e só executa **após aprovação manual** do ambiente
   `production`. É o "*build once, deploy many*" **com portão**.

**Conceitos (para a defesa):** **Sigstore/cosign** assina por **identidade** (não por chave); e
assinar só tem valor porque **alguém verifica** — por isso o `deploy-prod` recusa o
não-assinado. **SLSA** L0→L3 mede a confiança da **proveniência** do build.

> **Assinar a imagem + atestar o SBOM ainda não é SLSA L2.** São duas perguntas diferentes:
>
> | Documento | Responde | O pipeline gera? |
> |---|---|---|
> | **SBOM** | *do que* a imagem é feita (bibliotecas e versões) | ✅ sim, assinado |
> | **Proveniência** | *como* ela foi construída: de qual repositório e commit, por qual workflow | ❌ não |
>
> O nível 2 do SLSA exige a **proveniência assinada** pela plataforma de build. O que o pipeline já
> garante é que a imagem publicada **é a que ele construiu** e que ninguém a trocou depois — um
> passo importante, mas não o L2. Para chegar lá, faltaria gerar e assinar a proveniência (por
> exemplo, com `cosign attest --type slsaprovenance`). No dossiê, explique essa diferença: saber
> dizer **o que ainda falta** vale mais do que repetir um rótulo.

### O que você faz (não é configurar ferramenta)
- **Ler** os jobs `release-sign` / `deploy-staging` / `deploy-prod` numa execução na `main`
  (aba **Actions**).
- **Explicar no dossiê (§8)**: o fluxo acima + a **evidência** (imagem assinada em
  **Packages**; `cosign verify`/`verify-attestation` passando; a **pausa de aprovação** do
  `production`).
- **Nenhuma configuração:** o Environment `production` com revisor e a proteção da `main` são
  feitos pelo professor ([08-setup-grupos.md §3](08-setup-grupos.md#3-o-que-o-professor-configura-no-repositório)).

> ⚠️ A verificação amarra a identidade ao **seu** repositório
> (`--certificate-identity-regexp "https://github.com/<owner>/<repo>/.*"`) — **nunca** `.*`,
> que aceitaria qualquer signatário e esvaziaria a proveniência.

### Por que a identidade na verificação é o que importa

O ponto que costuma confundir: **assinar não vale nada se, ao verificar, você não checar
quem assinou.** O `--certificate-identity-regexp` é justamente o filtro de "quem"; pôr
`.*` ali é o mesmo que não checar nada.

**Assinatura *keyless* (por identidade).** O `release-sign` não assina com uma chave
guardada em segredo — assina por **identidade** (OIDC). Quando o job roda, o Sigstore emite
um certificado de curta duração que carimba na assinatura **quem assinou** — a identidade do
próprio workflow, algo como
`https://github.com/<owner>/<repo>/.github/workflows/ssdlc-pipeline.yml@refs/heads/main`.
A assinatura não diz só "isto foi assinado"; diz "isto foi assinado **por este workflow,
neste repositório**".

**Qualquer um consegue uma assinatura válida.** Um atacante abre o repositório dele, roda um
workflow e o Sigstore emite um certificado perfeitamente válido com a identidade *dele*,
registrado no Rekor. Por isso a pergunta certa na verificação **não** é "existe uma
assinatura válida?" — é "existe uma assinatura válida feita pela **identidade em que eu
confio**?".

| Valor de `--certificate-identity-regexp` | O que o `cosign verify` aceita |
|------------------------------------------|--------------------------------|
| `https://github.com/<owner>/<repo>/.*`   | **só** assinaturas feitas por um workflow **deste** repositório |
| `.*`                                     | **qualquer** identidade — de qualquer repositório, de qualquer pessoa |

**O ataque que o `.*` abriria.** Suponha o `deploy-prod` verificando com `.*`:

1. O atacante builda uma imagem maliciosa (`vidaplus-backend` com backdoor).
2. Assina com o **próprio** GitHub Actions, no **próprio** repositório — assinatura Sigstore
   legítima, identidade `https://github.com/atacante/malware/...`.
3. Consegue empurrar essa imagem/tag para o seu GHCR (ou você a puxa por engano).
4. O `deploy-prod` roda `cosign verify` → com `.*`, a identidade do atacante **casa** com o
   filtro → **verificação passa** → a imagem com backdoor vai para produção.

Com o regexp preso ao seu repositório, o passo 4 **falha**:
`https://github.com/atacante/malware/...` não casa com
`https://github.com/<owner>/<repo>/.*`, o `cosign verify` retorna erro e o *admission gate*
**recusa** a imagem.

**A analogia.** É o segurança conferindo documento na porta:
- **assinatura válida** = "o passaporte é autêntico" — todo mundo tem um, não filtra ninguém;
- **`--certificate-identity-regexp <seu repo>`** = "o nome está na lista de convidados" — *aí* filtra;
- **`.*`** = "deixe entrar qualquer um com passaporte autêntico" — é jogar a lista fora.

Por isso `.*` **esvazia a proveniência**: proveniência é saber *de onde* o artefato veio;
aceitar qualquer signatário destrói exatamente essa garantia — a verificação continua
existindo, mas não prova mais nada.

> O `--certificate-oidc-issuer "https://token.actions.githubusercontent.com"` é o **segundo
> cadeado**: exige que a identidade tenha sido emitida pelo OIDC **do GitHub Actions**. Os
> dois juntos dizem: "assinado por um workflow do GitHub Actions **E** desse workflow ser do
> meu repositório".
