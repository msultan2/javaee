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
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Java version: JDK 1.8
 *
 * Created By: Liban Abdulkadir
 *
 * Product: 674 - BlueTruthReceiver2
 */
package com.ssl.bluetruth.receiver.v2.entities;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.DBSetsParams;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.seed.Seed;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Liban Abdulkadir
 */
public class Detector {

    private static final Logger logger = LogManager.getLogger(Detector.class);
    
    private static HashMap<String, Detector> detectors = new HashMap<>();

    public static Detector getInstance(String id) {
        if (detectors.containsKey(id)) {
            return detectors.get(id);
        } else {
            detectors.put(id, new Detector(id));
            return detectors.get(id);
        }
    }
    public String id;

    public Detector(String id) {
        this.id = id;
    }
    private Seed seed = new Seed().setId(0);

    public Seed getSeed() {
        return seed;
    }

    /* called from status report when outstation confirms seed id */
    public void setSeed(int seed_id) {
        seed = new Seed().setId(seed_id);
        if (seed_id != 0) {
            logger.info(id + ": seed=" + seed_id);
            seed.load();
        } else {
            try {
                seed.setSeed(0);
            } catch (Exception e) {
            }
        }
    }

    public void setSeed(Seed seed) {
        this.seed = seed;
    }

    public void newSeed() {
        seed = new Seed().setDetectorId(id);
        seed.save();
    }

    public boolean getSignReports() {
        Boolean b = (Boolean) (new Db().sql("SELECT \"signReports\" FROM detector_configuration WHERE detector_id = ?")
                .set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                ps.setString(1, id);
            }
        }).go(new DBHasResult<Boolean>() {
            @Override
            public Boolean done(ResultSet rs) throws SQLException {
                Boolean bb = rs.next() && rs.getBoolean(1);
                return bb;
            }
        }).close().response());
        return b;
    }

    public Detector() {
    }
    
    private int obfuscatingFunction;
    
    public void setObfuscatingFunction(int of) {
        obfuscatingFunction = of;
    }
    
    public int getObfuscatingFunction() {
        return this.obfuscatingFunction;
    }

    public Detector id(String id) {
        this.id = id;
        return this;
    }
}
