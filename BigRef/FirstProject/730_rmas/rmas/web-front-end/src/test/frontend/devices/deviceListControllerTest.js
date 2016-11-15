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

describe("deviceListController", function () {

    var scope = {};
    var rootScope = {deviceDetails: {"ipAddress": "192.168.0.33"}, session: {"roles": ""}};
    var controller;
    var mockPromise = {then: function (success, failure) {}};
    var mockDeviceDetailsService = {
        getDeviceList: function () {
            return mockPromise;
        }
    };
    var mockAlertService = {
        addAlert: function () {}
    };

    beforeEach(module("rmasApp.devices.deviceList.controller"));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('devicesService', mockDeviceDetailsService);
        $provide.value('alertService', mockAlertService);
        spyOn(mockPromise, 'then').and.callThrough();
        spyOn(mockAlertService, 'addAlert').and.callThrough();
    }));

    beforeEach(inject(function ($controller) {
        controller = $controller('deviceListController', {$scope: scope, $rootScope: rootScope});
    }));

    it("should be defined", function () {
        expect(controller).not.toBeNull();
    });

    describe("and the back end responds", function () {
        var successHandler;
        var errorHandler;

        beforeEach(function () {
            successHandler = mockPromise.then.calls.mostRecent().args[0];
            errorHandler = mockPromise.then.calls.mostRecent().args[1];
        });

        describe("with an error", function () {
            beforeEach(function () {
                var error = {status: 500};
                errorHandler(error);
            });

            it("should show a error message to the user", function () {
                expect(mockAlertService.addAlert.calls.count()).toBe(1);
                expect(mockAlertService.addAlert.calls.mostRecent().args[1]).toBe('Unknown');
            });
        });

        describe("with the device list", function () {
            var devices = [{device: 1}, {device: 2}];
            var filters = [{filter: 1}, {filter: 2}];
            var returnFunction = function(){
                return {
                    maxDevicesReached: "no"
                };
            };
            var response = {data: {"deviceList": devices, "filterList": filters}, headers: returnFunction};

            beforeEach(function () {
                successHandler(response);
            });

            it("should add the device list in the scope", function () {
                expect(scope.devices.list).toBe(devices);
            });
        });
    });
});
