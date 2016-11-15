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
 */

'use strict';

angular.module('rmasApp').directive('editableForRoles', function (authenticationService) {
    return {
    	restrict: 'A',
        link: function (scope, element, attrs) {
        	void(element);
        	function checkEditability() {
            	var roles = attrs.editableForRoles.split(',');
            	if(authenticationService.userHasOneOfRoles(roles)) {
            		attrs.$set("readOnly", false);
            	} else {
            		attrs.$set("readOnly", true);
            	}
        	}

        	scope.$on('loggedIn',checkEditability);
        	scope.$on('loggedOut',checkEditability);
        	checkEditability();
        }
    };
});