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

var downloadLogsDialogControllerModule = angular.module('rmasApp.downloadLogsDialog.controller', ['rmasApp.deviceOperation.service', 'rmasApp.module.datepicker', 'rmasApp.deviceOperation.controller', 'constants', 'ngRoute', 'rmasApp.module.modalResultFooter']);

downloadLogsDialogControllerModule.controller('downloadLogsDialogController', function ($scope, $uibModalInstance, $log, deviceOperationService, promise, $routeParams) {
    $scope.promiseFunc = promise;
    $scope.message = {};

    function messageHandler(level, message, additionalInfo) {
        $scope.message = {level: level, text: message};
        ($log[level] || angular.noop)(message, additionalInfo);
    }
    
    function setDatePickerParams() {
        $scope.minDate = new Date(0);
        $scope.maxDate = new Date();
        $scope.startDate = new Date();
        $scope.endDate = new Date();
    }

    function setResults(results) {
        $scope.results = results;
    }

    $scope.close = function () {
        $scope.result = undefined;
        $uibModalInstance.close();
    };

    $scope.dismiss = function () {
        $scope.result = undefined;
        $uibModalInstance.dismiss('cancel');
    };

    $scope.download = function () {
        $scope.downloading = true;
        deviceOperationService.downloadLogs($scope.startDate, $scope.endDate, $routeParams.ipAddress)
                .then(function (response) {
                    $log.debug("The response received from the service", response);
                    deviceOperationService.pollForResults(messageHandler, setResults, response, $scope.promiseFunc);
                }, function (reason) {
                    messageHandler('error', 'Error processing the request', reason);
                });
    };

    $scope.$watch('startDate', function () {
        if ($scope.startDate > $scope.endDate) {
            $scope.endDate = $scope.startDate;
        }
    });

    $scope.$watch('endDate', function () {
        if ($scope.startDate > $scope.endDate) {
            $scope.startDate = $scope.endDate;
        }
    });

    setDatePickerParams();
});
