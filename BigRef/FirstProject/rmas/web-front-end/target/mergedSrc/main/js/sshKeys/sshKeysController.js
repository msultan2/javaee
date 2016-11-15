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
 * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 */
'use strict';

angular.module('rmasApp.sshKeys.controller', ['rmasApp.sshKeys.service', 'constants'])
        .controller('sshKeysController', function ($scope, $log, sshKeysService, alertService) {

            function addAlert(error) {
                $log.error("errorMessage", error);
                var errorMessage = (error.data[0] || "Unknown");
                alertService.addAlert('error', errorMessage);
            }

            function getCurrentPublicKey() {
                sshKeysService.getCurrent().then(function (response) {
                    $scope.publicKey.value = response.data;
                    $log.debug("publicKey:", $scope.publicKey);
                }, addAlert);
            }

            function generate() {
                $scope.state.generating = true;
                sshKeysService.generate().then(function () {
                    getCurrentPublicKey();
                    alertService.addAlert('success', 'New SSH key pair generated successfully');
                }, addAlert).finally(function() {
                    $scope.state.generating = false;
                });
            }

            $scope.state = {generating: false};
            $scope.publicKey = {};
            getCurrentPublicKey();
            $scope.generate = generate;

        });