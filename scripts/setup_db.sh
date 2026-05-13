#!/bin/bash

DB_USER=root
DB_NAME=paytrack_db
SCHEMA_PATH=backend/src/main/resources/schema.sql
DATA_PATH=backend/src/main/resources/data.sql

echo "====================================="
echo "Setting up PayTrack database..."
echo "====================================="

# Ask password once
read -s -p "Enter MySQL password: " DB_PASS
echo ""

echo "Running schema.sql..."
mysql -u $DB_USER -p$DB_PASS < $SCHEMA_PATH

echo "Running data.sql..."
mysql -u $DB_USER -p$DB_PASS $DB_NAME < $DATA_PATH

echo "====================================="
echo "Database setup complete ✅"
echo "====================================="