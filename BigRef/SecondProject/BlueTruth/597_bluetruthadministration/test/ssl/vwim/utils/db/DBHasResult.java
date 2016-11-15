/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.vwim.utils.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author liban
 */
public interface DBHasResult<T> {
    public T done(ResultSet rs) throws SQLException;
}
