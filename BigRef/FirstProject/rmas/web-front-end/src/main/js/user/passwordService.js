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
 */

'use strict';

var passwordModule = angular.module('rmasApp.user.password.service', ['constants']);

passwordModule.factory('passwordService', function ($http, paths, $window) {
    
    var pathChangePassword = paths.frontToBackEnd + paths.backEnd.changePassword;
    
    function requestResetPasswordEmail(user) {
        return $http.post(paths.frontToBackEnd + paths.backEnd.users + $window.encodeURIComponent(user.email) + '/' + paths.backEnd.resetPassword);
    }

    function resetPassword(tokenId, newPasswordForm) {
        return $http.post(paths.frontToBackEnd + '/' + paths.backEnd.resetPassword + '/' + $window.encodeURIComponent(tokenId), newPasswordForm.password);
    }
    
    function changePassword(currentPassword, newPasswordForm) {
        var passwordPair = {
            currentPassword : currentPassword.password,
            newPassword : newPasswordForm.password
        };
        return $http.post(pathChangePassword, passwordPair);
    }
    
    return {
    	requestResetPasswordEmail: requestResetPasswordEmail,
    	resetPassword: resetPassword,
        changePassword: changePassword
    };
});

