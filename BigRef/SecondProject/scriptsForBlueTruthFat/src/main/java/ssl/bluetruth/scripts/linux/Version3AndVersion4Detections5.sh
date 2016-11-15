 #
 # Version3AndVersion4Detections5.sh
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
 # Created on 12th November 2015
 #
 # Author: Estelle Edwards

#BT-170
#Objective:
#When the same device is detected by a v3 and v4 outstation (detector) the detections are recognised as being from the same device when the obsfucated v3 detector MAC address contains white space character(s) e.g. LxtmVPUHl4 f033lT4V5Jg_Phone.
#
#Preconditions:
#1.	BlueTruthReceiver2 web application is running.
#2.	BlueTruthAdministration web application is running.
#3.	SQL scripts for the FAT have been run.
#
#Method:
#1. Run ./lib/nowInHex.sh to display the elapsed time in s in hexadecimal format since the epoch.
#2. Run the following command with $now substitued with the date retrieved in step 1:
#wget --post-data 'v3DetectorV3andV4Detections5,$now,1E,LxtmVPUHl4 f033lT4V5Jg_Phone:0:$now:2:5,1234' 192.168.0.174:30002/BlueTruthReceiver2/Statistics --delete-after
#3. Wait for approximately 1 minute and re-run the command in step 1.
#4. Run the following command with $now substitued with the date retrieved in step 3:
#wget --post-data 'v3DetectorV3andV4Detections5,$now,1E,pGFaAtiJ 2L3F9ZvCES/5g_MaskMajorDeviceClass:0:$now:A:F,2234' 192.168.0.174:30002/BlueTruthReceiver2/Statistics --delete-after
#5. Wait for approximately 1 minute and re-run the command in step 1.
#6. Run the following command with $now substitued with the date retrieved in step 5:
#wget --post-data 'v3DetectorV3andV4Detections5,$now,E,F gG3X3c0FtwNi6KfllPuw_Computer:0:$now:E:F7,3234' 192.168.0.174:30002/BlueTruthReceiver2/Statistics --delete-after
#7. Wait for approximately 1 minute and run the script ‘Version4Detections5.sh’ on the BlueTruth test server.
#8. Log into the BlueTruth administration module and navigate to the map. Check the detectors 'v3DetectorV3andV4Detections5' and 'v4DetectorV3andV4Detections5' are present and wait for both detectors to turn green.
#9. In the BlueTruth administration module, navigate to the Analysis -> Span page. Scroll to the bottom of the page and select 'View Duration' for span 'v3v4SpanV3andV4Detections5'.
#10. Wait for approximately 1 minute and adjust the graph to the last hour and check that the three detections are present.

#!/bin/bash
#Run: ./lib/nowInHex.sh
#Then: wget --post-data 'v3DetectorV3andV4Detections5,$now,1E,LxtmVPUHl4 f033lT4V5Jg_Phone:0:$now:2:5,1234' 192.168.0.174:30002/BlueTruthReceiver2/Statistics --delete-after
#Wait for approx 1 minute
#Run: ./lib/nowInHex.sh
#Then: wget --post-data 'v3DetectorV3andV4Detections5,$now,1E,pGFaAtiJ 2L3F9ZvCES/5g_MaskMajorDeviceClass:0:$now:A:F,2234' 192.168.0.174:30002/BlueTruthReceiver2/Statistics --delete-after
#Wait for approx 1 minute
#Run: ./lib/nowInHex.sh
#Then: wget --post-data 'v3DetectorV3andV4Detections5,$now,E,F gG3X3c0FtwNi6KfllPuw_Computer:0:$now:E:F7,3234' 192.168.0.174:30002/BlueTruthReceiver2/Statistics --delete-after
sh ./Version4Detections5a.sh
sleep 40
sh ./Version4Detections5b.sh
sleep 30
sh ./Version4Detections5c.sh