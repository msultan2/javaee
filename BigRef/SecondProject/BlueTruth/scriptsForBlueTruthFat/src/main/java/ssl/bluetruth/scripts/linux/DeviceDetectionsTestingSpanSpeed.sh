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
 # Created on 6th July 2015
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

avoidFaulty $(inStation) Detector1;
deviceDetection $(inStation) Detector1 "$(dateBefore $(secondsOf 0 20 0) "$now")" dev1at$suffix; 
deviceDetection $(inStation) Detector2 "$now" dev1at$suffix; 
deviceDetection $(inStation) Detector2 "$(dateBefore $(secondsOf 0 10 0) "$now")" dev2at$suffix; 
deviceDetection $(inStation) Detector3 "$now" dev2at$suffix; 
deviceDetection $(inStation) Detector3 "$(dateBefore $(secondsOf 0 5 0) "$now")" dev3at$suffix; 
deviceDetection $(inStation) Detector4 "$now" dev3at$suffix; 
deviceDetection $(inStation) Detector4 "$(dateBefore $(secondsOf 0 10 0) "$now")" dev4at$suffix; 
deviceDetection $(inStation) Detector5 "$now" dev4at$suffix; 
deviceDetection $(inStation) Detector5 "$(dateBefore $(secondsOf 0 10 0) "$now")" dev5at$suffix; 
deviceDetection $(inStation) Detector6 "$now" dev5at$suffix; 
deviceDetection $(inStation) Detector6 "$(dateBefore $(secondsOf 0 5 0) "$now")" dev6at$suffix; 
deviceDetection $(inStation) Detector7 "$now" dev6at$suffix; 



