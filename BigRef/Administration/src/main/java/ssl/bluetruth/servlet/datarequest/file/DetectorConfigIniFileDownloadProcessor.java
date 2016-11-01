package ssl.bluetruth.servlet.datarequest.file;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.servlet.datarequest.DataRequestException;
import ssl.bluetruth.servlet.receiver.AbstractUnconfiguredDetector;

public class DetectorConfigIniFileDownloadProcessor extends AbstractFileDownloadProcessor {

    private static final Logger LOGGER = LogManager.getLogger(DetectorConfigIniFileDownloadProcessor.class);
    private static final Pattern VALID_VERSION_STRING_PATTERN = Pattern.compile("^[0-9]{1,64}_[0-9]{2}$");
    private static final String SQL_RETRIEVE_CONFIGURATION_VIEW = "SELECT * FROM configuration_view_version_%s WHERE \"outstationID\" = ?";
    private static final String UPDATE_LAST_CONFIGURATION_DOWNLOAD_REQUEST_STATISTIC = "UPDATE detector_statistic "
            + "SET last_configuration_download_request_timestamp = NOW(), "
            + "last_configuration_download_version = ? "
            + "WHERE detector_id = ?;";
    private byte[] configurationData = new byte[]{};
    private final String detectorId;

    public DetectorConfigIniFileDownloadProcessor(String view, String detectorId) throws IllegalArgumentException {
        this.detectorId = detectorId;

        Matcher patternMatcher = VALID_VERSION_STRING_PATTERN.matcher(view);

        if (!patternMatcher.matches()) {
            throw new IllegalArgumentException("Invalid configuration version specified");
        }

        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            StringBuilder sb = new StringBuilder();
            try {

                connection = dm.getDatasource().getConnection();

                final String RETRIEVE_CONFIGURATION_VIEW_STATEMENT = String.format(SQL_RETRIEVE_CONFIGURATION_VIEW,
                        view);

                stmt = connection.prepareStatement(RETRIEVE_CONFIGURATION_VIEW_STATEMENT);
                stmt.setString(1, detectorId);

                rs = stmt.executeQuery();

                if (rs.next()) {
                    int columnCount = rs.getMetaData().getColumnCount();
                    for (int column = 1; column <= columnCount; column++) {
                        String columnName = rs.getMetaData().getColumnName(column);
                        if (!columnName.startsWith("_")) {
                            String columnValue = rs.getString(column);
                            sb.append(columnName).append("=").append(columnValue).append("\r\n");
                        }
                    }
                } else {
                    // if it is not found, it means the detector could be not configured
                    AbstractUnconfiguredDetector unconfiguredDetector = new AbstractUnconfiguredDetector();
                    if (unconfiguredDetector.checkDetectorConfiguredInDatabase(detectorId)) {
                        // log an exception if it is configured in database but throw an SQL exception
                        LOGGER.warn("An exception occurred while fetching the requested configuration version data with DETECTOR ID:" + detectorId);
                    } else {
                        // insert/update unconfigured detector
                        unconfiguredDetector.insertUnconfiguredDetector(detectorId, AbstractUnconfiguredDetector.UnconfiguredType.CONFIG_DOWNLOAD_REQUEST);
                    }
                    throw new IllegalArgumentException("No data found for the specified detector ID:"+detectorId);
                }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                    rs = null;
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                    }
                    stmt = null;
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                    }
                    connection = null;
                }
            }

            updateLastConfigurationDownloadRequestStatistic(detectorId, view);

            configurationData = sb.toString().getBytes("UTF-8");
        } catch (Exception ex) {
            LOGGER.warn("An exception occurred while fetching the requested configuration version data", ex);
            throw new UnsupportedOperationException(ex.getMessage());
        }
    }

    @Override
    protected InputStream getData() throws DataRequestException {
        return new ByteArrayInputStream(configurationData);
    }

    @Override
    protected void addAdditionalHeaders(Map<String, String> headers) throws DataRequestException {
        headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    @Override
    protected long getLength() throws DataRequestException {
        return configurationData.length;
    }

    @Override
    protected String getOutputFilename() throws DataRequestException {
        return detectorId + "_ini.txt";
    }

    private void updateLastConfigurationDownloadRequestStatistic(String detectorId, String view)
            throws DatabaseManagerException, SQLException, NamingException {
        DatabaseManager dm = DatabaseManager.getInstance();
        Connection connection = null;
        PreparedStatement stmt = null;
        try {

            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(UPDATE_LAST_CONFIGURATION_DOWNLOAD_REQUEST_STATISTIC);
            stmt.setString(1, view);
            stmt.setString(2, detectorId);
            stmt.executeUpdate();

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }
}
