#!/bin/bash

function get_config {
    grep "$1" conf/server.conf | cut -d "=" -f2 | awk '{print $1}'
}

USER="$(get_config "database.user")"
PASSWORD="$(get_config "database.password")"
DATABASE="$(get_config "database.schema")"
PORT="$(get_config "database.port")"
HOST="$(get_config "database.hostname")"

echo "Connecting to '$DATABASE' at $HOST:$PORT with user '$USER'"
mysql -u ${USER} -h ${HOST} -P${PORT} -p${PASSWORD} ${DATABASE}

