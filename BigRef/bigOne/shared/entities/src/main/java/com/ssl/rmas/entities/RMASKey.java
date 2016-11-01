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
package com.ssl.rmas.entities;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="rmasKey")
public class RMASKey {

    public enum KeyType {
        PRIVATE,
        PUBLIC
    }

    @Id
    private String id;
    private KeyType type;
    private String algorithm;
    private String content;
    private Instant generatedTimestamp;
    private Optional<Instant> expiredTimestamp;

    @PersistenceConstructor
    public RMASKey(String id, KeyType type, String algorithm, String content, Instant generatedTimestamp, Optional<Instant> expiredTimestamp) {
        this.id = id;
        this.type = type;
        this.algorithm = algorithm;
        this.content = content;
        this.generatedTimestamp = generatedTimestamp;
        this.expiredTimestamp = expiredTimestamp;
    }

    @PersistenceConstructor
    public RMASKey(KeyType type, String algorithm, String content, Instant generatedTimestamp, Optional<Instant> expiredTimestamp) {
        this.type = type;
        this.algorithm = algorithm;
        this.content = content;
        this.generatedTimestamp = generatedTimestamp;
        this.expiredTimestamp = expiredTimestamp;
    }

    public String getId() {
        return id;
    }
    public KeyType getType() {
        return type;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public String getContent() {
        return content;
    }

    public Optional<Instant> getExpiredTimestamp() {
        return expiredTimestamp;
    }

    public Instant getGeneratedTimestamp() {
        return generatedTimestamp;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.id);
        hash = 71 * hash + Objects.hashCode(this.type);
        hash = 71 * hash + Objects.hashCode(this.algorithm);
        hash = 71 * hash + Objects.hashCode(this.content);
        hash = 71 * hash + Objects.hashCode(this.generatedTimestamp);
        hash = 71 * hash + Objects.hashCode(this.expiredTimestamp);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RMASKey other = (RMASKey) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.algorithm, other.algorithm)) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        if (!Objects.equals(this.generatedTimestamp, other.generatedTimestamp)) {
            return false;
        }
        if (!Objects.equals(this.expiredTimestamp, other.expiredTimestamp)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RMASKey{" + "id=" + id + ", type=" + type + ", algorithm=" + algorithm + ", content(hash)=" + Objects.hashCode(content) + ", generatedTimestamp=" + generatedTimestamp + ", expiredTimestamp=" + expiredTimestamp + '}';
    }
}
