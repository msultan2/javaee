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
    'rmasApp.deviceDetails.config',
    'rmasApp.deviceOperation.config',
    'rmasApp.alert.controller',
    'rmasApp.header.controller',
    'rmasApp.downloadStaticData.controller',
    'rmasApp.resetDeviceDialog.controller',
    'rmasApp.verifyDialog.controller',
    'rmasApp.updateKeyDialog.controller',
    'rmasApp.uploadFirmwareDialog.controller',
    'rmasApp.upgradeFirmware.controller',
    'rmasApp.downgradeFirmware.controller',
    'rmasApp.alert.service',
    'rmasApp.downloadLogsDialog.controller',
    'rmasApp.user.login.service',
    'ui.bootstrap',
    'ngRoute',
    'rmasApp.errorMessage.filter',
    'rmasApp.errorMessage.directive',
    'rmasApp.device.activity.listener',
    'rmasApp.device.activity.interceptor',
    'rmasApp.removeOldSshPublicKeysDialog.controller',
    'rmasApp.sshKeys.config',
    'rmasApp.modalDialog.service'
])
.config(function ($routeProvider, $logProvider, $httpProvider){
    $routeProvider.otherwise({redirectTo: '/deviceDetails'});
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
    $httpProvider.interceptors.push('addAdditionalDeviceDetailsHeadersInterceptor');
    $logProvider.debugEnabled(true);
})
.run(function($rootScope,routeChangeListenerForDeviceOperation){
    $rootScope.angular = angular;
    routeChangeListenerForDeviceOperation.register();
});
