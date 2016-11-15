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

describe("resetPasswordController", function () {
    var scope = {};
    var routeParams = {};
    var mockLocation = {};
    var controller;
    var mockPromise = {then: function (success, failure) {}};
    var mockAlertService = {
        addAlert: function () {}
    };
    var mockPasswordService = {
        resetPassword: function(tokenId, newPassword){
            console.log("Inside Mock service");
            return mockPromise;
        }
    };

    beforeEach(module('rmasApp.user.resetPassword.controller'));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('alertService', mockAlertService);
        $provide.value('passwordService', mockPasswordService);
        $provide.value('', mockLocation);
        spyOn(mockPromise, 'then').and.callThrough();
        spyOn(mockAlertService, 'addAlert').and.callThrough();
        spyOn(mockPasswordService, 'resetPassword').and.callThrough();
    }));

    beforeEach(function () {
        routeParams = jasmine.createSpy('routeParams');
        routeParams.tokenId = "57e52397e4b0078edc86d084";
    });

    beforeEach(inject(function ($controller) {
        controller = $controller('resetPasswordController', {$scope: scope, $routeParams: routeParams, $location: location});
    }));

    it("should be defined", function () {
        expect(controller).not.toBeNull();
    });

    describe("valid password is entered", function (){
       beforeEach(function(){
          scope.submit("Costain123"); 
       });
       it("should call resetPassord method", function(){
           expect(mockPasswordService.resetPassword.calls.count()).toBe(1);
       });

       describe("backedn responds with error", function(){
           beforeEach(function(){
                var successHandler = mockPromise.then.calls.mostRecent().args[1];
                successHandler();
           });
           it("should call alert service", function(){
               expect(mockAlertService.addAlert).toHaveBeenCalledWith('error', 'Password reset failed', 0);
           });

       });

    });
});
