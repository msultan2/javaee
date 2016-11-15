DO $$
    DECLARE  
        
        test varchar := 'DefaultConfigurationSpecifingUrlValues';
        
        detectorIdDefaultConfigurationURL varchar := 'DetectorTesting'||test;        
        
        firstPartOfAllUrls varchar;    
        urlJourneyTimesReporting varchar;
        urlCongestionReporting varchar;
        urlCongestionReports varchar;
        urlStatisticsReports varchar;
        urlStatusReports varchar;
        urlFaultReports varchar;
        brandingApp varchar;
    BEGIN
        RAISE INFO '%', test; 

        firstPartOfAllUrls := 'http://'||(SELECT current_setting('testSettings.databaseAddress'))||':'||(current_setting('testSettings.port'))||'/';
        urlJourneyTimesReporting := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlJourneyTimesReporting'));
        urlCongestionReporting := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlCongestionReporting'));
        urlCongestionReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlCongestionReports'));
        urlStatisticsReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlStatisticsReports'));
        urlStatusReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlStatusReports'));
        urlFaultReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlFaultReports'));
        brandingApp := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfBrandingApp'));

        DELETE FROM device_detection WHERE detector_id = detectorIdDefaultConfigurationURL;
        DELETE FROM occupancy WHERE detector_id = detectorIdDefaultConfigurationURL;
        DELETE FROM detector_configuration WHERE detector_id = detectorIdDefaultConfigurationURL;
        DELETE FROM detector WHERE detector_id = detectorIdDefaultConfigurationURL;

        INSERT INTO detector VALUES (detectorIdDefaultConfigurationURL, detectorIdDefaultConfigurationURL, 3, 'North', 'default', 51.496934974371236, -2.5681400299072266, detectorIdDefaultConfigurationURL, 1, true); 
        
        UPDATE detector_configuration
            SET "urlCongestionReporting" = urlCongestionReporting, 
            "urlCongestionReports" = urlCongestionReports,
            "urlStatisticsReports" = urlStatisticsReports,
            "urlStatusReports" = urlStatusReports,
            "urlFaultReports" = urlFaultReports,
            "urlJourneyTimesReporting" = urlJourneyTimesReporting,
            "signReports" = false
        WHERE detector_id = detectorIdDefaultConfigurationURL;

        INSERT INTO detector_logical_group VALUES (detectorIdDefaultConfigurationURL, (SELECT current_setting('testSettings.testGroup')));
    END
$$;