DO $$
    DECLARE  
        test varchar := 'silentDetections';
        detector1 varchar := test||'Detector1';
        detector2 varchar := test||'Detector2';
        detector3 varchar := test||'Detector3';
        span1 varchar := test||'Span1';
	span2 varchar := test||'Span2';
        route1 varchar := test||'Route1';
        route2 varchar := test||'Route2';

BEGIN
RAISE INFO '%', test; 

    DELETE FROM route_datex2 WHERE route_name in (route1);
    DELETE FROM route_logical_group WHERE route_name in (route1,route2);
    DELETE FROM route_span WHERE route_name in (route1,route2);
    DELETE FROM route WHERE route_name in (route1,route2);
    DELETE FROM span_logical_group WHERE span_name in (span1, span2);
    DELETE FROM span_journey_detection_analytics WHERE span_name in (span1, span2);
    DELETE FROM span_journey_detection_cache WHERE span_name in (span1, span2);
    DELETE FROM span_journey_detection WHERE span_name in (span1, span2);
    DELETE FROM span WHERE span_name in (span1, span2);
    DELETE FROM occupancy WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM device_detection WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM device_detection_historic WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM detector_logical_group WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM detector_configuration WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM detector WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM detector_status WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM detector_performance WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM detector_statistic WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM detector_performance WHERE detector_id in (detector1, detector2, detector3);
    DELETE FROM detector_configuration WHERE detector_id in (detector1, detector2, detector3);
        
    INSERT INTO detector VALUES (detector1, detector1, 1, 'North', 'default', 51.2726555022251, 0.061798095703125, 'testing activation threshold of silent status', 1500, true);
    INSERT INTO detector VALUES (detector2, detector2, 1, 'North', 'default', 51.2821588829694, 0.101194381713867, 'testing activation threshold of silent status', 1501, true);
    INSERT INTO detector VALUES (detector3, detector3, 1, 'North', 'default', 51.2888959646579, 0.134024620056152, 'testing activation threshold of silent status', 1502, true);

    UPDATE detector_configuration
        SET "congestionReportPeriodInSeconds" = '3',
            "statisticsReportPeriodInSeconds" = '10',
            "absenceThresholdInSeconds" = '5',
            "queueDetectionStartupIntervalInSeconds" = '1',
            "signReports" = false
        WHERE detector_id in (detector1, detector2, detector3);

    INSERT INTO detector_logical_group VALUES (detector1, (SELECT current_setting('testSettings.testGroup')));
    INSERT INTO detector_logical_group VALUES (detector2, (SELECT current_setting('testSettings.testGroup')));
    INSERT INTO detector_logical_group VALUES (detector3, (SELECT current_setting('testSettings.testGroup')));

    INSERT INTO span VALUES (span1, detector1, detector2);
    INSERT INTO span VALUES (span2, detector2, detector3);
    
    UPDATE span_osrm
        SET "route_geometry" = 'uf}wHy`KCo@c@iGe@}Fm@yFcAsI_AoGiAmG{ByKuC}OeAkH{@eI{@wJ{@{K}@mKiA}KaB_NmAuIgCyOeDaPcCmK_@aBy@gD{AgF}@iD',
            "total_distance" =2931, "total_time" = 118
        WHERE span_name = span1;

    UPDATE span_osrm
        SET "route_geometry" = 'ca_xHcxRWw@kFaQ}CuLmB_JeAmGw@{FsAgPg@sH[wGa@aNa@gJs@gMQiC[kEk@qHYyCg@qFo@oGm@eFWiBUu@[wBiAiH',
            "total_distance" =2407, "total_time" = 97
        WHERE span_name = span2;
	
    INSERT INTO span_logical_group VALUES((SELECT current_setting('testSettings.testGroup')), span1);
    INSERT INTO span_logical_group VALUES((SELECT current_setting('testSettings.testGroup')), span2);

    INSERT INTO route VALUES (route1, 'test route 1 for silent detectors');
    INSERT INTO route VALUES (route2, 'test route 2 for silent detectors');

    INSERT INTO route_span VALUES (route1, span1);
    INSERT INTO route_span VALUES (route2, span2);

    INSERT INTO route_logical_group VALUES((SELECT current_setting('testSettings.testGroup')), route1);
    INSERT INTO route_logical_group VALUES((SELECT current_setting('testSettings.testGroup')), route2);

    INSERT INTO route_datex2 VALUES (route1);
        
END$$