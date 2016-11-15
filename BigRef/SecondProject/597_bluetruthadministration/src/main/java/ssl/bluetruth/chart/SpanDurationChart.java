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

public class SpanDurationChart extends Chart {
    private final static String JOURNEY_TIMES =
            "SELECT EXTRACT(EPOCH FROM span_journey_detection.duration)*1000 AS duration, "
            + "span_journey_detection.completed_timestamp AT TIME ZONE '%s' AS completed_timestamp, "
            + "EXTRACT(EPOCH FROM span_journey_detection_analytics.duration_mean)*1000 AS duration_mean, "
            + "EXTRACT(EPOCH FROM span_journey_detection_analytics.duration_median)*1000 AS duration_median, "
            + "EXTRACT(EPOCH FROM span_journey_detection_analytics.duration_mode)*1000 AS duration_mode, "
            + "span_journey_detection.outlier AS duration_outlier "
            + "FROM span_journey_detection "
            + "JOIN span_journey_detection_analytics "
            + "ON span_journey_detection.span_journey_detection_id = "
            + "span_journey_detection_analytics.span_journey_detection_id "
            + "WHERE span_journey_detection.span_name = ? "
            + "AND completed_timestamp > ? "
            + "AND completed_timestamp < ? "
            + "ORDER BY completed_timestamp ASC;";
    private static final String PREDICATE_FIELD = "duration_outlier";
    private static final String[] SPAN_YFIELDS = {"duration", "duration_median", "duration_mean", "duration_mode"};
    private static final String DETECTION_TIMESTAMP_XFIELD = "completed_timestamp";
    private static final boolean DETECTOR_NULL_VALUE_UPDATED = false;

    public SpanDurationChart(Rectangle chartSize, String mimetype, String spanName, Period period,
            int maxDuration, boolean detections, boolean outliers, Average average, String timezone) throws BlueTruthException {
       
        super(new StringBuilder()
                .append(chartSize.height).append(COMMA_SPACE)
                .append(chartSize.width).append(COMMA_SPACE)
                .append(spanName).append(COMMA_SPACE)
                .append(timestamp(period.fromDate())).append(COMMA_SPACE)
                .append(timestamp(period.toDate())).append(COMMA_SPACE)
                .append(maxDuration).append(COMMA_SPACE)
                .append(detections).append(COMMA_SPACE)
                .append(outliers).append(COMMA_SPACE)
                .append(average.toString()).append(COMMA_SPACE)
                .append(timezone).toString(),
                
                period,
                chartSize, mimetype,
                "Journey Time", spanName,
                xyScatter(),
                dateAxis("Detection Time", "yyyy-MMM-dd HH:mm", period),
                relativeDateAxis("Journey Time", "hh:mm:ss", maxDuration),
                query(String.format(JOURNEY_TIMES, timezone),
                        fillFieldNamesWithPredectedField(DETECTION_TIMESTAMP_XFIELD, PREDICATE_FIELD, SPAN_YFIELDS),
                        DETECTOR_NULL_VALUE_UPDATED,
                        spanName, timestamp(period.fromDate()), timestamp(period.toDate())),
                intTimeSeries("Duration", DETECTION_TIMESTAMP_XFIELD, "duration", crosses(Color.RED))
                .when(detections).unless(PREDICATE_FIELD),
                intTimeSeries("Duration Outlier", DETECTION_TIMESTAMP_XFIELD, "duration",
                        crosses(Color.DARK_GRAY))
                .when(outliers).and().when(PREDICATE_FIELD),
                intTimeSeries("Median", DETECTION_TIMESTAMP_XFIELD, "duration_median",
                        lines(Color.BLUE))
                .when(average.isMedian()).and().gt("duration_median", 0),

                intTimeSeries("Mean", DETECTION_TIMESTAMP_XFIELD, "duration_mean",
                        lines(Color.BLUE))
                .when(average.isMean()).and().gt("duration_mean", 0),

                intTimeSeries("Mode", DETECTION_TIMESTAMP_XFIELD, "duration_mode",
                        lines(Color.BLUE))
                .when(average.isMode()).and().gt("duration_mode", 0));
    }
}
