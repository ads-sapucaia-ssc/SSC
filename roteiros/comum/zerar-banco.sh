#!/usr/bin/env bash
# Religa o VidaPlus do zero: recompila o que mudou, apaga o banco e recria com os dados
# originais (schema.sql + seed.sql), e espera a API ficar pronta.
#
# Uso (no terminal, na RAIZ do repositorio):
#   bash roteiros/comum/zerar-banco.sh
#
# Use sempre que um roteiro disser "zere o banco", e depois de mudar o codigo.

cd "$(dirname "$0")/../.." || exit 1
COMPOSE="docker compose -f legacy-app/docker-compose.yml"

# Depois da correcao da F-10, as senhas e a chave do token vem do arquivo legacy-app/.env.
if grep -q '\${JWT_SECRET' legacy-app/docker-compose.yml && [ ! -f legacy-app/.env ]; then
  echo "Falta o arquivo legacy-app/.env, com as senhas do banco e a chave do token."
  echo "Ele nao vai pelo Git: cada integrante cria o seu. Veja roteiros/comum/08-segredos-fora-do-codigo.md (Passo 4)."
  exit 1
fi

echo "1/3 Desligando o VidaPlus e apagando o banco..."
$COMPOSE down -v >/dev/null 2>&1

echo "2/3 Ligando de novo (a primeira vez, ou depois de mudar o codigo, demora alguns minutos)..."
if ! $COMPOSE up --build -d; then
  echo "ERRO ao ligar. Leia as mensagens acima (procure 'ERROR' e o nome de um arquivo .java)."
  exit 1
fi

echo "3/3 Esperando a API responder..."
for i in $(seq 1 60); do
  if curl -s -o /dev/null "http://localhost:8080/api/pacientes/busca?q=x"; then
    echo "PRONTO: VidaPlus no ar, com o banco original. Tela: http://localhost:4200"
    exit 0
  fi
  sleep 2
done
echo "A API nao respondeu em 2 minutos. Veja o log: docker compose -f legacy-app/docker-compose.yml logs backend --tail 40"
exit 1
