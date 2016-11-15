/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart;

import ssl.bluetruth.chart.common.Average;
import ssl.bluetruth.chart.common.Period;
import ssl.bluetruth.chart.common.Rectangle;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author pwood
 */
public class RouteSpeedChart extends Chart {

private final static String ROUTE_SPEED =
             "SELECT span_journey_detection.span_name, "
             + "CASE WHEN span_osrm.total_distance != 0 "
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
             +"JOIN route_span ON route_span.span_name = span_journey_detection.span_name "
             + "WHERE route_span.route_name = ? "
             + "AND completed_timestamp > ? "
             + "AND completed_timestamp < ?"
             + "ORDER BY completed_timestamp ASC; ";
    private static final String[] ROUTE_YFIELDS = {"speed", "speed_mean", "speed_median", "speed_mode"};
    private static final String DETECTION_TIMESTAMP_XFIELD = "completed_timestamp";
    private static final boolean DETECTOR_NULL_VALUE_UPDATED = false;

    public RouteSpeedChart(Rectangle chartSize, String mimetype, String routeName, Period period, Average average, String timezone) throws BlueTruthException {
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
                String.format("Speed (%s)", average), routeName,
                xyScatter(),
                dateAxis("Detection Time", "yyyy-MMM-dd HH:mm", period),
                numberAxis("Speed"),
                query(String.format(ROUTE_SPEED, timezone),
                        fillFieldNames(DETECTION_TIMESTAMP_XFIELD, "span_name", ROUTE_YFIELDS),
                        DETECTOR_NULL_VALUE_UPDATED, routeName, timestamp(period.fromDate()), timestamp(period.toDate())),
                multipleFloatSeries(DETECTION_TIMESTAMP_XFIELD, "speed_mean", "span_name",
                        lines())
                .gt("speed_mean", 0.0)
                .when(average.isMean()),

                multipleFloatSeries(DETECTION_TIMESTAMP_XFIELD, "speed_median", "span_name",
                        lines())
                .gt("speed_median", 0.0)
                .when(average.isMedian()),

                multipleFloatSeries(DETECTION_TIMESTAMP_XFIELD, "speed_mode", "span_name",
                        lines())
                .gt("speed_mode", 0.0)
                .when(average.isMode()));
    }
}
