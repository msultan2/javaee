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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 */

'use strict';

var uploadFirmwareDialogControllerModule = angular.module('rmasApp.uploadFirmwareDialog.controller', ['base64', 'rmasApp.deviceOperation.controller', 'ngRoute', 'rmasApp.module.modalResultFooter']);

uploadFirmwareDialogControllerModule.controller('uploadFirmwareDialogController', function ($scope, $uibModalInstance, $log, deviceOperationService, promise, $routeParams) {
    $scope.promiseFunc = promise;

    $scope.reset = function () {
        $scope.firmware = null;
        $scope.fileNames = null;
    };

    function messageHandler(level, message, additionalInfo) {
        $scope.message = {level: level, text: message};
        ($log[level] || angular.noop)(message, additionalInfo);
    }

    function resetMessageHandler() {
        messageHandler(undefined, undefined, undefined);
    }

    $scope.selectFiles = function (files) {
        if (files.length === 0) {
            resetMessageHandler();
        }
        for (var i = 0; i < files.length; i++) {
            if (files[i].size === 0) {
                messageHandler('warn', 'The empty file(s) will not be uploaded to the device');
            }
        }
        if (files.length !== 0) {
            $scope.firmware = files;
            $scope.fileNames = files.map(function (file) {
                return file.name;
            });
        }
    };

    function setResults(results) {
        $log.debug("The new results is ", results);
        $scope.results = results;
        $scope.showSpinner = false;
    }

    $scope.uploadFiles = function () {
        var files = $scope.firmware;
        if (files && files.length) {
            $scope.showSpinner = true;
            $log.debug("Uploading files", files);
            deviceOperationService.uploadFirmware($routeParams.ipAddress, files)
                    .then(function (response) {
                        deviceOperationService.pollForResults(messageHandler, setResults, response, $scope.promiseFunc);
                    }, function (reason) {
                        $scope.showSpinner = false;
                        $log.debug("Reason:  ", reason);
                        if (reason.status === 400) {
                            $scope.reset();
                            messageHandler('error', 'Error while uploading files', reason);
                        } else {
                            messageHandler('error', 'Error processing the request', reason);
                        }
                    });
        }
    };

    $scope.isFirmwareLoaded = function () {
        return (!$scope.firmware || $scope.firmware.progress < 100);
    };

    $scope.close = function () {
        $uibModalInstance.close();
    };

    $scope.dismiss = function () {
        $uibModalInstance.dismiss('cancel');
    };

});
