/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.common;

/**
 *
 * @author pwood
 */
public class BlueTruthException extends Throwable {
    public BlueTruthException(Exception ex) {
        super(ex);
    }

    public BlueTruthException(String message) {
        super(message);
    }
}
