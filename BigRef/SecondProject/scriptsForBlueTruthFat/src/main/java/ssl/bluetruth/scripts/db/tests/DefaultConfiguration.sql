DO $$
    DECLARE  
        test varchar := 'DefaultConfiguration';
        
        detectorIdDefaultConfiguration varchar := 'DetectorTesting'||test;             
        
    BEGIN
        RAISE INFO '%', test; 

        DELETE FROM device_detection WHERE detector_id = detectorIdDefaultConfiguration;
        DELETE FROM occupancy WHERE detector_id = detectorIdDefaultConfiguration;
        DELETE FROM detector_configuration WHERE detector_id = detectorIdDefaultConfiguration;
        DELETE FROM detector WHERE detector_id = detectorIdDefaultConfiguration;
        
        INSERT INTO detector VALUES (detectorIdDefaultConfiguration, detectorIdDefaultConfiguration, 3, 'North', 'default', 51.499766912405946, -2.57080078125, detectorIdDefaultConfiguration, 1, true); 
        
        UPDATE detector_configuration
            SET "signReports" = false
        WHERE detector_id = detectorIdDefaultConfiguration;

        INSERT INTO detector_logical_group VALUES (detectorIdDefaultConfiguration, (SELECT current_setting('testSettings.testGroup')));
        
    END
$$;