DO $$
    DECLARE  
        test varchar := 'SpecifyingAllValues';
               
        detectorIdAllValues varchar := 'DetectorTesting'||test;
        
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

        DELETE FROM device_detection WHERE detector_id = detectorIdAllValues;
        DELETE FROM occupancy WHERE detector_id = detectorIdAllValues;
        DELETE FROM detector_configuration WHERE detector_id = detectorIdAllValues;
        DELETE FROM detector WHERE detector_id = detectorIdAllValues;
        INSERT INTO detector VALUES (detectorIdAllValues, detectorIdAllValues, 3, 'North', 'default', 51.499660050014434, -2.5620460510253906, detectorIdAllValues, 1, true); 
        UPDATE detector_configuration
            SET "urlCongestionReporting" = urlCongestionReporting, 
            "urlCongestionReports" = urlCongestionReports,
            "urlStatisticsReports" = urlStatisticsReports,
            "urlStatusReports" = urlStatusReports,
            "urlFaultReports" = urlFaultReports,
            "urlJourneyTimesReporting" = urlJourneyTimesReporting,
            "congestionReportPeriodInSeconds" = '10',
            "statisticsReportPeriodInSeconds" = '15',
            "statusReportPeriodInSeconds" = '600',
            "settingsCollectionInterval1" = '15',
            "settingsCollectionInterval2" = '15',
            "inquiryCycleDurationInSeconds" = '10',
            "freeFlowSpeedCyclesThreshold" = '2',
            "moderateSpeedCyclesThreshold" = '4',
            "slowSpeedCyclesThreshold" = '6',
            "verySlowSpeedCyclesThreshold" = '8',
            "absenceThresholdInSeconds" = '15',
            "backgroundLatchTimeThresholdInSeconds" = '15',
            "backgroundClearanceTimeThresholdInSeconds" = '15',
            "queueAlertThresholdBin" = 'staticFlow',
            "queueDetectThreshold" = '10',
            "queueClearanceThreshold" = '8',
            "queueDetectionStartupIntervalInSeconds" = '30',            
            "signReports" = false,
            "seed" = 1
        WHERE detector_id = detectorIdAllValues;
        INSERT INTO detector_logical_group VALUES (detectorIdAllValues, (SELECT current_setting('testSettings.testGroup')));

    END
$$;