/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.receiver.v2.entities;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.DBSetsParams;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.GenericParsable;
import fj.F;
import java.util.Date;
import com.ssl.bluetruth.receiver.v2.misc.annotations.Variable;
import java.util.ArrayList;
import static fj.data.Array.array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 *
 * @author Liban Abdulkadir
 */
@Component
@Scope("prototype")
public class FaultReport extends GenericParsable {

    public @Variable(index = 0)
    String id;
    public @Variable(index = 1)
    Date reportStart;
  
    public ArrayList<Fault> faults;
    private String report_table = "fault_report", fault_table = "fault_message";

    public FaultReport() {
    }

    public FaultReport reportTable(String table) {
        this.report_table = table;
        return this;
    }

    public FaultReport faultTable(String table) {
        this.fault_table = table;
        return this;
    }

    private ArrayList<Fault> parseFaults(String data) {
        String[] ss = data.split(",");
        return new ArrayList(array(ss).toList().drop(2).take(ss.length - 3)
                .map(new F<String, Fault>() {
            @Override
            public Fault f(String a) {
                return Fault.deserialise(a);
            }
        }).toCollection());
    }

    @Override
    public void postParse() {
        faults = parseFaults(data);
    }

    public void insertFaults(final int report_id) {
        StringBuilder sb = new StringBuilder("INSERT INTO %s(report_id,code,time,status) VALUES");
        for (int i = 0; i < faults.size(); i++) {
            sb.append("(?,?,?,?),");
        }
        String statement = String.format(sb.toString().substring(0, sb.length() - 1), fault_table);
        new Db().sql(statement).set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                int i = 0;
                for (Fault f : faults) {
                    ps.setInt(++i, report_id);
                    ps.setInt(++i, f.fn);
                    ps.setTimestamp(++i, new Timestamp(f.time.getTime()));
                    ps.setInt(++i, f.status);
                }
            }
        }).go().close();
    }

    public int insertReport() {
        return (Integer) (new Db().sql(String.format("INSERT INTO %s (detector_id,time)"
                + "VALUES (?,?) RETURNING report_id", report_table))
                .set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                ps.setString(1, id);
                ps.setTimestamp(2, new Timestamp(reportStart.getTime()));
            }
        })
                .go(new DBHasResult<Integer>() {
            @Override
            public Integer done(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getInt("report_id");
            }
        }).close().response());
    }

    public void insert() {
        int report_id = insertReport();
        insertFaults(report_id);
    }
    
    @Override
    public void onValidated() {
        insert();
    }
}
