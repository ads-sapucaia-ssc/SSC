# 06 · Pipeline CI/CD — gates de segurança do SSDLC

O arquivo [`.github/workflows/ssdlc-pipeline.yml`](../.github/workflows/ssdlc-pipeline.yml)
implementa o pipeline. A ideia pedagógica: **tornar a segurança visível e versionada**. O
pipeline nasce com quase todos os gates **vermelhos** (o legado é inseguro) e a **prova de
conceito** do grupo faz o(s) gate(s) da classe corrigida transitar **🔴 → 🟢**.

> ⚠️ **O pipeline é gerador de EVIDÊNCIA e FEEDBACK — não é a nota.** Como o foco é o
> processo (SSDLC + LGPD + privacidade), a avaliação vem dos **artefatos** e da **defesa**
> (ver [08-rubrica-avaliacao.md](08-rubrica-avaliacao.md)). Um gate verde **sustenta** a
> evidência de um controle; ele não substitui o requisito, o threat model, o RIPD e a
> rastreabilidade por trás. Nem todos os gates precisam ficar verdes — só os das PoCs
> escolhidas; os demais sustentam o **plano de remediação**.

---

## 1. Gates e o que cada um verifica

| Gate | Ferramenta | Fase SSDLC | Falhas que acusa |
|------|-----------|:----------:|------------------|
| Build backend | Maven | 4 | compila/roda + testes |
| Build frontend | React (Vite) | 4 | compila |
| **Secret Scanning** | Gitleaks | 4/6 | V-05 (segredos no código) |
| **SAST** | Semgrep + CodeQL | 4/5 | V-01, V-02, V-07, V-10 |
| **SCA** | Dependency-Check + Trivy (fs) | 5/6 | higiene de supply chain (CVE em dependências) |
| **Container Scanning** | Trivy (image) | 5/6 | CVE na imagem base/SO do backend/frontend |
| **SBOM** | Syft (CycloneDX) | 6 | inventário de componentes |
| **DAST autenticado** | OWASP ZAP (com JWT) | 5 | V-03 IDOR, V-11, V-12, V-10 (dinâmico, autenticado) |
| **Testes de segurança** | JUnit (`-Dgroups=security`) | 5 | V-03 IDOR, V-04 RBAC, auth, V-06 auditoria |
| Placar | summary job | 8 | consolida o estado dos gates |

> IDOR (V-03) e RBAC (V-04) são falhas **lógicas** — não aparecem em scanner genérico.
> São cobertas por **testes de segurança** escritos pelos grupos (ex.: paciente A pede
> `GET /api/exames/{id_de_B}` e o teste exige **403 sem metadados**).

> **Por que não há IaC scanning (Checkov):** aplique o controle **proporcional ao contexto** —
> o VidaPlus roda **localmente** via Docker Compose, sem Terraform/Kubernetes a analisar.

---

## 2. Informativo → bloqueante

No legado, os gates usam `continue-on-error: true` (**informativo**): reportam vermelho
mas não travam o merge, senão nada avança. Conforme um grupo corrige uma classe de falha,
ele **remove o `continue-on-error`** daquele gate — que passa a ser **bloqueante**:
a partir dali, qualquer regressão volta a barrar o merge.

```yaml
secret-scan:
  name: "Gate - Secret Scanning (Gitleaks)"
  runs-on: ubuntu-latest
  continue-on-error: true      # <-- legado (informativo). REMOVA só esta linha => gate bloqueante
  steps:
    - uses: actions/checkout@v4
    # ... (o resto do job permanece inalterado)
```

> Remova **apenas** a linha `continue-on-error: true`; o job (`name`/`runs-on`/`steps`)
> **permanece**. A meta é tornar o gate **bloqueante**, não excluí-lo — apagar o bloco
> inteiro removeria o próprio gate (o Gitleaks deixaria de rodar).

Essa transição é, ela própria, um **entregável avaliável** (Fase 6): "quais gates o grupo
tornou bloqueantes e mantém verdes?".

---

## 3. Fluxo recomendado de PR

1. Grupo cria branch `fix/F-02-idor`.
2. Implementa a correção + teste de segurança.
3. Abre PR; o pipeline roda todos os gates.
4. **Revisão por pares intra-grupo** (regra dos "4 olhos"): **outro membro do grupo — não
   o autor do PR** — revisa com o [checklist de secure code review](08-rubrica-avaliacao.md#L55)
   e **aprova**.
5. Merge só com o gate da classe corrigida **verde** **e** a revisão **aprovada**.
6. Atualiza a **matriz de rastreabilidade** (requisito → teste → evidência → status).

> **Quem revisa:** a revisão é **intra-grupo** e **bloqueante** — autor ≠ revisor. Isso
> mantém os grupos independentes (nenhum PR espera por outra equipe).
>
> **Torne "verde + aprovação" uma regra:** os passos 4–5 devem ser **impostos** por
> *branch protection* na `main` — exigir os **status checks verdes** dos gates das PoCs **e
> 1 aprovação** antes do merge. É a prática **PS.1 do NIST SSDF** (proteger o código-fonte
> contra adulteração/bypass). Passo a passo para o aluno configurar em
> [09-setup-grupos.md §5.2](09-setup-grupos.md).

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

## 5. Atividade obrigatória — supply chain: assinatura e proveniência

Fecha a Parte 9 do conteúdo (Deploy + Supply Chain) e é **entregável da Fase 6** (ver
[08-rubrica-avaliacao.md](08-rubrica-avaliacao.md)). O grupo demonstra que sabe **provar a
origem** de um artefato, não só produzi-lo.

**SLSA** (*Supply-chain Levels for Software Artifacts*, OpenSSF) — 4 níveis:
`L0` sem proveniência · `L1` proveniência básica · `L2` proveniência **assinada** ·
`L3` build endurecido/auditado. **Meta obrigatória: L2.**

**Sigstore / cosign** assina por **identidade OIDC** (*keyless* — sem gerenciar chaves de
longa duração): o GitHub Actions emite um token OIDC, o cosign registra a assinatura no log
público **Rekor** e a amarra ao **quem/como** do build.

### Ponto-chave: assina-se imagem **publicada**, pelo digest

Você **não** assina um `docker build` local — assina uma imagem que já está **num registry**,
e **pelo digest** (imutável), não pela tag. Logo, a ordem é: **login → build & push →
assinar → verificar**. Adicione um **job novo** ao seu `ssdlc-pipeline.yml`.

**1) Permissões (no job) — sem isto, falha:**

```yaml
permissions:
  contents: read
  packages: write     # publicar a imagem no GHCR (ghcr.io)
  id-token: write     # token OIDC que o cosign usa (keyless)
```

**2) Login no GHCR + build & push (capture o digest):**

```yaml
- uses: actions/checkout@v4
- uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
- id: push
  uses: docker/build-push-action@v6
  with:
    context: legacy-app/backend
    push: true
    tags: ghcr.io/${{ github.repository }}/vidaplus-backend:${{ github.sha }}
```

O passo `id: push` expõe **`steps.push.outputs.digest`** — use-o para assinar.

**3) Assinar (keyless) e verificar:**

```yaml
- uses: sigstore/cosign-installer@v3
- name: Assinar por digest
  run: cosign sign --yes
        ghcr.io/${{ github.repository }}/vidaplus-backend@${{ steps.push.outputs.digest }}
- name: Verificar (a identidade deve ser o SEU workflow)
  run: cosign verify
        --certificate-identity-regexp "https://github.com/${{ github.repository }}/.*"
        --certificate-oidc-issuer "https://token.actions.githubusercontent.com"
        ghcr.io/${{ github.repository }}/vidaplus-backend@${{ steps.push.outputs.digest }}
```

> ⚠️ Na verificação, **não** use `--certificate-identity-regexp '.*'`: isso aceitaria
> **qualquer** signatário e esvazia a proveniência. Amarre ao **seu** repositório.

### Entregável (evidência)
- job **verde** com `cosign sign` + `cosign verify` passando (print/log do run);
- a imagem em **Packages** do repo, marcada como assinada;
- **bônus:** atestar o **SBOM** da imagem (`cosign attest --type cyclonedx …`), ligando a
  proveniência ao **conteúdo**.

Imagem assinada + verificação + SBOM atestado = **SLSA L2** na prática para o artefato do grupo.
