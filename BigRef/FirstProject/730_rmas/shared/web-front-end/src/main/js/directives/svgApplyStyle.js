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
 * This code is based on http://blog.akquinet.de/2014/02/26/interactive-svg-with-angularjs-part-2/
 *
 */

'use strict';

angular.module('rmasApp').directive("svgApplyStyle", function (d3Service) {
    return {
        restrict: "E",
        scope: {
            "style": "@",
            "value": "@",
            "href": "@"
        },
        require: "^svgControl",
        link: function (scope, element, attrs, svgControl) {
            void(element);
            void(attrs);
            function setElementStyle() {
                d3Service.setStyle(svgControl.svg(), scope.href, scope.style, scope.value);
            }

            svgControl.init(function () {
                setElementStyle();
                scope.$watchGroup(['style', 'value', 'href'], setElementStyle);
            });
        }
    };
});