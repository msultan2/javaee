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
 */

'use strict';
describe("sshKeysController", function () {
    var controller;
    var scope = {};
    var mockFinally = {finally: function () {}};
    var mockPromise = {
        then: function (success, failure) {
            return mockFinally;
        }
    };
    var mockAlertService = {
        addAlert : function (){}
    };
    var mockSshKeysService = {
        generate : function (){
            return mockPromise;
        }
    };
    
    
    beforeEach(module('rmasApp.sshKeys.controller'));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
        $provide.value('alertService', mockAlertService);
        $provide.value('sshKeysService', mockSshKeysService);
        spyOn(mockAlertService, 'addAlert').and.callThrough();
        spyOn(mockSshKeysService, 'generate').and.callThrough();
        spyOn(mockPromise, 'then').and.callThrough();
   }));

    beforeEach(inject(function ($controller) {
        controller = $controller('sshKeysController', {$scope: scope});
    }));

    it("should not be null", function () {
        expect(controller).not.toBeNull();
    });

    it("generate function should be in the scope", function () {
        expect(scope.generate).not.toBeUndefined();
    });
    
    describe("calling generate function", function () {

        beforeEach(function () {
            scope.generate();
        });

        it("it should call generate function from sshKeysService", function () {
            expect(mockSshKeysService.generate).toHaveBeenCalled();
        });

        describe("when back end responds with a success", function () {
            beforeEach(function () {
                var successHandler = mockPromise.then.calls.mostRecent().args[0];
                successHandler({
                    "data":{
                        "privateFileName":"privateFileName",
                        "publicFileName":"publicFileName"
                    }
                });
            });

            it("should add sshKeyPaths to the scope", function () {
                expect(scope.sshKeyPaths.private).toEqual("privateFileName");
                expect(scope.sshKeyPaths.public).toEqual("publicFileName");
            });

            it("should call alert service with success", function () {
                expect(mockAlertService.addAlert).toHaveBeenCalledWith("success", "New SSH key pair generated successfully");
            });
        });

        describe("when back end responds with an error", function () {
            beforeEach(function () {
                var errorHandler = mockPromise.then.calls.mostRecent().args[1];
                errorHandler({
                    "data":["error"]
                });
            });

            it("should call alert service with failure", function () {
                expect(mockAlertService.addAlert).toHaveBeenCalledWith("error", "error");
            });
        });

    });
    
});