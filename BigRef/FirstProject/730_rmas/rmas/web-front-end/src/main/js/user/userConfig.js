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

angular.module('rmasApp.user.config', [
    'ngRoute',
    'rmasApp.user.login.controller',
    'rmasApp.user.profile.controller',
    'rmasApp.user.registration.controller',
    'rmasApp.user.requestResetPassword.controller', 
    'rmasApp.user.resetPassword.controller',
    'rmasApp.pending.user.registration.controller',
    'rmasApp.pending.user.registration.detail.controller',
    'rmasApp.user.setup2fa.controller'])
        .config(function ($routeProvider) {
    $routeProvider.when('/login', {
        templateUrl: 'user/userLogin.html',
        controller: 'userLoginController',
        auth: function() {return true;}
    }).when('/registration', {
        templateUrl: 'user/registration.html',
        controller: 'userRegistrationController',
        auth: function() {return true;}
    }).when('/profile', {
        templateUrl: 'user/profile.html',
        controller: 'profileController'
    }).when('/requestResetPassword', {
        templateUrl: 'user/requestResetPassword.html',
        controller: 'requestResetPasswordController',
        auth: function() {return true;}
    }).when('/resetPassword/:tokenId', {
        templateUrl: 'user/resetPassword.html',
        controller: 'resetPasswordController',
        auth: function() {return true;}
    }).when('/pendingUserRegistrationRequests', {
        templateUrl: 'user/pendingUserRegistrationRequestsView.html',
        controller: 'pendingUserRegistrationController',
        auth: function(userLoginService) {
            return userLoginService.userHasOneOfRoles(['ROLE_HEAPPROVER']);}
    }).when('/pendingUserRegistrationRequestDetail/:id', {
        templateUrl: 'user/pendingUserRegistrationRequestDetailView.html',
        controller: 'pendingUserRegistrationDetailController',
        auth: function(userLoginService) {
            return userLoginService.userHasOneOfRoles(['ROLE_HEAPPROVER']);}
    }).when('/userGroups', {
        templateUrl: 'user/userGroupView.html',
        controller: 'userGroupController',
        auth: function(userLoginService) {
            return userLoginService.userHasOneOfRoles(['ROLE_HEAPPROVER']);
        }
    }).when('/setup2fa', {
        templateUrl: 'user/setup2fa.html',
        controller: 'setup2faController',
    }).when('/changePassword', {
        templateUrl: 'user/changePassword.html',
        controller: 'changePasswordController'
    }).when('/accountClassifications', {
        templateUrl: 'user/accountClassificationView.html',
        controller: 'accountClassificationController',
        auth: function(userLoginService) {
            return userLoginService.userHasOneOfRoles(['ROLE_HEAPPROVER']);
        }
    });
});

