 #!/bin/bash
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
 # Created on 14th August 2015
 #
 #
 # Author: nchavan

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
source $dir/lib/DateUtils.sh

williamsInStation="localhost:8080/"

inStation=$williamsInStation;

#Main
test="PastDetection";

detectorId="DetectorTesting"$test;

now=$(nowInUtc);

oneHourAgo=$(dateBefore $(secondsOf 1 0 0) "$now");
oneHourAgoHex=$(secondsInHexOf "$oneHourAgo");

nowHex=$(secondsInHexOf "$now");

suffix=$(dateInFormat "$now" "%H%M%S");

statisticsReport $inStation "$detectorId,"$nowHex",1E,dev1at$suffix:0:"$nowHex":2:5,dev2at$suffix:0:"$nowHex":0:0,0";

sleep 10;

statisticsReport $inStation "$detectorId,"$oneHourAgoHex",1E,dev3at$suffix:0:"$oneHourAgoHex":2:5,dev4at$suffix:0:"$oneHourAgoHex":0:0,0";


