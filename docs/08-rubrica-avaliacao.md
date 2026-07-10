# 08 · Rubrica de avaliação (alinhada às fases)

A avaliação é **contínua e por fase** — não há prova única. O foco da disciplina é
**SSDLC + LGPD + privacidade**, então o peso está nos **artefatos do processo**; a
remediação de código entra como **prova de conceito** e o pipeline como **evidência**, não
como a nota em si.

| # | Fase / critério | Pontos | O que se espera |
|---|-----------------|:------:|-----------------|
| 0 | Inventário & classificação de dados | 8 | Ativos e endpoints mapeados; dados classificados; fluxos críticos identificados |
| 1 | Requisitos & **LGPD** | 14 | RFS+RGS testáveis; **base legal por dado**; nível ASVS justificado |
| 2 | Threat modeling (STRIDE) | 10 | DFD com trust boundaries; STRIDE-per-element; árvore de ataque |
| 2p | **Privacidade — LINDDUN + PbD + RIPD/DPIA** | 18 | LINDDUN-per-element; controles/PET; minimização; **RIPD** derivado |
| 3 | Projeto seguro & priorização | 10 | Catálogo consolidado; risco→P0/P1/P2 (**CVSS/DREAD + EPSS/KEV**); **Definition of Done** segura |
| 4 | **Remediação — prova de conceito** | 8 | 1–2 controles corrigidos ponta-a-ponta **+ plano de remediação** do restante |
| 5 | Verificação & evidências | 12 | Teste que exercita o controle; scanners como evidência; evidência real |
| 6 | DevSecOps / pipeline (evidência) | 6 | Pipeline gera evidência; SBOM; sem segredos no repo |
| 7 | **Operação, incidente & LGPD art. 48** | 6 | Trilha de auditoria íntegra; runbook + fluxo de notificação ANPD |
| 8 | Rastreabilidade & defesa | 8 + defesa | Matriz completa; métricas; **defesa fecha o fio ao vivo** |
| — | **Total** | **100** | |

> **Onde está o peso:** Fases 1, 2, 2p e 8 (requisitos+LGPD, threat modeling, **privacidade**
> e rastreabilidade) somam **50 pts** — metade da nota está no **processo e na privacidade**.
> A implementação (F4) vale **8**: importa **provar o ciclo**, não a quantidade de código.

**Bônus (até +10):** attack-defense tree quantificada; DAST autenticado exercitando IDOR;
mais de um módulo LGPD implementado; anonimização real na eliminação; gate tornado
bloqueante e mantido verde; **assinatura de artefatos (cosign) + SLSA L2**; priorização
usando **EPSS/KEV** além do CVSS.

**Penalidades (até −15):** requisito vago/não testável; "privacidade/segurança no fim";
correção sem teste/evidência; status "OK" na matriz sem prova; **PII em log**; segredo
reintroduzido no código; ameaça de privacidade (LINDDUN) sem controle; comportamento sob
falha ignorado.

---

## O papel do pipeline e do autograding

São **instrumentos de evidência e feedback**, **não a nota**:
- o **placar de gates** dá ao grupo um sinal objetivo do efeito da PoC (🔴→🟢);
- o **autograding** dá nota parcial automática de apoio.

A nota real vem do **dossiê de segurança e privacidade** (artefatos das fases) e da
**defesa**. Um pipeline verde sem os artefatos por trás **não** vale a nota; artefatos
sólidos com uma PoC bem-feita **valem**.

## Definition of Done — dois níveis (ver docs/05)

- **Analisada** (todas as funcionalidades do pacote): requisito + base legal + ameaça
  (STRIDE/LINDDUN) + controle proposto + linha na matriz + plano de remediação.
- **Implementada** (1–2 por grupo, prova de conceito): + correção em PR + teste + evidência
  + status "Verificado" + gate 🔴→🟢.

## Checklist de secure code review (para os PRs da PoC)

- [ ] Entrada validada e consultas parametrizadas (sem concatenação SQL).
- [ ] Autorização server-side (ownership + RBAC deny-by-default).
- [ ] Sem segredo no código; config por ambiente.
- [ ] Erro genérico ao cliente; detalhe só no log (sem PII).
- [ ] Evento de segurança auditado (append-only, sem PII).
- [ ] Dado tratado tem base legal e finalidade (LGPD).
- [ ] Teste cobre o caminho de ataque e o comportamento fail-secure.
