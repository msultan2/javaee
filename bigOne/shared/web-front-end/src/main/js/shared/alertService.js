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
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */

'use strict';

var alertModule = angular.module('rmasApp.alert.service', []);

alertModule.factory('alertService', function ($log, $rootScope) {
    var alertsHolder = {};
    alertsHolder.alerts = [];

    function addAlert(type, msg, persistent) {
        if (type === 'error') {
            type = 'danger';
        }
        if(angular.isNumber(persistent)) {
            $log.debug("Raised persistent alert: ", type, msg, persistent);
            alertsHolder.alerts.push({type: type, msg: msg, persistent: persistent});
        } else {
            $log.debug("Raised alert: ", type, msg, 0);
            alertsHolder.alerts.push({type: type, msg: msg, persistent: 0});
        }
    }

    function closeAlert(index) {
        $log.debug("closed alert ", index);
        alertsHolder.alerts.splice(index, 1);
    }

    function getAlertsHolder() {
        return alertsHolder;
    }

    function clearAlerts() {
        $log.debug("Clearing alerts");
        alertsHolder.alerts.length = 0;        
    }
    
    function expireAlerts() {
        var oldAlerts = alertsHolder.alerts;
        alertsHolder.alerts = alertsHolder.alerts.filter(function(alert){
            return alert.persistent>0;
        }).map(function(alert){
            alert.persistent -= 1;
            return alert;
        });
        $log.debug("Expired alerts, was", oldAlerts, "is now", alertsHolder.alerts);
    }

    $rootScope.$on("$routeChangeStart", function (){
        expireAlerts();
    });

    return {
        addAlert: addAlert,
        closeAlert: closeAlert,
        clearAlerts: clearAlerts,
        expireAlerts: expireAlerts,
        getAlertsHolder : getAlertsHolder
    };
});