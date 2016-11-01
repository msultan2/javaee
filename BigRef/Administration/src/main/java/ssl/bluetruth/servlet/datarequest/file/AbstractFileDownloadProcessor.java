/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.servlet.datarequest.file;

import java.io.InputStream;
import java.util.Map;
import ssl.bluetruth.servlet.datarequest.DataRequestException;
import ssl.bluetruth.servlet.response.DataResponse;

/**
 * @author ipacker
 */
public abstract class AbstractFileDownloadProcessor {

    
    public DataResponse processRequest() throws DataRequestException {
        DataResponse response = new DataResponse();

        response.setResponseData(getData());

        response.getResponseHeaders().put("Content-Disposition", "attachment; filename=" + getOutputFilename());

        long length = getLength();

        if (length >= 0) {
            response.getResponseHeaders().put("Content-Length", String.valueOf(length));
        }

        addAdditionalHeaders(response.getResponseHeaders());

        return response;
    }
    

    protected abstract InputStream getData() throws DataRequestException;

    protected abstract long getLength() throws DataRequestException;
    
    protected abstract String getOutputFilename() throws DataRequestException;

    protected abstract void addAdditionalHeaders(Map<String, String> headers) throws DataRequestException;
}

