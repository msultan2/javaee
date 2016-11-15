/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.receiver.v2.entities;

import com.ssl.bluetruth.db.DBSetsParams;
import com.ssl.bluetruth.db.Db;
import com.ssl.bluetruth.receiver.v2.GenericParsable;
import com.ssl.bluetruth.receiver.v2.QueueManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.ssl.bluetruth.receiver.v2.misc.annotations.Variable;
import com.ssl.bluetruth.receiver.v2.seed.Seed;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author Liban Abdulkadir, svenkataramanappa
 */
@Component
@Scope("prototype")
public class StatusReport extends GenericParsable {

    private static final Logger logger = Logger.getLogger(StatusReport.class);
    public @Variable(index = 0)
    String id;
    public @Variable(index = 1)
    Date timestamp;
    private final int seedport = 50000;
    public ArrayList<String> commands = new ArrayList<>();

    public StatusReport() {
    }

    @Override
    public void onValidated() {
        update();
        if (varHas("ssh") && varGet("ssh").contains("open")) {
            String[] parts = varGet("ssh").split(" ");
            int remoteport = Integer.parseInt(parts[2]);

            // Generate New Seed
            if (remoteport == seedport) {
                Seed tmps = new Seed().setDetectorId(id).setPort(seedport);
                tmps.save();
                tmps.sendToOutstation();
                logger.info(id + ": seed file sent to outstation");
                Detector.getInstance(id).setSeed(0);
                commands.add("closeSSHConnection");
                commands.add("changeSeed");
            }
        }
        if (varHas("of")) {
            Detector.getInstance(id).setObfuscatingFunction(Integer.parseInt(varGet("of")));
        }
    }

    public void seedManage() {
        QueueManager qm = new QueueManager(id);
        if (varHas("seed") && Detector.getInstance(id).getSignReports()) {
            if (varGet("seed").equals("0")) {
                /*  Opening SSH connection on seed port, 
                 Generates new seed when connection is established */
                qm.enqueue("openSSHConnection:" + String.valueOf(seedport));
            } else {
                Detector.getInstance(id).setSeed(Integer.parseInt(varGet("seed")));
            }
        }
    }

    private static class StatusProperty {

        Class type;
        String name, value;

        public StatusProperty(String name, Class type) {
            this.name = name;
            this.type = type;
        }

        public static int set(PreparedStatement ps, StatusProperty[] sps, String detector_id) {
            int i;
            for (i = 1; i <= sps.length; i++) {
                try {
                    if (sps[i - 1].type.equals(Integer.class)) {
                        ps.setInt(i, Integer.parseInt(sps[i - 1].value));
                    } else {
                        ps.setString(i, sps[i - 1].value);
                    }
                } catch (SQLException ex) {
                }
            }
            return i - 1;
        }

        public static String buildUpdate(StatusProperty[] sps) {
            StringBuilder sb = new StringBuilder("UPDATE detector_status SET ");
            for (StatusProperty sp : sps) {
                sb.append(sp.name);
                sb.append(" = ?, ");
            }
            sb.append("timestamp = ? WHERE detector_id = ?");
            return sb.toString();
        }

        public static StatusProperty[] intersection(StatusProperty[] sps, HashMap<String, String> hm) {
            ArrayList<StatusProperty> sps2 = new ArrayList<>();
            for (StatusProperty sp : sps) {
                if (hm.containsKey(sp.name)) {
                    sp.value = hm.get(sp.name);
                    sps2.add(sp);
                }
            }
            sps = new StatusProperty[sps2.size()];
            return sps2.toArray(sps);
        }
    }
    StatusProperty[] properties = new StatusProperty[]{
        new StatusProperty("fv", String.class),
        new StatusProperty("sn", String.class),
        new StatusProperty("cv", String.class),
        new StatusProperty("sl_2g_min", Integer.class),
        new StatusProperty("sl_2g_avg", Integer.class),
        new StatusProperty("sl_2g_max", Integer.class),
        new StatusProperty("sl_3g_min", Integer.class),
        new StatusProperty("sl_3g_avg", Integer.class),
        new StatusProperty("sl_3g_max", Integer.class),
        new StatusProperty("of", Integer.class),
        new StatusProperty("ssh", String.class),
        new StatusProperty("seed", Integer.class),
        new StatusProperty("up", Integer.class),
        new StatusProperty("pi", Integer.class)
    };

    public void update() {
        if (vars.containsKey("sl")) {
            String sl = vars.remove("sl");
            String[] slarray = sl.split(":");
            vars.put("sl_2g_min", (slarray.length > 0) ? slarray[0] : "-255");  // default value is -255 
            vars.put("sl_2g_avg", (slarray.length > 1) ? slarray[1] : "-255");
            vars.put("sl_2g_max", (slarray.length > 2) ? slarray[2] : "-255");
            vars.put("sl_3g_min", (slarray.length > 3) ? slarray[3] : "-255");
            vars.put("sl_3g_avg", (slarray.length > 4) ? slarray[4] : "-255");
            vars.put("sl_3g_max", (slarray.length > 5) ? slarray[5] : "-255");
        }

        final StatusProperty[] sps = StatusProperty.intersection(properties, vars);

        new Db().sql(StatusProperty.buildUpdate(sps)).set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                ps.setString(1, id + "");
                int set_i = StatusProperty.set(ps, sps, id);
                ps.setTimestamp(++set_i, new Timestamp(timestamp.getTime()));
                ps.setString(++set_i, id);

            }
        }).go().close();
    }

    public enum Statuses {
        OK("OK"),
        FAULTY("FAULTY"),
        UNCOMMISSIONED("UNCOMMISSIONED");

        private String status;

        private Statuses(String s) {
            status = s;
        }

        public String getStatus() {
            return status;
        }
    }
}
