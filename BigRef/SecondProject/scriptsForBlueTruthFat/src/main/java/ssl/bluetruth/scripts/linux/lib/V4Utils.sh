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

# Sends a http request to the InStation with the Statistics Report included as a body
# $1 = InStation
# $2 = Detector
# Example:
#       statisticsReport $inStation "Detector1,559FD5CF,1,Device1:0:559FD5CF:2:5,0";
statisticsReport() {
        inStation=$1;
        report=$2;
        servlet=$inStation'BlueTruthReceiver2/Statistics';
        sendHttpRequest $servlet $report;
}

# Sends a http request to the InStation with the Fault Report included as a body
# $1 = InStation
# $2 = Detector
# Example:
#       faultReport $inStation "Detector1,2592c00,101:386d4380:1,102:386d438a:0,ABCD";
faultReport() {
        inStation=$1;
        report=$2;
        servlet=$inStation'BlueTruthReceiver2/Fault';
        sendHttpRequest $servlet $report;
}

# Sends a http request to the InStation with the Congestion Report included as a body
# $1 = InStation
# $2 = Detector
# Example:
#       congestionReport $inStation "Detector1,2592c00,2:0:1:1:23,9,0";
congestionReport() {
        inStation=$1;
        report=$2;
        servlet=$inStation'BlueTruthReceiver2/Congestion';
        sendHttpRequest $servlet $report;
}

# Sends a http request to the InStation with the Status Report included as a body
# $1 = InStation
# $2 = Detector
# Example:
#       statusReport $inStation "Detector1,2592c00,fv=0.99,ssh=closed,3245";
statusReport() {
        inStation=$1;
        report=$2;
        servlet=$inStation'BlueTruthReceiver2/Status';
        sendHttpRequest $servlet $report;
}