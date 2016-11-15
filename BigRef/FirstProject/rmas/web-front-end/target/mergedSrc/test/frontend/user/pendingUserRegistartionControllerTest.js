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
 * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 */
'use strict';

describe("pendingUserRegistrationController", function () {

    var controller;
    var scope={};
    var mockPromise = {then: function (success, failure) {}};
    var mockUserRegistrationService = {
        getUserRegistrationRequests: function () {
            return mockPromise;
        }
    };

    var mockAlertService = {
        addAlert: function () {}
    };

    beforeEach(module("rmasApp.pending.user.registration.controller"));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('userRegistrationService', mockUserRegistrationService);
        $provide.value('alertService', mockAlertService);
        spyOn(mockPromise, 'then').and.callThrough();
        spyOn(mockAlertService, 'addAlert').and.callThrough();
    }));

    beforeEach(inject(function ($controller) {
        controller = $controller('pendingUserRegistrationController', {$scope: scope});
    }));

    it("should be defined", function () {
        expect(controller).not.toBeNull();
    });

    describe("when backend responds with http status as 500", function(){
        var successHandler;
        var errorHandler;

        beforeEach(function () {
            successHandler = mockPromise.then.calls.mostRecent().args[0];
            errorHandler = mockPromise.then.calls.mostRecent().args[1];
            var error = {status: 500};
            errorHandler(error);
        });
        it("should show a error message to the user", function () {
            expect(mockAlertService.addAlert.calls.count()).toBe(1);
            expect(mockAlertService.addAlert.calls.mostRecent().args[1]).toBe('Unknown');
        });
    });
});


