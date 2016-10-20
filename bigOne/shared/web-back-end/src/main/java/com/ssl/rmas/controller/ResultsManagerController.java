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

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.ResultsManager;

@Controller
@RequestMapping(method = RequestMethod.GET)
public class ResultsManagerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsManagerController.class);

    @Autowired private ResultsManager manager;

    @RequestMapping(value = "activityResult/{activityId}")
    public ResponseEntity<OperationResult> sendHttppResponse(@PathVariable(value = "activityId") String activityId) {
        UUID uuid = UUID.fromString(activityId);
        Optional<OperationResult> result = manager.getData(SecurityContextHolder.getContext().getAuthentication(), uuid);
        ResponseEntity<OperationResult> retval = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if(result.isPresent()) {
            LOGGER.debug("Returning result {}", result.get());
            retval = new ResponseEntity<>(result.get(), HttpStatus.OK);
        } else {
            LOGGER.debug("No result found for activity {}", activityId);
        }
        return retval;
    }
}
