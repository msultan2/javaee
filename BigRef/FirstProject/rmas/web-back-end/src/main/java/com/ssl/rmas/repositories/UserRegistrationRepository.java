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
package com.ssl.rmas.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import com.ssl.rmas.entities.UserRegistration;

public interface UserRegistrationRepository extends Repository<UserRegistration, String> {

    @PreAuthorize("hasRole('HEAPPROVER') and hasRole('LOGGED_IN_WITH_2FA')")
    long count();

    @PreAuthorize("hasRole('HEAPPROVER') and hasRole('LOGGED_IN_WITH_2FA')")
    @RestResource(exported = false)
    UserRegistration findOne(String id);

    @RestResource(exported = false)
    <S extends UserRegistration> S save(S entity);

    @PreAuthorize("hasRole('HEAPPROVER') and hasRole('LOGGED_IN_WITH_2FA')")
    @Query("{'projectSponsor': ?#{principal.userId},'requestStatus': 'PENDING'}")
    List<UserRegistration> findUsersPendingRequests();
}
