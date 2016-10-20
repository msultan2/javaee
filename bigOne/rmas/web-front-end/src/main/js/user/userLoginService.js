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

var userLoginModule = angular.module('rmasApp.user.login.service', ['constants', 'angular-jwt', 'ngStorage','rmasApp.alert.service']);

userLoginModule.factory('userLoginService', function ($timeout, $log, $rootScope, $http, $httpParamSerializer, $localStorage, jwtHelper, paths, $q, alertService, $location) {


    var authenticationBackEnd = paths.frontToBackEnd + paths.backEnd.login;
    var refreshTokenBackEnd = paths.frontToBackEnd + paths.backEnd.refreshToken;
    var refreshTimeout;
    var authenticationExp = 'Authentication expired';
    $rootScope.session = {};
    var cachedToken;

    function cancelRefreshTimeout() {
        if(angular.isDefined(refreshTimeout)) {
        	$timeout.cancel(refreshTimeout);
        	refreshTimeout = undefined;
        }
    }

    function logoutUser(){
        cancelRefreshTimeout();
        $http.defaults.headers.common.Authorization = undefined;
        $localStorage.jwtToken=undefined;
        $rootScope.$broadcast("loggedOut");
        $location.path('/login');
        $rootScope.session.name = undefined;
    }

    function processToken(token) {
        $http.defaults.headers.common.Authorization = token;
        $localStorage.jwtToken=token;
        cachedToken = jwtHelper.decodeToken(token);
        if (angular.isUndefined(refreshTimeout)) {
            var timeoutTime = jwtHelper.getTokenExpirationDate(token) - new Date();
            //Make sure we wait at least 10 seconds;
            var refreshTime = Math.max(10000, (timeoutTime - 30000 - Math.round(Math.random() * 30000)));

            var tokenExpiryTime = jwtHelper.getTokenExpirationDate(token);
            var tokenRefreshTime = new Date(Date.now() + refreshTime);

            $log.debug("Token timeout", tokenExpiryTime, "timeout time is", timeoutTime, "refresh time is", refreshTime, tokenRefreshTime);
            refreshTimeout = $timeout(function () {
                if (!jwtHelper.isTokenExpired(token)) {
                    // Check if something else has already got the token
                    if ($localStorage.jwtToken === token) {
                        $http.get(refreshTokenBackEnd).finally(function () {
                            refreshTimeout = undefined;
                        }).then(function (success) {
                            var token = success.headers("Authorization");
                            if(angular.isDefined(token)) {
                                processToken(token);
                            }
                        }, function (error) {
                            $log.error("Failed to refresh token", error);
                            logoutUser();
                            alertService.addAlert('error', authenticationExp, 1);
                        });
                    }
                } else {
                    $log.error("Token expired! Non authenticated request");
                    logoutUser();
                    alertService.addAlert('error', authenticationExp, 1);
                }
            }, refreshTime);
        }
        $log.debug("User name", cachedToken.name);
        $rootScope.session.name = cachedToken.name;
        $rootScope.session.roles = cachedToken.roles;
    }

    function loginUser(credentials){
    	return $q(function(resolve, reject) {
            $http.post(authenticationBackEnd,$httpParamSerializer(credentials), {
                headers: {'Content-Type': 'application/x-www-form-urlencoded', 'Authorization': undefined}
             }).then(function (success){
                 var token = success.headers("Authorization");
                 if(angular.isDefined(token)) {
                 	 processToken(token);
                     $rootScope.$broadcast("loggedIn", token);                    
                 }
                 var passwordExpired = (success.headers("PasswordExpired")=== "true");
                 $log.debug("passwordExpired: ", passwordExpired);
                 (resolve || angular.noop)(passwordExpired);
             },function (error){
                 logoutUser();
                 (reject || angular.noop)(error);
             });
    	});
    }
    
    function isLoggedIn() {
    	var token = $localStorage.jwtToken;
    	var isTokenValid = angular.isDefined(token) && !jwtHelper.isTokenExpired(token);
    	if(isTokenValid) {
    		if($http.defaults.headers.common.Authorization!==token) {
    			processToken(token);
    		}
    	}
    	return isTokenValid;
    }

    function userHasOneOfRoles(roles) {
    	if(isLoggedIn()) {
    		$log.debug("Token:", cachedToken);
    		$log.debug("Users roles", cachedToken.roles, "required roles", roles);
    		return cachedToken.roles.some(function(userRole){return roles.some(function(role){return role===userRole})});
    	} else {
    		$log.debug("User not logged in");
    	}
    	return false;
    }

    isLoggedIn();

    return{
      loginUser: loginUser,
      logoutUser: logoutUser,
      isLoggedIn: isLoggedIn,
      userHasOneOfRoles: userHasOneOfRoles
    };
})
.run(function ($rootScope, $location, $log, userLoginService, alertService) {
    function buildPathFrom(next) {
        var path = next.$$route.originalPath;
        for (var property in next.pathParams) {
            if (next.pathParams.hasOwnProperty(property)) {
                var regEx = new RegExp(":" + property, "gi");
                path = path.replace(regEx, next.pathParams[property].toString());
            }
        }
        return path;
    }

    $rootScope.$on('$routeChangeStart', function (ev, next) {
        void(ev);
        if(userLoginService.isLoggedIn() && userLoginService.userHasOneOfRoles(['ROLE_HEAPPROVER']) && !userLoginService.userHasOneOfRoles(['ROLE_LOGGED_IN_WITH_2FA'])) {
        	$location.path('/setup2fa');
        }
        if (next.$$route) {
            var authMethod = next.$$route.auth || function (userLoginService) {
                $log.debug("Using default auth check");
                return userLoginService.isLoggedIn();
            };
            if (!authMethod(userLoginService)) {
                var path = buildPathFrom(next);
                $log.debug("User is not allowed access to", path);
                alertService.addAlert('error', 'Authentication expired', 1);
                $rootScope.loginProcess = {successRedirect: path};
                $location.path('/login');
            }
        }
    });
});

