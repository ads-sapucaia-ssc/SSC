#!/usr/bin/env bash
# Testa a matriz de permissoes do VidaPlus: cada funcao restrita x cada papel.
# Mostra o codigo HTTP de cada combinacao (200 = deixou; 403 = negou; 401 = sem login).
#
# Uso (na raiz do repositorio, com o VidaPlus ligado e o banco ZERADO):
#   bash roteiros/G3-papeis-e-consentimento/matriz-de-acesso.sh
#   bash roteiros/G3-papeis-e-consentimento/matriz-de-acesso.sh | tee entregaveis/evidencias/fase4-matriz-antes.txt
#
# ATENCAO: o script PUBLICA o laudo 104 e CRIA atendimentos (funcoes que mudam dados).
# Depois de rodar, zere o banco: bash roteiros/comum/zerar-banco.sh

API=localhost:8080

login() {
  curl -s -X POST "$API/api/auth/login" -H "Content-Type: application/json" \
    -d "{\"email\":\"$1\",\"senha\":\"$2\"}" | sed -E 's/.*"access_token":"([^"]+)".*/\1/'
}

pede() {   # pede METODO ENDERECO TOKEN [CORPO]
  if [ -n "$4" ]; then
    curl -s -o /dev/null -w '%{http_code}' -X "$1" "$API$2" -H "Authorization: Bearer $3" \
      -H "Content-Type: application/json" -d "$4"
  else
    curl -s -o /dev/null -w '%{http_code}' -X "$1" "$API$2" -H "Authorization: Bearer $3"
  fi
}

declare -A TK
TK[PACIENTE]=$(login ana.paciente@email.com Paciente123)
TK[MEDICO]=$(login dra.house@vidaplus.local Medico123)
TK[RECEPCAO]=$(login recepcao@vidaplus.local Recep123)
TK[BIOQUIMICO]=$(login bio@vidaplus.local Bio123)
TK[ADMIN]=$(login admin@vidaplus.local Admin123)

# Funcao | metodo | endereco | corpo (se houver)
FUNCOES=(
  "Listar usuarios|GET|/api/usuarios|"
  "Ver a agenda|GET|/api/atendimentos|"
  "Agendar|POST|/api/atendimentos|{\"medicoId\":1,\"pacienteId\":3}"
  "Carteira do medico 1|GET|/api/atendimentos/medico/1|"
  "Fila de laudos|GET|/api/exames/pendentes|"
  "Editar cadastro do paciente 2|PUT|/api/pacientes/2|{\"telefone\":\"51999990000\"}"
  "Publicar laudo 104|POST|/api/exames/104/publicar|"
)
PAPEIS=(PACIENTE MEDICO RECEPCAO BIOQUIMICO ADMIN)

printf "%-32s" "FUNCAO"; for P in "${PAPEIS[@]}"; do printf " %-10s" "$P"; done; echo
for F in "${FUNCOES[@]}"; do
  IFS='|' read -r NOME METODO END CORPO <<< "$F"
  printf "%-32s" "$NOME"
  for P in "${PAPEIS[@]}"; do printf " %-10s" "$(pede "$METODO" "$END" "${TK[$P]}" "$CORPO")"; done
  echo
done
printf "%-32s %s\n" "Publicar laudo SEM login" "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/api/exames/104/publicar")"
