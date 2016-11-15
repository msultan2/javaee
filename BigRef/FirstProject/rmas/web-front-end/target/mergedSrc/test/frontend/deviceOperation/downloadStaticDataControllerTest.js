/*
 *  THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 *  ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *  PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 *  SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 *  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *  ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 */

'use strict';
describe("downloadStaticDataController", function () {
    var controller;
    var scope = {
        $on: function (event, action) {}
    };
    var ipAddress = "192.168.0.33";
    var mockRouteParams = {
        ipAddress: ipAddress
    };
    var mockModalInstance = {close: function () {}};
    var mockPromise = {then: function (success, failure) {}};
    var deviceDetailsPromise = {then: function (success, failure,evt) {}};
    var mockCurrentPromise = {promise: function(){
            return mockPromise;
    }};
    var mockGetStaticDataResults = {then: function () {}};
    var response = {
        "data": {
            "activityId": {}
        }
    };
    var mockModalService = {
        open: function () {
            return mockModalInstance;
        }
    };
    var mockDeviceOperationService = {
        getStaticData: function () {
            return mockPromise;
        },
        getResults: function () {
            return mockGetStaticDataResults;
        },
        pollForResults: function () {},
        stopPolling: function(){
            return mockPromise;
        },
        getDeviceDetails: function () {
        	return deviceDetailsPromise;
        }
    };
    var mockMessageHandlingService = {
        messageHandler: function(){
           scope.message.level = 'error';
        }
    };
    var message = "Error 500";
    var mockErrorMessageFilter = function () {
    };

    beforeEach(module('rmasApp.downloadStaticData.controller'));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('deviceOperationService', mockDeviceOperationService);
        $provide.value('messageHandlerService', mockMessageHandlingService);
        $provide.value('errorMessageFilter', mockErrorMessageFilter);
        $provide.value('$uibModalInstance', mockModalInstance);
        $provide.value('$modal', mockModalService);
        $provide.value('promise', mockCurrentPromise);
        $provide.value('$routeParams', mockRouteParams);
        spyOn(mockDeviceOperationService, 'getStaticData').and.callThrough();
        spyOn(mockDeviceOperationService, 'getResults').and.callThrough();
        spyOn(mockDeviceOperationService, 'pollForResults').and.callThrough();
        spyOn(mockMessageHandlingService, 'messageHandler').and.callThrough();
        spyOn(mockModalInstance, 'close').and.callThrough();
        spyOn(mockPromise, 'then').and.callThrough();
    }));

    beforeEach(inject(function ($controller) {
        controller = $controller('downloadStaticDataController', {$scope: scope});
    }));


    it("should not be null", function () {
        expect(controller).not.toBeNull();
    });

    it("should call getStaticData function from deviceOperationService", function () {
        expect(mockDeviceOperationService.getStaticData).toHaveBeenCalledWith(ipAddress);
    });

    describe("when cancel method is called", function () {
        beforeEach(function () {
            scope.close();
        });

        it("it should call close function from modalInstance", function () {
            expect(mockModalInstance.close).toHaveBeenCalled();
        });
    });

    describe("when back end responds with a success", function () {
        beforeEach(function () {
            var successHandler = mockPromise.then.calls.mostRecent().args[0];
            successHandler(response);
        });

        it("should call getResults function in deviceOperationService", function () {
            expect(mockDeviceOperationService.pollForResults.calls.count()).toBe(1);
        });
    });

    describe("when back end responds with a failure", function () {
        beforeEach(function () {
            var errorHandler = mockPromise.then.calls.mostRecent().args[1];
            errorHandler(mockErrorMessageFilter(message));
        });

        it("should show a error message to the user", function () {
            expect(scope.message.level).toEqual('error');
        });
    });
});
