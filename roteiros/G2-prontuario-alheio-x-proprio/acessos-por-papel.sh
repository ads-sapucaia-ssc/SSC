#!/usr/bin/env bash
# Testa QUEM consegue ver QUAIS exames no VidaPlus (a regra de dono do P0 do G2).
# Para cada combinacao pessoa x endereco, mostra o codigo HTTP:
#   200 = entregou o exame | 403 = negou | 401 = sem login.
#
# Uso (na RAIZ do repositorio, com o VidaPlus ligado e o banco ZERADO):
#   bash roteiros/G2-prontuario-alheio-x-proprio/acessos-por-papel.sh
#   bash roteiros/G2-prontuario-alheio-x-proprio/acessos-por-papel.sh | tee entregaveis/evidencias/fase4-acessos-antes.txt

cd "$(dirname "$0")/../.." || exit 1
source roteiros/comum/tokens.sh >/dev/null

linha() {  # linha PESSOA TOKEN ENDERECO O-QUE-DEVERIA
  printf "%-6s %-26s -> %s   (deveria: %s)\n" "$1" "$3" \
    "$(curl -s -o /dev/null -w '%{http_code}' "localhost:8080$3" -H "Authorization: Bearer $2")" "$4"
}

echo "PESSOA ENDERECO                      CODIGO"
linha ANA   "$TOKEN_ANA"   /api/exames/101            "200: e dela"
linha ANA   "$TOKEN_ANA"   /api/exames/102            "403: e do Bruno"
linha ANA   "$TOKEN_ANA"   /api/exames/102/pdf        "403: PDF do Bruno"
linha ANA   "$TOKEN_ANA"   /api/exames/paciente/2     "403: lista do Bruno"
linha HOUSE "$TOKEN_HOUSE" /api/exames/101            "200: ela atende a Ana"
linha HOUSE "$TOKEN_HOUSE" /api/exames/102            "403: ela nao atende o Bruno"
linha BIO   "$TOKEN_BIO"   /api/exames/104            "200: ainda nao publicado"
linha BIO   "$TOKEN_BIO"   /api/exames/101            "403: ja publicado"
linha REC   "$TOKEN_REC"   /api/exames/101            "403: recepcao nao ve conteudo clinico"
