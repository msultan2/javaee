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
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * This code is based on http://blog.akquinet.de/2014/02/26/interactive-svg-with-angularjs-part-2/
 *
 */

'use strict';

angular.module('rmasApp').directive("svgApplyText", function (d3Service,$log) {
    return {
        restrict: "E",
        scope: {
            "text": "@",
            "href": "@"
         },
        require: "^svgControl",
        link: function (scope, element, attrs, svgControl) {
            void(element);
            void(attrs);
            function setElementText() {
                d3Service.setText(svgControl.svg(), scope.href, scope.text);
            }

            svgControl.init(function () {
                setElementText();
                scope.$watchGroup(['text', 'href'], setElementText);
                $log.debug("The text in selector ", scope.href, " changed to: ", scope.text, " and also been added to watchGroup");
            });
        }
    };
});
