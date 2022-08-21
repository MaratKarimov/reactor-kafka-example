#!/bin/bash

set -o errexit

readonly REQUIRED_ENV_VARS=(
  "POSTGRES_HOST"
  "POSTGRES_PORT"
  "POSTGRES_DB"
  "POSTGRES_USER"
  "POSTGRES_PASSWORD")

check_env_vars_set() {
  for required_env_var in ${REQUIRED_ENV_VARS[@]}; do
    if [[ -z "${!required_env_var}" ]]; then
      echo "Error:
    Environment variable '$required_env_var' not set.
    Make sure you have the following environment variables set:
      ${REQUIRED_ENV_VARS[@]}
Aborting."
      exit 1
    fi
  done
}

wait_for_postgres() {
  until nc -z -v -w30 ${POSTGRES_HOST} ${POSTGRES_PORT}; do
    echo 'Waiting for PostgreSQL...'
     sleep 5
  done
  echo "PostgreSQL is up and running"
}

check_env_vars_set
wait_for_postgres

echo "=> Run emdr Liquibase migration scripts"
liquibase --classpath=reactor-kafka-example-liquibase-lib.jar \
  --url=jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB} \
  --changeLogFile=db/changelog/db.changelog-master.xml \
  --username=${POSTGRES_USER} \
  --password=${POSTGRES_PASSWORD} \
  update