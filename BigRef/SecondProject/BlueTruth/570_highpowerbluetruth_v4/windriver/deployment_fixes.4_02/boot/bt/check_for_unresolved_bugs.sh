#!/bin/sh

#This script fixes some identified bugs that will be addressed in the future

#Fix a bug which locks the network interface and the outstation does not recover
SEQUENCE_TO_SEARCH_FOR='NOTICE: 7, Connection timeout'
THRESHOLD=10 #number of occurrences of the SEQUENCE_TO_SEARCH_FOR

LOG_DIR=/var/log/bt
LAST_LOG_FILE_NAME=`ls -rt $LOG_DIR | tail -n 1`
LAST_LOG_FILE_NAME_FULL=`find $LOG_DIR -name $LAST_LOG_FILE_NAME`
echo $LAST_LOG_FILE_NAME_FULL
NUMBER_OF_OCCURRENCES=`cat $LAST_LOG_FILE_NAME_FULL | grep -rohF "$SEQUENCE_TO_SEARCH_FOR" | wc -l`
echo `date`
printf "Number of lines containing '$SEQUENCE_TO_SEARCH_FOR' is $NUMBER_OF_OCCURRENCES.\n"

if [ $NUMBER_OF_OCCURRENCES -gt $THRESHOLD ]
then
        #Sending of QUIT signal should result with outstation program exitting with code 0
        #Exiting with code 0 should cause the script outstation.sh to restart the program
        printf "Possible bug in the program. Sending ABRT signal...\n"
        pkill -ABRT outstation
else
        printf "The value is below the threshold. Ignoring\n"
fi
