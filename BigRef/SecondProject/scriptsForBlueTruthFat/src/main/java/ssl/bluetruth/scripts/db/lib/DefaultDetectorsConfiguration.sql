
DO $$
    DECLARE  
        firstPartOfAllUrls varchar;    
        urlJourneyTimesReporting varchar;
        urlCongestionReporting varchar;
        urlCongestionReports varchar;
        urlStatisticsReports varchar;
        urlStatusReports varchar;
        urlFaultReports varchar;
        brandingApp varchar;

    BEGIN
        RAISE INFO 'Default Configuration: Someday, this script should just do inserts in the default configuration table';

        firstPartOfAllUrls := 'http://'||(SELECT current_setting('testSettings.databaseAddress'))||':'||(current_setting('testSettings.port'))||'/';
        urlJourneyTimesReporting := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlJourneyTimesReporting'));
        urlCongestionReporting := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlCongestionReporting'));
        urlCongestionReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlCongestionReports'));
        urlStatisticsReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlStatisticsReports'));
        urlStatusReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlStatusReports'));
        urlFaultReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlFaultReports'));
        brandingApp := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfBrandingApp'));

        UPDATE detector_configuration
            SET "urlJourneyTimesReporting" = urlJourneyTimesReporting
        WHERE "urlJourneyTimesReporting" = ''; 

        UPDATE detector_configuration
            SET "urlCongestionReporting" = urlCongestionReporting
        WHERE "urlCongestionReporting" = ''; 

        UPDATE detector_configuration
            SET "urlCongestionReports" = urlCongestionReports
        WHERE "urlCongestionReports" = ''; 

        UPDATE detector_configuration
            SET "urlStatisticsReports" = urlStatisticsReports
        WHERE "urlStatisticsReports" = '';

        UPDATE detector_configuration
            SET "urlStatusReports" = urlStatusReports
        WHERE "urlStatusReports" = '';

        UPDATE detector_configuration
            SET "urlFaultReports" = urlFaultReports
        WHERE "urlFaultReports" = '';

        UPDATE detector_configuration
            SET "backgroundLatchTimeThresholdInSeconds" = (SELECT CAST(current_setting('testSettings.backgroundLatchTimeThresholdInSeconds') AS integer))
        WHERE "backgroundLatchTimeThresholdInSeconds" is NULL;

        UPDATE detector_configuration
            SET "backgroundClearanceTimeThresholdInSeconds" = (SELECT CAST(current_setting('testSettings.backgroundClearanceTimeThresholdInSeconds') AS integer))
        WHERE "backgroundClearanceTimeThresholdInSeconds" is NULL;

        UPDATE detector_configuration
            SET "absenceThresholdInSeconds" = (SELECT CAST(current_setting('testSettings.absenceThresholdInSeconds') AS integer))
        WHERE "absenceThresholdInSeconds" is NULL;

        UPDATE detector_configuration
            SET "queueAlertThresholdBin" = (SELECT current_setting('testSettings.queueAlertThresholdBin'))
        WHERE "queueAlertThresholdBin" is NULL;

        UPDATE detector_configuration
            SET "queueDetectionStartupIntervalInSeconds" = (SELECT CAST(current_setting('testSettings.queueDetectionStartupIntervalInSeconds') AS integer))
        WHERE "queueDetectionStartupIntervalInSeconds" is NULL;

        UPDATE detector_configuration
            SET "freeFlowSpeedCyclesThreshold" = (SELECT CAST(current_setting('testSettings.freeFlowSpeedCyclesThreshold') AS integer))
        WHERE "freeFlowSpeedCyclesThreshold" is NULL;

        UPDATE detector_configuration
            SET "moderateSpeedCyclesThreshold" = (SELECT CAST(current_setting('testSettings.moderateSpeedCyclesThreshold') AS integer))
        WHERE "moderateSpeedCyclesThreshold" is NULL;

        UPDATE detector_configuration
            SET "slowSpeedCyclesThreshold" = (SELECT CAST(current_setting('testSettings.slowSpeedCyclesThreshold') AS integer))
        WHERE "slowSpeedCyclesThreshold" is NULL;

        UPDATE detector_configuration
            SET "verySlowSpeedCyclesThreshold" = (SELECT CAST(current_setting('testSettings.verySlowSpeedCyclesThreshold') AS integer))
        WHERE "verySlowSpeedCyclesThreshold" is NULL;

        UPDATE detector_configuration
            SET "queueClearanceThreshold" = (SELECT CAST(current_setting('testSettings.queueClearanceThreshold') AS integer))
        WHERE "queueClearanceThreshold" is NULL;

        UPDATE detector_configuration
            SET "seed" = (SELECT CAST(current_setting('testSettings.seed') AS integer))
        WHERE "seed" is NULL;
        
        DELETE FROM default_configuration;

        INSERT INTO default_configuration("property","value")
            VALUES ('statisticsReportContents', 'full'),
            ('missingDeviceDetectionsToConsiderDead', '3'),
            ('congestionReportDelayInSeconds', '5'),
            ('absenceThresholdInSeconds', '15'),
            ('backgroundClearanceTimeThresholdInSeconds', '15'),
            ('backgroundLatchTimeThresholdInSeconds', '15'),
            ('queueAlertThresholdBin', 'staticFlow'),
            ('queueClearanceThreshold', '8'),
            ('queueDetectionStartupIntervalInSeconds', '30'),
            ('seed', '1234'),
            ('urlCongestionReports', urlCongestionReports),
            ('urlStatisticsReports', urlStatisticsReports),
            ('urlStatusReports', urlStatusReports),
            ('urlFaultReports', urlFaultReports),
            ('urlJourneyTimesReporting', urlJourneyTimesReporting),
            ('congestionReportPeriodInSeconds', '10'),
            ('statisticsReportPeriodInSeconds', '15'),
            ('statusReportPeriodInSeconds', '600'),
            ('settingsCollectionInterval1', '15'),
            ('settingsCollectionInterval2', '15'),
            ('inquiryCycleDurationInSeconds', '10'),
            ('freeFlowSpeedCyclesThreshold', '2'),
            ('moderateSpeedCyclesThreshold', '4'),
            ('slowSpeedCyclesThreshold', '6'),
            ('verySlowSpeedCyclesThreshold', '8'),
            ('queueDetectThreshold', '10'),
            ('freeFlowSpeedCyclesThreshold', '10'),
            ('moderateSpeedCyclesThreshold', '10'),
            ('verySlowSpeedCyclesThreshold', '10'),
            ('signReports', 'false'),
            ('timestampToleranceInMs', '5000');
    END
$$;
