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
 */
package com.ssl.rmas.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;

import com.ssl.rmas.entities.OperationResult;
import com.ssl.rmas.entities.RMASOperationResult;
import com.ssl.rmas.repositories.RMASOperationResultRepository;
import com.ssl.rmas.security.RMASUserDetails;

@RunWith(MockitoJUnitRunner.class)
public class MongoBackedResultsManagerTest {

    private final String testUserName = "testUser";
    private UUID activityId = UUID.randomUUID();

    @Mock
    private RMASOperationResultRepository rmasOperationResultRepository;
    @Mock
    private Authentication currentUser;
    @Mock
    private RMASUserDetails mockUser;
    @Mock
    private OperationResult result;
    @Mock
    private RMASOperationResult rmasResult;
    @InjectMocks
    private MongoBackedResultsManager manager;

    @Before
    public void setup() {
        when(currentUser.getPrincipal()).thenReturn(mockUser);
        when(mockUser.getUserId()).thenReturn(testUserName);
        when(rmasResult.getActivityId()).thenReturn(activityId);
        when(result.getActivityId()).thenReturn(activityId);
    }

    @Test
    public void testSave() {

        ArgumentCaptor<RMASOperationResult> opResultCapture = ArgumentCaptor.forClass(RMASOperationResult.class);

        manager.storeResult(currentUser, result);
        verify(rmasOperationResultRepository).save(opResultCapture.capture());

        assertEquals("Check activity ID", activityId,opResultCapture.getValue().getActivityId());
        assertEquals("Check username", testUserName, opResultCapture.getValue().getUserId());
    }

    @Test
    public void testSuccessfulGet() {

        when(rmasOperationResultRepository.findOneByActivityIdAndUserId(activityId, testUserName)).thenReturn(rmasResult);

        Optional<OperationResult> actualResult = manager.getData(currentUser, activityId);

        assertEquals("Check activity ID", activityId, actualResult.get().getActivityId());
    }

    @Test
    public void testNoResult() {
        when(rmasOperationResultRepository.findOneByActivityIdAndUserId(activityId, testUserName)).thenReturn(null);

        Optional<OperationResult> actualResult = manager.getData(currentUser, activityId);

        assertFalse("Check no activity", actualResult.isPresent());
    }
}
