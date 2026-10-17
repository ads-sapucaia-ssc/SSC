#!/usr/bin/env bash
# Fabrica um token do VidaPlus com o usuario e o papel que voce quiser, usando a chave que
# esta gravada no codigo (JwtUtil.java). Serve para DEMONSTRAR que o controle por papel
# e tao forte quanto o segredo da chave (F-10/F-11).
#
# Uso (na RAIZ do repositorio):
#   FORJADO=$(bash roteiros/G3-papeis-e-consentimento/fabricar-token.sh 5 ADMIN)
#   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/usuarios -H "Authorization: Bearer $FORJADO"
#
# Argumentos: 1) numero do usuario (5 = Ana)  2) papel (ADMIN, MEDICO, RECEPCAO, BIOQUIMICO, PACIENTE)
# So use contra o VidaPlus do seu computador.

cd "$(dirname "$0")/../.." || exit 1
USUARIO="${1:-5}"
PAPEL="${2:-ADMIN}"

# 1) le a chave de dentro do codigo (a linha: SECRET = "...")
CHAVE=$(grep -o 'SECRET = "[^"]*"' legacy-app/backend/src/main/java/br/ifsul/vidaplus/security/JwtUtil.java | cut -d'"' -f2)
if [ -z "$CHAVE" ]; then
  echo "Nao achei a chave no JwtUtil.java (ela foi tirada do codigo?)" >&2
  exit 1
fi

# 2) monta as tres partes do token (cabecalho.conteudo.assinatura) e assina com a chave
CHAVE="$CHAVE" USUARIO="$USUARIO" PAPEL="$PAPEL" python3 - <<'PY'
import base64, hmac, hashlib, json, os
b = lambda d: base64.urlsafe_b64encode(d).rstrip(b'=').decode()
cabecalho = b(json.dumps({"alg": "HS256"}, separators=(',', ':')).encode())
conteudo  = b(json.dumps({"sub": os.environ["USUARIO"], "papel": os.environ["PAPEL"]}, separators=(',', ':')).encode())
assinatura = b(hmac.new(os.environ["CHAVE"].encode(), f"{cabecalho}.{conteudo}".encode(), hashlib.sha256).digest())
print(f"{cabecalho}.{conteudo}.{assinatura}")
PY
