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
 *  Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 */

'use strict';
describe("downloadLogsDialogController", function () {
    var controller;
    var scope = {
        $watch: function (watchExpression, callback) {},
        $on: function (event, action) {}
    };
    var mockModalInstance = {close: function () {}};
    var mockPromise = {then: function (success, failure) {}};
    var deviceDetailsPromise = {then: function (success, failure,evt) {}};
    var mockCurrentPromise = {promise: function(){
            return mockPromise;
        }};
    var mockDeviceOperationService = {
        downloadLogs: function () {
            return mockPromise;
        },
        getResults: function () {
            return mockPromise;
        },
        stopPolling: function () {
            return mockPromise;
        },
        getDeviceDetails: function () {
        	return deviceDetailsPromise;
        }
    };

    var dateBeforeConstructing;
    var dateAfterConstructing;
    var ipAddress = "192.168.0.33";
    var mockRouteParams = {
        ipAddress: ipAddress
    };
    var message = "failure reason";
    var mockErrorMessageFilter = function () {
    };

    function expectTimeBetween(time, start, end) {
        expect(start.getTime() <= time.getTime() <= end.getTime()).toBeTruthy();
    }

    beforeEach(module('rmasApp.downloadLogsDialog.controller'));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('$uibModalInstance', mockModalInstance);
        $provide.value('deviceOperationService', mockDeviceOperationService);
        $provide.value('promise', mockCurrentPromise);
        $provide.value('$routeParams', mockRouteParams);
        $provide.value('errorMessageFilter', mockErrorMessageFilter);
        spyOn(mockModalInstance, 'close').and.callThrough();
        spyOn(mockDeviceOperationService, 'downloadLogs').and.callThrough();
        spyOn(mockDeviceOperationService, 'getResults').and.callThrough();
        spyOn(mockPromise, 'then').and.callThrough();
    }));

    beforeEach(inject(function ($controller) {
        dateBeforeConstructing = new Date();
        controller = $controller('downloadLogsDialogController', {$scope: scope});
        dateAfterConstructing = new Date();
    }));

    it("should not be null", function () {
        expect(controller).not.toBeNull();
    });

    it("should call setDatePickerParams setting with epoch and current time", function () {
        expect(scope.minDate.getTime()).toBe(0);
        expectTimeBetween(scope.maxDate, dateBeforeConstructing, dateAfterConstructing);
        expectTimeBetween(scope.startDate, dateBeforeConstructing, dateAfterConstructing);
        expectTimeBetween(scope.endDate, dateBeforeConstructing, dateAfterConstructing);
    });

    describe("when cancel method is called", function () {
        beforeEach(function () {
            scope.close();
        });

        it("it should call close function from modalInstance", function () {
            expect(mockModalInstance.close).toHaveBeenCalled();
        });
    });

    describe("when download button is clicked", function () {
        beforeEach(function () {
            scope.download();
        });

        it("should call downloadLogs method from deviceOperationService", function () {
            expect(mockDeviceOperationService.downloadLogs).toHaveBeenCalledWith(scope.startDate, scope.endDate, ipAddress);
            expect(mockDeviceOperationService.downloadLogs.calls.mostRecent().args[0]).toBe(scope.startDate);
            expect(mockDeviceOperationService.downloadLogs.calls.mostRecent().args[1]).toBe(scope.endDate);
            expect(mockDeviceOperationService.downloadLogs.calls.mostRecent().args[2]).toBe(ipAddress);
        });

        describe("and the back end responds", function () {
            var errorHandler;

            beforeEach(function () {
                errorHandler = mockPromise.then.calls.mostRecent().args[1];
            });

            describe("with a failure", function () {
                beforeEach(function () {
                    errorHandler(mockErrorMessageFilter(message));
                });

                it("should show a error message to the user", function () {
                    expect(scope.message.level).toEqual('error');
                });
            });
        });
    });
});
