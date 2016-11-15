
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

#Desc:  Performs generations of status reports
#Params:
#       1. Detector Name:Fills data based on this detector.
#       2. Delay Time:   Optional parameter to submit the report delayed (in minutes)

#Example: wget -q localhost:8080/BlueTruthReceiver1_50/Message?id=ssl207402&dt=2016-10-10 18:52:03&m=3&s=code:1:x:y:z --delete-after



#!/bin/bash

# This method is needed to import the library in a way that this script can be used from any directory

if [[ "$1" == "" && "$2" == "" ]];then
	echo ""
	echo "Please pass detectorID DetectionDelayTime(optional)"
	echo ""
	exit
fi

if [ "$2" == "" ];then
	DetectionDelayTime=0
fi

DetectionID=$1
DetectionDelayTime=$2

dir=$(dirname "$SCRIPT")

source $dir/../lib/V3Utils.sh
source DB_Query_Executer.sh

inStation=$(inStation);   #gets current local server e.g. "127.0.0.1:8080/"
dateTime=$(date +"%Y-%m-%d %H:%M:%S" --date "$DetectionDelayTime minutes ago" --utc);

echo "Executing Status report for $DetectionID"
code="code"
count=1
m=3
x="x"
y="y"
z="z"

request="id=$DetectionID&dt=$dateTime&m=$m&s=$code:$count:$x:$y:$z"

servlet=$inStation'BlueTruthReceiver1_50/Message';
sendHttpRequest "$servlet?$request"
