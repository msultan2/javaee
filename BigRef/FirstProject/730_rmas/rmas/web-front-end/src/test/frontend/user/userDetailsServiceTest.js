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

describe("userProfileService", function () {

    beforeEach(module("rmasApp.user.profile.service"));

    var $scope;
    var $http;
    var service;
    var $httpBackend;
    var pathUserDetails;
    var pathUserName;

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
    }));

    beforeEach(inject(function (_userProfileService_, paths, _$http_, _$httpBackend_) {
        $scope = {};
        service = _userProfileService_;
        pathUserDetails = paths.frontToBackEnd + paths.backEnd.userDetails;
        pathUserName = paths.frontToBackEnd + paths.backEnd.userName;
        $http = _$http_;
        $httpBackend = _$httpBackend_;
    }));

    it("should be defined", function () {
        expect(service).not.toBeNull();
    });

    describe("when getUserDetails() is called", function () {
        var getUserDetails;
        var backendData;

        beforeEach(function () {
            backendData = "backendData";
            $httpBackend.expectGET(pathUserDetails).respond(200, backendData);
            getUserDetails = service.getUserDetails();
            $httpBackend.flush();
        });

        afterEach(function () {
            $httpBackend.verifyNoOutstandingRequest();
            $httpBackend.verifyNoOutstandingExpectation();
        });

        it("should return a promise with the bakend data", function () {
            expect(getUserDetails.$$state.value.data).toBe(backendData);
        });
    });
});
