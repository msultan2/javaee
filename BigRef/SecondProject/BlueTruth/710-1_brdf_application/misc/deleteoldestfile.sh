#!/bin/sh

#This file checks if directory (first parameter) contents size is 
#greater than the threshold and if so deletes the oldest files from 
#the directory.

#Directory to examine
DIRECTORY=$1
#Size over which file deletion starts/continues
SIZE_THRESHOLD=$2

if [ -d $DIRECTORY ]; then
	LOOP=1
	while [ $LOOP -eq 1 ]
	do
		SIZE_USED=$(du -c $DIRECTORY | tail -n1 | awk '{print $1}')
		if [ "$SIZE_USED" -ge $SIZE_THRESHOLD ]
		then
			OLDEST_FILE=$(ls -t $DIRECTORY | tail -n1 )
			echo $SIZE_USED KB used, exceeds threshold of $SIZE_THRESHOLD KB, deleting $OLDEST_FILE
			logger -t DeleteOldestFile "$SIZE_USED KB used, exceeds threshold of $SIZE_THRESHOLD KB, deleting $OLDEST_FILE"
			rm $DIRECTORY/$OLDEST_FILE
		else
			echo $SIZE_USED KB used, less than threshold of $SIZE_THRESHOLD KB, doing nothing
		        LOOP=0
		        exit 0
		fi
	done
else
	# do nuffink! 
	logger -t DeleteOldestFile "$DIRECTORY not found"
fi

