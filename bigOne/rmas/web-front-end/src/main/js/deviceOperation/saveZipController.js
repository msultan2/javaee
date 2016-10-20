/*
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
 *
 */

'use strict';

var saveZipModule = angular.module('rmasApp.saveZip.controller', ['rmasApp.deviceOperation.service', 'ngFileSaver']);

saveZipModule.controller('saveZipController', function ($scope, $log, deviceOperationService, FileSaver, Blob) {

    function messageHandler(level, message, additionalInfo) {
        if(angular.isDefined($scope.message)){
            $scope.message.level = level;
            $scope.message.text = message;
            ($log[level] || angular.noop)(message, additionalInfo);
        }else{
            $log.warn("Error message "+ message+" obtained of level "+level);
        }
    }

    $scope.getZipFile = function () {
        deviceOperationService.getZippedFiles($scope.results.activityId)
                .then(function (response) {
                    var blob = new Blob([response.data], {
                        type: 'application/zip'
                    });
                    FileSaver.saveAs(blob, 'logs' + '.zip');
                    $log.debug("Saved file successfully");
                }, function (reason) {
                    messageHandler('error', 'Error getting the log file(s)', reason);
                });
    };
});

