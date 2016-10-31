/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.vwim.utils.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author liban
 */
public interface DBSetsParams {
    void set(PreparedStatement ps) throws SQLException;
}
