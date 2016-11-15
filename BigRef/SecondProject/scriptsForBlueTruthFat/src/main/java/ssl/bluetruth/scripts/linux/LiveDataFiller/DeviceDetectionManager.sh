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

# This script acts as a manager to launch each sub-scripts dedicated 
# to each receiver controller. 

scriptName=$0
outputFileNameForStatisticsReport=/tmp/allDetectors.temp
outputFileNameForDeviceMacAddress=/tmp/MacAddresses.temp
waveID=0
logFileName=$scriptName.log
> $logFileName
reportingType=$1

source ./DB_Query_Executer.sh

if [[ "$reportingType" != "Full"  && "$reportingType" != "Partial" ]];then
	echo ""
	echo "---------------------------------------------------------"
	echo "Please pass your desrired reporting type"
	echo "Full:	Means that all detectors will report instantiously"
	echo "Partial:Means that only once, 33% of the detectors will report instantiously, 33% will report between 5 and 10 minutes and 33% will not report"
	echo "---------------------------------------------------------"
	exit
fi

if [ -e $outputFileNameForStatisticsReport ]; then
	rm $outputFileNameForStatisticsReport
fi

while true
do

	if [ "`jobs | grep -v $scriptName | grep -v Done`" == "" ]; then
		waveID=$(( $waveID + 1 ))	
		$(getDetectors "$outputFileNameForStatisticsReport")
        	$(getDevices "$outputFileNameForDeviceMacAddress")
		detectorsCount=`wc -l $outputFileNameForStatisticsReport | cut -d" " -f1`
		echo "Wave $waveID: Filling $detectorsCount detectors randomly with statistics, status,fault and occupancy reports"
		step=0
		while read detectorID
			do           
				step=$(($step+1))
				if [ "$detectorID" != "" ];then
					if [ "$reportingType" == "Full" ];then
    					./SingleDetectorStatisticsReportFiller.sh $detectorID >> $logFileName &
					else if [[ "$reportingType" == "Partial"  ]];then
        					case $(($step % 3)) in
 					               	0) ./SingleDetectorStatisticsReportFiller.sh "$detectorID"  >> $logFileName & ;;
                					1) ./SingleDetectorStatisticsReportFiller.sh "$detectorID" 70 >> $logFileName & ;;
                					#2) echo "DeviceID:$detectorID is Grey, Step=$step";
        					esac
						fi
					fi
					./SingleDetectorStatusReportFiller.sh $detectorID >> $logFileName &
					./SingleDetectorOccupancyReportFiller.sh $detectorID  >> $logFileName &
					./SingleDetectorFaultReportFiller.sh $detectorID  >> $logFileName &
					./SingleDetectorMessageReportFiller.sh $detectorID  >> $logFileName &
				fi
		done <$outputFileNameForStatisticsReport
		#echo "Loop Finished"
	else
		#echo "found jobs output="`jobs`
		noJobs=`jobs -p | wc -l`
		echo "Waiting for $noJobs jobs to finish"
		if [ $noJobs -gt 1 ]; then
			for job in `jobs -p`
				do
					#echo "Waiting for job: "$job
    					wait $job 
			done
		else
			echo "Nothing to wait for"
		fi
	fi
done

echo "Script ends"
