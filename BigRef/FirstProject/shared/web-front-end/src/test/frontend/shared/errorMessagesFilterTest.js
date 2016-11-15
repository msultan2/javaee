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
 * 
 */

'use strict';

describe("errorMessagesFilter", function () {
    var messageArray = {
                "message" :"Last logged in",
                "args" : ["20-10-2016 12:24:00"]
    } ;
    var invalidMessageArray = {
                "message" :"Completely unknown message",
                "args" : ["20-10-2016 12:24:00"]
    } ;
    var insufficientArgumentsArray = {
                "message" : "Last logged in",
                "args" : []
    } ;
    var tooManyArgumentsArray = {
                "message" :"Last logged in",
                "args" : ["21-11-2016 12:24:00", "fred", "Bill"]
    } ;
    
    beforeEach(module('rmasApp.errorMessage.filter', function ($provide) {
        $provide.value('$log', console);
    }));

    it("should not be null", inject(function ($filter) {
        expect($filter('errorMessageFilter')).not.toBeNull();
    }));

    describe("when the filter is called with a string", function () {
        
        it("should return a simple string message", inject(function ($filter) {
            expect($filter('errorMessageFilter')("Device not registered")).toBe("The specified device is not registered in RMAS");
        }));
        
        it("should return an unknown message", inject(function ($filter) {
            expect($filter('errorMessageFilter')("This message does not exist in the list")).toBe("An unexpected error occurred. Please contact the RMAS service desk");
        }));
    });
    
    describe("when the filter is called with an object", function () {
        
        it("should return a constructed message", inject(function ($filter) {
            expect($filter('errorMessageFilter')(messageArray)).toBe("Last logged in: 20-10-2016 12:24:00");
        }));
        
        it("should return an unknown message", inject(function ($filter) {
            expect($filter('errorMessageFilter')(invalidMessageArray)).toBe("An unexpected error occurred. Please contact the RMAS service desk");
        }));
    });
    
    describe("when the filter is called with an object with insufficient arguments", function () {
        
        it("should return a constructed message", inject(function ($filter) {
            expect($filter('errorMessageFilter')(insufficientArgumentsArray)).toBe("Last logged in: {}");
        }));
    });
    
    describe("when the filter is called with an object with too many arguments", function () {
        
        it("should return a constructed message", inject(function ($filter) {
            expect($filter('errorMessageFilter')(tooManyArgumentsArray)).toBe("Last logged in: 21-11-2016 12:24:00");
        }));
    });
});
