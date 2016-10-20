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

describe("uploadFirmwareDialogController", function () {
    var scope = {
        $on: function (event, action) {}
    };
    var uploadFirmwareDialogController;
    var mockUploadFirmware = {then: function () {}};
    var mockUploadFirmwareResults = {then: function () {}};
    var mockPromise = {then: function (success, failure,evt) {}};
    var deviceDetailsPromise = {then: function (success, failure,evt) {}};
    var mockCurrentPromise = {promise: function(){
            return mockPromise;
    }}; 
    var mockModalInstance = {dismiss: function () {}};
    var mockDeviceOperationService = {
        uploadFirmware: function () {
            return mockPromise;
        },
        uploadFirmwareResults: function () {
            return mockUploadFirmwareResults;
        },
        stopPolling: function(){
            return mockPromise;
        },
        getDeviceDetails: function () {
        	return deviceDetailsPromise;
        }
    };
    var mockMessageHandlerService = {
        messageHandler: function(){
           scope.message.level = 'error';
        }
    };
    var mockModalService = {
        open: function () {
            return mockModalInstance;
        }
    };
    var mockRouteParams = {
        ipAddress:"DummyIpAddress"      
    };
    var mockErrorMessageFilter = function () {
        return {
            reason : {
                status : "DummyStatus"
            }
        };
    };
    
    beforeEach(module('rmasApp.uploadFirmwareDialog.controller'));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('deviceOperationService', mockDeviceOperationService);
        $provide.value('errorMessageFilter', mockErrorMessageFilter);
        $provide.value('$uibModalInstance', mockModalInstance);
        $provide.value('$modal', mockModalService);
        $provide.value('messageHandlerService', mockMessageHandlerService);
        $provide.value('promise', mockCurrentPromise);
        $provide.value('$routeParams', mockRouteParams);
        spyOn(mockDeviceOperationService, 'uploadFirmware').and.callThrough();
        spyOn(mockDeviceOperationService, 'uploadFirmwareResults').and.callThrough();
        spyOn(mockUploadFirmwareResults, 'then').and.callThrough();
        spyOn(mockUploadFirmware, 'then').and.callThrough();
        spyOn(mockModalInstance, 'dismiss').and.callThrough();
        spyOn(mockPromise, 'then').and.callThrough();
        
    }));

    beforeEach(inject(function ($controller) {
        uploadFirmwareDialogController = $controller('uploadFirmwareDialogController', {$scope: scope});
    }));

    it("should not be null", function () {
        expect(uploadFirmwareDialogController).not.toBeNull();
    });

    it("should not show the spinner", function () {
        expect(scope.showSpinner).not.toBe(true);
    });

    describe("when upload button is clicked", function () {
        beforeEach(function () {
            scope.firmware = "DummyFirmware";
            scope.uploadFiles("ipAddress", scope.firmware);
        });

        it("should call uploadFirmware method from deviceOperationService", function () {
            expect(mockDeviceOperationService.uploadFirmware.calls.count()).toBe(1);
        });

        it("should active the spinner", function () {
            expect(scope.showSpinner).toBe(true);
        });

         describe("and the back end responds with failure", function () {
            beforeEach(function () {
                var errorHandler = mockPromise.then.calls.mostRecent().args[1];
                errorHandler(mockErrorMessageFilter());
            });

            it("should show a error message to the user", function () {
                expect(scope.message.level).toEqual('error');
            });
        });
    });
});
