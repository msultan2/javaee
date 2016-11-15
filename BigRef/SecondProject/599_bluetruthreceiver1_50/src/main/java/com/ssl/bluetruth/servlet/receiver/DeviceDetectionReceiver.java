package com.ssl.bluetruth.servlet.receiver;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author nthompson
 */
public class DeviceDetectionReceiver extends AbstractDeviceDetectionReceiver {

    private final String[] timeZones;

    public DeviceDetectionReceiver() {
        timeZones = TimeZone.getAvailableIDs();
        Arrays.sort(timeZones);
    }

    public Long parseDateString(String parameterDate) throws ParseException {
        //Not used - see below methods
        return 0L;
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setAttribute("timezone", Constants.TIMEZONE_UTC);

        Pattern p = Pattern.compile("^.*DeviceDetection/(.+)$");
        Matcher m = p.matcher(request.getRequestURI());
        if (m.matches()) {
            String timeZone = m.group(1);
            if (Arrays.binarySearch(timeZones, timeZone) >= 0) {
                request.setAttribute("timezone", timeZone);
            }
        }
        super.processRequest(request, response);
    }

    @Override
    public Timestamp getTimestampFromHTTPRequest(HttpServletRequest request) {
        try {
            String parameterDate = request.getParameter(PARAMETER_DATE);
            Long time = null;
            String timeZone = (String) request.getAttribute("timezone");
            time = convertTimestampToMillis(parameterDate, Constants.DATE_FORMAT, timeZone);
            return new Timestamp(time);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Bad!");
        }
    }
}
