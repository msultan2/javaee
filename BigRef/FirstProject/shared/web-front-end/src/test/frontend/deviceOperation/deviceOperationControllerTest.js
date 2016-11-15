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
describe("deviceOperationController", function () {
    var deviceOperationController;
    var scope = {
        $on: function (event, action) {}
    };
    var ipAddress = "192.168.0.33";

    var mockModalInstance = {
        then: function () {}
    };
    var mockModalService = {
        openModalInstance: function () {
            return mockModalInstance;
        },
        closeDialog: function () {}
    };
    var mockRouteParams = {
        ipAddress: ipAddress
    };
    var mockErrorMessageFilter = {
        errorMessageFilter: function () {}
    };

    function itShouldCallModalServiceOpenAndModalInstanceThenFor(dialog) {
        it("should call ModalService open for dialog: "+dialog, function () {
            expect(mockModalService.openModalInstance.calls.count()).toBe(1);
            expect(mockModalService.openModalInstance).toHaveBeenCalledWith(
                'deviceOperation/'+dialog+'View.html',
                dialog+'Controller',
                scope,
                jasmine.any(Function)
            );
        });

        it("should call ModalInstance then", function () {
            expect(mockModalInstance.then.calls.count()).toBe(1);            
        });
    }

    beforeEach(module('rmasApp.deviceOperation.controller'));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('modalDialogService', mockModalService);
        $provide.value('$routeParams', mockRouteParams);
        $provide.value('errorMessageFilter', mockErrorMessageFilter);
        spyOn(mockModalService, 'openModalInstance').and.callThrough();
        spyOn(mockModalService, 'closeDialog').and.callThrough();
        spyOn(mockModalInstance, 'then').and.callThrough();
        spyOn(scope, '$on').and.callThrough();
    }));

    beforeEach(inject(function ($controller) {
        deviceOperationController = $controller('deviceOperationController', {$scope: scope});
    }));

    it("should not be null", function () {
        expect(deviceOperationController).not.toBeNull();
    });

    describe("when downloadStaticData function is called", function () {
        beforeEach(function () {
            scope.openDownloadStaticDataDialog();
        });
        itShouldCallModalServiceOpenAndModalInstanceThenFor('downloadStaticData');
    });

    describe("when updateKey function is called", function () {
        beforeEach(function () {
            scope.openUpdateKeyDialog();
        });
        itShouldCallModalServiceOpenAndModalInstanceThenFor('updateKeyDialog');
    });

    describe("when downloadLogs function is called", function () {
        beforeEach(function () {
            scope.openDownloadLogsDialog();
        });
        itShouldCallModalServiceOpenAndModalInstanceThenFor('downloadLogsDialog');
    });

    describe("when openUploadFirmwareDialog function is called", function () {
        beforeEach(function () {
            scope.openUploadFirmwareDialog();
        });
        itShouldCallModalServiceOpenAndModalInstanceThenFor('uploadFirmwareDialog');
    });

    describe("when openResetDeviceDialog function is called", function () {
        beforeEach(function () {
            scope.openResetDeviceDialog();
        });
        itShouldCallModalServiceOpenAndModalInstanceThenFor('resetDeviceDialog');
    });

    describe("when openRemoveOldSshPublicKeysDialog function is called", function () {
        beforeEach(function () {
            scope.openRemoveOldSshPublicKeysDialog();
        });
        itShouldCallModalServiceOpenAndModalInstanceThenFor('removeOldSshPublicKeysDialog');
    });

    describe("when openUpgradeFirmwareDialog function is called", function () {
        beforeEach(function () {
            scope.openUpgradeFirmwareDialog();
        });
        itShouldCallModalServiceOpenAndModalInstanceThenFor('upgradeFirmwareDialog');
    });

    describe("when openDowngradeFirmwareDialog function is called", function () {
        beforeEach(function () {
            scope.openDowngradeFirmwareDialog();
        });
        itShouldCallModalServiceOpenAndModalInstanceThenFor('downgradeFirmwareDialog');
    });
});
