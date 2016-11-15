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
package com.ssl.rmas.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import com.ssl.rmas.entities.User;
import com.ssl.rmas.entities.UserGroup;

@RepositoryRestResource(excerptProjection = UserNameOnly.class)
public interface UserRepository extends Repository<User, String> {

    @RestResource(exported=false)
    User findOneByEmail(@Param("email") String emailAddress);
    @RestResource(exported=false)
    User save(User user);

    @Query("{'roles':'HEAPPROVER'}")
    List<User> findAllHEApprovers();

    @Query(value = "{ 'id' : ?#{principal.userId}}", fields = "{ 'passwordHash' : 0}")
    User findOwnUserProfile();

    @RestResource(exported=false)
    @Query(value = "{ 'id' : ?#{principal.userId}}")
    User findOwnUser();

    @RestResource(exported=false)
    long countByUserGroup(@Param("userGroup") UserGroup userGroup);
}
