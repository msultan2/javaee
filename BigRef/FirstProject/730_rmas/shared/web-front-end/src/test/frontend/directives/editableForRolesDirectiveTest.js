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

describe("editableForRoles", function() {
    var element;
    var $rootScope;
    var $window;
    var $compile;
    var mockAuthenticationService = {
    		userHasOneOfRoles: function(neededRoles){}
    };

    beforeEach(module("rmasApp"));

    beforeEach(module(function($provide) {
        $provide.value('authenticationService', mockAuthenticationService);
    }));

    beforeEach(inject(function (_$compile_, _$rootScope_, _$window_, $document) {
        $rootScope = _$rootScope_;
        $window = _$window_;
        $compile = _$compile_;
        element = angular.element("<input editable-for-roles='ROLE_MISSING,ROLE_2,ROLE_ALSO_MISSING'/>");
    }));

    describe('when the element is initially loaded', function(){
        it('should make the input read only when not logged in', function() {
            spyOn(mockAuthenticationService, 'userHasOneOfRoles').and.returnValue(false);
            $compile(element)($rootScope);
            $rootScope.$digest();
            expect(mockAuthenticationService.userHasOneOfRoles).toHaveBeenCalled();
            expect(element.attr('readOnly')).toBe('readonly');
        });

        it('should make the input writable when logged in with the right roles', function() {
            spyOn(mockAuthenticationService, 'userHasOneOfRoles').and.returnValue(true);
            $compile(element)($rootScope);
            $rootScope.$digest();
            expect(mockAuthenticationService.userHasOneOfRoles).toHaveBeenCalled();
            expect(element.attr('readOnly')).toBeUndefined();
        });
    });
    describe('when it is read only and then the user logs in', function() {
    	var userHasOneOfRolesSpy;
        beforeEach(function() {
            userHasOneOfRolesSpy = spyOn(mockAuthenticationService, 'userHasOneOfRoles').and.returnValue(false);
            $compile(element)($rootScope);
            $rootScope.$digest();
            expect(mockAuthenticationService.userHasOneOfRoles).toHaveBeenCalled();
            expect(element.attr('readOnly')).toBe('readonly');
        });

        it('should make it writable', function(){
        	userHasOneOfRolesSpy.and.returnValue(true);
            $rootScope.$broadcast('loggedIn');
            expect(element.attr('readOnly')).toBeUndefined();
        });
    });
    describe('when it is read write and then the user logs out', function() {
    	var userHasOneOfRolesSpy;
        beforeEach(function() {
            userHasOneOfRolesSpy = spyOn(mockAuthenticationService, 'userHasOneOfRoles').and.returnValue(true);
            $compile(element)($rootScope);
            $rootScope.$digest();
            expect(mockAuthenticationService.userHasOneOfRoles).toHaveBeenCalled();
            expect(element.attr('readOnly')).toBeUndefined();
        });

        it('should make it read only', function(){
        	userHasOneOfRolesSpy.and.returnValue(false);
            $rootScope.$broadcast('loggedOut');
            expect(element.attr('readOnly')).toBe('readonly');
        });
    });
});
