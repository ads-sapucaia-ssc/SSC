# 08 · Rubrica de avaliação (alinhada às fases)

A avaliação é **contínua e por fase** — não há prova única. Cada fase tem entregável e
peso. A soma é 100, com bônus por automação/rigor.

| # | Fase / critério | Pontos | O que se espera |
|---|-----------------|:------:|-----------------|
| 0 | Inventário & classificação de dados | 8 | Ativos e endpoints mapeados; dados classificados; fluxos críticos identificados |
| 1 | Requisitos & LGPD | 12 | RFS+RGS testáveis; base legal por dado; nível ASVS justificado |
| 2 | Threat modeling (STRIDE) | 10 | DFD com trust boundaries; STRIDE-per-element; árvore de ataque |
| 2p | **Privacidade (LINDDUN + PbD)** | 12 | LINDDUN-per-element; controles/PET; esboço de RIPD; minimização aplicada |
| 3 | Projeto seguro & priorização | 10 | Catálogo consolidado; risco→P0/P1/P2; **Definition of Done** segura |
| 4 | Implementação (correções) | 18 | PRs corrigindo as falhas do pacote; novos módulos LGPD (consentimento/erasure/export) |
| 5 | Verificação (SAST/DAST/SCA + testes) | 14 | Testes que exercitam os requisitos; scanners no CI; evidências reais |
| 6 | DevSecOps (pipeline) | 8 | Gates verdes/bloqueantes; SBOM; sem segredos no repo |
| 7 | Operação & resposta | 4 | Trilha de auditoria íntegra; runbook + fluxo de notificação ANPD |
| 8 | Rastreabilidade & defesa | 4 + defesa | Matriz completa; métricas; defesa fecha o fio ao vivo |
| — | **Total** | **100** | |

**Bônus (até +10):** gates bloqueantes mantidos verdes; DAST autenticado exercitando
IDOR; attack-defense tree quantificada; assinatura de artefatos/SBOM; anonimização real
na eliminação.

**Penalidades (até −15):** requisito vago/não testável; "segurança no fim"; correção sem
teste/evidência; status "OK" na matriz sem prova; **PII em log** (regride V-17); segredo
reintroduzido no código; comportamento sob falha ignorado.

---

## Métrica-mestra do semestre

> **Gates verdes / total de gates** ao longo do tempo — o placar objetivo do progresso da
> turma, versionado no pipeline. Um grupo que leva 3 gates de vermelho a verde-bloqueante
> entregou segurança **mensurável**, não apenas documentada.

## Definition of Done por funcionalidade (repete a de 05)

1. Requisito (RFS+RGS) com ameaça/base legal.
2. Ameaça mapeada em STRIDE e/ou LINDDUN.
3. Correção em PR revisado e mergeado.
4. Teste que exercita + cobertura por scanner quando aplicável.
5. Evidência real anexada (log CI/print/relatório).
6. Linha "Verificado" na matriz de rastreabilidade.
7. Gate da classe de falha 🔴 → 🟢.

## Checklist de secure code review (para os PRs)

- [ ] Entrada validada e consultas parametrizadas (sem concatenação SQL).
- [ ] Autorização server-side (ownership + RBAC deny-by-default).
- [ ] Sem segredo no código; config por ambiente.
- [ ] Erro genérico ao cliente; detalhe só no log (sem PII).
- [ ] Evento de segurança auditado (append-only, sem PII).
- [ ] Dado tratado tem base legal e finalidade (LGPD).
- [ ] Teste cobre o caminho de ataque e o comportamento fail-secure.
