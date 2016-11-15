package com.ssl.bluetruth.servlet.receiver;

import java.sql.Timestamp;
import java.text.ParseException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author nthompson
 */
public class OccupancyReceiver extends AbstractOccupancyReceiver {
    private static final String PARAMETER_DATE = "t";
    
    public Timestamp getTimestampFromHTTPRequest(HttpServletRequest request) throws ParseException {
        String timestamp = request.getParameter(PARAMETER_DATE);
        long timeInMillis = AbstractDeviceDetectionReceiver.convertTimestampToMillis(timestamp, Constants.DATE_FORMAT, Constants.TIMEZONE_UTC);
        return new Timestamp(timeInMillis);
    }
}
