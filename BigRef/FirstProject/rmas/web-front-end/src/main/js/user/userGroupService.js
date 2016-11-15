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

var userGroupModule = angular.module('rmasApp.user.userGroup.service', ['constants']);

userGroupModule.factory('userGroupService', function ($http, paths, $log) {

    var pathUserGroups = paths.frontToBackEnd + paths.backEnd.userGroups;

    function submit(userGroup, status) {
        if (angular.isDefined(status)) {
            var copyOfUserGroup = angular.copy(userGroup);
            copyOfUserGroup.status = status;
            userGroup = copyOfUserGroup;
        }
        $log.debug("Submitting user group ", userGroup);
        return $http.post(pathUserGroups, userGroup);
    }

    function deleteBy(userGroupId) {
        $log.debug("Deleting user group ", userGroupId);
        return $http.delete(pathUserGroups+'/'+userGroupId);
    }

    function getUserGroups() {
        $log.debug("Getting user groups");
        return $http.get(pathUserGroups);
    }

    return {
        submit: submit,
        getUserGroups: getUserGroups,
        deleteBy: deleteBy
    };
});
