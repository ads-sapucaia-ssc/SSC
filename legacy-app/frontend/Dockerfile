# Frontend VidaPlus (React 19 + Vite)
FROM node:20 AS build
WORKDIR /app
COPY package.json ./
RUN npm install || true
COPY . .
# garante que dist/ exista mesmo se o build ainda estiver incompleto (template)
RUN npm run build || mkdir -p dist

FROM nginx:1.27
# V-12/V-13: nginx sem headers de seguranca / sem TLS (discutir no gate de DAST)
# nginx.conf so adiciona o fallback de SPA (React Router) - nao adiciona headers.
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/ /usr/share/nginx/html/
EXPOSE 80
