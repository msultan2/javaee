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
 # Created on 21th July 2015
 #
 #
 # Author: nchavan
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
source $dir/lib/DateUtils.sh

williamsInStation="localhost:8080/"
inStation=$williamsInStation;

now=$(nowInUtc);
tenSecondsAgo=$(dateBefore $(secondsOf 0 0 10) "$now");
fiveSecondsAgo=$(dateBefore $(secondsOf 0 0 5) "$now");
fortyMinsAgo=$(dateBefore $(secondsOf 0 40 0) "$now");

nowInHex=$(secondsInHexOf "$now");
tenSecondsAgoInHex=$(secondsInHexOf "$tenSecondsAgo");
fiveSecondsAgoInHex=$(secondsInHexOf "$fiveSecondsAgo");
fortyMinsAgoInHex=$(secondsInHexOf "$fortyMinsAgo");

congestionReport $inStation "Detector1,"$tenSecondsAgoInHex",2:0:1:1:23,9,0";
congestionReport $inStation "Detector2,"$nowInHex",2:0:1:1:23,0,0";
congestionReport $inStation "Detector3,"$nowInHex",2:0:1:1:23,FF,0";
congestionReport $inStation "Detector4,"$tenSecondsAgoInHex",2:0:1:1:23,FE,0";
congestionReport $inStation "Detector2,"$fortyMinsAgoInHex",2:0:1:1:23,FF,0";
