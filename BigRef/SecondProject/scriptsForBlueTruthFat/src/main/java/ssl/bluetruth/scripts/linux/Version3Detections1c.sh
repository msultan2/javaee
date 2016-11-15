 #
 # Version3Detections1c.sh
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
 # Created on 5th November 2015
 #
 # Author: Estelle Edwards

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

test="V3andV4Detections1";
v3DetectorId="v3Detector"$test;
v3v4SpanId="v3v4Span"$test;
inStation=$(inStation);
now=$(secondsInHexOf "$(nowInUtc)");

#Tests for the same device IDs between a v3 and v4 detector
#Called by Version3AndVersion4Detections1.sh
statisticsReport $inStation $v3DetectorId","$now",1E,9tYWAh9A63P6hvxEzHZVlA:0:"$now":E:F7,3234";