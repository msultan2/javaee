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

angular.module('rmasApp.user.profile.service', ['constants'])
        .factory('userProfileService', function ($http, paths) {

    var pathUserDetails = paths.frontToBackEnd + paths.backEnd.userDetails;
    var pathUpdateUserDetails = paths.frontToBackEnd + paths.backEnd.updateUserDetails;

    function getUserDetails() {
        return $http.get(pathUserDetails);
    }

    function updateUserDetails(userDetails) {
        return $http.post(pathUpdateUserDetails, userDetails);
    }

    function getUser2faEnabled() {
    	return $http.get(paths.frontToBackEnd + paths.backEnd.user2faEnabled);
    }
    
    return {
        getUserDetails: getUserDetails,
        updateUserDetails: updateUserDetails,
        getUser2faEnabled: getUser2faEnabled
    };
});
