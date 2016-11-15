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
 *
 */
 
'use strict';

describe("deviceOperationService", function () {
    var deviceOperationService;
    var ipAddress = "192.168.0.33";
    var config = {headers: {ipAddress: ipAddress}};
    var publicKey = "dummyPublicKey";
    var activityId = 'dummyActivityId';
    var mockPromise = {then: function (success, failure) {}};
    var backEndStaticData;
    var backEndUpdateKey;
    var backEndDownloadLogs;
    var backEndResetDevice;
    var backEndremoveOldSshPublicKeys;
    var backEndUpgradeFirmware;
    var backEndDowngradeFirmware;
    var backEndResults;
    var $http;
    var $httpBackend;
    var messageHandler = function(){};
    var setResults = function(){};
    var promiseFunc = function(){};
    var response = {
        "data": {
            "activityId": activityId
        }
    };
    var mockTimeout = function(poll){
        poll();
    };
    var mockUpload = {
        upload: function () {
            return mockPromise;
        }
    };   
    

    beforeEach(module('rmasApp.deviceOperation.service', function ($provide) {
        $provide.value('$log', console);
        $provide.value('$timeout', mockTimeout);
        $provide.value('Upload', mockUpload);
        spyOn(mockUpload, 'upload').and.callThrough();
    }));

    beforeEach(inject(function (_deviceOperationService_, paths, _$http_, _$httpBackend_) {
        deviceOperationService = _deviceOperationService_;
        $http = _$http_;
        $httpBackend = _$httpBackend_;
        backEndStaticData = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.staticData;
        backEndUpdateKey = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.updateKey;
        backEndDownloadLogs = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.downloadLogs;
        backEndResetDevice = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.resetDevice;
        backEndremoveOldSshPublicKeys = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.removeOldSshPublicKeys;
        backEndUpgradeFirmware = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.upgradeFirmware;
        backEndDowngradeFirmware = paths.frontToBackEnd + paths.backEnd.activity + paths.backEnd.downgradeFirmware;
        backEndResults = paths.frontToBackEnd + paths.backEnd.activityResult + activityId;
        spyOn($http, 'post');
        spyOn($http, 'get').and.callThrough();
        spyOn(mockPromise, 'then').and.callThrough();
    }));

    it("should not be null", function () {
        expect(deviceOperationService).not.toBeNull();
    });

    describe("when getStaticData function is called", function () {        
        beforeEach(function () {
            deviceOperationService.getStaticData(ipAddress);
        });

        it("should return post request with connection params", function () {
            expect($http.post).toHaveBeenCalledWith(backEndStaticData, {}, config);
        });
    });

    describe("when updateKey function is called", function () {
        beforeEach(function () {
            deviceOperationService.updateKey(publicKey,ipAddress);
        });

        it("should return post request with connection params", function () {
            expect($http.post).toHaveBeenCalledWith(backEndUpdateKey, publicKey ,config);
        });
    });

    describe("when downloadLogs function is called", function () {
        var startDate = new Date(0);
        var endDate = new Date(0);
        endDate.setDate(2);
        var httpPostData = {startDate: '1970-01-01', endDate: '1970-01-02'};

        beforeEach(function () {
            deviceOperationService.downloadLogs(startDate, endDate ,ipAddress);
        });

        it("should return post request with connection params", function () {
            expect($http.post).toHaveBeenCalledWith(backEndDownloadLogs, httpPostData, config);
        });
    });
    
    describe("when resetDevice function is called", function () {
        beforeEach(function () {
            deviceOperationService.resetDevice(ipAddress);
        });

        it("should return post request with backend path", function () {
            expect($http.post).toHaveBeenCalledWith(backEndResetDevice, {}, config);
        });
    });

    describe("when removeOldSshPublicKeys function is called", function () {
        beforeEach(function () {
            deviceOperationService.removeOldSshPublicKeys(ipAddress);
        });

        it("should return post request with backend path", function () {
            expect($http.post).toHaveBeenCalledWith(backEndremoveOldSshPublicKeys, {}, config);
        });
    });
    describe("when upgradeFirmware function is called", function () {
        beforeEach(function () {
            deviceOperationService.upgradeFirmware(ipAddress);
        });

        it("should return post request with backend path", function () {
            expect($http.post).toHaveBeenCalledWith(backEndUpgradeFirmware, {}, config);
        });
    });

    describe("when downgradeFirmware function is called", function () {
        beforeEach(function () {
            deviceOperationService.downgradeFirmware(ipAddress);
        });

        it("should return post request with backend path", function () {
            expect($http.post).toHaveBeenCalledWith(backEndDowngradeFirmware, {}, config);
        });
    });

    describe("when getResults function is called", function () {
        beforeEach(function () {
            deviceOperationService.getResults(activityId);
        });

        it("should return get request with connection params", function () {
            expect($http.get).toHaveBeenCalledWith(backEndResults);

        });
    });

    describe("when pollForResults function is called", function () {
        beforeEach(function () {
            deviceOperationService.pollForResults(messageHandler, setResults, response, promiseFunc);
        });

        it("should return get request with activity id", function () {
            expect($http.get).toHaveBeenCalledWith(backEndResults);
        });
    });
});
