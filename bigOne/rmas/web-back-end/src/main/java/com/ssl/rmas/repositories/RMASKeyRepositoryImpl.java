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

import com.ssl.rmas.entities.RMASKey;
import com.ssl.rmas.entities.RMASKey.KeyType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class RMASKeyRepositoryImpl implements RMASKeyRepositoryCustom{

    private static final Logger LOGGER = LoggerFactory.getLogger(RMASKeyRepositoryImpl.class);

    @Autowired
    RMASKeyRepository rmasKeyRepository;

    public RMASKey findCurrentKey(KeyType keyType) throws IllegalStateException {
        List<RMASKey> rmasKeyList = rmasKeyRepository.findAllCurrentKeys(keyType);
        RMASKey currentKey = validateAndGetResult(rmasKeyList, keyType.toString());
        return currentKey;
    }

    public boolean checkLegalState() {
        return rmasKeyRepository.countAllCurrentKeys(RMASKey.KeyType.PRIVATE).size() == 1
                && rmasKeyRepository.countAllCurrentKeys(RMASKey.KeyType.PUBLIC).size() == 1;
    }

    private RMASKey validateAndGetResult(List<RMASKey> rmasKeyList, String keyType) throws IllegalStateException {
        int numberOfKeys = rmasKeyList.size();
        if(numberOfKeys == 1){
            RMASKey key = rmasKeyList.get(0);
            if(!StringUtils.isEmpty(key.getContent())){
                LOGGER.debug("Key returned: {}", key);
                return key;
            }else{
                String erroMessage = "Current "+ keyType+" key has an empty body";
                LOGGER.error(erroMessage);
                throw new IllegalStateException(erroMessage);
            }
        }else{
            String erroMessage = "No unique key found";
            LOGGER.error(erroMessage);
            throw new IllegalStateException(erroMessage);
        }
    }
}
