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
public class Detector3GStatisticsChart extends Chart {
    private static final String DETECTOR_3G_SIGNAL_LEVEL_SQL
            =            "SELECT detection_timestamp AT TIME ZONE '%s' AS detected_timestamp, sl_3g_min, sl_3g_avg, sl_3g_max "
            + "FROM detector_performance "
            + "WHERE detector_id LIKE ? "
            + "AND detection_timestamp > ? "
            + "AND detection_timestamp < ? ORDER BY detection_timestamp";
    private static final String DETECTION_TIMESTAMP_XFIELD = "detected_timestamp";
    private static final String[] DETECTION_YFIELDS = {"sl_2g_min", "sl_2g_avg", "sl_2g_max"};
    private static final boolean DETECTOR_NULL_VALUE_UPDATED = false;

    public Detector3GStatisticsChart(Rectangle chartSize, String mimetype, String detector, Period period, String timezone) throws BlueTruthException {
        super(new StringBuilder()
                .append(chartSize.height).append(COMMA_SPACE)
                .append(chartSize.width).append(COMMA_SPACE)
                .append(detector).append(COMMA_SPACE)
                .append(timestamp(period.fromDate())).append(COMMA_SPACE)
                .append(timestamp(period.toDate())).append(COMMA_SPACE)
                .append(timezone).toString(),
                
                period,
                chartSize, mimetype,
                "3G Signal Statistics", detector,
                xyScatter(),
                dateAxis("Detection Time", "yyyy-MMM-dd HH:mm", period),
                numberAxis("Signal level"),
                query(String.format(DETECTOR_3G_SIGNAL_LEVEL_SQL, timezone),
                        fillFieldNames(DETECTION_TIMESTAMP_XFIELD, DETECTION_YFIELDS),
                        DETECTOR_NULL_VALUE_UPDATED,
                        detector, timestamp(period.fromDate()), timestamp(period.toDate())),
                intTimeSeries("Minimum", DETECTION_TIMESTAMP_XFIELD, "sl_3g_min",
                        lines(Color.RED)),

                intTimeSeries("Average", DETECTION_TIMESTAMP_XFIELD, "sl_3g_avg",
                        lines(Color.BLUE)),
                
                intTimeSeries("Maximum", DETECTION_TIMESTAMP_XFIELD, "sl_3g_max",
                        lines(Color.GRAY)));
    }
}
