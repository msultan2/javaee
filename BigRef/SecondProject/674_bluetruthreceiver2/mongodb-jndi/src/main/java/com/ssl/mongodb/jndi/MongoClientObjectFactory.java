/*
 * MongoClientObjectFactory.java
 * 
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
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Java version: JDK 1.7
 *
 * Created on 03-Jul-2015 09:42 AM
 * 
 */
package com.ssl.mongodb.jndi;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * A Mongo Client Factory that can be used to create Mongo client instances
 *
 * @author svenkataramanappa
 */
public class MongoClientObjectFactory implements ObjectFactory {

    public static final String PARAMETER_MONGO_DB = "db";
    public static final String PARAMETER_MONGO_HOST = "host";
    public static final String PARAMETER_MONGO_PORT = "port";
    public static final String PARAMETER_MONGO_USERNAME = "username";
    public static final String PARAMETER_MONGO_PASSWORD = "password";
    public static final String PARAMETER_MONGO_CONNECTIONS_PER_HOST = "connectionsPerHost";

    private char[] passwordChar;
    private ServerAddress serverAddress;
    private MongoCredential credential;

    @Override
    public Object getObjectInstance(Object obj,
                                    Name name,
                                    Context nameCtx,
                                    Hashtable<?, ?> environment) throws IllegalArgumentException {

        validateReferenceProperty(obj, "Null/ Invalid JNDI object reference");

        String db = null;
        String host = null;
        String username = null;
        String password = null;
        int port = 27017;

        Reference ref = (Reference) obj;
        Enumeration<RefAddr> props = ref.getAll();
        while (props.hasMoreElements()) {
            RefAddr addr = (RefAddr) props.nextElement();
            String propName = addr.getType();
            String propValue = (String) addr.getContent();
            switch (propName) {
                case PARAMETER_MONGO_DB:
                    db = propValue;
                    break;
                case PARAMETER_MONGO_HOST:
                    host = propValue;
                    break;
                case PARAMETER_MONGO_USERNAME:
                    username = propValue;
                    break;
                case PARAMETER_MONGO_PASSWORD:
                    password = propValue;
                    break;
                case PARAMETER_MONGO_PORT:
                    try {
                        port = Integer.parseInt(propValue);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid port value " + propValue);
                    }
                    break;
            }
        }

        //VALIDATE PROPERTIES
        validateProperty(db, "Invalid or empty mongo database name");
        validateProperty(host, "Invalid or empty mongo host");
        validateProperty(username, "Invalid or empty mongo username");
        validateProperty(password, "Invalid or empty mongo password");

        passwordChar = password.toCharArray();
        serverAddress = new ServerAddress(host, port);
        credential = MongoCredential.createMongoCRCredential(username, db, passwordChar);

        return this;
    }

    /**
     * Validate internal String properties
     *
     * @param property
     * @param errorMessage
     * @throws IllegalArgumentException
     */
    private void validateProperty(String property, String errorMessage)
            throws IllegalArgumentException {
        if (property == null || property.trim().equals("")) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Validate internal Object properties
     *
     * @param property
     * @param errorMessage
     * @throws IllegalArgumentException
     */
    private void validateReferenceProperty(Object property, String errorMessage)
            throws IllegalArgumentException {
        if (property == null || !(property instanceof Reference)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Create and return Mongo Client
     *
     * @return MongoClient
     */
    public MongoClient getMongoClient() {
        return new MongoClient(serverAddress, Arrays.asList(credential));
    }
}
