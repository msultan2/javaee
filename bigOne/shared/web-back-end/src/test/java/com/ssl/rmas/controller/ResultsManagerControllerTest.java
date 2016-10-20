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
 */
package com.ssl.rmas.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.ResultsManager;

@RunWith(MockitoJUnitRunner.class)
public class ResultsManagerControllerTest {

    private final String suuid = "11b81f31-f2a6-4295-9ca5-1a3d23c9f49e";
    private final UUID uuid = UUID.fromString(suuid);

    @Mock private ResultsManager mockResultsManager;
    @Mock private OperationResult operationResult;
    @InjectMocks private ResultsManagerController resultsManagerController;

    @Test
    public void testPositiveResponseForHttpRequest() {
        Mockito.when(mockResultsManager.getData(Mockito.any(), Mockito.any())).thenReturn(Optional.of(operationResult));
        Mockito.when(operationResult.getActivityId()).thenReturn(UUID.fromString(suuid));
        ResponseEntity<OperationResult> RequestMappingEndpoint = resultsManagerController.sendHttppResponse(suuid);

        assertThat(uuid, is(equalTo(RequestMappingEndpoint.getBody().getActivityId())));
        assertThat(RequestMappingEndpoint.getStatusCode(), is(equalTo(HttpStatus.OK)));
    }

    @Test
    public void testNullResponseForHttpRequest() {
        Mockito.when(mockResultsManager.getData(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<OperationResult> requestMappingEndpoint = resultsManagerController.sendHttppResponse(suuid);

        assertThat(requestMappingEndpoint.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
        assertThat(requestMappingEndpoint.getBody(), is(equalTo(null)));
    }

}
