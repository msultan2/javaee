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
package com.ssl.rmas.init;

import com.ssl.rmas.entities.validation.AccountClassificationValidator;
import com.ssl.rmas.entities.validation.UserGroupCreateValidator;
import com.ssl.rmas.entities.validation.UserGroupDeleteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

/**
 * Responsible for configuring Spring Data's REST MVC service
 */
@Configuration
public class RestMvcConfig extends RepositoryRestConfigurerAdapter {

    private AccountClassificationValidator accountClassificationValidator;   
    private UserGroupCreateValidator uGroupCreateValidator;    
    private UserGroupDeleteValidator uGroupDeleteValidator;

    @Autowired
    public void setAccountClassificationValidator(AccountClassificationValidator validator) {
        this.accountClassificationValidator = validator;
    }

    @Autowired
    public void setUserGroupCreateValidator(UserGroupCreateValidator uGroupCreateValidator) {
        this.uGroupCreateValidator = uGroupCreateValidator;
    }

    @Autowired
    public void setUserGroupDeleteValidator(UserGroupDeleteValidator uGroupDeleteValidator) {
        this.uGroupDeleteValidator = uGroupDeleteValidator;
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        super.configureValidatingRepositoryEventListener(validatingListener);
        validatingListener.addValidator("beforeCreate", accountClassificationValidator);
        validatingListener.addValidator("beforeCreate", uGroupCreateValidator);
        validatingListener.addValidator("beforeDelete", uGroupDeleteValidator);
    }
}
