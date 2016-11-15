/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.utils;

import java.io.IOException;

/**
 *
 * @author wingc
 */
public abstract class AbstractResponseProcessor {

    public AbstractResponseProcessor() {
        
    }

    public abstract void createResponse(Object responseObject, Object output) throws IOException, NullPointerException;
        

}

