 #
 # THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 # ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 # TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 # PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 # SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 # SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 # LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 # USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 # AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 # LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 # ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 # POSSIBILITY OF SUCH DAMAGE.
 #
 # Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 #
 # Created on 14th July 2015
 #
 # Author: josetrujillo-brenes

# This method is needed to import the library in a way that this script can be used from any directory
getScriptDirectory() {
        SOURCE="${BASH_SOURCE[0]}"
        while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
                DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
                SOURCE="$(readlink "$SOURCE")"
                [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
        done
        DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
        echo $DIR
}

source $(getScriptDirectory)/NetUtils.sh
source $(getScriptDirectory)/DateUtils.sh

# Sends a http request to the Instation with 1..N devices
# $1 = InStation
# $2 = Detector
# $3 = Time of detection
# $4..$N = Devices detected
# Examples:
#	deviceDetection Detector1 $(nowInUtc) Device1 
#	deviceDetection Detector1 $(nowInUtc) Device1 Device2 Device3
deviceDetection() {	
	inStation=$1;
	detector=$2;
	timeOfDetection=$3;
	servlet=$inStation'BlueTruthReceiver1_50/DeviceDetection';
	startTime=$(date +"%Y-%m-%d %H:%M:%S" --date "$timeOfDetection" --utc);
	parameterPosition=1;
	devCount=0;
	devices=""; 
	for parameter in "$@";
	do
		if [ "$parameterPosition" -ge 4 ]; then
			devCount=$(($devCount + 1));
			devices="$devices&d$devCount=$parameter";						
	    	fi
		parameterPosition=$(($parameterPosition + 1));		
	done 
	servletWithParameters="$servlet?outstationID=$detector&startTime=$startTime&devCount=$devCount$devices";
        sendHttpRequest "$servletWithParameters"
        #avoidFaulty $inStation $detector
}

# Sends a number of device detections 
# $1 = InStation
# $2 = Detector
# $6 = Repetitions 
# $7 = Seconds between repetitions (Optional: 1 second if nothing is specify)
# Example:
# 	ContinuousDeviceDetections inStation Detector1 $(secondsOf 0 1 0)
# 	ContinuousDeviceDetections inStation Detector1 6 10
ContinuousDeviceDetections() {
	inStation=$1;
	detector=$2;
	repeat=$3;
	sleepBetweenRepetitions=1;
	if [[ $4 ]]; then
		sleepBetweenRepetitions=$4		
	fi
	for i in `seq 1 $repeat`; do
            	now=$(nowInUtc);                
                suffix=$(dateInFormat "$now" "%H%M%S");
                deviceDetection $inStation $detectorId "$now" devat$suffix; 	
		sleep $sleepBetweenRepetitions;
        done
}


# Sends a http request to the Instation with a Occupancy report
# $1 = InStation
# $2 = Detector
# $3 = Time of report
# $4 = Number of vehicles in free flow
# $5 = Number of vehicles in moderate flow
# $6 = Number of vehicles in slow flow
# $7 = Number of vehicles in very slow flow
# $8 = Number of vehicles in stationary flow
# $9 = Queue start or end in format qs or qe (Optional)
# $10 = Time of the queue start or end (Optional)
# Examples:
#	occupancyReport $inStation Detector1 "$(nowInUtc)" 1 2 3 4 5;
#       occupancyReport $inStation Detector2 "$(nowInUtc)" 1 2 3 4 5 qs "$(nowInUtc)"; 
occupancyReport() {
        inStation=$1;
	detector=$2;
	timeOfReport=$(date +"%Y-%m-%d %H:%M:%S" --date "$3" --utc);
        freeFlow=$4;
        moderateFlow=$5;
        slowFlow=$6;
        verySlowFlow=$7;
        stationaryFlow=$8;
        qsOrQe=$9;
        timeOfQsOrQe=$(date +"%Y-%m-%d %H:%M:%S" --date "${10}" --utc);

	servlet=$inStation'BlueTruthReceiver1_50/Occupancy';
        qsOrQeAndTimeOfQsOrQe="";
        if [[ $qsOrQe && $timeOfQsOrQe ]]; then
                qsOrQeAndTimeOfQsOrQe="&$qsOrQe=$timeOfQsOrQe";
        fi
        servletWithParameters="$servlet?id=$detector&t=$timeOfReport&f=$freeFlow&m=$moderateFlow&s=$slowFlow&vs=$verySlowFlow&st=$stationaryFlow$qsOrQeAndTimeOfQsOrQe";
        sendHttpRequest "$servletWithParameters"
}

# Sends a http request to the Instation now with no devices, this will avoid the InStation to reports faulty devices for some time 
# $1 = InStation
# $2..$N = Detectors
avoidFaulty() {
        inStation=$1;
        servlet=$inStation'BlueTruthReceiver1_50/DeviceDetection';
        startTime=$(date +"%Y-%m-%d %H:%M:%S" --date "$(nowInUtc)" --utc);
        parameterPosition=1;
        for parameter in "$@";
	do
		if [ "$parameterPosition" -ge 2 ]; then
			detector=$parameter;
                        servletWithParameters="$servlet?outstationID=$detector&startTime=$startTime&devCount=0";
                        sendHttpRequest "$servletWithParameters"
	    	fi
                parameterPosition=$(($parameterPosition + 1));	
	done 
}

avoidSilent() {
        inStation=$1;
        servlet=$inStation'BlueTruthReceiver1_50/DeviceDetection';
        now=$(nowInUtc);
        startTime=$(date +"%Y-%m-%d %H:%M:%S" --date "$now" --utc);
        suffix=$(dateInFormat "$now" "%H%M%S");
        parameterPosition=1;
        for parameter in "$@";
	do
		if [ "$parameterPosition" -ge 2 ]; then
			detector=$parameter;
                        servletWithParameters="$servlet?outstationID=$detector&startTime=$startTime&devCount=1&d1=noS"$parameterPosition"at"$suffix;
                        sendHttpRequest "$servletWithParameters"
	    	fi
                parameterPosition=$(($parameterPosition + 1));	
	done 
}


