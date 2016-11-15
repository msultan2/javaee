#!/bin/bash

# Copyright (c) 2013 Radoslaw Golebiewski, Simulation Systems Ltd
# Project: BlueTruth

CURRENT_DIRECTORY=`dirname "$0"`

#createLink (sourceDirectory, destinationLink)
#Function that creates a software link
function createLink
{
	SOURCE_DIRECTORY=$1
	DESTINATION_LINK=$2
	if [ ! -h $DESTINATION_LINK ]
	then
		ln -s $SOURCE_DIRECTORY $DESTINATION_LINK
		if [ -h $DESTINATION_LINK ]
		then
			echo "Link $DESTINATION_LINK created"
		else
			echo "Link $DESTINATION_LINK could not be created. Error!"
		fi
	else
		echo "Link $DESTINATION_LINK exists";
	fi
}



INSTALLATION_DIRECTORY=$CURRENT_DIRECTORY/src

echo Creating links in directory $INSTALLATION_DIRECTORY

#Create all links necessary for this program
#createLink ../.. $INSTALLATION_DIRECTORY/brdf
