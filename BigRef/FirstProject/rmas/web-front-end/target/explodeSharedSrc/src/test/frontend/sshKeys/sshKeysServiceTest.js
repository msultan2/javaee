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
 * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 */
 
'use strict';

describe("sshKeysService", function () {
    var sshKeysService;
    var $http;
    var paths;

    beforeEach(module('rmasApp.sshKeys.service', function ($provide) {
        $provide.value('$log', console);
    }));

    beforeEach(inject(function (_sshKeysService_, _$http_, _paths_) {
        sshKeysService = _sshKeysService_;
        paths = _paths_;
        $http = _$http_;
        spyOn($http, 'post');
        spyOn($http, 'get');
    }));

    it("should not be null", function () {
        expect(sshKeysService).not.toBeNull();
    });

    describe("when generate function is called", function () {
        var pathToBackend;
        beforeEach(function () {
            pathToBackend = paths.frontToBackEnd + paths.backEnd.rmasKeysOperations + paths.backEnd.generateSshKeyPair;
            sshKeysService.generate();
        });

        it("should call http post", function () {
            expect($http.post).toHaveBeenCalledWith(pathToBackend);
        });
    });

    describe("when get current function is called", function () {
        var pathToBackend;
        beforeEach(function () {
            pathToBackend =  paths.frontToBackEnd + paths.backEnd.rmasKeysOperations + paths.backEnd.currentPublicKey;
            sshKeysService.getCurrent();
        });

        it("should call http get", function () {
            expect($http.get).toHaveBeenCalledWith(pathToBackend);
        });
    });

});