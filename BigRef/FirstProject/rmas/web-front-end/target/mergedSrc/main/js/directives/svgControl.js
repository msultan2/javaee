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

angular.module('rmasApp').directive("svgControl", function () {
    return {
        restrict: "A",
        link: function (scope, element, attrs, svgControl) {
            void(attrs);
            scope.$on("$includeContentLoaded", function() {
                svgControl.ready(element);
            });
        },
        controller: function ($scope) {
            var svgElement = null;
            var deferred = [];
            this.init = function (fn) {
                if (svgElement) {
                    deferred.push(fn);
                    fn($scope);
                } else {
                    deferred.push(fn);
                }
            };
            this.svg = function () {
                return svgElement;
            };
            this.ready = function (element) {
                svgElement = element;
                deferred.forEach(function (fn) {
                    fn($scope);
                });
            };
        }
    };
});