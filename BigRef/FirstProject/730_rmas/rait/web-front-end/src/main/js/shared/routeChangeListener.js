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

var addHeadersModule = angular.module('rmasApp.device.activity.listener', []);

addHeadersModule.factory('routeChangeListenerForDeviceOperation', function ($log, $rootScope, $location) {

    function register() {

        $rootScope.$on('$routeChangeStart', function (ev, next) {
            void(ev);

            if($location.$$path.startsWith("/deviceOperation")) {
                if (angular.isUndefined($rootScope.deviceDetails) || ($rootScope.deviceDetails.ipAddress !== next.params.ipAddress)) {
                    $log.debug("Found no root scope device details or IP is changed, redirecting to deviceDetails");
                    $location.path('/deviceDetails');
                }
            }
        });
    }
    return{
        register: register
    };
});

