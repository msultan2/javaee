/* global expect */

/**
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

describe("showForRoles", function() {
    var element;
    var $rootScope;
    var $window;
    var $compile;
    var mockUserLoginService = {
    		userHasOneOfRoles: function(neededRoles){}
    };

    beforeEach(module("rmasApp"));

    beforeEach(module(function($provide) {
        $provide.value('userLoginService', mockUserLoginService);
    }));

    beforeEach(inject(function (_$compile_, _$rootScope_, _$window_) {
        $rootScope = _$rootScope_;
        $window = _$window_;
        $compile = _$compile_;
        element = angular.element("<div show-for-roles='ROLE_MISSING,ROLE_2,ROLE_ALSO_MISSING'></div>");
    }));

    describe('when the element is initially loaded', function(){
        it('should make the div invisible when not logged in', function() {
            spyOn(mockUserLoginService, 'userHasOneOfRoles').and.returnValue(false);
            $compile(element)($rootScope);
            $rootScope.$digest();
            expect(mockUserLoginService.userHasOneOfRoles).toHaveBeenCalled();
            expect(element.hasClass('hidden')).toBe(true);
        });

        it('should make the div visible when logged in with the right roles', function() {
            spyOn(mockUserLoginService, 'userHasOneOfRoles').and.returnValue(true);
            $compile(element)($rootScope);
            $rootScope.$digest();
            expect(mockUserLoginService.userHasOneOfRoles).toHaveBeenCalled();
            expect(element.hasClass('hidden')).toBe(false);
        });
    });
    describe('when it is invisible and then the user logs in', function() {
    	var userHasOneOfRolesSpy;
        beforeEach(function() {
            userHasOneOfRolesSpy = spyOn(mockUserLoginService, 'userHasOneOfRoles').and.returnValue(false);
            $compile(element)($rootScope);
            $rootScope.$digest();
            expect(mockUserLoginService.userHasOneOfRoles).toHaveBeenCalled();
            expect(element.hasClass('hidden')).toBe(true);
        });

        it('should make it visible', function(){
        	userHasOneOfRolesSpy.and.returnValue(true);
            $rootScope.$broadcast('loggedIn');
            expect(element.hasClass('hidden')).toBe(false);
        });
    });
    describe('when it is visible and then the user logs out', function() {
    	var userHasOneOfRolesSpy;
        beforeEach(function() {
            userHasOneOfRolesSpy = spyOn(mockUserLoginService, 'userHasOneOfRoles').and.returnValue(true);
            $compile(element)($rootScope);
            $rootScope.$digest();
            expect(mockUserLoginService.userHasOneOfRoles).toHaveBeenCalled();
            expect(element.hasClass('hidden')).toBe(false);
        });

        it('should make it invisible', function(){
        	userHasOneOfRolesSpy.and.returnValue(false);
            $rootScope.$broadcast('loggedOut');
            expect(element.hasClass('hidden')).toBe(true);
        });
    });
});
