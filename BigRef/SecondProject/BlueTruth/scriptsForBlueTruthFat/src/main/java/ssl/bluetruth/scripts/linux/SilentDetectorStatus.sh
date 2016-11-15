#
# SilentDetectorStatus.sh
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
# Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
#BT-185
#Objectives:
#To confirm following behaviour:
#1. A detector is at status 'reporting' for at least 40 mins after the last detection.
#2. A detector 'silent' status is activated 45 mins after the last detection.
#3. A detector does not display a 'degraded' status and is updated from 'reporting' to 'silent' status.
#4. A route remains in a 'reporting' status for at least 40 mins after the last journey time is logged.
#5. A route 'silent' status is activated 45 mins after the last logged journey.
#
#Preconditions:
#1. BlueTruthReceiver2 web application is running.
#2. BlueTruthAdministration web application is running.
#3. SQL scripts for this test have been run.

#Method:
#1. Log into the BlueTruth administration module and navigate to the map. Check detectors 'silentDetectionsDetector1', 'silentDetectionsDetector2' are present and are set to status 'silent' (grey).
#2. Navigate to the Diagnostic -> Diagnostic Routes screen and check routes 'silentDetectionsRoute1' and 'silentDetectionsRoute2' are set to status 'silent'.
#3. Navigate to the Diagnostic -> Spans screen and check spans 'silentDetectionsSpan1' and 'silentDetectionsSpan2' are set to status 'silent'.
#4. Navigate to the Diagnostic -> Detectors screen and check detectors 'silentDetectionsDetector1', 'silentDetectionsDetector2' are set to status 'silent'.
#5. Run the script ‘SilentDetectorStatus.sh’ on the BlueTruth test server.
#6. Check the status of the detectors 'silentDetectionsDetector1', 'silentDetectionsDetector2' has been updated to 'reporting' in the BlueTruth map - both detectors will change colour from grey to green.
#7. Navigate to the Diagnostic -> Detectors screen and check detectors 'silentDetectionsDetector1', 'silentDetectionsDetector2' are set to status 'reporting'.
#8. Wait for the script to trigger the second set of detections and check routes 'silentDetectionsRoute1' and 'silentDetectionsRoute2' are green with status 'reporting' in the BlueTruth map.
#9. Navigate to the Diagnostic -> Diagnostic Routes screen and check routes 'silentDetectionsRoute1' and 'silentDetectionsRoute2' are set to status 'reporting'.
#10. Navigate to the Diagnostic -> Spans screen and check spans 'silentDetectionsSpan1' and 'silentDetectionsSpan2' are set to status 'reporting'.
#11. Wait for approximately 44 mins since the last logged detection. Refresh the map page and check the detectors and routes are still displayed with 'reporting' status.
#12. Check the detector status is updated from 'reporting' to 'silent' status approximately 45 mins after the last detection was logged.
#13. Check the route status is updated from 'reporting' to 'silent' status approximately 45 mins after the last route journey was logged.
#14. Check the span status is also updated to 'silent' approximately 45 mins after the last span journey was logged.

#!/bin/bash
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

dir=$(getScriptDirectory);

source $dir/lib/V4Utils.sh

test="silentDetections";
detector1Id=$test"Detector1";
detector2Id=$test"Detector2";
detector3Id=$test"Detector3";
inStation=$(inStation);

now=$(nowInUtc);

#Set timestamp to 40 mins ago
fortyMinutesAgo=$(dateBefore $(secondsOf 0 40 0) "$now");
fortyMinutesAgoHex=$(secondsInHexOf "$fortyMinutesAgo");

#Set timestamp to 40 mins ago
thirtyEightMinutesAgo=$(dateBefore $(secondsOf 0 38 0) "$now");
thirtyEightMinutesAgoHex=$(secondsInHexOf "$thirtyEightMinutesAgo");

#Tests whether a detector and its route are in 'reporting' status until 45 mins have elapsed
statisticsReport $inStation $detector1Id","$fortyMinutesAgoHex",E,CC3A61D5C275:0:"$fortyMinutesAgoHex":2:5,7234";
sleep 1;
statisticsReport $inStation $detector2Id","$fortyMinutesAgoHex",E,5b88f0ee9f2b:0:"$fortyMinutesAgoHex":A:F,8234";
sleep 1;
statisticsReport $inStation $detector2Id","$thirtyEightMinutesAgoHex",E,CC3A61D5C275:0:"$thirtyEightMinutesAgoHex":2:5,7234";
sleep 1;
statisticsReport $inStation $detector3Id","$thirtyEightMinutesAgoHex",E,5b88f0ee9f2b:0:"$thirtyEightMinutesAgoHex":A:F,8234";
