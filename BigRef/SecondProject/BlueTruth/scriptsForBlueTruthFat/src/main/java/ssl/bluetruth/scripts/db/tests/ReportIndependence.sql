
DO $$
    DECLARE  
        test varchar := 'ReportIndependence';
        detectorId varchar := 'DetectorTesting'||test;
    BEGIN
        RAISE INFO '%', test; 

        DELETE FROM occupancy WHERE detector_id = detectorId;
        DELETE FROM device_detection WHERE detector_id = detectorId;
        DELETE FROM detector_configuration WHERE detector_id = detectorId;
        DELETE FROM detector WHERE detector_id = detectorId;
        
        INSERT INTO detector VALUES (detectorId, detectorId, 3, 'North', 'default', 51.48672785116955, -2.7666664123535156, detectorId, 1, true); 

        UPDATE detector_configuration
            SET "congestionReportPeriodInSeconds" = '1',
            "statisticsReportPeriodInSeconds" = '60',
            "signReports" = false
        WHERE detector_id = detectorId;

        INSERT INTO detector_logical_group VALUES (detectorId, (SELECT current_setting('testSettings.testGroup')));
    END
$$;