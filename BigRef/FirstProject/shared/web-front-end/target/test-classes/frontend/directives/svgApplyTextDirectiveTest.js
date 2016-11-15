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
 * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 */

'use strict';

describe("svgApplyText", function() {
    var element;
    var $scope;
    var mockD3 = {
        setText: function(document, element, value) {
            //do nothing
        }
    };

    var mockSvgControl = {
        init: function (fn) {},
        svg: function () { return element; }
    };

    beforeEach(module("rmasApp"));

    beforeEach(module(function($provide) {
        $provide.value('d3Service', mockD3);
        spyOn(mockD3, 'setText');

        spyOn(mockSvgControl, 'init');
        spyOn(mockSvgControl, 'svg').and.callThrough();
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope.$new();

        // Compile a piece of HTML containing the directive
        element = angular.element("<svg-apply-text href='#M20_J13' text='Flow Rate: 380'></svg-apply-text>");
        element.data('$svgControlController', mockSvgControl);

        $compile(element)($scope);
        $scope.$digest();
    }));

    it('Registers with the parent svg control directive', function() {
        expect(mockSvgControl.init.calls.count()).toEqual(1);
    });

    it('Alters the text value of the svg element when ready', function() {
        var readyFunction = mockSvgControl.init.calls.mostRecent().args[0];
        readyFunction(element.scope());
        expect(mockD3.setText).toHaveBeenCalledWith(element, '#M20_J13', 'Flow Rate: 380');
    });
});
