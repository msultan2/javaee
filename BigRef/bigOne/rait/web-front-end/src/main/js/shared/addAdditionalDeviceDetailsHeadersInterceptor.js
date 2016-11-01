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

var addHeadersModule = angular.module('rmasApp.device.activity.interceptor', ['base64', 'constants']);

addHeadersModule.factory('addAdditionalDeviceDetailsHeadersInterceptor', function ($log, $rootScope, $base64, paths) {

    var activityPath = paths.frontToBackEnd + paths.backEnd.activity;
    var fileActivityPath = paths.frontToBackEnd + paths.backEnd.fileActivity;

    function addHeaders(config) {
        if(config.url.startsWith(activityPath) || config.url.startsWith(fileActivityPath)) {
            config.headers.bandwidthLimit = $rootScope.deviceDetails.bandwidthLimit;
            config.headers.privateKey = $base64.encode($rootScope.deviceDetails.privateKey);
            $log.debug("Headers after adding new ones to request:", config.headers);
        }
        return config;
    }

    return {
        request: addHeaders
    };
});

