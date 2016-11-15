/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 * 
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
'use strict';

var modalResultFooterModule = angular.module('rmasApp.module.modalResultFooter', ['ui.bootstrap']);

modalResultFooterModule.directive("modalResultFooter", function () {
    return {
        restrict: "E",
        templateUrl: 'shared/resultFooterView.html',
        replace:true,
        controller: function($scope){
            function isMessageError(){
                return angular.isDefined($scope.message) && angular.isDefined($scope.message.level) && $scope.message.level === 'error';
            }
            
            function isResultStatusFailure(){
                return angular.isDefined($scope.results) && angular.isDefined($scope.results.status) && $scope.results.status==='FAILURE';
            }
            
            $scope.toDisplayOk = function () {
                if (!angular.isDefined($scope.message) && !angular.isDefined($scope.results)) {
                    return false;
                }
                if (angular.isDefined($scope.message) && !angular.isDefined($scope.results)) {
                    return false;
                } else {
                    return !(isMessageError() || isResultStatusFailure());
                }
            };
            
            $scope.toDisplayClose = function(){                
                return isMessageError()|| isResultStatusFailure();
            };                        
        }
    };
    
});