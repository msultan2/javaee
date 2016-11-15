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
package com.ssl.rmas.security.ssh;

import com.jcraft.jsch.KeyPair;
import com.ssl.rmas.entities.RMASKey;
import com.ssl.rmas.repositories.RMASKeyRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.joining;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RsaKeyPairMongoPersister {

    private final int index_of_openssh_public_key_body = 1;
    private final String delimiter_of_openssh_public_key_tokens = " ";
    private final String defaultPublicKeyComment = "";
    private final String keyType = "ssh-rsa";
    private RMASKeyRepository rmasKeyRepository;
    private Clock clock;

    @Autowired
    public void setRMASKeyRepository(RMASKeyRepository rmasKeyRepository) {
        this.rmasKeyRepository = rmasKeyRepository;
    }

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void persist(final KeyPair keyPair) throws SshKeyPairGenerationException {
        persist(keyPair, defaultPublicKeyComment);
    }

    public void persist(final KeyPair keyPair, final String publicKeyComment) throws SshKeyPairGenerationException {
        try {
            RMASKey newPrivateKey = getPrivateKey(keyPair);
            RMASKey newPublicKey = getPublicKey(keyPair, publicKeyComment);
            expiredOldPrivateKey();
            deleteOldPublicKey();
            rmasKeyRepository.save(newPrivateKey);
            rmasKeyRepository.save(newPublicKey);
        } catch (IOException ex) {
            throw new SshKeyPairGenerationException(ex);
        }
    }

    private RMASKey getPrivateKey(KeyPair keyPair) throws IOException {
        ByteArrayOutputStream outputPrivateKey = new ByteArrayOutputStream();
        keyPair.writePrivateKey(outputPrivateKey);
        InputStream inputPrivateKey = new ByteArrayInputStream(outputPrivateKey.toByteArray());
        String privateKey = IOUtils.readLines(inputPrivateKey, StandardCharsets.UTF_8).stream().collect(joining(System.lineSeparator()));
        return new RMASKey(RMASKey.KeyType.PRIVATE, keyType, privateKey, Instant.now(clock), Optional.empty());
    }

    private RMASKey getPublicKey(KeyPair keyPair, final String publicKeyComment) throws IOException {
        ByteArrayOutputStream outputpublicKey = new ByteArrayOutputStream();
        keyPair.writePublicKey(outputpublicKey, publicKeyComment);
        InputStream inputPublicKey = new ByteArrayInputStream(outputpublicKey.toByteArray());
        List<String> streamContent = IOUtils.readLines(inputPublicKey, StandardCharsets.UTF_8);
        String publicKey = streamContent.get(0).split(delimiter_of_openssh_public_key_tokens)[index_of_openssh_public_key_body];
        return new RMASKey(RMASKey.KeyType.PUBLIC, keyType, publicKey, Instant.now(clock), Optional.empty());
    }

    private void expiredOldPrivateKey() {
        List<RMASKey> findAllCurrentKeys = rmasKeyRepository.findAllCurrentKeys(RMASKey.KeyType.PRIVATE);
        findAllCurrentKeys.stream().map((currentPrivateKeys)
                -> new RMASKey(currentPrivateKeys.getId(), currentPrivateKeys.getType(), currentPrivateKeys.getAlgorithm(), currentPrivateKeys.getContent(), currentPrivateKeys.getGeneratedTimestamp(), Optional.of(Instant.now(clock)))).forEach((expiredPrivateKey) -> {
            rmasKeyRepository.save(expiredPrivateKey);
        });
    }

    private void deleteOldPublicKey() {
        List<RMASKey> currentPublicKeys = rmasKeyRepository.findAllCurrentKeys(RMASKey.KeyType.PUBLIC);
        currentPublicKeys.stream().forEach((currentPublicKey) -> {
            rmasKeyRepository.delete(currentPublicKey);
        });
    }
}
