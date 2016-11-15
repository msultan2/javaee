/* *
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

var modalDialogServiceModule = angular.module('rmasApp.modalDialog.service', ['constants']);

modalDialogServiceModule.factory('modalDialogService', function ($log, $timeout, $uibModal) {

    function closeDialog(pollingPromise) {
        if (angular.isDefined(pollingPromise)) {
            $timeout.cancel(pollingPromise);
        }
        $log.debug("Modal dialog closed", new Date());
    }

    function openModalInstance(templateUrl, controller, scope, setPromise) {
        var modalInstance = $uibModal.open({
            templateUrl: templateUrl,
            controller: controller,
            scope: scope,
            resolve: {
                promise: function () {
                    return setPromise;
                }
            }
        });

        return modalInstance.result;
    }

    return{
        openModalInstance: openModalInstance,
        closeDialog: closeDialog
    };
});

