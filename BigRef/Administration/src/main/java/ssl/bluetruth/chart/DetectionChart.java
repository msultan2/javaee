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
 * @author pwood
 */
public class DetectionChart extends Chart {

    private static final String DETECTIONS
            = "select "
            + "(date_trunc('H',detection_timestamp) + "
            + "floor(EXTRACT('minute' FROM detection_timestamp)/5)*5 * "
            + "'1 minute'::interval) AT TIME ZONE '%s' AS detected_timestamp_section, "
            + "COUNT(DISTINCT device_id) AS count_distinct, "
            + "COUNT(device_id) AS count "
            + "from device_detection_historic "
            + "where detector_id LIKE ? "
            + "AND detection_timestamp > ? "
            + "AND detection_timestamp < ? "
            + "GROUP BY detected_timestamp_section";
    private static final String DETECTION_TIMESTAMP_XFIELD = "detected_timestamp_section";
    private static final String[] DETECTION_YFIELDS = {"count", "count_distinct"};
    private static final boolean DETECTOR_NULL_VALUE_UPDATED = true;

    public DetectionChart(Rectangle chartSize, String mimetype, String detector, Period period, boolean showUnique, String timezone) throws BlueTruthException {
        super(new StringBuilder()
                .append(chartSize.height).append(COMMA_SPACE)
                .append(chartSize.width).append(COMMA_SPACE)
                .append(detector).append(COMMA_SPACE)
                .append(timestamp(period.fromDate())).append(COMMA_SPACE)
                .append(timestamp(period.toDate())).append(COMMA_SPACE)
                .append(showUnique).append(COMMA_SPACE)
                .append(timezone).toString(),
                
                period,
                chartSize, mimetype,
                "Detections", detector,
                xyScatter(),
                dateAxis("Detection Time", "yyyy-MMM-dd HH:mm", period),
                numberAxis("Count"),
                query(String.format(DETECTIONS, timezone, timezone),
                        fillFieldNames(DETECTION_TIMESTAMP_XFIELD, DETECTION_YFIELDS),
                        DETECTOR_NULL_VALUE_UPDATED,
                        detector,
                        timestamp(period.fromDate()),
                        timestamp(period.toDate())
        ),
                intTimeSeries("Detection", DETECTION_TIMESTAMP_XFIELD, "count",
                        lines(Color.RED)),

                intTimeSeries("Unique Detection", DETECTION_TIMESTAMP_XFIELD, "count_distinct",
                        lines(Color.BLUE))
                .when(showUnique));
    }
}
