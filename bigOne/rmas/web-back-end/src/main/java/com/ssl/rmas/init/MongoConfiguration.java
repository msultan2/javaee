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
package com.ssl.rmas.init;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.WriteConcern;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * Responsible for initialising Mongo Database. All mongo base repositories will
 * be loaded.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfiguration extends AbstractMongoConfiguration {

    @Autowired
    private MongoProperties properties;

    @Autowired(required = false)
    private MongoClientOptions options;

    @Autowired
    private Environment environment;

    @Override
    protected String getDatabaseName() {
        return properties.getDatabase();
    }

    @Autowired
    private MappingMongoConverter converter;

    @Bean
    @Override
    public Mongo mongo() throws Exception {
        MongoClient client = properties.createMongoClient(options, environment);
        client.setWriteConcern(WriteConcern.MAJORITY);
        return client;
    }

    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(new Object[]{new OptionalConverter()}));
    }

    private class OptionalConverter implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            Set<ConvertiblePair> convertibleTypes = new HashSet<>();
            convertibleTypes.add(new ConvertiblePair(Optional.class, Object.class));
            convertibleTypes.add(new ConvertiblePair(Object.class, Optional.class));
            return convertibleTypes;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (Optional.class.equals(sourceType.getObjectType())) {
                return convertToMongoType(source);
            } else if (Optional.class.equals(targetType.getObjectType())) {
                return Optional.ofNullable(source);
            }
            throw new IllegalArgumentException("Neither the source or the target was a Optional");
        }

        private Object convertToMongoType(Object source) {
            Optional<?> optionalSource = (Optional<?>) source;
            Object value = null;
            if (optionalSource.isPresent()) {
                value = converter.convertToMongoType(optionalSource.get());
            }
            return value;
        }
    }
}
