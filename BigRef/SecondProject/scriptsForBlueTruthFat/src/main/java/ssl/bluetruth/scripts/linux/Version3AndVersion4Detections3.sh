 #
 # Version3AndVersion4Detections3.sh
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
 # Created on 10th November 2015
 #
 # Author: Estelle Edwards

#BT-170
#Objective:
#When the same device is detected by a v3 and v4 outstation (detector) the detections are recognised as being from the same device when the v4 detector MAC addresses are recorded in lower case letters e.g. 8aaa48742c58.
#
#Preconditions:
#1.	BlueTruthReceiver2 web application is running.
#2.	BlueTruthAdministration web application is running.
#3.	SQL scripts for the FAT have been run.
#
#Method:
#1. Run the script ‘Version3AndVersion4Detections3.sh’ on the BlueTruth test server.
#2. Log into the BlueTruth administration module and navigate to the map. Check the detectors 'v3DetectorV3andV4Detections3' and 'v4DetectorV3andV4Detections3' are present and wait for both detectors to turn green.
#3. In the BlueTruth administration module, navigate to the Analysis -> Span page. Scroll to the bottom of the page and select 'View Duration' for span 'v3v4SpanV3andV4Detections3'.
#4. Adjust the graph to the last hour and check that there three detections are present.


#!/bin/bash
sh ./Version3Detections3a.sh
sleep 50
sh ./Version3Detections3b.sh
sleep 20
sh ./Version3Detections3c.sh
sleep 90
sh ./Version4Detections3a.sh
sleep 40
sh ./Version4Detections3b.sh
sleep 30
sh ./Version4Detections3c.sh