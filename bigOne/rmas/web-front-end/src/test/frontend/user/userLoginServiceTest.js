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
 *  Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */

'use strict';

describe("rmasUserLoginService", function () {

    beforeEach(module("rmasApp.user.login.service"));

    var $scope;
    var $http;
    var backEnd;
    var userLoginService;
    var $localStorage;
    var $httpBackend;
    var $httpParamSerializer;
    var $rootScope;
    var successHandler;
    var errorHandler;
    var pathUserName;
    var $location;
    var mockLocation = {
            path : function (path){}
        };
    var mockAlertService = {
            addAlert : function (){}
        };

    var token = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL3NhbXBsZXMuYXV0aDAuY29tLyIsInN1YiI6ImZhY2Vib29rfDEwMTU0Mjg3MDI3NTEwMzAyIiwiYXVkIjoiQlVJSlNXOXg2MHNJSEJ3OEtkOUVtQ2JqOGVESUZ4REMiLCJleHAiOjE0MTIyMzQ3MzAsImlhdCI6MTQxMjE5ODczMH0.7M5sAV50fF1-_h9qVbdSgqAnXVF7mz3I6RjS6JiH0H8';

    beforeEach(module(function ($provide) {
        $provide.value('alertService', mockAlertService);
        $provide.value('$location', mockLocation);
        $provide.value('$log', console);
        spyOn(mockAlertService, 'addAlert').and.callThrough();
        spyOn(mockLocation, 'path').and.callThrough();
        
    }));
    
    beforeEach(inject(function (_userLoginService_, paths,  _$http_,
                            _$localStorage_, _$httpParamSerializer_,_$httpBackend_, jwtHelper, _$rootScope_, _$location_) {
        $scope = {};
        $http =  _$http_;
        $localStorage = _$localStorage_;
        $httpParamSerializer = _$httpParamSerializer_;
        backEnd = paths.frontToBackEnd + paths.backEnd.login;
        userLoginService = _userLoginService_;
        pathUserName = paths.frontToBackEnd + paths.backEnd.userName;
        $httpBackend = _$httpBackend_;
        jwtHelper = jwtHelper;
        $rootScope = _$rootScope_;
        $location = _$location_;
    }));

    beforeEach(function () {
        successHandler = jasmine.createSpy('successHandler');
        errorHandler = jasmine.createSpy('errorHandler');
        spyOn($rootScope, "$broadcast");
    });

    function itShouldTearDownafterLogout(){
        it("should clear the Authorization in header and token in localStorage", function() {
            userLoginService.logoutUser();
            expect($http.defaults.headers.common.Authorization).toBe(undefined);
            expect($localStorage.jwtToken).toBe(undefined);
        });
        it("should redirect to the login page", function(){
            userLoginService.logoutUser();
            expect($location.path.calls.count()).toBe(1);
            expect($location.path.calls.mostRecent().args[0]).toEqual("/login");
        });
    }

    it("should be defined", function () {
        expect(userLoginService).not.toBeNull();
    });

    it("should have functions loginUser and logoutUser", function () {
        expect(userLoginService.loginUser).toBeDefined();
        expect(userLoginService.logoutUser).toBeDefined();
    });

    describe("when logoutUser function is called", function(){
        beforeEach(function(){
            $http.defaults.headers.common.Authorization = "Something";
            $localStorage.jwtToken="Token";
        });
        itShouldTearDownafterLogout();
    });

    describe("when loginUser function is called and backend sends a 200 response", function(){
       var credentials = {"username":"username", "password":"password"};
       beforeEach(function(){
           var headerToken = {'Authorization': token};
           var serverResponse = {};
           $httpBackend.expectPOST(backEnd, $httpParamSerializer(credentials), function(headers){
                return headers.Authorization === undefined;
            }).respond(200, serverResponse, headerToken);
       });
       describe("and when loginUser function is called", function(){
            it("should post request to backend", function(){
               userLoginService.loginUser(credentials).then(successHandler,errorHandler);
               $httpBackend.flush();
               expect($http.defaults.headers.common.Authorization).toBe(token);
               expect($rootScope.$broadcast).toHaveBeenCalledWith('loggedIn', token);
               expect($localStorage.jwtToken).toBe(token);
               expect(successHandler.calls.count()).toBe(1);
               expect(errorHandler.calls.count()).toBe(0);
            });
       });
    });

    describe("when loginUser function is called and backend sends a 401 response", function(){
       var credentials = {"username":"username", "password":"password"};

       beforeEach(function(){
           var headerToken = {'Authorization': token};
           var serverResponse = {};
           $httpBackend.expectPOST(backEnd, $httpParamSerializer(credentials)).respond(401,serverResponse, headerToken);
           spyOn(userLoginService, "logoutUser");
           $http.defaults.headers.common.Authorization = token;
           $localStorage.jwtToken=token;
           userLoginService.loginUser(credentials).then(successHandler,errorHandler);
           $httpBackend.flush();
           $rootScope.$digest();
       });
       describe("and when handlers are given", function(){
           it("should call logoutUser function", function(){
                expect(successHandler.calls.count()).toBe(0);
                expect(errorHandler.calls.count()).toBe(1);
            });
            itShouldTearDownafterLogout();
       });
    });

    describe("when loginUser function is called and backend sends a 500 response", function(){
        var credentials = {"username":"username", "password":"password"};

        beforeEach(function(){
            var headerToken = {'Authorization': token};
            var serverResponse = {};
            $httpBackend.expectPOST(backEnd, $httpParamSerializer(credentials)).respond(500,serverResponse, headerToken);
            spyOn(userLoginService, "logoutUser");
            $http.defaults.headers.common.Authorization = token;
            $localStorage.jwtToken=token;
            userLoginService.loginUser(credentials).then(successHandler,errorHandler);
            $httpBackend.flush();
            $rootScope.$digest();
        });
        describe("and when handlers are given", function(){
            it("should not call the success function", function(){
                 expect(successHandler.calls.count()).toBe(0);
                 expect(errorHandler.calls.count()).toBe(1);
             });
             itShouldTearDownafterLogout();
        });
     });

    describe("when isLoggedIn function is called", function(){
        it("should check if the JWT token is valid", function(){
            var infiniteToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEyMzQ1Njc4OTAsIm5hbWUiOiJKb2huIERvZSIsImFkbWluIjp0cnVlfQ.eoaDVGTClRdfxUZXiPs3f8FmJDkDE_VCQFXqKxpLsts';
            $localStorage.jwtToken = infiniteToken;
            expect(userLoginService.isLoggedIn()).toEqual(true);
        });
        it("should check if the JWT token is invalid", function(){
            $localStorage.jwtToken = token;
            expect(userLoginService.isLoggedIn()).toEqual(false);
        });
        it("should check if the JWT token is missing", function(){
            $localStorage.jwtToken = undefined;
            expect(userLoginService.isLoggedIn()).toEqual(false);
        });
    });

    describe("when user roles is checked", function() {
    	beforeEach(function(){
    		$localStorage.jwtToken = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0Iiwicm9sZXMiOlsiUk9MRV8xIiwiUk9MRV8yIiwiUk9MRV8zIl0sImlhdCI6MTQ0NTcwODcxMCwiZXhwIjoyMDc2ODYwNzEwfQ.l594vHzYMQhCSYRRVi_0Hbyx43rKhukGU-BRC_tA9y3vmXK40O77HFF0SVYlZVLvkSSjTtWMJNRAiuk52Rn6eA';
    	});
    	it("should return true when the role is in the list", function(){
    		expect(userLoginService.userHasOneOfRoles(["ROLE_2"])).toEqual(true);
    	});
    	it("should return false when the role is not in the list", function(){
    		expect(userLoginService.userHasOneOfRoles(["ROLE_MISSING"])).toEqual(false);
    	});
    	it("should return true when a role is in the list", function(){
    		expect(userLoginService.userHasOneOfRoles(["ROLE_MISSING", "ROLE_2", "ROLE_ALSO_MISSING"])).toEqual(true);
    	});
    	it("should return false when a role is not in the list", function(){
    		expect(userLoginService.userHasOneOfRoles(["ROLE_MISSING", "ROLE_MORE_MISSING", "ROLE_STILL_MISSING"])).toEqual(false);
    	});
    	it("should return false when the user is not logged in", function(){
    		$localStorage.jwtToken = undefined;
    		expect(userLoginService.userHasOneOfRoles(["ROLE_MISSING", "ROLE_2", "ROLE_STILL_MISSING"])).toEqual(false);
    	});
    });
});
