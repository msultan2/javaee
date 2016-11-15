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

# Acts as DB interface for different DataBase queries

#PG_DB=10.163.49.151
#PG_DB=10.163.50.112
PG_DB=localhost

executeQuery(){
	query=$1
	outputFileName=$2
	PGPASSWORD=ssl1324 psql -h $PG_DB -d bluetruth --username=bluetruth --no-readline -c"$query" -t -o $outputFileName
}

getDetectors() {
	outputFileName=$1
	if [ "$outputFileName" == "" ];then
		outputFileName=allDetectors.temp
	fi
	$(executeQuery "select detector_id from detector" "$outputFileName")
}

getDevices() {
	outputFileName=$1
	if [ "$outputFileName" == "" ];then
		outputFileName=allDevices.temp
	fi
	query="select distinct detector_id,device_id from device_detection where device_id not like 'Device%' and device_id<>'' and device_id is not null  union select distinct detector_id,device_id from device_detection_historic where device_id not like 'Device%'  and device_id<>'' and device_id is not null"
	$(executeQuery "$query" "$outputFileName")
	sed 's/^ *//g' $outputFileName > $outputFileName.X
	mv $outputFileName.X  $outputFileName
}
