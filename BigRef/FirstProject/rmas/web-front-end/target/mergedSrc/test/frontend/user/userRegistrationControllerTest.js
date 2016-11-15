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
 *  Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 */

'use strict';

describe("userRegistrationController", function () {
    var controller;
    var $scope;
    var mockLocation = {
        path : function (path){}
    };
    var mockAlertService = {
        addAlert : function (){}
    };
    var mockSubmitPromise = {then: function (success, failure) {}};
    var mockSponsorsPromise = {then: function (success, failure) {}};
    var mockUserRegistrationService = {
        submit: function (user) {
            return mockSubmitPromise;
        },
        getProjectSponsors: function () {
            return mockSponsorsPromise;
        }
    };

    var mockForm= {$setPristine: function(){}};

    beforeEach(module("rmasApp.user.registration.controller"));

    beforeEach(module(function ($provide) {
        $provide.value('userRegistrationService', mockUserRegistrationService);
        $provide.value('alertService', mockAlertService);
        $provide.value('$location', mockLocation);
        $provide.value('$log', console);
        spyOn(mockUserRegistrationService, 'submit').and.callThrough();
        spyOn(mockUserRegistrationService, 'getProjectSponsors').and.callThrough();
        spyOn(mockSubmitPromise, 'then').and.callThrough();
        spyOn(mockSponsorsPromise, 'then').and.callThrough();
        spyOn(mockAlertService, 'addAlert').and.callThrough();
        spyOn(mockLocation, 'path').and.callThrough();
        
    }));

    beforeEach(inject(function ($controller) {
        $scope = {};
        $scope.userRegistrationForm=mockForm;
        controller = $controller('userRegistrationController', {$scope: $scope});
    }));

    it("should not be null", function () {
        expect(controller).not.toBeNull();
    });

    it("should add submit function to the scope", function () {
        expect($scope.submit).not.toBeUndefined();
    });

    it("should add clear function to the scope", function () {
        expect($scope.clear).not.toBeUndefined();
    });

    it("should call getProjectSponsors() and then()", function () {
        expect(mockUserRegistrationService.getProjectSponsors.calls.count()).toBe(1);
        expect(mockSponsorsPromise.then.calls.count()).toBe(1);
    });

    describe("when we ask for project sponsors and it return successfully", function () {
        beforeEach(function () {
            var successHandler = mockSponsorsPromise.then.calls.mostRecent().args[0];
            var response = {
                data: {
                    _embedded: {
                        users: "users"
                    }
                }
            };
            successHandler(response);
        });

        it("should show success message", function () {
            expect($scope.projectSponsors).toBe('users');
        });
    });

    describe("when we ask for project sponsors and it return with failure", function () {
        beforeEach(function () {
            var failureHandler = mockSponsorsPromise.then.calls.mostRecent().args[1];
            failureHandler();
        });

        it("should show failure message", function () {
            expect(mockAlertService.addAlert.calls.count()).toBe(1);
        });
    });


    describe("when clear button is clicked", function () {
        beforeEach(function () {
            $scope.clear();
        });
        it("should assign null to the user", function () {
            expect($scope.user).toBe(null);
        });
    });

    describe("when submit button is clicked", function () {
        beforeEach(function () {
            $scope.user = "myUser";
            $scope.submit();
        });

        it("should call submit() from the service with the correct user and then()", function () {

            expect(mockUserRegistrationService.submit.calls.count()).toBe(1);
            expect(mockUserRegistrationService.submit.calls.mostRecent().args[0]).toEqual("myUser");

            expect(mockSubmitPromise.then.calls.count()).toBe(1);
            expect(mockSubmitPromise.then.calls.mostRecent().args.length).toEqual(2);
        });

        describe("and when success is returned", function () {
            beforeEach(function () {
                var successHandler = mockSubmitPromise.then.calls.mostRecent().args[0];
                successHandler();
            });

            it("should stack a message in the messageService and redirect the page using locationService", function () {
                expect(mockAlertService.addAlert.calls.count()).toBe(1);
                expect(mockLocation.path.calls.count()).toBe(1);
            });
        });

        describe("and when failure is returned", function () {
            beforeEach(function () {
                var failureHandler = mockSubmitPromise.then.calls.mostRecent().args[1];
                failureHandler();
            });

            it("should show failure message", function () {
                expect(mockAlertService.addAlert.calls.count()).toBe(1);
            });
        });
    });
});
