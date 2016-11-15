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
package com.ssl.rmas.test.rmas.utils;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

/**
 * Mongo Database Utility class
 */
@Component
public class MongoUtils {
    private String dbName;

    private final String[] POSSIBLE_MONGO_PATHS = new String[]{
            "/usr/bin/mongo",
            "/usr/local/bin/mongo",
            "/bin/mongo"
    };

    public MongoUtils() {
        dbName = System.getProperty("test.database.name", "rmas");
    }

    public String getDBName() {
        return dbName;
    }

    private String runMongo(String... mongoArgs) throws IOException, InterruptedException {
        Optional<String> mongoPath;
        String retval = "";

        mongoPath = Arrays.asList(POSSIBLE_MONGO_PATHS).stream().filter(possiblePath -> {
            return new File(possiblePath).exists();
        }).findFirst();

        List<String> mongoCommand = new ArrayList<>();
        mongoCommand.add(0, mongoPath.orElse("mongo"));
        mongoCommand.add(1, dbName);
        mongoCommand.addAll(Arrays.asList(mongoArgs));
        Process process = new ProcessBuilder(mongoCommand).start();
        int result = process.waitFor();
        if(result!=0) {
            fail("Failed to run the mongo script: \n" + IOUtils.toString(process.getInputStream()) + "\n" + IOUtils.toString(process.getErrorStream()));
        } else {
            retval = IOUtils.toString(process.getInputStream());
        }
        return retval;
    }

    public String getCollections() {
        String retval = "";
        try {
            retval = runMongo("--quiet", "--eval", "printjson(db.getCollectionNames())");
        } catch (IOException | InterruptedException mex) {
            fail("unable to run mongo query: IOException: "+mex.getLocalizedMessage());
        }
        return retval;
    }

    public void runScript(String scriptPath) {
        try {
            if (new File(scriptPath).exists()) {
                runMongo(scriptPath);
            } else {
                fail("unable to run mongo script: "+ scriptPath + " does not exist relative to " + System.getProperty("user.dir"));
            }
        } catch (IOException | InterruptedException mex) {
            fail("unable to run mongo script: "+scriptPath+" IOException: "+mex.getLocalizedMessage());
        }
    }

}
