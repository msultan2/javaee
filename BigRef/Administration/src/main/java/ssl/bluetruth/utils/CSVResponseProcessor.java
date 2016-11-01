/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import ssl.bluetruth.servlet.response.DataResponse;

/**
 *
 * @author wingc
 */
public class CSVResponseProcessor extends AbstractResponseProcessor {

    @Override
    public void createResponse(Object responseObject, Object output) throws IOException, NullPointerException {
        if (output == null) {
            throw new NullPointerException("Output must not be null");
        }

        if ((responseObject instanceof DataResponse) && (output instanceof HttpServletResponse)) {
            DataResponse responseData = (DataResponse) responseObject;
            HttpServletResponse response = (HttpServletResponse)output;

            // set response header
            Map<String, String> responseHeaders = responseData.getResponseHeaders();
            for (Entry<String, String> entry : responseHeaders.entrySet()) {
                response.setHeader(entry.getKey(), entry.getValue());
            }

            // transmit response data
            transmitResponse(responseData.getResponseData(), response.getOutputStream());
            
        } else {
            throw new NullPointerException("Output must be HttpServletResponse and responseObject must be DataResponse");
        }
    }

    private void transmitResponse(InputStream responseInputStream, ServletOutputStream output) throws IOException {
        // get the response data and write to file
        InputStream responseData = responseInputStream;
        byte[] buf = new byte[1024];
        int bytesRead;
        while ((bytesRead = responseData.read(buf)) > 0) {
            output.write(buf, 0, bytesRead);
        }
    }

}

