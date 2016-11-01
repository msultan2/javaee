/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.response;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import ssl.bluetruth.common.Response;

/**
 *
 * @author edt
 */
public class DataResponse implements Response {

    private final Map<String, String> responseHeaders;

    private InputStream responseData;

    public DataResponse() {
        responseHeaders = new HashMap<String, String>();
    }

    public InputStream getResponseData() {
        return responseData;
    }

    public void setResponseData(InputStream responseData) {
        this.responseData = responseData;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
}
