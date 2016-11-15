DO $$
    DECLARE  
        test varchar := 'FirstSeenTimestampTolerance2';            
        detectorId varchar := 'DetectorTesting'||test;

BEGIN
RAISE INFO '%', test; 

    BEGIN
        RAISE INFO '%', test; 

        DELETE FROM occupancy WHERE detector_id = detectorId;
        DELETE FROM device_detection WHERE detector_id = detectorId;           
        DELETE FROM detector_configuration WHERE detector_id = detectorId;
        DELETE FROM detector WHERE detector_id = detectorId;
        
        INSERT INTO detector VALUES (detectorId, detectorId, 1, 'North', 'default', 51.330354, -2.548115, 'testing configured timestamp validation for 2nd detector', 1, true); 

        UPDATE detector_configuration
        SET "timestampToleranceInMs" = '16000',            
            "congestionReportPeriodInSeconds" = '3',
            "statisticsReportPeriodInSeconds" = '10',
            "absenceThresholdInSeconds" = '5',
            "queueDetectionStartupIntervalInSeconds" = '1',
            "signReports" = false
        WHERE detector_id = detectorId;

        INSERT INTO detector_logical_group VALUES (detectorId, (SELECT current_setting('testSettings.testGroup')));
        
    END;

END$$;