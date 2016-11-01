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

describe("setPassword", function () {
    var $scope;
    var form;
    var mockPasswordCheckFormObject = {
		$validate: function(){}
    };

    beforeEach(module('rmasApp.module.setPassword'));

    beforeEach(module(function ($provide) {
        $provide.value('$log', console);
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope;
        var element = angular.element(
            '<form name="setPasswordForm">' +
        	    '<input ng-model="model.password" name="password" password/>' +        	    
        	'</form>'
        );
        $scope.model = { password: undefined };
        $compile(element)($scope);
        form = $scope.setPasswordForm;
    }));

    describe('password', function() {
    	beforeEach(function(){
    		$scope.setPasswordForm.passwordCheck = mockPasswordCheckFormObject;
    		spyOn(mockPasswordCheckFormObject, '$validate');
    	});
        it('should pass when pristine', function() {
            $scope.$digest();
            expect($scope.model.password).toBeUndefined();
            expect(form.password.$pristine).toBe(true);
            expect(form.password.$valid).toBe(true);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should pass with a valid', function() {
            form.password.$setViewValue('1234eE^t');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(true);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should not pass with a short password', function() {
            form.password.$setViewValue('1234eE^');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(false);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should not pass with repeats', function() {
            form.password.$setViewValue('134eE^^t');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(false);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should pass with just missing lowercase', function() {
            form.password.$setViewValue('ER1234^&');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(true);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should pass with just missing uppercase', function() {
            form.password.$setViewValue('er1234^&');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(true);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should pass with just missing numbers', function() {
            form.password.$setViewValue('er£%RG^&');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(true);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should pass with just missing non alpha', function() {
            form.password.$setViewValue('s1S1s1S1');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(true);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should not pass with only numbers and non alpha', function() {
            form.password.$setViewValue('1234!\"£$');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(false);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
        it('should not pass with only numbers and uppercase and lowercase', function() {
            form.password.$setViewValue('eEeEeEeE');
            $scope.$digest();
            expect(form.password.$pristine).toBe(false);
            expect(form.password.$valid).toBe(false);
            expect(mockPasswordCheckFormObject.$validate).toHaveBeenCalled();
        });
    });
});