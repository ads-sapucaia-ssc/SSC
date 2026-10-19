# 09 · Dossiê de Segurança e Privacidade

O **dossiê** é o **entregável avaliado** do grupo (junto com a defesa). Ele consolida o "fio
completo" de cada funcionalidade do pacote — **requisito → ameaça (STRIDE/LINDDUN) → controle →
teste → evidência** — além do RIPD, da matriz de rastreabilidade e do plano de remediação.

> **O arquivo já vem no repositório:** [`entregaveis/DOSSIE.md`](../entregaveis/DOSSIE.md). Cada
> seção mapeia **uma fase** do [plano](01-plano-disciplina-ssdlc.md) e um critério da
> [rubrica](07-rubrica-avaliacao.md); preencha a seção da fase **enquanto a fase acontece**. As
> linhas _em itálico_ do modelo são exemplos: apague-as e ponha o conteúdo do grupo.

## Dois níveis de "pronto"

- **Analisada** — **todas** as funcionalidades do pacote: requisito nas duas formas (*shall* no
  dossiê + cenário Gherkin `@analise`) + base legal + ameaça (STRIDE/LINDDUN) + controle proposto +
  linha na matriz + plano de remediação.
- **Implementada** — o **P0** do grupo (aula guiada da Fase 4) e o **F-10** (segredos, aula guiada
  da Fase 6): além da análise, correção em PR + cenário `@security` (ou a verificação do F-10) que
  passa de vermelho para verde + evidência + status "Verificado" na matriz.

## Como fazer e como deve ficar

- **Como fazer:** os [`roteiros/`](../roteiros/README.md) — um por fase, para as funcionalidades do
  **seu** pacote.
- **Como deve ficar:** o [exemplo completo](12-exemplo-dossie-preenchido.md), do **pacote 5** (não
  atribuído a nenhum grupo nesta turma). É **modelo de qualidade**, não resposta para o seu
  pacote.
