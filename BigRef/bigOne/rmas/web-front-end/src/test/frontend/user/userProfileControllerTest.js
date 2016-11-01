/*
 *   THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 *   LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 *   EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 *   BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 *   INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 *   OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 *   Copyright 2016 © Costain Integrated Technology Solutions Limited.
 *   All Rights Reserved.
 */

'use strict';
describe("profileController", function () {
    var controller;
    var $scope;
    var mockAlertService = {
        addAlert: function () {}
    };
    var mockUserProfilePromise = {then: function (success, failure) {}};
    var mockUserProfileService = {
        getUserDetails: function () {
            return mockUserProfilePromise;
        }
    };
    var mockRccsPromise = {then: function (success, failure) {}};
    var mockRccsService = {
        getRccs: function () {
            return mockRccsPromise;
        }
    };
    var userDetails = {address: "Calle Rio Ebro",
        email: "sergio@ssl.com",
        employer: "SSL",
        enabled: false,
        id: null,
        mcr: "ABC",
        name: "Sergio",
        passwordHash: null,
        primaryPhone: "123456789",
        projectSponsor: "Roberto",
        rcc: "CBA"};

    beforeEach(module("rmasApp.user.profile.controller"));
    beforeEach(module(function ($provide) {
        $provide.value('userProfileService', mockUserProfileService);
        $provide.value('rccService', mockRccsService);
        $provide.value('alertService', mockAlertService);
        $provide.value('$log', console);
        spyOn(mockUserProfileService, 'getUserDetails').and.callThrough();
        spyOn(mockRccsService, 'getRccs').and.callThrough();
        spyOn(mockUserProfilePromise, 'then').and.callThrough();
        spyOn(mockRccsPromise, 'then').and.callThrough();
        spyOn(mockAlertService, 'addAlert').and.callThrough();
    }));
    beforeEach(inject(function ($controller) {
        $scope = {};
        controller = $controller('profileController', {$scope: $scope});
    }));
    it("should not be null", function () {
        expect(controller).not.toBeNull();
    });

    it("should call getUserDetails() and then()", function () {
        expect(mockUserProfileService.getUserDetails.calls.count()).toBe(1);
        expect(mockUserProfilePromise.then.calls.count()).toBe(1);
    });

    it("should call getRccs() and then()", function () {
        expect(mockRccsService.getRccs.calls.count()).toBe(1);
        expect(mockRccsPromise.then.calls.count()).toBe(1);
    });

    describe("when we ask for user details and it return successfully", function () {
        beforeEach(function () {
            var successHandler = mockUserProfilePromise.then.calls.mostRecent().args[0];
            var response = {
                data: userDetails
            };
            successHandler(response);
        });
        it("should show success message", function () {
            expect($scope.user.details).toBe(userDetails);
        });
    });

    describe("when we ask for user details and it return with failure", function () {
        beforeEach(function () {
            var failureHandler = mockUserProfilePromise.then.calls.mostRecent().args[1];
            failureHandler();
        });

        it("should show failure message", function () {
            expect(mockAlertService.addAlert.calls.count()).toBe(1);
        });
    });

    describe("when we ask for rccs and it returns successfully", function () {
        beforeEach(function () {
            var successHandler = mockRccsPromise.then.calls.mostRecent().args[0];
            var response = {
                data: {
                    _embedded: {
                        rccs: ["rccs"]
                    }
                }
            };
            successHandler(response);
        });

        it("should show success message", function () {
            expect($scope.rccs).toEqual(["rccs"]);
        });
    });

    describe("when we ask for rccs and it returns with failure", function () {
        beforeEach(function () {
            var failureHandler = mockRccsPromise.then.calls.mostRecent().args[1];
            failureHandler();
        });

        it("should show failure message", function () {
            expect(mockAlertService.addAlert.calls.count()).toBe(1);
        });
    });
});
