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
package com.ssl.rmas.managers;


import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.entities.RMASOperationResult;
import com.ssl.rmas.repositories.RMASOperationResultRepository;
import com.ssl.rmas.security.RMASUserDetails;

@Component("ResultsManager")
public class MongoBackedResultsManager extends AbstractResultManagerImpl {

    private final Logger logger = LoggerFactory.getLogger(MongoBackedResultsManager.class);
    private RMASOperationResultRepository rmasOperationResultRepository;

    @Autowired
    public MongoBackedResultsManager(RMASOperationResultRepository rmasResultOperationResultRepository) {
        if(rmasResultOperationResultRepository==null) {
            throw new NullPointerException("rmasResultOperationResultRepository must not be null");
        }
        this.rmasOperationResultRepository = rmasResultOperationResultRepository;
    }

    @Override
    public Optional<OperationResult> getData(Authentication currentUser, UUID activityID) {
        String userId = getUserIdFromAuth(currentUser);
        RMASOperationResult operationResult = null;
        logger.debug("Getting activity {} for user {}", activityID, userId);

        if(userId!=null) operationResult = rmasOperationResultRepository.findOneByActivityIdAndUserId(activityID, userId);

        return Optional.ofNullable(operationResult);
    }

    private String getUserIdFromAuth(Authentication currentUser) {
        String userId = null;    
        if(currentUser!=null && currentUser.getPrincipal()!=null && RMASUserDetails.class.isAssignableFrom(currentUser.getPrincipal().getClass())) {
            userId = ((RMASUserDetails)currentUser.getPrincipal()).getUserId();
        } else {
            throw new IllegalStateException("Authentication is null or has a invalid principal");
        }
        return userId;
    }

    @Override
    public void storeResult(Authentication currentUser, OperationResult result) {
        String userId = getUserIdFromAuth(currentUser);
        logger.debug("Storing activity {} for user {}", result.getActivityId(), userId);

        rmasOperationResultRepository.save(new RMASOperationResult(result, userId));
    }

    @Override
    public OperationResult buildNewResult(Authentication currentUser, UUID activityId) {
        return new RMASOperationResult(getUserIdFromAuth(currentUser), activityId);
    }

}
