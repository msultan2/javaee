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

angular.module('rmasApp', [
    'rmasApp.header.controller',
    'rmasApp.alert.controller',
    'rmasApp.user.config',
    'rmasApp.home.config',
    'rmasApp.home.controller',
    'rmasApp.alert.service',
    'ui.bootstrap',
    'ngRoute',
    'ngFileSaver',
    'monospaced.elastic',
    'rmasApp.errorMessage.filter',
    'rmasApp.errorMessage.directive',
    'rmasApp.devices.deviceList.config',
    'rmasApp.devices.deviceList.controller',
    'rmasApp.downloadStaticData.controller',
    'rmasApp.resetDeviceDialog.controller',
    'rmasApp.pewNumberForResetDeviceDialog.controller',
    'rmasApp.verifyDialog.controller',
    'rmasApp.removeOldSshPublicKeysDialog.controller',
    'rmasApp.user.userGroup.controller',
    'rmasApp.user.userGroup.service',
    'rmasApp.user.registration.service',
    'rmasApp.deviceOperation.config',
    'rmasApp.sshKeys.config',
    'rmasApp.http.timeout.interceptor',
    'rmasApp.downloadLogsDialog.controller',
    'rmasApp.user.unauthorised.interceptor',
    'rmasApp.saveZip.controller',
    'rmasApp.module.devicefilter',
    'rmasApp.module.userDetails',
    'rmasApp.module.setPassword',
    'rmasApp.rcc.service',
    'rmasApp.user.changePassword.controller',
    'rmasApp.user.password.service',
    'rmasApp.deviceEnrolment.controller'
])
.config(function($routeProvider, $httpProvider, $logProvider) {
    $routeProvider.otherwise({redirectTo: '/home'});
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
    $httpProvider.interceptors.push('unauthorisedInterceptor');
    $httpProvider.interceptors.push('httpTimeoutInterceptor');
    $logProvider.debugEnabled(true);
})
.run(function($rootScope){
  $rootScope.angular = angular;
});
