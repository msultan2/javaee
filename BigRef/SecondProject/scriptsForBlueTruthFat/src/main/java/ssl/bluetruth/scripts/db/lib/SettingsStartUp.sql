

DO $$
    DECLARE     
        
    BEGIN
        RAISE INFO 'Settings Start Up'; 

        SET SESSION testSettings.databaseAddress to '192.168.0.174';
        SET SESSION testSettings.dataBaseName to 'bluetruth';
        SET SESSION testSettings.port to '30002';
        SET SESSION testSettings.testGroup to 'Test Group';        
        SET SESSION testSettings.lastPartOfUrlJourneyTimesReporting to 'BlueTruthReceiver1_50/DeviceDetection';
        SET SESSION testSettings.lastPartOfurlCongestionReporting to 'BlueTruthReceiver1_50/Occupancy';
        SET SESSION testSettings.lastPartOfurlCongestionReports to 'BlueTruthReceiver2/Congestion';
        SET SESSION testSettings.lastPartOfurlStatisticsReports to 'BlueTruthReceiver2/Statistics';
        SET SESSION testSettings.lastPartOfurlStatusReports to 'BlueTruthReceiver2/Status';
        SET SESSION testSettings.lastPartOfurlFaultReports to 'BlueTruthReceiver2/Fault';
        SET SESSION testSettings.lastPartOfbrandingApp to 'BlueTruthBranding/css/';        
        SET SESSION testSettings.queueDetectionStartupIntervalInSeconds to '30';
        SET SESSION testSettings.freeFlowSpeedCyclesThreshold to '2';
        SET SESSION testSettings.moderateSpeedCyclesThreshold to '4';
        SET SESSION testSettings.slowSpeedCyclesThreshold to '6';
        SET SESSION testSettings.verySlowSpeedCyclesThreshold to '8';
        SET SESSION testSettings.backgroundLatchTimeThresholdInSeconds to '15';
        SET SESSION testSettings.backgroundClearanceTimeThresholdInSeconds to '15';
        SET SESSION testSettings.absenceThresholdInSeconds to '15';
        SET SESSION testSettings.queueAlertThresholdBin to 'staticFlow';
        SET SESSION testSettings.queueClearanceThreshold to '8';
        SET SESSION testSettings.seed to '1';
        SET SESSION testSettings.signReports to 'false';        
    END
$$;
