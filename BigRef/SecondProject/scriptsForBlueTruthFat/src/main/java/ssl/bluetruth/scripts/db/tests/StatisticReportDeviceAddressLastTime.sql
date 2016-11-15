DO $$
    DECLARE  
        test varchar := 'StatisticReportDevicesAddressLastTime';
        detectorId varchar := 'DetectorTesting'||test;
    BEGIN
        RAISE INFO '%', test; 

        DELETE FROM detector_configuration WHERE detector_id = detectorId;
        DELETE FROM detector WHERE detector_id = detectorId;
        
        INSERT INTO detector VALUES (detectorId, detectorId, 3, 'North', 'default', 51.441168762039396, -2.465057373046875, detectorId, 1, true); 

        UPDATE detector_configuration
            SET "signReports" = false
        WHERE detector_id = detectorId;

        INSERT INTO detector_logical_group VALUES (detectorId, (SELECT current_setting('testSettings.testGroup')));
    END
$$;