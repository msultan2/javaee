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

var userRegistrationModule = angular.module('rmasApp.user.registration.service', ['constants']);

userRegistrationModule.factory('userRegistrationService', function ($http, paths) {

    var pathUserRegistrations = paths.frontToBackEnd + paths.backEnd.userRegistrations;
    var pathHEApprovers = paths.frontToBackEnd + paths.backEnd.allHEApprovers;
    var pathUserRegistrationRequests = paths.frontToBackEnd + paths.backEnd.userRegistrationRequests;

    function submit(user) {
        return $http.post(pathUserRegistrations, user);
    }

    function update(user) {
        var pathToBackend = pathUserRegistrations + "/" + user.id;
        return $http.put(pathToBackend, user);
    }

    function getProjectSponsors() {
        return $http.get(pathHEApprovers);
    }

    function getUserRegistrationRequests() {
        return $http.get(pathUserRegistrationRequests);
    }
    
    function getPendingUserRegistrationRequest(id) {
        var pathToBackend = pathUserRegistrations + "/" + id;
        return $http.get(pathToBackend);
    }

    return {
        submit: submit,
        update: update,
        getProjectSponsors: getProjectSponsors,
        getUserRegistrationRequests: getUserRegistrationRequests,
        getPendingUserRegistrationRequest: getPendingUserRegistrationRequest
    };

});
