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

#Desc:  Performs generations of status reports
#Params:
#       1. Detector Name:Fills data based on this detector.
#       2. Delay Time:   Optional parameter to submit the report delayed (in minutes)

#Example:statusReport inStation DetectionID,DateInHex,sl_2g_min=42,sl_2g_avg=113,sl_2g_max=157,sl_3g_avg=54,sl_3g_min=113,sl_3g_max=114,pi=162,ssh=closed,3245



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

source $dir/../lib/V4Utils.sh
source DB_Query_Executer.sh

inStation=$(inStation);   #gets current local server e.g. "127.0.0.1:8080/"
DateInHex=`echo $(printf '%X' $(date +"%s" --date "$DetectionDelayTime minutes ago" --utc))`

echo "Executing Status report for DetectionID"

VoltageMultiplier=10
RandomVoltageValue=$(bc <<< "scale=2;${RANDOM} * $VoltageMultiplier/32767")

sl_2g_avg=`shuf -i1-255 -n1`
sl_2g_min=`shuf -i0-$sl_2g_avg -n1`
sl_2g_max=`shuf -i$sl_2g_avg-255 -n1`
sl_3g_avg=`shuf -i1-255 -n1`
sl_3g_min=`shuf -i0-$sl_2g_avg -n1`
sl_3g_max=`shuf -i$sl_2g_avg-255 -n1`
pi=`shuf -i1-255 -n1`
mv=$RandomVoltageValue

command="statusReport $inStation $DetectionID,$DateInHex,sl_2g_min=$sl_2g_min,sl_2g_avg=$sl_2g_avg,sl_2g_max=$sl_2g_max,sl_3g_avg=$sl_3g_avg,sl_3g_min=$sl_3g_min,sl_3g_max=$sl_3g_max,pi=$pi,mv=$mv,ssh=closed,3245"
eval  $command
