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
describe('unauthorisedInterceptor', function () {

    beforeEach(module('rmasApp.user.unauthorised.interceptor'));

    var interceptor;  
    var $location;
    var $rootScope;
    var mockLocation = {
            path : function (path){}
        };
       var mockAlertService = {
            addAlert : function (){}
        };

    beforeEach(module(function($provide){
         $provide.value('$location', mockLocation);
         $provide.value('alertService', mockAlertService);
         $provide.value('$log', console);
         spyOn(mockAlertService, 'addAlert').and.callThrough();
         spyOn(mockLocation, 'path').and.callThrough();
    }));

    beforeEach(inject(function (_unauthorisedInterceptor_, _$location_, _$rootScope_) {
        interceptor = _unauthorisedInterceptor_;
        $location = _$location_;
        $rootScope = _$rootScope_;
    }));

    describe('interceptorService', function () {
        it('should be defined', function () {
            expect(interceptor).toBeDefined();
        });

        it('should have a handler for responseError', function () {
            expect(angular.isFunction(interceptor.responseError)).toBe(true);
        });

        describe('when HTTP 401', function () {
            beforeEach(function () {
                var reason = {status: 401};
                interceptor.responseError(reason);
            });

            it('should redirect to login page first and then the previous page', function () {
                 expect($location.path.calls.count()).toBe(2);
                 expect(mockAlertService.addAlert.calls.count()).toBe(1);
                 expect($location.path.calls.mostRecent().args[0]).toEqual("/login");
                 expect(mockAlertService.addAlert.calls.mostRecent().args[1]).toBe('Authentication expired');
            });
        });

        describe('when not HTTP 401', function () {
            beforeEach(function () {
                var reason = {status: 500};
                interceptor.responseError(reason);
            });

            it('should not redirect to previous page', function () {
                expect($location.path.calls.count()).toBe(0);
            });
        });
    });
});

