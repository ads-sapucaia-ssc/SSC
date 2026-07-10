# 06 · Pipeline CI/CD — gates de segurança do SSDLC

O arquivo [`.github/workflows/ssdlc-pipeline.yml`](../.github/workflows/ssdlc-pipeline.yml)
implementa o pipeline. A ideia pedagógica: **segurança vira métrica objetiva e
versionada**. O pipeline nasce com quase todos os gates **vermelhos** (o legado é
inseguro) e o progresso da turma é medido pela transição **🔴 → 🟢**.

---

## 1. Gates e o que cada um verifica

| Gate | Ferramenta | Fase SSDLC | Falhas que acusa |
|------|-----------|:----------:|------------------|
| Build backend | Maven | 4 | compila/roda + testes |
| Build frontend | Angular CLI | 4 | compila |
| **Secret Scanning** | Gitleaks | 4/6 | V-05 (segredos no código) |
| **SAST** | Semgrep + CodeQL | 4/5 | V-01, V-02, V-07, V-10 |
| **SCA** | Dependency-Check + Trivy | 5/6 | V-18 (dependências com CVE) |
| **SBOM** | Syft (CycloneDX) | 6 | inventário de componentes |
| **DAST** | OWASP ZAP baseline | 5 | V-11, V-12, V-01, V-10 (dinâmico) |
| **Testes de segurança** | JUnit (`-Dgroups=security`) | 5 | V-03 IDOR, V-04 RBAC, auth, V-06 auditoria |
| Placar | summary job | 8 | consolida o estado dos gates |

> IDOR (V-03) e RBAC (V-04) são falhas **lógicas** — não aparecem em scanner genérico.
> São cobertas por **testes de segurança** escritos pelos grupos (ex.: paciente A pede
> `GET /api/exames/{id_de_B}` e o teste exige **403 sem metadados**).

---

## 2. Informativo → bloqueante

No legado, os gates usam `continue-on-error: true` (**informativo**): reportam vermelho
mas não travam o merge, senão nada avança. Conforme um grupo corrige uma classe de falha,
ele **remove o `continue-on-error`** daquele gate — que passa a ser **bloqueante**:
a partir dali, qualquer regressão volta a barrar o merge.

```yaml
# antes (legado, informativo):
secret-scan:
  continue-on-error: true
# depois (corrigido, bloqueante): remova a linha acima
```

Essa transição é, ela própria, um **entregável avaliável** (Fase 6): "quais gates o grupo
tornou bloqueantes e mantém verdes?".

---

## 3. Fluxo recomendado de PR

1. Grupo cria branch `fix/F-02-idor`.
2. Implementa a correção + teste de segurança.
3. Abre PR; o pipeline roda todos os gates.
4. Revisão por pares (checklist de secure code review).
5. Merge só com o gate da classe corrigida **verde**.
6. Atualiza a **matriz de rastreabilidade** (requisito → teste → evidência → status).

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

## 5. Extensões sugeridas (bônus)

- **Container scanning** (Trivy image) do backend/frontend.
- **IaC scanning** (Checkov) se houver Terraform/K8s.
- **DAST autenticado** no ZAP (com token) para exercitar IDOR de forma automatizada.
- **Assinatura de artefatos** (cosign) e verificação de proveniência (SLSA).
- **Branch protection** exigindo os gates verdes para merge na `main`.
