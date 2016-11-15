/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart;

import ssl.bluetruth.chart.common.Period;
import java.awt.Color;
import ssl.bluetruth.chart.common.Rectangle;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author svenkataramanappa
 */
public class DetectorPerformanceIndexChart extends Chart {
    private static final String DETECTOR_PI_SQL
            =            "SELECT detection_timestamp AT TIME ZONE '%s' AS detected_timestamp, pi "
            + "FROM detector_performance "
            + "WHERE detector_id LIKE ? "
            + "AND detection_timestamp > ? "
            + "AND detection_timestamp < ? ORDER BY detection_timestamp";
    private static final String DETECTION_TIMESTAMP_XFIELD = "detected_timestamp";
    private static final String[] DETECTION_YFIELD = {"pi"};
    private static final boolean DETECTOR_NULL_VALUE_UPDATED = false;

    public DetectorPerformanceIndexChart(Rectangle chartSize, String mimetype, 
            String detector, Period period, String timezone) throws BlueTruthException {
        
        super(new StringBuilder()
                .append(chartSize.height).append(COMMA_SPACE)
                .append(chartSize.width).append(COMMA_SPACE)
                .append(detector).append(COMMA_SPACE)
                .append(timestamp(period.fromDate())).append(COMMA_SPACE)
                .append(timestamp(period.toDate())).append(COMMA_SPACE)
                .append(timezone).toString(),
                
                period,
                chartSize, mimetype,
                "Performance Index Statistics", detector,
                xyScatter(),
                dateAxis("Detection Time", "yyyy-MMM-dd HH:mm", period),
                numberAxis("Performance Index"),
                query(String.format(DETECTOR_PI_SQL, timezone),
                        fillFieldNames(DETECTION_TIMESTAMP_XFIELD, DETECTION_YFIELD),
                        DETECTOR_NULL_VALUE_UPDATED,
                        detector, timestamp(period.fromDate()), timestamp(period.toDate())),
                intTimeSeries("Minimum", DETECTION_TIMESTAMP_XFIELD, "pi",
                        lines(Color.RED)));
    }
}
