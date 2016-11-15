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
package com.ssl.rmas.utils.ssh;

import com.ssl.rmas.entities.RMASKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.ssl.rmas.repositories.RMASKeyRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RmasKeyProvider implements ExpiredKeyProvider{
    private final Logger logger = LoggerFactory.getLogger(RmasKeyProvider.class);
   
    private RMASKeyRepository keyRepository;
    
    @Autowired
    public void setKeyRepository(RMASKeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @Override
    public Collection<String> getExpiredPrivateKeys() {
        List<RMASKey> findAllExpiredKeys = keyRepository.findAllExpiredKeys(RMASKey.KeyType.PRIVATE);
        logger.debug("List of expired keys : {}", findAllExpiredKeys);
        return findAllExpiredKeys.stream()
                .map(expiredKey -> expiredKey.getContent())
                .collect(Collectors.toList());
    }

    @Override
    public String getPublicKey() {
        RMASKey publicKey = keyRepository.findCurrentKey(RMASKey.KeyType.PUBLIC);
        return publicKey.getAlgorithm() + " " + publicKey.getContent();
    }
}
