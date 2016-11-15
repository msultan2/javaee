DO $$
    DECLARE  
        test varchar := 'PastDetection';
        
        detectorId varchar := 'DetectorTesting'||test;             
        
    BEGIN
        RAISE INFO '%', test; 

        DELETE FROM device_detection WHERE detector_id = detectorId;
        DELETE FROM occupancy WHERE detector_id = detectorId;
        DELETE FROM detector_configuration WHERE detector_id = detectorId;
        DELETE FROM detector WHERE detector_id = detectorId;
        
        INSERT INTO detector VALUES (detectorId, detectorId, 3, 'North', 'default', 51.46448872009329, -2.4993896484375, detectorId, 1, true); 
        
        UPDATE detector_configuration
            SET "signReports" = false
        WHERE detector_id = detectorId;

        INSERT INTO detector_logical_group VALUES (detectorId, (SELECT current_setting('testSettings.testGroup')));
        
    END
$$;