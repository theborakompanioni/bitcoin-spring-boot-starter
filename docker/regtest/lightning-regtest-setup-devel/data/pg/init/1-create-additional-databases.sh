#!/bin/bash

set -e
set -u

function create_user_and_database() {
	local database=$1
	echo "  Creating user and database '$database'..."
	psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
	    CREATE ROLE $database WITH LOGIN ENCRYPTED PASSWORD '$database' VALID UNTIL 'infinity';
	    CREATE DATABASE $database;
	    ALTER DATABASE $database OWNER TO $database;
EOSQL
}

if [ -n "$POSTGRES_ADDITIONAL_DATABASES" ]; then
	echo "Additional database creation requested: $POSTGRES_ADDITIONAL_DATABASES"
	for db in $(echo $POSTGRES_ADDITIONAL_DATABASES | tr ',' ' '); do
		create_user_and_database $db
	done
	echo "Additional databases created."
fi
