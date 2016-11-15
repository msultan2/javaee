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

var upgradeFirmwareDialogControllerModule = angular.module('rmasApp.upgradeFirmware.controller', ['rmasApp.deviceOperation.service', 'rmasApp.deviceOperation.controller', 'constants', 'ngRoute', 'rmasApp.module.modalResultFooter']);

upgradeFirmwareDialogControllerModule.controller('upgradeFirmwareDialogController', function ($scope, $uibModalInstance, $log, deviceOperationService, promise, $routeParams) {
    $scope.promiseFunc = promise;

    function messageHandler(level, message, additionalInfo) {
        $scope.message = {level: level, text: message};
        ($log[level] || angular.noop)(message, additionalInfo);
    }

    function setResults(results) {
        $log.debug("The new results is ", results);
        $scope.results = results;
    }

    function upgradeFirmware() {
        deviceOperationService.upgradeFirmware($routeParams.ipAddress)
                .then(function (response) {
                    $log.debug("The response received from the service", response);
                    deviceOperationService.pollForResults(messageHandler, setResults, response, $scope.promiseFunc);
                }, function (reason) {
                    messageHandler('error', 'Error processing the request', reason);
                });
    }

    $scope.close = function () {
        $uibModalInstance.close();
    };

    $scope.dismiss = function () {
        $uibModalInstance.dismiss('cancel');
    };

    upgradeFirmware();
});
