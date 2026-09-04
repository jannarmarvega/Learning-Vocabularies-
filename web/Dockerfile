# syntax=docker/dockerfile:1

# ---------- build ----------
FROM node:22-bookworm-slim AS build
WORKDIR /app

# better-sqlite3 ships prebuilt binaries, but keep a toolchain around so it can
# fall back to compiling from source rather than failing the build.
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 make g++ ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# node:22's bundled npm 10.9 hits an arborist crash ("edgesOut") on this tree.
RUN npm install -g npm@11

COPY package.json ./
RUN npm install --no-audit --no-fund

COPY . .
RUN npm run build

# ---------- runtime ----------
FROM node:22-bookworm-slim AS runtime
WORKDIR /app
ENV NODE_ENV=production \
    HOST=0.0.0.0 \
    PORT=3000 \
    NUXT_DATA_DIR=/app/data \
    NUXT_SEED_DB=/app/seed/dictionary.db

COPY --from=build /app/.output ./.output
# The word list ships read-only; it is copied into /app/data on first boot.
COPY --from=build /app/server/db/dictionary.seed.db /app/seed/dictionary.db

RUN mkdir -p /app/data && chown -R node:node /app/data
USER node
VOLUME ["/app/data"]
EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD node -e "fetch('http://127.0.0.1:3000/api/parts-of-speech').then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"

CMD ["node", ".output/server/index.mjs"]
