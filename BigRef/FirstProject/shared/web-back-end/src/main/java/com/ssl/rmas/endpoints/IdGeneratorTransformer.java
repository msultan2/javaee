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
package com.ssl.rmas.endpoints;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;

import com.ssl.rmas.entities.HeaderKeys;
import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.managers.ResultsManager;

public class IdGeneratorTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdGeneratorTransformer.class);

    private ResultsManager resultsManager;

    @Autowired
    public void setResultManager(ResultsManager resultsManager) {
        this.resultsManager = resultsManager;
    }

    @Transformer
    public Message<?> generateUuid(Message<?> message) {
        UUID activityId = UUID.randomUUID();
        Authentication currentUser = message.getHeaders().get(HeaderKeys.CURRENT_USER.toString(), Authentication.class);
        LOGGER.debug("Activity Id Generated in IdGenerator is : {}", activityId.toString());

        OperationResult pendingResult = resultsManager.buildNewResult(currentUser, activityId);
        resultsManager.storeResult(currentUser, pendingResult);
        LOGGER.debug("Pending Result stored in ResultsManager {}", pendingResult);

        return MessageBuilder.fromMessage(message).setHeader(HeaderKeys.ACTIVITY_ID.toString(), activityId).build();
    }
}
