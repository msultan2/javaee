DO $$
    DECLARE  
        test varchar := 'V3andV4Detections3';
        v3DetectorId varchar := 'v3Detector'||test;
        v4DetectorId varchar := 'v4Detector'||test;
        v3v4Span varchar := 'v3v4Span'||test;

BEGIN
RAISE INFO '%', test; 

    DELETE FROM occupancy WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM device_detection WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM device_detection_historic WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM detector_logical_group WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM detector_configuration WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM detector WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM span_journey_detection_analytics WHERE span_name in (v3v4Span);
    DELETE FROM span_journey_detection_cache WHERE span_name in (v3v4Span);
    DELETE FROM span_journey_detection WHERE span_name in (v3v4Span);
    DELETE FROM span_logical_group WHERE span_name in (v3v4Span);
    DELETE FROM span WHERE span_name in (v3v4Span);
    DELETE FROM detector_status WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM detector_performance WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM detector_statistic WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM detector_performance WHERE detector_id in (v3DetectorId, v4DetectorId);
    DELETE FROM detector_configuration WHERE detector_id in (v3DetectorId, v4DetectorId);
        
    INSERT INTO detector VALUES (v3DetectorId, v3DetectorId, 1, 'North', 'default', 51.564140, -2.534316, 'testing v3 and v4 device ID compatibility', 1557, true);
    INSERT INTO detector VALUES (v4DetectorId, v4DetectorId, 1, 'North', 'default', 51.665886, -2.414361, 'testing v3 and v4 device ID compatibility', 1558, true);

    UPDATE detector_configuration
        SET "congestionReportPeriodInSeconds" = '3',
            "statisticsReportPeriodInSeconds" = '10',
            "absenceThresholdInSeconds" = '5',
            "queueDetectionStartupIntervalInSeconds" = '1',
            "signReports" = false
        WHERE detector_id in (v3DetectorId, v4DetectorId);

    INSERT INTO detector_logical_group VALUES (v3DetectorId, (SELECT current_setting('testSettings.testGroup')));
    INSERT INTO detector_logical_group VALUES (v4DetectorId, (SELECT current_setting('testSettings.testGroup')));

    INSERT INTO span VALUES (v3v4Span, v3DetectorId, v4DetectorId);
    INSERT INTO span_logical_group VALUES((SELECT current_setting('testSettings.testGroup')), v3v4Span);
        
END$$;