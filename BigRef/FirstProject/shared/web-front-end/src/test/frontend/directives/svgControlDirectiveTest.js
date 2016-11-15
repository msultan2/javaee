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

describe("svgControl", function() {
    var element;
    var $scope;

    beforeEach(module("rmasApp"));

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope.$new();

        // Compile a piece of HTML containing the directive
        element = angular.element("<div svg-control></div>");

        $compile(element)($scope);
        // fire all the watches, so scope expressions are evaluated
        $scope.$digest();
    }));

    it('Binds the svgControl controller to the element', function() {
        expect(element.data('$svgControlController')).toBeDefined();
    });

    it('Calls registered ready methods only after $includeContentLoaded events', function() {
        var controller = element.data('$svgControlController');

        var spy = jasmine.createSpy('readyMethod');

        controller.init(spy);
        expect(spy.calls.count()).toEqual(0);

        element.scope().$emit('$includeContentLoaded');

        expect(spy).toHaveBeenCalledWith(element.scope());
    });
});
