# CrediYa

docker network ls

docker network create bootcamp_network

INIT_SQL_AUTH="$(pwd)/Autenticacion/deployment/db/init" \
INIT_SQL_SOLI="$(pwd)/Solicitudes/deployment/db/init" \
docker compose \
--env-file ./.env \
-p credi_ya \
-f Autenticacion/deployment/docker-compose.yml \
-f Autenticacion/deployment/docker-compose.override.yml \
-f Solicitudes/deployment/docker-compose.yml \
-f Solicitudes/deployment/docker-compose.override.yml \
up -d --build

INIT_SQL_SOLI="$(pwd)/Solicitudes/deployment/db/init" \
docker compose \
--env-file ./.env \
-p credi_ya \
-f Solicitudes/deployment/docker-compose.yml \
-f Solicitudes/deployment/docker-compose.override.yml \
up -d --build