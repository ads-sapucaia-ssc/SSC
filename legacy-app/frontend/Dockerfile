# Frontend VidaPlus (legado - React 17 / react-scripts)
FROM node:16 AS build
WORKDIR /app
COPY package.json ./
RUN npm install || true
COPY . .
RUN npm run build || echo "build placeholder - completar componentes/rotas"

FROM nginx:1.21
# V-12/V-13: nginx sem headers de seguranca / sem TLS (discutir no gate de DAST)
COPY --from=build /app/build/ /usr/share/nginx/html/ 2>/dev/null || true
EXPOSE 4200
