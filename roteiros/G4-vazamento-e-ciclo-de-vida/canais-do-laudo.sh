#!/usr/bin/env bash
# Testa os CANAIS por onde o laudo pode vazar no VidaPlus (o recorte do P0 do G4).
# Para cada pedido, mostra o codigo HTTP e O QUE VAZOU na resposta:
#   Glicemia  = o tipo do exame do Bruno apareceu
#   exception/trace = a resposta trouxe detalhes internos do servidor
#
# Uso (na RAIZ do repositorio, com o VidaPlus ligado e o banco ZERADO):
#   bash roteiros/G4-vazamento-e-ciclo-de-vida/canais-do-laudo.sh
#   bash roteiros/G4-vazamento-e-ciclo-de-vida/canais-do-laudo.sh | tee entregaveis/evidencias/fase4-canais-antes.txt

cd "$(dirname "$0")/../.." || exit 1
source roteiros/comum/tokens.sh >/dev/null

linha() {  # linha PESSOA TOKEN ENDERECO
  local corpo codigo vazou
  corpo=$(curl -s -w '\n%{http_code}' "localhost:8080$3" ${2:+-H "Authorization: Bearer $2"})
  codigo=$(echo "$corpo" | tail -1)
  vazou=$(echo "$corpo" | grep -o -E 'Glicemia|exception|trace' | sort -u | tr '\n' ' ')
  printf "%-9s %-24s -> %s   vazou: %s\n" "$1" "$3" "$codigo" "${vazou:-nada}"
}

echo "== API (canal 1) e mensagens de erro (canal 3)"
linha ANA       "$TOKEN_ANA"   /api/exames/101
linha ANA       "$TOKEN_ANA"   /api/exames/102
linha ANA       "$TOKEN_ANA"   /api/exames/999
linha ANA       "$TOKEN_ANA"   /api/exames/102/pdf
linha ANA       "$TOKEN_ANA"   /api/exames/999/pdf
linha ANA       "$TOKEN_ANA"   /api/exames/paciente/2
linha HOUSE     "$TOKEN_HOUSE" /api/exames/101
linha HOUSE     "$TOKEN_HOUSE" /api/exames/102
linha BIO       "$TOKEN_BIO"   /api/exames/104
linha BIO       "$TOKEN_BIO"   /api/exames/101
linha SEM-LOGIN ""             /api/exames/101

echo "== Arquivo direto no servidor do site (canal 2), sem login"
for ID in 102 104 105; do
  echo "laudo-$ID.pdf -> $(curl -s -o /dev/null -w '%{http_code} %{content_type}' localhost:4200/laudos/laudo-$ID.pdf)"
done
