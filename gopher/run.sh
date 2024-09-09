#!/bin/bash

echo "Building watchdog..."
go build -o watchdog

# load environment variables
echo "Loading environment variables..."
export $(grep -v '^#' .env | xargs)

echo "Running watchdog..."
./watchdog