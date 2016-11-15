/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart;

import static ssl.bluetruth.chart.Chart.stack;
import ssl.bluetruth.chart.common.Period;
import ssl.bluetruth.chart.common.Average;
import ssl.bluetruth.chart.common.Rectangle;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author pwood
 */
public class RouteDurationChart extends Chart {
    private final static String ROUTE_DURATIONS = "SELECT "
            + "route_span.span_name, "
            + "EXTRACT(EPOCH FROM duration_median)*1000 AS duration_median, "
            + "EXTRACT(EPOCH FROM duration_mean)*1000 AS duration_mean, "
            + "EXTRACT(EPOCH FROM duration_mode)*1000 AS duration_mode, "
            + "completed_timestamp AT TIME ZONE '%s' AS completed_timestamp "
            + "FROM route_span "
            + "JOIN span_journey_detection  "
            + "ON route_span.span_name = span_journey_detection.span_name "
            + "JOIN span_journey_detection_analytics "
            + "ON span_journey_detection.span_journey_detection_id = span_journey_detection_analytics.span_journey_detection_id "
            + "WHERE route_span.route_name = ? "
            + "AND completed_timestamp > ? "
            + "AND completed_timestamp < ? "
            + "ORDER BY completed_timestamp ASC";
    private static final String DETECTION_TIMESTAMP_XFIELD = "completed_timestamp";
    private static final String[] ROUTE_YFIELDS = {"duration_median", "duration_mean", "duration_mode"};
    private static final boolean DETECTOR_NULL_VALUE_UPDATED = false;

    public RouteDurationChart(Rectangle chartSize, String mimetype, String routeName, Period period, Average average, String timezone) throws BlueTruthException {
        super(new StringBuilder()
                .append(chartSize.height).append(COMMA_SPACE)
                .append(chartSize.width).append(COMMA_SPACE)
                .append(routeName).append(COMMA_SPACE)
                .append(timestamp(period.fromDate())).append(COMMA_SPACE)
                .append(timestamp(period.toDate())).append(COMMA_SPACE)
                .append(average.toString()).append(COMMA_SPACE)
                .append(timezone).toString(),
                
                period,
                chartSize, mimetype,
                "Journey Time", routeName,
                xyStackedArea(),
                dateAxis("Detection Time", "yyyy-MMM-dd HH:mm", period),
                relativeDateAxis("Journey Time", "hh:mm:ss"),
                query(String.format(ROUTE_DURATIONS, timezone),
                        fillFieldNames(DETECTION_TIMESTAMP_XFIELD, "span_name", ROUTE_YFIELDS),
                        DETECTOR_NULL_VALUE_UPDATED, routeName, timestamp(period.fromDate()), timestamp(period.toDate())),
                stack(DETECTION_TIMESTAMP_XFIELD, "duration_median", "span_name")
                .gt("duration_median", 0)
                .when(average.isMedian()),
                
                stack(DETECTION_TIMESTAMP_XFIELD, "duration_mean", "span_name")
                .gt("duration_mean", 0)
                .when(average.isMean()),

                stack(DETECTION_TIMESTAMP_XFIELD, "duration_mode", "span_name")
                .gt("duration_mode", 0)
                .when(average.isMode()));
    }
}
