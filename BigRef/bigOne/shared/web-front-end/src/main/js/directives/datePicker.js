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
 */

'use strict';

var datePickerModule = angular.module('rmasApp.module.datepicker', ['ui.bootstrap']);

datePickerModule.directive("datePicker", function () {
    return {
        restrict: "E",
        require: "ngModel",
        scope: {
            ngModel: '=',
            ngReadonly: '=?',
            minDate: '=?',
            maxDate: '=?',
            dateOptions: '=?'
        },
        templateUrl: 'directives/datePickerView.html',
        controller: function ($scope) {
            $scope.dateOptions = $scope.dateOptions || {
                formatYear: 'yy',
                startingDay: 1,
                showWeeks: false
            };

            $scope.openDatePicker = function ($event) {
                $event.stopPropagation();
                $scope.opened = true;
            };
            
            $scope.format = 'dd-MMMM-yyyy';
            $scope.dateOptions = {
                maxDate: $scope.maxDate,
                minDate: $scope.minDate,
                showWeeks: false
            };
        }
    };
});