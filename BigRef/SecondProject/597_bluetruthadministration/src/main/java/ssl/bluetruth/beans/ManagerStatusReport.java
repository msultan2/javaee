package ssl.bluetruth.beans;

import com.ssl.bluetruth.db.DBHasResult;
import com.ssl.bluetruth.db.DBSetsParams;
import com.ssl.bluetruth.db.Db;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ManagerStatusReport {

    private boolean ssh_open;
    private int seed;

    public boolean getSshOpen() {
        return ssh_open;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public void setSshOpen(boolean b) {
        ssh_open = b;
    }

    public ManagerStatusReport(final String id) {
        new Db().sql("SELECT detector_seed.seed AS seed, (CASE WHEN "
                + "detector_status.ssh IN ('N/A','closed') THEN true "
                + "ELSE false END) as seed FROM detector_status JOIN detector_seed "
                + "ON detector_seed.id = detector_status.seed "
                + "AND detector_status.detector_id = ?").set(new DBSetsParams() {
            @Override
            public void set(PreparedStatement ps) throws SQLException {
                ps.setString(1, id);
            }
        }).go(new DBHasResult() {
            @Override
            public Object done(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    seed = rs.getInt(1);
                    ssh_open = rs.getBoolean(2);
                }
                return null;
            }
        }).close();
    }
}