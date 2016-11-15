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

import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import com.ssl.rmas.entities.RMASKey;
import com.ssl.rmas.entities.RMASKey.KeyType;
import java.util.List;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(exported=false)
public interface RMASKeyRepository extends Repository<RMASKey, String>, RMASKeyRepositoryCustom {
    @PreAuthorize("hasRole('HELPDESK')")
    void save(RMASKey rmasKey);

    @PreAuthorize("hasRole('HELPDESK')")
    void delete(RMASKey rmasKey);

    @Query("{'type': ?0, 'expiredTimestamp': null}")
    List<RMASKey> findAllCurrentKeys(KeyType type);

    @Query("{'type': ?0, 'expiredTimestamp': null}")
    List<RMASKey> countAllCurrentKeys(KeyType type);

    @Query("{'type': ?0, 'expiredTimestamp': {$ne : null}}")
    List<RMASKey> findAllExpiredKeys(KeyType type);

}
