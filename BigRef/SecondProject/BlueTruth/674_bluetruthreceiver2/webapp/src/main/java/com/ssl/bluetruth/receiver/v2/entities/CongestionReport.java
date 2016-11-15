/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.receiver.v2.entities;

import com.ssl.bluetruth.db.DBSetsParams;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.GenericParsable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import com.ssl.bluetruth.receiver.v2.misc.annotations.Hex;
import com.ssl.bluetruth.receiver.v2.misc.annotations.Variable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 *
 * @author liban
 */
@Component
@Scope("prototype")
public class CongestionReport extends GenericParsable {

    private String table = "occupancy";
    public @Variable(index = 0)
    String detector_id;
    public @Variable(index = 1)
    Date inquiryStart;
    public @Variable(index = 2)
    String bins;
    public @Hex @Variable(index = 3)
    int queuePresent;
    
    // Second round of parsing
    public int staticFlowCount, verySlowFlowCount, slowFlowCount,moderateFlowCount,freeFlowCount;
    private static final String SQL_INSERT_INTO_OCCUPANCY = "INSERT INTO %s (detector_id, reported_timestamp, stationary, very_slow, slow, moderate, free, queue_present) VALUES (?,?,?,?,?,?,?,?);";

    public void table(String table) {
        this.table = table;
    }

    public CongestionReport() {
    }
    
    
    @Override
    public void postParse() {
        String[] bins_parts = bins.split(":");
        freeFlowCount = Integer.parseInt(bins_parts[0]);
        moderateFlowCount = Integer.parseInt(bins_parts[1]);
        slowFlowCount = Integer.parseInt(bins_parts[2]);
        verySlowFlowCount = Integer.parseInt(bins_parts[3]);
        staticFlowCount = Integer.parseInt(bins_parts[4]);
    }

    public void insert() {
        new Db().sql(String.format(SQL_INSERT_INTO_OCCUPANCY, table)).set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                ps.setString(1, detector_id);
                ps.setTimestamp(2, new Timestamp(inquiryStart.getTime()));
                ps.setInt(3, staticFlowCount);
                ps.setInt(4, verySlowFlowCount);
                ps.setInt(5, slowFlowCount);
                ps.setInt(6, moderateFlowCount);
                ps.setInt(7, freeFlowCount);
                ps.setInt(8, queuePresent);
            }
        }).go().close();
    }
    
    @Override
    public void onValidated() {
        insert();
    }
}
