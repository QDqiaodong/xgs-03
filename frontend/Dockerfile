ARG DOCKER_REGISTRY=docker.m.daocloud.io/
FROM ${DOCKER_REGISTRY}library/node:20-alpine AS builder

WORKDIR /app

RUN npm config set registry https://registry.npmmirror.com

COPY package.json package-lock.json ./
RUN npm install --no-audit --no-fund

COPY . .
RUN npm run build

ARG DOCKER_REGISTRY=docker.m.daocloud.io/
FROM ${DOCKER_REGISTRY}library/nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
