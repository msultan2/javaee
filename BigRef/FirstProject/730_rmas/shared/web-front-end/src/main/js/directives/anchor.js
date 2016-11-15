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
 * Copyright 2015 © Simulation Systems Ltd. All Rights Reserved.
 *
 * This code is based on http://stackoverflow.com/a/15181570
 *
 */

'use strict';

angular.module('rmasApp').directive('anchor', function ($window, $document) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {

            var anchorBounds = { top: 0, left: 0, right: 0, bottom: 0 };
            var targetBounds = { top: 0, left: 0, right: 0, bottom: 0 };

            function getElementByXpath(path) {
                /* globals XPathResult: false */
                return $document[0].evaluate(path, $document[0], null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
            }

            function updateBounds(path, boundsObj) {
                var boundsElement = getElementByXpath(path);
                if (boundsElement) {
                    var boundsToCopy = boundsElement.getBoundingClientRect();
                    boundsObj.top = boundsToCopy.top;
                    boundsObj.left = boundsToCopy.left;
                    boundsObj.right = boundsToCopy.right;
                    boundsObj.bottom = boundsToCopy.bottom;
                }
                return boundsObj;
            }

            scope.$watchGroup([
                    function() {
                        return updateBounds(attrs.target, targetBounds);
                    },
                    function() {
                        return updateBounds(attrs.anchor, anchorBounds);
                    },
                    function() {
                        return targetBounds.top + targetBounds.left + targetBounds.right + targetBounds.bottom;
                    },
                    function() {
                        return anchorBounds.top + anchorBounds.left + anchorBounds.right + anchorBounds.bottom;
                    }
                ], function () {
                    element.css('top', (targetBounds.top - anchorBounds.top) + "px");
                    element.css('left', (targetBounds.left - anchorBounds.left) + "px");
                    element.css('height', (targetBounds.bottom - targetBounds.top) + "px");
                    element.css('width', (targetBounds.right - targetBounds.left) + "px");
                    if (attrs.notify) {
                        scope[attrs.notify] = true;
                    }
                });

            angular.element($window).bind('resize', function () {
                scope.$apply();
            });
        }
    };
});
