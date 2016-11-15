DO $$
    DECLARE  
        test varchar := 'FirstSeenTimestampTolerance1';            
        detectorId varchar := 'DetectorTesting'||test;

BEGIN
RAISE INFO '%', test; 

        DELETE FROM occupancy WHERE detector_id = detectorId;
        DELETE FROM device_detection WHERE detector_id = detectorId;        
        DELETE FROM detector_configuration WHERE detector_id = detectorId;
        DELETE FROM detector WHERE detector_id = detectorId;
        
        INSERT INTO detector VALUES (detectorId, detectorId, 1, 'North', 'default', 51.33661771600667, -2.54608154296875, 'testing configured timestamp validation', 1, true);

        UPDATE detector_configuration
        SET "timestampToleranceInMs" = '12000',            
            "congestionReportPeriodInSeconds" = '3',
            "statisticsReportPeriodInSeconds" = '10',
            "absenceThresholdInSeconds" = '5',
            "queueDetectionStartupIntervalInSeconds" = '1',
            "signReports" = false
        WHERE detector_id = detectorId;


        INSERT INTO detector_logical_group VALUES (detectorId, (SELECT current_setting('testSettings.testGroup')));

END$$;