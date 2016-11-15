/*
 *
 * headerController.js
 *
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

var headerModule = angular.module('rmasApp.header.controller', ['rmasApp.user.login.service', 'constants']);

headerModule.controller('headerController', function ($scope, $log, userLoginService, application, paths) {
    
    $scope.navbar = paths.frontEnd;
    $log.debug("headers:", $scope.navbar);
    $scope.application={
        brand: application.brand
    };

    $scope.logout = function () {
        userLoginService.logoutUser();
    };

    $scope.isLoggedIn = userLoginService.isLoggedIn();

    $scope.$on("loggedIn", function (event, args) {
        $log.info(event.name, args);
        $scope.isLoggedIn = true;
    });

    $scope.$on("loggedOut", function (event, args) {
        $log.info(event.name, args);
        $scope.isLoggedIn = false;
    });

    $scope.status = {
        isopen: false
    };

    $scope.showRMASMenus = function () {
        return $scope.isLoggedIn && (application.brand === "RMAS");
    };
    
    $scope.showRAITMenus = function () {
        return (application.brand === "RAIT");
    };
});
