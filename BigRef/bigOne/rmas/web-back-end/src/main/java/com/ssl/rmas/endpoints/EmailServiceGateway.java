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
package com.ssl.rmas.endpoints;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import com.ssl.rmas.entities.PasswordChangeToken;
import com.ssl.rmas.entities.User;
import com.ssl.rmas.entities.UserRegistration;

@MessagingGateway
public interface EmailServiceGateway {
    @Gateway(requestChannel="sendRmasWelcomeTokenEmailChannel")
    void sendRmasWelcomeEmail(PasswordChangeToken token);

    @Gateway(requestChannel="sendPasswordResetTokenEmailChannel")
    void sendPasswordResetEmail(PasswordChangeToken token);

    @Gateway(requestChannel="sendPasswordChangedEmailChannel")
    void sendPasswordChangedEmail(User user);

    @Gateway(requestChannel="sendProfileChangedEmailChannel")
    void sendProfileChangedEmail(User user);

    @Gateway(requestChannel="sendUserRejectedEmailChannel")
    void sendUserRejectedEmail(UserRegistration userRegistration);
}
