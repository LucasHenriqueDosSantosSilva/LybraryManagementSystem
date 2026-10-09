FROM node@sha256:775ba24d35a13e74dedce1d2af4ad510337b68d8e22be89e0ce2ccc299329083 AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    --mount=type=secret,id=ca_certs \
    set -eu; if [ -f /run/secrets/ca_certs ]; then export NODE_EXTRA_CA_CERTS=/run/secrets/ca_certs; fi; \
    npm ci --no-audit --no-fund
COPY . .
RUN npm run build
FROM nginx@sha256:30f1c0d78e0ad60901648be663a710bdadf19e4c10ac6782c235200619158284
COPY --chown=101:101 --chmod=444 nginx.conf /etc/nginx/nginx.conf
COPY --from=build --chown=101:101 /app/dist /usr/share/nginx/html
USER 101:101
EXPOSE 8080
ENTRYPOINT ["nginx"]
CMD ["-g", "daemon off;"]
