#!/usr/bin/env bash

database="${MONGO_INITDB_DATABASE:-aguiaexpress}"
dump_directory="/seed/mongodb/${database}"

if [[ -d "${dump_directory}" ]] && find "${dump_directory}" -name '*.bson' -print -quit | grep -q .; then
    mongorestore \
        --drop \
        --host 127.0.0.1 \
        --port 27017 \
        --username "${MONGO_INITDB_ROOT_USERNAME}" \
        --password "${MONGO_INITDB_ROOT_PASSWORD}" \
        --authenticationDatabase admin \
        --db "${database}" \
        "${dump_directory}"
else
    echo "Nenhum dump MongoDB encontrado em ${dump_directory}; seguindo sem dados iniciais."
fi
