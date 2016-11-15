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
import ssl.bluetruth.database.DatabaseManager;

public class MessageReceiver extends AbstractMessageReceiver {

    private final String[] timeZones;

    public MessageReceiver() {
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
        Pattern p = Pattern.compile("^.*Message/(.+)$");
        Matcher m = p.matcher(request.getRequestURI());
        if (m.matches()) {
            String timeZone = m.group(1);
            if (Arrays.binarySearch(timeZones, timeZone) >= 0) {
                request.setAttribute("timezone", timeZone);
            }
        }

        String detectorId = UNKNOWN;
        try {
            detectorId = getDetectorIdFromHTTPRequest(request);
            if (getMessageContextFromHTTPRequest(request).equals(SYSTEM_MESSAGE_CONTEXT)) {
                DatabaseManager dm = DatabaseManager.getInstance();
                Timestamp timestamp = getTimestampFromHTTPRequest(request);
                String[] codeCountPairs = getMessageCodeCountPairsFromHTTPRequest(request).split(CODE_COUNT_PAIR_DELIMITER);
                for (int i = 0; i < codeCountPairs.length; i++) {
                    String[] codeCount = codeCountPairs[i].split(CODE_COUNT_DELIMITER);
                    String code = codeCount[0];
                    String count = codeCount[1];
                    Message message = Message.getByCode(code);
                    if(message == null){
                        insertMessage(dm.getDatasource().getConnection(), 
                                detectorId, timestamp, 
                                code, Integer.parseInt(count),
                                UNKNOWN, 
                                UNKNOWN_DESCRIPTION,
                                UNKNOWN_DESCRIPTION_DETAIL);
                    } else {
                        insertMessage(dm.getDatasource().getConnection(), 
                                detectorId, timestamp,
                                code, Integer.parseInt(count),
                                message.getCategory().name(),
                                message.getDescription(),
                                message.getDescriptionDetail());
                    }
                }
            }
        } catch (ParseException ex) {
            LOGGER.warn("Illegal Argument: " + ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (NumberFormatException ex) {
            LOGGER.warn("Number format exception: " + ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();

            LOGGER.warn("An exception occurred while processing message data received from "
                    + request.getRemoteAddr() + " DETECTOR ID:" + detectorId, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Timestamp getTimestampFromHTTPRequest(HttpServletRequest request) throws ParseException {

        String parameterDate = request.getParameter(PARAMETER_DATE);
        Long time = null;

        String timeZone = (String) request.getAttribute("timezone");

        time = convertTimestampToMillis(parameterDate, Constants.DATE_FORMAT, timeZone);

        return new Timestamp(time);
    }
}
