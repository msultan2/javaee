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

describe("anchor", function() {
    var element2;
    var element;
    var $rootScope;
    var $window;
    var $document;

    beforeEach(module("rmasApp"));

    beforeEach(inject(function ($compile, _$rootScope_, _$window_, _$document_) {
        $rootScope = _$rootScope_;
        $window = _$window_;
        $document = _$document_;

        var element1 = "<div id='element_1' style='position:absolute; top: 10px; left: 15px; width: 20px; height: 25px'></div>";
        element2 = angular.element("<div id='element_2' style='position:absolute; top: 5px; left: 5px'></div>");

        $document.find('body').append(element1);
        $document.find('body').append(element2);

        element = angular.element("<div></div>");
        element.attr("anchor", "//*[@id='element_2']");
        element.attr("target", "//*[@id='element_1']");
        element.attr("notify", "anchor_result");
        $document.find('body').append(element);
        $compile(element)($rootScope);
    }));

    it("Should have style matching the offset and size of the two elements", function() {
        $rootScope.$digest();
        expect(element.css('top')).toBe('5px');
        expect(element.css('left')).toBe('10px');
        expect(element.css('width')).toBe('20px');
        expect(element.css('height')).toBe('25px');
        expect($rootScope.anchor_result).toBe(true);
    });

    it('Should update following a window resize', function() {
        $document.find('body').find('div').eq(1).css({top: "15px", left: "25px"});
        var evt = $window.document.createEvent('UIEvents');
        evt.initUIEvent('resize', true, false, $window, 0);
        $window.dispatchEvent(evt);
        expect(element.css('top')).toBe('-5px');
        expect(element.css('left')).toBe('-10px');
    });

});
