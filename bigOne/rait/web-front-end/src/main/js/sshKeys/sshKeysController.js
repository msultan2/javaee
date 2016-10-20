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
 */
'use strict';

angular.module('rmasApp.sshKeys.controller', ['rmasApp.sshKeys.service', 'constants'])
        .controller('sshKeysController', function ($scope, $log, sshKeysService, alertService) {

            function errorHandler(error) {
                $log.error("errorMessage", error);
                var errorMessage = (error.data[0] || "Unknown");
                alertService.addAlert('error', errorMessage);
            }

            function generate() {
                $scope.state.generating = true;
                sshKeysService.generate().then(function (response) {
                    $scope.sshKeyPaths = {
                        private: response.data.privateFileName,
                        public: response.data.publicFileName
                    };
                    alertService.addAlert('success', 'New SSH key pair generated successfully');
                }, errorHandler).finally(function() {
                    $scope.state.generating = false;
                });
            }

            $scope.state = {generating: false};
            $scope.generate = generate;
        });