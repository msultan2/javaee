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

var devicesModule = angular.module('rmasApp.devices.service', ['constants']);

devicesModule.factory('devicesService', function ($rootScope, $http, paths, $log, time, $q) {

    var pathFilteredDevices = paths.frontToBackEnd + paths.backEnd.filteredDevices;
    var pathInsertDevice = paths.frontToBackEnd + paths.backEnd.insertDevice;
    var cachePromise;
    var cacheExpiryDate;
    var previousFilters;
    var canceler = $q.defer();
    var requestInProgress = false;    

    var cancel = function(){
        $log.debug("cancelling promise");
        canceler.resolve();
    };
    
    $rootScope.$on('loggedOut', function(){
        cancel();
        $log.debug("CachePromise destroyed");
    });

    function addMinutes(date, minutes) {
        return new Date(date.getTime() + minutes * 60 * 1000);
    }

    function cacheShouldBeUpdated(cachePromise, cacheExpiryDate, currentDate, currentFilters) {
        return angular.isUndefined(cachePromise) || cacheExpiryDate < currentDate || !angular.equals(currentFilters, previousFilters);
    }

    function getDeviceDetails(ipAddress) {
        $log.debug("Getting device details", ipAddress);
        var pathToBackend = paths.frontToBackEnd + paths.backEnd.devices + "/" + ipAddress;
        $log.debug("get from", pathToBackend);
        return $http.get(pathToBackend);
    }

    function getDeviceList(currentFilters) {
        if(requestInProgress){
            cancel();
        }
        $log.debug("currentFilters: ", currentFilters);
        canceler = $q.defer();
        requestInProgress = true;
        var currentDate = new Date();
        if (cacheShouldBeUpdated(cachePromise, cacheExpiryDate, currentDate, currentFilters)) {
            $log.debug("updating device list cache");            
            cachePromise = $http({
                method:"post",
                url:pathFilteredDevices,
                data:currentFilters,
                timeout:canceler.promise
            });
            cachePromise.then(function(response){
                requestInProgress = false;
                $log.debug('repsonse from promise', response);
            });
            cacheExpiryDate = addMinutes(currentDate, time.cacheTimeoutInMinutes);
            $log.debug("expiry date of device list cache: ", cacheExpiryDate);
            previousFilters = angular.copy(currentFilters);            
        }
        return cachePromise;
    }

    function insert(device) {
        return $http.put(pathInsertDevice + "/" + device.ipAddress + "/", device);
    }

    function deleteCache() {
        cancel();
        cachePromise=undefined;
    }

    return{
        getDeviceList: getDeviceList,
        getDeviceDetails: getDeviceDetails,
        insert: insert,
        deleteCache: deleteCache
    };
});
