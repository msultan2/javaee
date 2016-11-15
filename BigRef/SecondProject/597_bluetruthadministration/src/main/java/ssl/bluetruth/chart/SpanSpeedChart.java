/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart;

import ssl.bluetruth.chart.common.Period;
import ssl.bluetruth.chart.common.Average;
import java.awt.Color;
import ssl.bluetruth.chart.common.Rectangle;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author pwood
 */
public class SpanSpeedChart extends Chart {
    private final static String SPEED =
            "SELECT CASE WHEN span_osrm.total_distance != 0 "
            + "AND span_journey_detection.duration != '00:00:00'::interval "
            + "THEN (total_distance/EXTRACT(EPOCH FROM duration))*2.23693629 "
            + "ELSE NULL END AS speed, "
            + "CASE WHEN span_osrm.total_distance != 0 "
            + "AND span_journey_detection_analytics.duration_mean != '00:00:00'::interval "
            + "THEN (total_distance/EXTRACT(EPOCH FROM duration_mean))*2.23693629 "
            + "ELSE NULL END AS speed_mean, "
            + "CASE WHEN span_osrm.total_distance != 0 "
            + "AND span_journey_detection_analytics.duration_median != '00:00:00'::interval "
            + "THEN (total_distance/EXTRACT(EPOCH FROM duration_median))*2.23693629 "
            + "ELSE NULL END AS speed_median, "
            + "CASE WHEN span_osrm.total_distance != 0 "
            + "AND span_journey_detection_analytics.duration_mode != '00:00:00'::interval "
            + "THEN (total_distance/EXTRACT(EPOCH FROM duration_mode))*2.23693629 "
            + "ELSE NULL END AS speed_mode, "
            + "span_journey_detection.completed_timestamp AT TIME ZONE '%s' AS completed_timestamp, "
            + "span_journey_detection.outlier AS outlier "
            + "FROM span_journey_detection "
            + "JOIN span_osrm "
            + "ON span_journey_detection.span_name = "
            + "span_osrm.span_name "
            + "JOIN span_journey_detection_analytics "
            + "ON span_journey_detection.span_journey_detection_id = "
            + "span_journey_detection_analytics.span_journey_detection_id "
            + "WHERE span_journey_detection.span_name = ? "
            + "AND completed_timestamp > ? "
            + "AND completed_timestamp < ? "
            + "ORDER BY completed_timestamp ASC;";
    private static final String PREDICATE_FIELD = "outlier";
    private static final String[] SPAN_YFIELDS = {"speed", "speed_mean", "speed_median", "speed_mode"};
    private static final String DETECTION_TIMESTAMP_XFIELD = "completed_timestamp";
    private static final boolean DETECTOR_NULL_VALUE_UPDATED = false;
    
    public SpanSpeedChart(Rectangle chartSize, String mimetype, String spanName, Period period,
            int maxSpeed, boolean speed, boolean outliers, Average average, String timezone) throws BlueTruthException {
        
        super(new StringBuilder()
                .append(chartSize.height).append(COMMA_SPACE)
                .append(chartSize.width).append(COMMA_SPACE)
                .append(spanName).append(COMMA_SPACE)
                .append(timestamp(period.fromDate())).append(COMMA_SPACE)
                .append(timestamp(period.toDate())).append(COMMA_SPACE)
                .append(maxSpeed).append(COMMA_SPACE)
                .append(speed).append(COMMA_SPACE)
                .append(outliers).append(COMMA_SPACE)
                .append(average.toString()).append(COMMA_SPACE)
                .append(timezone).toString(),
                
                period,
                chartSize, mimetype,
                "Speed", spanName,
                xyScatter(),
                dateAxis("Detection Time", "yyyy-MMM-dd HH:mm", period),
                numberAxis("Speed", maxSpeed),
                query(String.format(SPEED, timezone),
                        fillFieldNamesWithPredectedField(DETECTION_TIMESTAMP_XFIELD, PREDICATE_FIELD, SPAN_YFIELDS),
                        DETECTOR_NULL_VALUE_UPDATED, spanName, timestamp(period.fromDate()), timestamp(period.toDate())),
                floatTimeSeries("Speed", DETECTION_TIMESTAMP_XFIELD, "speed",
                        crosses(Color.RED))
                .when(speed)
                .unless(PREDICATE_FIELD),
                floatTimeSeries("Speed Outlier", DETECTION_TIMESTAMP_XFIELD, "speed",
                        crosses(Color.DARK_GRAY))
                .when(outliers)
                .when(PREDICATE_FIELD),
                floatTimeSeries("Speed Mean", DETECTION_TIMESTAMP_XFIELD, "speed_mean",
                        lines(Color.BLUE))
                .when(average.isMean())
                .gt("speed_mean", 0.0),

                floatTimeSeries("Speed Median", DETECTION_TIMESTAMP_XFIELD, "speed_median",
                        lines(Color.BLUE))
                .when(average.isMedian())
                .gt("speed_median", 0.0),

                floatTimeSeries("Speed Mode", DETECTION_TIMESTAMP_XFIELD, "speed_mode",
                        lines(Color.BLUE))
                .when(average.isMode())
                .gt("speed_mode", 0.0));
    }
}
