/**
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 */


'use strict';

var deviceOperationServiceModule = angular.module('rmasApp.deviceOperation.service', ['constants', 'ngFileUpload']);

deviceOperationServiceModule.factory('deviceOperationService', function ($log, $http, $timeout, paths, time, Upload) {
    var startPollWaitTime = time.activityIdStartPollWaitTimeInMilliseconds;
    var pollWaitTime = time.activityIdPollWaitTimeInMilliseconds;

    function getHeaders(ipAddress) {
        return {
            'ipAddress': ipAddress
        };
    }

    function addIpAddressInHeader(ipAddress) {
        return {
            headers: getHeaders(ipAddress)
        };
    }
    function getStaticData(ipAddress) {
        $log.debug("Posting IP Address", ipAddress);
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.staticData;
        $log.debug("POST to", pathToBackend);
        return $http.post(pathToBackend, {}, addIpAddressInHeader(ipAddress));
    }

    function updateKey(publicKey, ipAddress) {
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.updateKey;
        $log.debug("POST to", pathToBackend, "with", publicKey);
        return $http.post(pathToBackend, publicKey, addIpAddressInHeader(ipAddress));
    }

    function convertDateTimeStampToDateString(dateTimeStamp) {
        var dateTimeStampInJsonFormat = dateTimeStamp.toJSON();
        return dateTimeStampInJsonFormat.substr(0, dateTimeStampInJsonFormat.indexOf('T'));
    }

    function downloadLogs(startDate, endDate, ipAddress) {
        var httpPostData = {};
        httpPostData.startDate = convertDateTimeStampToDateString(startDate);
        httpPostData.endDate = convertDateTimeStampToDateString(endDate);
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.downloadLogs;
        $log.debug("POST to", pathToBackend, "with startDate", httpPostData.startDate, "and endDate", httpPostData.endDate, addIpAddressInHeader(ipAddress));
        return $http.post(pathToBackend, httpPostData, addIpAddressInHeader(ipAddress));
    }

    function upgradeFirmware(ipAddress) {
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.upgradeFirmware;
        $log.debug("POST to", pathToBackend);
        return $http.post(pathToBackend, {}, addIpAddressInHeader(ipAddress));
    }
    
    function downgradeFirmware(ipAddress) {
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.downgradeFirmware;
        $log.debug("POST to", pathToBackend);
        return $http.post(pathToBackend, {}, addIpAddressInHeader(ipAddress));
    }
    
    function resetDevice(ipAddress) {
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.resetDevice;
        $log.debug("POST to", pathToBackend);
        return $http.post(pathToBackend, {}, addIpAddressInHeader(ipAddress));
    }
    
    function verify(ipAddress) {
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.verify;
        $log.debug("POST to", pathToBackend);
        return $http.post(pathToBackend, {}, addIpAddressInHeader(ipAddress));
    }

    function removeOldSshPublicKeys(ipAddress) {
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.removeOldSshPublicKeys;
        $log.debug("POST to", pathToBackend);
        return $http.post(pathToBackend, {}, addIpAddressInHeader(ipAddress));
    }

    function uploadFirmware(ipAddress, files){
        return Upload.upload({
            url: paths.frontToBackEnd + paths.backEnd.fileActivity + paths.backEnd.uploadFirmware,
            headers: getHeaders(ipAddress),
            data: {
                files: files
            }
        });        
    }

    function getResults(activityId) {
        $log.debug("Getting results for activity id", activityId);
        return $http.get(paths.frontToBackEnd + paths.backEnd.activityResult + activityId);
    }

    function pollForResults(messageHandler, setResults, response, promiseFunc) {
        function poll() {
            getResults(response.data.activityId)
                    .then(function (response) {
                        $log.debug("The result received from the service ", response);
                        if (response.data.status !== 'PENDING') {
                            setResults(response.data);
                            $log.debug("The results obtain is ", response);
                        } else {
                            promiseFunc($timeout(poll, pollWaitTime));
                        }
                    }, function (reason) {
                        messageHandler('error', 'Error occured while getting the results', reason);
                    });
        }
        promiseFunc($timeout(poll, startPollWaitTime));
    }

    function stopPolling(currentPromise) {
        $timeout.cancel(currentPromise);
    }

    return{
        getStaticData: getStaticData,
        updateKey: updateKey,
        downloadLogs: downloadLogs,
        upgradeFirmware: upgradeFirmware,
        downgradeFirmware: downgradeFirmware,
        resetDevice: resetDevice,
        verify: verify,
        uploadFirmware: uploadFirmware,
        getResults: getResults,
        pollForResults: pollForResults,
        stopPolling: stopPolling,
        removeOldSshPublicKeys: removeOldSshPublicKeys
    };
});
