#!/bin/bash

LOGS_DIR="/home/$USER/rmasdata/archivedLogs/"
N_DAYS=7

# delete archives of logs over N days old that are descendants of logs directory
find $LOGS_DIR -name logs.zip -mtime +$N_DAYS -delete

# find and delete empty dirs under logs directory
find $LOGS_DIR -type d -empty -delete

