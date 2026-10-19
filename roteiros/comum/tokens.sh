# Faz login com os seis usuarios de teste e guarda o token de cada um numa variavel.
#
# Uso (no terminal, na RAIZ do repositorio, com o VidaPlus ligado):
#   source roteiros/comum/tokens.sh
#
# Use "source" (e nao "bash"): assim as variaveis ficam neste terminal.
# Variaveis criadas: TOKEN_ANA, TOKEN_BRUNO, TOKEN_HOUSE, TOKEN_REC, TOKEN_BIO, TOKEN_ADMIN.
# Elas valem so neste terminal; se abrir outro, rode de novo.

_vp_login() {
  curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
    -d "{\"email\":\"$1\",\"senha\":\"$2\"}" | sed -n -E 's/.*"access_token":"([^"]+)".*/\1/p'
}

TOKEN_ANA=$(_vp_login ana.paciente@email.com Paciente123)
TOKEN_BRUNO=$(_vp_login bruno.paciente@email.com Paciente123)
TOKEN_HOUSE=$(_vp_login dra.house@vidaplus.local Medico123)
TOKEN_REC=$(_vp_login recepcao@vidaplus.local Recep123)
TOKEN_BIO=$(_vp_login bio@vidaplus.local Bio123)
TOKEN_ADMIN=$(_vp_login admin@vidaplus.local Admin123)

for _vp_nome in ANA BRUNO HOUSE REC BIO ADMIN; do
  _vp_valor=$(eval echo "\$TOKEN_$_vp_nome")
  if [ -n "$_vp_valor" ]; then echo "TOKEN_$_vp_nome ok"; else echo "TOKEN_$_vp_nome FALHOU (o VidaPlus esta ligado? a conta esta ativa?)"; fi
done
unset _vp_nome _vp_valor
