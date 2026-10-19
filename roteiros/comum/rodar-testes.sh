#!/usr/bin/env bash
# Roda os testes de seguranca do VidaPlus no seu computador, sem instalar Java nem Maven:
# o Maven roda dentro de um container Docker, ligado ao banco do VidaPlus que ja esta no ar.
#
# Uso (no terminal Git Bash/Linux/Mac, na pasta raiz do repositorio do grupo):
#   bash roteiros/comum/rodar-testes.sh                      # todos os testes @security
#   bash roteiros/comum/rodar-testes.sh "security & F-02"    # so os testes da F-02
#
# Antes: o VidaPlus precisa estar rodando: bash roteiros/comum/zerar-banco.sh
# A primeira execucao demora (baixa as bibliotecas); as seguintes sao rapidas.

FILTRO="${1:-security}"
cd "$(dirname "$0")/../../legacy-app/backend" || { echo "Nao achei legacy-app/backend. Rode da raiz do repositorio."; exit 1; }

PASTA="$(pwd)"
if pwd -W >/dev/null 2>&1; then      # Git Bash no Windows: o Docker precisa do caminho no formato C:/...
  PASTA="$(pwd -W)"
  export MSYS_NO_PATHCONV=1
fi

if ! docker network inspect legacy-app_default >/dev/null 2>&1; then
  echo "O VidaPlus nao esta rodando. Antes, rode: bash roteiros/comum/zerar-banco.sh"
  exit 1
fi

# Depois da correcao da F-10 (segredos fora do codigo), a senha de root do banco e a chave do
# token ficam no arquivo legacy-app/.env (fora do Git). Os testes usam as mesmas.
SENHA_DB=""
if grep -q 'MYSQL_ROOT_PASSWORD: *\${' ../docker-compose.yml 2>/dev/null; then
  if [ ! -f ../.env ]; then
    echo "Falta o arquivo legacy-app/.env. Veja roteiros/comum/08-segredos-fora-do-codigo.md (Passo 4)."
    exit 1
  fi
  SENHA_DB=$(grep -E '^MYSQL_ROOT_PASSWORD=' ../.env | cut -d= -f2-)
fi
CHAVE_JWT=$(grep -E '^JWT_SECRET=' ../.env 2>/dev/null | cut -d= -f2-)
[ -n "$CHAVE_JWT" ] || CHAVE_JWT=$(openssl rand -hex 32 2>/dev/null || echo "chave-so-para-testes-locais-0123456789abcdef")

echo "Rodando os testes com o filtro: $FILTRO"
docker run --rm --network legacy-app_default \
  -v vidaplus-m2:/root/.m2 -v "$PASTA":/app -w /app \
  -e JWT_SECRET="$CHAVE_JWT" \
  ${SENHA_DB:+-e SPRING_DATASOURCE_PASSWORD="$SENHA_DB"} \
  maven:3.9-eclipse-temurin-21 \
  mvn -B -q test "-Dgroups=$FILTRO" \
  "-Dspring.datasource.url=jdbc:mysql://db:3306/vidaplus?useSSL=false&allowPublicKeyRetrieval=true"

if [ $? -eq 0 ]; then
  echo "RESULTADO: todos os testes do filtro \"$FILTRO\" PASSARAM (verde)."
else
  echo "RESULTADO: algum teste FALHOU (vermelho). Procure acima as linhas com 'expected' e 'but was'."
fi
