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

describe("userRegistrationService", function () {

    beforeEach(module("rmasApp.user.registration.service"));

    var $scope;
    var $http;
    var service;
    var $httpBackend;
    var pathUserRegistrations;
    var pathUserRegistrationRequests;
    var pathUsers;

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
    }));

    beforeEach(inject(function (_userRegistrationService_, paths, _$http_, _$httpBackend_) {
        $scope = {};
        service = _userRegistrationService_;
        pathUserRegistrations = paths.frontToBackEnd + paths.backEnd.userRegistrations;
        pathUsers = paths.frontToBackEnd + paths.backEnd.allHEApprovers;
        pathUserRegistrationRequests = paths.frontToBackEnd + paths.backEnd.userRegistrationRequests;
        $http = _$http_;
        $httpBackend = _$httpBackend_;
    }));

    it("should be defined", function () {
        expect(service).not.toBeNull();
    });

    describe("when submit() is called", function () {
        var submitResponse;
        var backendData;

        beforeEach(function () {
            var user = "myUser";
            backendData = "backendData";

            $httpBackend.expectPOST(pathUserRegistrations, user).respond(200, backendData);
            submitResponse = service.submit(user);
            $httpBackend.flush();
        });

        afterEach(function () {
            $httpBackend.verifyNoOutstandingRequest();
            $httpBackend.verifyNoOutstandingExpectation();
        });

        it("should return a promise with the backend data", function () {
            expect(submitResponse.$$state.value.data).toBe(backendData);
        });

    });

    describe("when getProjectSponsors() is called", function () {
        var getProjectSponsorsResponse;
        var backendData;

        beforeEach(function () {
            backendData = "backendData";

            $httpBackend.expectGET(pathUsers).respond(200, backendData);
            getProjectSponsorsResponse = service.getProjectSponsors();
            $httpBackend.flush();
        });  
        
        afterEach(function () {
            $httpBackend.verifyNoOutstandingRequest();
            $httpBackend.verifyNoOutstandingExpectation();
        });

        it("should return a promise with the backend data", function () {
            expect(getProjectSponsorsResponse.$$state.value.data).toBe(backendData);
        });
        
    });
    
    describe("when getUserRegistrationRequests() is called", function () {
        var getUserRegistrationRequestsResponse;
        var backendData;

        beforeEach(function () {
            backendData = "backendData";

            $httpBackend.expectGET(pathUserRegistrationRequests).respond(200, backendData);
            getUserRegistrationRequestsResponse = service.getUserRegistrationRequests();
            $httpBackend.flush();
        });  
        
        afterEach(function () {
            $httpBackend.verifyNoOutstandingRequest();
            $httpBackend.verifyNoOutstandingExpectation();
        });

        it("should return a promise with the backend data", function () {
            expect(getUserRegistrationRequestsResponse.$$state.value.data).toBe(backendData);
        });
        
    });
    
    describe("when getPendingUserRegistrationRequest() is called", function () {
        var getPendingUserRegistrationRequestResponse;
        var backendData;

        beforeEach(function () {
            backendData = "backendData";

            $httpBackend.expectGET(pathUserRegistrations + "/abc").respond(200, backendData);
            getPendingUserRegistrationRequestResponse = service.getPendingUserRegistrationRequest("abc");
            $httpBackend.flush();
        });

        afterEach(function () {
            $httpBackend.verifyNoOutstandingRequest();
            $httpBackend.verifyNoOutstandingExpectation();
        });

        it("should return a promise with the backend data", function () {
            expect(getPendingUserRegistrationRequestResponse.$$state.value.data).toBe(backendData);
        });

    });

});
