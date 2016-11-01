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
import com.ssl.rmas.entities.SshKeyPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:config/application.properties")
public class RsaKeyPairFilePersister {

    @Value("${rmas.sshKeys.localPath:rait/sshKeys}")
    private String sshKeysLocalPath;
    private final String userHomePath = System.getProperty("user.home");
    private final String defaultPublicKeyComment = ""; 
    private final String publicKeyExtension = ".pub";
    private final String filesNameFormat = "'id_rsa_'yyyyMMdd_HHmmss";
    private Clock clock;

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public SshKeyPaths persist(final KeyPair keyPair) throws SshKeyPairGenerationException {
        return persist(keyPair, defaultPublicKeyComment);
    }

    public SshKeyPaths persist(final KeyPair keyPair, final String publicKeyComment) throws SshKeyPairGenerationException {
        try {
            String filesName = getFilesName();
            Path sshKeysPath = Paths.get(userHomePath, sshKeysLocalPath);
            Files.createDirectories(sshKeysPath);
            String privateKeyFileName = persistPrivateKey(keyPair, sshKeysPath, filesName);
            String publicKeyFileName = persistPublicKey(keyPair, sshKeysPath, filesName, publicKeyComment);
            return new SshKeyPaths(privateKeyFileName, publicKeyFileName);
        } catch (IOException ex) {
            throw new SshKeyPairGenerationException(ex);
        }
    }

    private String getFilesName() {
        LocalDateTime localDateTime = LocalDateTime.now(clock);
        return localDateTime.format(DateTimeFormatter.ofPattern(filesNameFormat));
    };

    private String persistPrivateKey(final KeyPair keyPair, final Path sshKeysPath, final String fileName) throws IOException {
        Path privateKeyPath = sshKeysPath.resolve(fileName);
        keyPair.writePrivateKey(privateKeyPath.toString());
        return privateKeyPath.toString();
    }

    private String persistPublicKey(final KeyPair keyPair, final Path sshKeysPath, final String fileName, final String publicKeyComment) throws IOException {
        Path publicKeyPath = sshKeysPath.resolve(fileName+publicKeyExtension);
        keyPair.writePublicKey(publicKeyPath.toString(), publicKeyComment);
        return publicKeyPath.toString();
   }
}