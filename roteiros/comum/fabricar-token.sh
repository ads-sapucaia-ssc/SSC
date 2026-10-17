#!/usr/bin/env bash
# Fabrica um token do VidaPlus com o usuario e o papel que voce quiser, usando a chave que
# esta gravada no codigo (JwtUtil.java). Serve para DEMONSTRAR que o controle de acesso
# e tao forte quanto o segredo da chave (F-10/F-11).
#
# Se a chave ja saiu do codigo (F-10 corrigida), o script faz o que um atacante faria:
# procura a chave ANTIGA no historico do Git. Se o valor foi TROCADO, o token fabricado
# com a chave antiga e recusado pelo servidor.
#
# Uso (na RAIZ do repositorio):
#   FORJADO=$(bash roteiros/comum/fabricar-token.sh 5 ADMIN)
#   curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/api/usuarios -H "Authorization: Bearer $FORJADO"
#
# Argumentos: 1) numero do usuario (5 = Ana)  2) papel (ADMIN, MEDICO, RECEPCAO, BIOQUIMICO, PACIENTE)
# So use contra o VidaPlus do seu computador.

cd "$(dirname "$0")/../.." || exit 1
USUARIO="${1:-5}"
PAPEL="${2:-ADMIN}"

ARQ=legacy-app/backend/src/main/java/br/ifsul/vidaplus/security/JwtUtil.java

# 1) le a chave de dentro do codigo (a linha: SECRET = "...")
CHAVE=$(grep -o 'SECRET = "[^"]*"' "$ARQ" | cut -d'"' -f2)
if [ -n "$CHAVE" ]; then
  echo "Chave lida do codigo atual ($ARQ)." >&2
else
  # 2) nao esta mais no codigo: procura a versao mais recente da chave no HISTORICO do Git
  CHAVE=$(git log -p --all -- "$ARQ" | grep -o 'SECRET = "[^"]*"' | head -1 | cut -d'"' -f2)
  if [ -z "$CHAVE" ]; then
    echo "Nao achei a chave nem no codigo nem no historico do Git." >&2
    exit 1
  fi
  echo "A chave nao esta mais no codigo, mas o historico do Git guarda a ANTIGA. Usando a antiga." >&2
fi

# 3) monta as tres partes do token (cabecalho.conteudo.assinatura) e assina com a chave
CHAVE="$CHAVE" USUARIO="$USUARIO" PAPEL="$PAPEL" python3 - <<'PY'
import base64, hmac, hashlib, json, os
b = lambda d: base64.urlsafe_b64encode(d).rstrip(b'=').decode()
cabecalho = b(json.dumps({"alg": "HS256"}, separators=(',', ':')).encode())
conteudo  = b(json.dumps({"sub": os.environ["USUARIO"], "papel": os.environ["PAPEL"]}, separators=(',', ':')).encode())
assinatura = b(hmac.new(os.environ["CHAVE"].encode(), f"{cabecalho}.{conteudo}".encode(), hashlib.sha256).digest())
print(f"{cabecalho}.{conteudo}.{assinatura}")
PY
