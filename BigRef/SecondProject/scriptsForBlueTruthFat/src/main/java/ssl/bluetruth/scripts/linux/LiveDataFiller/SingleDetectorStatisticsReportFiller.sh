#
#  THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
#  LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
#  EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
#  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
#  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
#  INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
#  OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
#  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
#  POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
#  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
#  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
#  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
#  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
#
#  Copyright 2016 (C) Costain Integrated Technology Solutions Limited.
#  All Rights Reserved.

#Note: Some commands are reserved for certain detectors. In case doesn't exist randam command will be selected

#Desc:  Performs generations of statistics reports
#Params:        
#       1. Detector Name:Fills data based on this detector.
#       2. Delay Time:   Optional parameter to submit the report delayed (in minutes)

#Example:statisticsReport inStationIP DetectionID,DateInHex,StartTime,deviceMacAddress:0:DateInHex:2:5,1234

#!/bin/bash

# This method is needed to import the library in a way that this script can be used from any directory

DetectionID=$1
DetectionDelayTime=$2
MacAddressesFilePath=$3

if [[ "$1" == "" && "$2" == "" ]];then
	echo ""
	echo "Please pass detectorID DetectionDelayTime(optional)"
	echo ""
	exit
fi

if [ "$2" == "" ];then
	DetectionDelayTime=0
fi

if [ "$MacAddressesFilePath" == "" ];then
	MacAddressesFilePath=/tmp/MacAddresses.temp
fi

if [ ! -e "$MacAddressesFilePath" ];then
	source ./DB_Query_Executer.sh
	$(getDevices "$MacAddressesFilePath")
fi

dir=$(dirname "$SCRIPT")

source $dir/../lib/V4Utils.sh

inStation=$(inStation);   #gets current local server e.g. "127.0.0.1:8080/"
DateInHex=`echo $(printf '%X' $(date +"%s" --date "$DetectionDelayTime minutes ago" --utc))`

deviceMacAddress=`grep ^$DetectionID $MacAddressesFilePath | cut -d"|" -f2 | sed 's/^ //g' |  shuf -n 1`
if [ "$deviceMacAddress" = "" ]; then
	echo "Couldn't expect deviceMac Address; random is selected"
	deviceMacAddress=`tr -dc A-F0-9 < /dev/urandom | head -c 10`
fi
echo "Executing Static report for $DetectionID"

command="statisticsReport $inStation $DetectionID,$DateInHex,1E,$deviceMacAddress:0:$DateInHex:2:5,1234"

eval  $command
