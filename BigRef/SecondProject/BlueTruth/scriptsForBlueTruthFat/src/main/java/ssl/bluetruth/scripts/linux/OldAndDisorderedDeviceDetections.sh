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
 #
 # Created on 6th July 2015
 #
 #
 # Author: josetrujillo-brenes

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

source $dir/lib/V3Utils.sh

#Main
now=$(nowInUtc);
suffix=$(dateInFormat "$now" "%H%M%S");

fiveMinutesAgo=$(dateBefore $(secondsOf 0 5 0) "$now");
oneHourAgo=$(dateBefore $(secondsOf 1 0 0) "$now");

deviceDetection $(inStation) DetectorTestingDisorderedDetections "$now" dev1At$suffix backgroundAt$suffix;
deviceDetection $(inStation) DetectorTestingDisorderedDetections "$fiveMinutesAgo" dev1At$suffix; 
deviceDetection $(inStation) DetectorTestingDisorderedDetections "$oneHourAgo" backgroundAt$suffix;
sleep 10;
avoidFaulty $(inStation) DetectorTestingDisorderedDetections;

#   dev1At$suffix should be seen in a statistics report in the web administration with first seen five minutes ago and last seen now.
#   backgroundAt$suffix wouldn't show because background devices are ignored by the receiver 2, but it could be seen in the logs
#   with a message similar to: WARN DataLogger: - LastSeen is invalid for device ID: backgroundAt$suffix. Value is: 55A8A21D + FFFFFFFE
