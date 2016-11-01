/* global expect, spyOn */

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

describe("deviceDetailsController", function () {
    var controller;
    var scope;
    var mockReturnPromise = {then: function () {}};
    var mockSshConnectionService = {
        getStaticData: function (connectionParameters) {}
    };
    var mockModalInstanceResult = {then: function () {}};
    var mockModalInstance = {result: mockModalInstanceResult, dismiss: function (type) {}, close: function () {}};
    var modal = {open: function () {}};

    beforeEach(module('rmasApp.deviceDetails.controller', function ($provide) {
        $provide.value('$log', console);
    }));

    beforeEach(module(function ($provide) {
        $provide.value('deviceDetailsService', mockSshConnectionService);
        spyOn(mockSshConnectionService, 'getStaticData').and.returnValue(mockReturnPromise);
        spyOn(modal, 'open').and.returnValue(mockModalInstance);
        spyOn(mockModalInstanceResult, 'then');
        spyOn(mockReturnPromise, 'then');
    }));

    beforeEach(inject(function ($controller) {
        scope = {};
        controller = $controller('deviceDetailsController', {$scope: scope, $modal: modal});
    }));

    it("should not be null", function () {
        expect(controller).not.toBeNull();
    });
});
