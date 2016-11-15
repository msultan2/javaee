/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.datarequest;


/**
 *
 * @author williammasek
 */
public class DataRequestException extends Exception {
    private static final long serialVersionUID = 4369749300702146706L;

    public DataRequestException(String message) {
        super(message);
    }

    public DataRequestException(String message, Throwable cause) {
        super(message,cause);
    }
}
