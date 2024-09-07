#!/bin/bash

# load environment variables
export $(grep -v '^#' .env | xargs)

./watchdog