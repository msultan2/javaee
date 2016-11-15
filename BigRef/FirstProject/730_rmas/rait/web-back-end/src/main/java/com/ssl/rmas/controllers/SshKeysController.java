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

import com.jcraft.jsch.KeyPair;
import com.ssl.rmas.entities.SshKeyPaths;
import com.ssl.rmas.security.ssh.RsaKeyPairFilePersister;
import com.ssl.rmas.security.ssh.RsaKeyPairGenerator;
import com.ssl.rmas.security.ssh.SshKeyPairGenerationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.ssl.rmas.utils.ErrorMessage;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SshKeysController {

    private final Logger logger = LoggerFactory.getLogger(SshKeysController.class);
    private RsaKeyPairGenerator rsaKeyPairGenerator;
    private RsaKeyPairFilePersister rsaKeyPairFilePersister;

    @Autowired
    public void setRsaKeyPairGenerator(RsaKeyPairGenerator rsaKeyPairGenerator) {
        this.rsaKeyPairGenerator = rsaKeyPairGenerator;
    }

    @Autowired
    public void setRsaKeyPairFilePersister(RsaKeyPairFilePersister rsaKeyPairFilePersister) {
        this.rsaKeyPairFilePersister = rsaKeyPairFilePersister;
    }

    @RequestMapping(value = "rmasKeys/generateKeyPair", method = RequestMethod.POST)
    public ResponseEntity<?> generateSshKeyPair() {
        try {
            KeyPair keyPair = rsaKeyPairGenerator.generate();
            SshKeyPaths sshKeyPaths = rsaKeyPairFilePersister.persist(keyPair);
            keyPair.dispose();
            return new ResponseEntity<>(sshKeyPaths, HttpStatus.OK);
        } catch (SshKeyPairGenerationException ex) {            
            return new ResponseEntity<>(getErrorMessageSet(ex), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Set<ErrorMessage> getErrorMessageSet(SshKeyPairGenerationException ex) {
        Set<ErrorMessage> errorMessageSet = new HashSet<>();
        errorMessageSet.add(ErrorMessage.KEY_PAIR_GENERATION_ERROR);
        logger.warn("Error trying to generate new SSH key pair", ex);
        return errorMessageSet;
    }
}