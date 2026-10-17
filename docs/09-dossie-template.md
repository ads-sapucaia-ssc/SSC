# 09 · Dossiê de Segurança e Privacidade

O **dossiê** é o **entregável avaliado** do grupo (junto com a defesa). Ele consolida, num
único documento, o "fio completo" de cada ativo — **requisito → ameaça (STRIDE/LINDDUN) →
controle → teste → evidência** — além do RIPD, da matriz de rastreabilidade e do plano de
remediação.

> **O arquivo já vem no repositório:** [`entregaveis/DOSSIE.md`](../entregaveis/DOSSIE.md).
> **Não é preciso criá-lo** — abra e **preencha ao longo das fases**. As linhas _em itálico_ do
> modelo são exemplos: apague-as e ponha o conteúdo do grupo.

---

## Dois níveis de "pronto"

- **Analisada** — para **todas** as funcionalidades do pacote: requisito (RFS+RGS) + base legal
  + ameaça (STRIDE/LINDDUN) + controle proposto + linha na matriz + plano de remediação.
- **Implementada** (prova de conceito) — **1–2** por grupo: além da análise, correção em PR +
  teste + evidência + status "Verificado" + gate 🔴→🟢.

## Como o dossiê se conecta às fases

Cada seção do `DOSSIE.md` mapeia **uma fase** do
[plano](01-plano-disciplina-ssdlc.md) e um critério da [rubrica](07-rubrica-avaliacao.md) — o
cabeçalho de cada seção do arquivo indica a fase correspondente. Preencha
**incrementalmente**: a cada fase, a sua seção.

> A **estrutura completa** (todas as seções, tabelas e exemplos de qualidade) está no próprio
> [`entregaveis/DOSSIE.md`](../entregaveis/DOSSIE.md) — este documento é só a **visão geral**.

## Exemplo completo

Para ver **um dossiê inteiro preenchido** — com o nível de detalhe esperado em cada fase, os
`.feature`, os PRs e a evidência do CI —, leia
[12-exemplo-dossie-preenchido.md](12-exemplo-dossie-preenchido.md). Ele trata do **pacote 5**,
que não foi atribuído a nenhum grupo nesta turma: serve de **modelo de qualidade**, não de
resposta para o seu pacote.
