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
package com.ssl.rmas.controllers;

import com.ssl.rmas.entities.RMASKey;
import com.ssl.rmas.repositories.RMASKeyRepository;
import com.ssl.rmas.security.ssh.RsaKeyPairGenerator;
import com.ssl.rmas.security.ssh.RsaKeyPairMongoPersister;
import com.ssl.rmas.security.ssh.SshKeyPairGenerationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import com.jcraft.jsch.KeyPair;
import org.springframework.http.ResponseEntity;
import com.ssl.rmas.utils.ErrorMessage;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SshKeysController {

    private final Logger logger = LoggerFactory.getLogger(SshKeysController.class);
    private RsaKeyPairMongoPersister rsaKeyPairMongoPersister;
    private RsaKeyPairGenerator rsaKeyPairGenerator;
    private RMASKeyRepository rmasKeyRepository;

    @Autowired
    public void setRsaKeyPairMongoPersister(RsaKeyPairMongoPersister rsaKeyPairMongoPersister) {
        this.rsaKeyPairMongoPersister = rsaKeyPairMongoPersister;
    }

    @Autowired
    public void setRsaKeyPairGenerator(RsaKeyPairGenerator rsaKeyPairGenerator) {
        this.rsaKeyPairGenerator = rsaKeyPairGenerator;
    }

    @Autowired
    public void setRMASKeyRepository(RMASKeyRepository rmasKeyRepository) {
        this.rmasKeyRepository = rmasKeyRepository;
    }

    @RequestMapping(value = "/rmasKeys/search/findCurrentPublicKey", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RMASKey> findCurrentPublicKey() {
        RMASKey currentPublicKey = rmasKeyRepository.findCurrentKey(RMASKey.KeyType.PUBLIC);
        return new ResponseEntity<RMASKey>(currentPublicKey, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HELPDESK')")
    @RequestMapping(value = "rmasKeys/generateKeyPair", method = RequestMethod.POST)
    public ResponseEntity<Set<ErrorMessage>> generateSshKeyPair() {
        try {
            KeyPair keyPair = rsaKeyPairGenerator.generate();
            rsaKeyPairMongoPersister.persist(keyPair);
            keyPair.dispose();
            if (rmasKeyRepository.checkLegalState()) {
                return new ResponseEntity<>(null, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(getErrorMessageSet(ErrorMessage.MULTIPLE_SSH_KEYS), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (SshKeyPairGenerationException ex) {
            logger.warn("Error trying to generate new SSH key pair", ex);
            return new ResponseEntity<>(getErrorMessageSet(ErrorMessage.KEY_PAIR_GENERATION_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Set<ErrorMessage> getErrorMessageSet(ErrorMessage e) {
        Set<ErrorMessage> errorMessageSet = new HashSet<>();
        errorMessageSet.add(e);
        return errorMessageSet;
    }
}
