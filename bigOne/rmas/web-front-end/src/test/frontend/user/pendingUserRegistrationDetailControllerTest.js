/* 
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 * 
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 * 
 */
'use strict';

describe("pendingUserRegistrationDetailController", function (){

    var controller;
    var scope={};
    var routeParams={
        id: 'abc'
    };
    var mockAlertService = {
        addAlert: function () {}
    };
    var mockPromiseGetPendingUserRegistrationRequest = {then: function () {}};
    var mockPromiseUpdate = {then: function () {
            return { finally: function () {}};
    }};
    var mockUserRegistrationService = {
        getPendingUserRegistrationRequest: function () {
            return mockPromiseGetPendingUserRegistrationRequest;
        },
        update: function () {
            return mockPromiseUpdate;
        }
    };
    var mockRccsPromise = {then: function (success, failure) {}};
    var mockRccsService = {
        getRccs: function () {
            return mockRccsPromise;
        }
    };

    beforeEach(module("rmasApp.pending.user.registration.detail.controller"));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('userRegistrationService', mockUserRegistrationService);
        $provide.value('rccService', mockRccsService);
        $provide.value('alertService', mockAlertService);
        spyOn(mockUserRegistrationService, 'update').and.callThrough();
        spyOn(mockPromiseUpdate, 'then').and.callThrough();
        spyOn(mockPromiseGetPendingUserRegistrationRequest, 'then').and.callThrough();
        spyOn(mockRccsService, 'getRccs').and.callThrough();
        spyOn(mockRccsPromise, 'then').and.callThrough();
        spyOn(mockAlertService, 'addAlert').and.callThrough();
    }));

    beforeEach(inject(function ($controller) {
        controller = $controller('pendingUserRegistrationDetailController', {$scope: scope, $routeParams: routeParams});
    }));

    it("should be defined", function () {
        expect(controller).not.toBeNull();
    });

    it("should call getRccs() and then()", function () {
        expect(mockRccsService.getRccs.calls.count()).toBe(1);
        expect(mockRccsPromise.then.calls.count()).toBe(1);
    });

    describe("when and the back end responds", function () {
        var getPendingUserRegistrationRequestSuccessHandler;
        var getPendingUserRegistrationRequestErrorHandler;

        beforeEach(function () {
            getPendingUserRegistrationRequestSuccessHandler = mockPromiseGetPendingUserRegistrationRequest.then.calls.mostRecent().args[0];
            getPendingUserRegistrationRequestErrorHandler = mockPromiseGetPendingUserRegistrationRequest.then.calls.mostRecent().args[1];
        });

        describe("with an error", function () {
            beforeEach(function () {
                var error = {status: 500};
                getPendingUserRegistrationRequestErrorHandler(error);
            });

            it("should show a error message to the user", function () {
                expect(mockAlertService.addAlert.calls.count()).toBe(1);
                expect(mockAlertService.addAlert.calls.mostRecent().args[1]).toBe('Failed to find pending request details');
            });
        });

        describe("with the user details", function () {
            var userDetails = {name: "Jose"};
            var response = {data: userDetails};

            beforeEach(function () {
                getPendingUserRegistrationRequestSuccessHandler(response);
            });

            it("should add the user details in the scope", function () {
                expect(scope.user.details).toBe(userDetails);
            });

            describe("and the HE Approver rejects the request", function () {
                var rejectReason = "rejectReason";

                beforeEach(function () {
                    scope.user.details.rejectReason = rejectReason;
                    scope.reject();
                });

                it("should call the update with the reject reason", function () {
                    expect(mockUserRegistrationService.update.calls.count()).toBe(1);
                    expect(mockUserRegistrationService.update.calls.mostRecent().args[0].rejectReason).toBe(rejectReason);
                });

                describe("when and the back end responds", function () {
                    var updateErrorHandler;

                    beforeEach(function () {
                        updateErrorHandler = mockPromiseUpdate.then.calls.mostRecent().args[1];
                    });

                    describe("with an error", function () {
                        var errorMessage = "errorMessage";

                        beforeEach(function () {                            
                            var error = {
                                status: 500,
                                data: errorMessage
                            };
                            updateErrorHandler(error);
                        });

                        it("should show a error message to the user", function () {
                            expect(mockAlertService.addAlert.calls.count()).toBe(1);
                            expect(mockAlertService.addAlert.calls.mostRecent().args[1]).toBe(errorMessage);
                        });
                    });
                });

            });
        });
    });
});

