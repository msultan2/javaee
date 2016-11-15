/**
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SIMULATION SYSTEMS LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF TH
 * IS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2015 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 * Created on 07-May-2015 08:29 AM
 */
package ssl.bluetruth.emitter2converter.configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import static ssl.bluetruth.emitter2converter.configuration.ConfigurationManager.*;
import ssl.bluetruth.emitter2converter.exceptions.DataBaseException;
import ssl.bluetruth.emitter2converter.exceptions.InvalidConfigurationException;

/**
 * This class is used in ConfigurationManagerImpl to implement the ConfigurationManager interface. 
 * It only has one method that creates a new map with the values in the database each time needed.
 * It's up to ConfigurationManagerImpl to call this method in the correct situations
 * Not all the values from the database are included in the map, only the ones that are been used in this project. 
 * Each new value should be added in mapOf(ResultSet resultSet).
 * @author josetrujillo-brenes
 */
public class OutStationsConfigurationFromDataBase implements OutStationsConfiguration {
    
    private static final String SQL_SELECT_CONFIGURATION = "SELECT * FROM configuration_view_version_2_00 WHERE \"outstationID\"=?";
    private final Logger logger = LogManager.getLogger(getClass());
               
    public OutStationsConfigurationFromDataBase() {
    }
        
    @Override
    public Map<String, String> getMap(String idOutStation) throws InvalidConfigurationException {
       return getMapFromDataBase(idOutStation); 
    }
    
        private Map<String, String> getMapFromDataBase(String idOutStation) throws InvalidConfigurationException {
        try (Connection connection = DatabaseManager.getInstance().getDatasource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_CONFIGURATION)) {
                preparedStatement.setString(1, idOutStation);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapOf(resultSet);
                    } else {
                        String message = "There where no results for OutStation '" + idOutStation + "'";
                        throw new InvalidConfigurationException(message);
                    }
                }
            }
        } catch (SQLException | NamingException | DatabaseManagerException | DataBaseException | RuntimeException mex) {
            String message = "Fail to load the configuration from database. Cause: " + mex.getLocalizedMessage();
            logger.error(message, mex);
            throw new InvalidConfigurationException(message, mex);
        }
    }
    
    private Map<String, String> mapOf(ResultSet resultSet) throws DataBaseException {
        Map<String, String> newMap = new HashMap();
        try {         
            newMap.put(MODE, resultSet.getString(MODE));
            newMap.put(SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES, resultSet.getString(SETTINGS_COLLECTION_INTERVAL_FOR_MODES_1_2_3_IN_MINUTES));
            newMap.put(SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES, resultSet.getString(SETTINGS_COLLECTION_INTERVAL_FOR_MODE_0_IN_MINUTES));
            newMap.put(URL_CONGESTION_REPORTS, resultSet.getString(URL_CONGESTION_REPORTS));
            newMap.put(URL_STATISTICS_REPORTS, resultSet.getString(URL_STATISTICS_REPORTS));
            newMap.put(URL_STATUS_REPORTS, resultSet.getString(URL_STATUS_REPORTS));
            newMap.put(URL_FAULT_REPORTS, resultSet.getString(URL_FAULT_REPORTS));
            newMap.put(STATISTICS_REPORT_PERIOD_IN_SECONDS, resultSet.getString(STATISTICS_REPORT_PERIOD_IN_SECONDS));
            newMap.put(CONGESTION_REPORT_PERIOD_IN_SECONDS, resultSet.getString(CONGESTION_REPORT_PERIOD_IN_SECONDS));
            newMap.put(STATUS_REPORT_PERIOD_IN_SECONDS, resultSet.getString(STATUS_REPORT_PERIOD_IN_SECONDS));
            newMap.put(FREE_FLOW_BIN_THRESHOLD_IN_SECONDS, resultSet.getString(FREE_FLOW_BIN_THRESHOLD_IN_SECONDS));
            newMap.put(MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS, resultSet.getString(MODERATE_FLOW_BIN_THRESHOLD_IN_SECONDS));
            newMap.put(SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, resultSet.getString(SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS));
            newMap.put(VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS, resultSet.getString(VERY_SLOW_FLOW_BIN_THRESHOLD_IN_SECONDS));
            newMap.put(ABSENCE_THREHOLD_IN_SECONDS, resultSet.getString(ABSENCE_THREHOLD_IN_SECONDS));
            newMap.put(BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS, resultSet.getString(BACKGROUND_LATCHTIME_THREHOLD_IN_SECONDS));
            newMap.put(BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS, resultSet.getString(BACKGROUND_CLEARANCETIME_THREHOLD_IN_SECONDS));
            newMap.put(EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS, resultSet.getString(EXPECTED_DEVICE_DETECTION_PERIOD_IN_SECONDS));
            newMap.put(QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS, resultSet.getString(QUEUE_DETECTIONS_STARUP_INTERVAL_IN_SECONDS));
            newMap.put(QUEUE_ALERT_THREHOLD_BIN, resultSet.getString(QUEUE_ALERT_THREHOLD_BIN));
            newMap.put(QUEUE_DETECT_THREHOLD, resultSet.getString(QUEUE_DETECT_THREHOLD));
            newMap.put(QUEUE_CLEARANCE_THREHOLD, resultSet.getString(QUEUE_CLEARANCE_THREHOLD));
            newMap.put(TIMESTAMP_TOLERANCE_MS, resultSet.getString(TIMESTAMP_TOLERANCE_MS));
        } catch (SQLException sqlex) {
            String message = "Fail to create the configuration map. Cause: "+sqlex.getLocalizedMessage();
            logger.error(message, sqlex);
            throw new DataBaseException(message, sqlex);
        }         
        return newMap;
    }
}
