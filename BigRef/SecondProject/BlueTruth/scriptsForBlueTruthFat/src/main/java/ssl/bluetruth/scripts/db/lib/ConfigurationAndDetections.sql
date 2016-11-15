DO $$DECLARE     
    firstPartOfAllUrls varchar;    
    urlJourneyTimesReporting varchar;
    urlCongestionReporting varchar;
    urlCongestionReports varchar;
    urlStatisticsReports varchar;
    urlStatusReports varchar;
    urlFaultReports varchar;
    brandingApp varchar;
    testGroup varchar;

    testUserName varchar := 'administrator';
    testUserPassword varchar := 'ssl1324';
    testUserEmail varchar := testUserName||'@simulation-systems.co.uk';

    userTestingTimeZoneChangeName varchar := 'userTestingTimeZoneChange';
    userTestingTimeZoneChangePassword varchar := 'ssl1324';
    userTestingTimeZoneChangeEmail varchar := userTestingTimeZoneChangeName||'@simulation-systems.co.uk';
       
    testingReportTrafficFlow varchar := 'TestingReportTrafficFlow';
    testingAnalysisRoute varchar := 'TestingAnalysisRoute';
    testingAnalysisSpan varchar := 'TestingAnalysisSpan';
    testingAnalysisDetector varchar := 'TestingAnalysisDetector';
    testingDiagnostic varchar := 'TestingDiagnostic';    
    testingDiagnosticDetector varchar := 'TestingDiagnosticDetector';
    testingReportIndependence varchar := 'TestingReportIndependence';
    
BEGIN	
        RAISE INFO 'Configuration And Detections';   
   
        firstPartOfAllUrls := 'http://'||(SELECT current_setting('testSettings.databaseAddress'))||':'||(current_setting('testSettings.port'))||'/';
        urlJourneyTimesReporting := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlJourneyTimesReporting'));
        urlCongestionReporting := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlCongestionReporting'));
        urlCongestionReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlCongestionReports'));
        urlStatisticsReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlStatisticsReports'));
        urlStatusReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlStatusReports'));
        urlFaultReports := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfUrlFaultReports'));
        brandingApp := firstPartOfAllUrls||(SELECT current_setting('testSettings.lastPartOfBrandingApp'));

        INSERT INTO audit_trail_action VALUES ('ADD USER');
        INSERT INTO audit_trail_action VALUES ('REMOVE BRAND');
        INSERT INTO audit_trail_action VALUES ('ADD ROUTE');
        INSERT INTO audit_trail_action VALUES ('UPDATE USER');
        INSERT INTO audit_trail_action VALUES ('ADD SPAN NOTE');
        INSERT INTO audit_trail_action VALUES ('REMOVE USER');
        INSERT INTO audit_trail_action VALUES ('UPDATE ROUTE');
        INSERT INTO audit_trail_action VALUES ('ADD LOGICAL GROUP SPANS');
        INSERT INTO audit_trail_action VALUES ('UPDATE BROADCAST MESSAGE');
        INSERT INTO audit_trail_action VALUES ('ACTIVATE USER');
        INSERT INTO audit_trail_action VALUES ('ADD ROLE TO USER');
        INSERT INTO audit_trail_action VALUES ('ADD BRAND CONTACT DETAILS');
        INSERT INTO audit_trail_action VALUES ('REMOVE SPAN NOTE');
        INSERT INTO audit_trail_action VALUES ('UPDATE SPAN OSRM');
        INSERT INTO audit_trail_action VALUES ('ADD DETECTOR');
        INSERT INTO audit_trail_action VALUES ('REMOVE SPAN');
        INSERT INTO audit_trail_action VALUES ('REMOVE LOGICAL GROUP DETECTORS');
        INSERT INTO audit_trail_action VALUES ('ADD BRAND');
        INSERT INTO audit_trail_action VALUES ('UPDATE BRAND');
        INSERT INTO audit_trail_action VALUES ('UPDATE LOGICAL GROUP');
        INSERT INTO audit_trail_action VALUES ('ADD DETECTOR LOGICAL GROUP');
        INSERT INTO audit_trail_action VALUES ('ADD LOGICAL GROUP DETECTORS');
        INSERT INTO audit_trail_action VALUES ('UPDATE SPAN SPEED THRESHOLD');
        INSERT INTO audit_trail_action VALUES ('USER LOGOUT');
        INSERT INTO audit_trail_action VALUES ('REMOVE BROADCAST MESSAGE');
        INSERT INTO audit_trail_action VALUES ('UPDATE DETECTOR CONFIGURATION');
        INSERT INTO audit_trail_action VALUES ('REMOVE DETECTOR');
        INSERT INTO audit_trail_action VALUES ('REMOVE UNCONFIGURED DETECTOR');
        INSERT INTO audit_trail_action VALUES ('ADD LOGICAL GROUP');
        INSERT INTO audit_trail_action VALUES ('REMOVE LOGICAL GROUP ROUTES');
        INSERT INTO audit_trail_action VALUES ('ADD SPAN');
        INSERT INTO audit_trail_action VALUES ('REMOVE ROLE FROM USER');
        INSERT INTO audit_trail_action VALUES ('ADD LOGICAL GROUP USERS');
        INSERT INTO audit_trail_action VALUES ('ADD BROADCAST MESSAGE');
        INSERT INTO audit_trail_action VALUES ('ADD ROUTE LOGICAL GROUP');
        INSERT INTO audit_trail_action VALUES ('REMOVE LOGICAL GROUP');
        INSERT INTO audit_trail_action VALUES ('REMOVE SPAN FROM ROUTE');
        INSERT INTO audit_trail_action VALUES ('ADD LOGICAL GROUP ROUTES');
        INSERT INTO audit_trail_action VALUES ('UPDATE DETECTOR');
        INSERT INTO audit_trail_action VALUES ('REMOVE LOGICAL GROUP USERS');
        INSERT INTO audit_trail_action VALUES ('ADD SPAN LOGICAL GROUP');
        INSERT INTO audit_trail_action VALUES ('DEACTIVATE USER');
        INSERT INTO audit_trail_action VALUES ('USER LOGIN');
        INSERT INTO audit_trail_action VALUES ('ADD BROADCAST MESSAGE LOGICAL GROUP');
        INSERT INTO audit_trail_action VALUES ('UPDATE BRAND CONTACT DETAILS');
        INSERT INTO audit_trail_action VALUES ('REMOVE ROUTE');
        INSERT INTO audit_trail_action VALUES ('ADD SPAN TO ROUTE');
        INSERT INTO audit_trail_action VALUES ('REMOVE DETECTOR NOTE');
        INSERT INTO audit_trail_action VALUES ('REMOVE LOGICAL GROUP SPANS');
        INSERT INTO audit_trail_action VALUES ('ADD DETECTOR NOTE');
        INSERT INTO audit_trail_action VALUES ('UPDATE SPAN');
        INSERT INTO audit_trail_action VALUES ('REMOVE BRAND CONTACT DETAILS');
        INSERT INTO audit_trail_action VALUES ('ADD SPAN EVENT');
        INSERT INTO audit_trail_action VALUES ('REMOVE SPAN EVENT');
        INSERT INTO audit_trail_action VALUES ('ADD SPAN INCIDENT');
        INSERT INTO audit_trail_action VALUES ('REMOVE SPAN INCIDENT');
        INSERT INTO audit_trail_action VALUES ('UPDATE USER PASSWORD EXPIRY DAYS');

        INSERT INTO branding VALUES ('Golden River', brandingApp||'golden-river-theme/jquery-ui-1.8.22.custom.css', 'www.golden-river.com');
        INSERT INTO branding VALUES ('SSL', brandingApp||'ssl-theme/jquery-ui-1.8.22.custom.css', 'www.ssl.com');
        INSERT INTO branding_contact_details VALUES ('SSL', 'Contact me ', 'admin@ssl.com', '24 * 7 email support', 'email');

        INSERT INTO broadcast_message (title, message) VALUES ('New Instation Version Released', 'Version 2.0 of the cloud instation has now been released - 2012/12/23
        Features Include:
        1.1.1	Email address and user account validation
        1.1.2	Strong password validation
        1.1.3	Forgotten password mechanism
        1.1.4	Administrator password change facility
        1.1.5	Password expiry mechanism');
        INSERT INTO broadcast_message (title, message) VALUES ('Merry Christmas', 'Santa claus hohoho');

        INSERT INTO logical_group VALUES ('Test Group', 'Test Group');
        INSERT INTO logical_group VALUES ('Group'||testingDiagnostic, 'Group'||testingDiagnostic);

        INSERT INTO instation_role VALUES ('journey_report', 'Allows the user to access the journey report interface.');
        INSERT INTO instation_role VALUES ('map_interface', 'Allows the user to access the map interface.');
        INSERT INTO instation_role VALUES ('analysis', 'Allows the user to use the analysis section of the interface.');
        INSERT INTO instation_role VALUES ('occupancy_report', 'Allows the user to access the occupancy report interface.');
        INSERT INTO instation_role VALUES ('manager', 'Allows the user to update the web application.');
        INSERT INTO instation_role VALUES ('current_user_administration', 'Allows the user to manage their own details and access information available to them.');
        INSERT INTO instation_role VALUES ('detector_configuration', 'Allows the user to manage detectors.');
        INSERT INTO instation_role VALUES ('logical_group_administration', 'Allows the user to manage logical groups.');
        INSERT INTO instation_role VALUES ('role_administration', 'Allows the user to manage roles.');
        INSERT INTO instation_role VALUES ('route_configuration', 'Allows the user to manage routes.');
        INSERT INTO instation_role VALUES ('span_configuration', 'Allows the user to manage spans.');
        INSERT INTO instation_role VALUES ('user_administration', 'Allows the user to manage users.');
        INSERT INTO instation_role VALUES ('diagnostic', 'Allows the user to use instation diagnostic tools.');
        INSERT INTO instation_role VALUES ('brand_administration', 'Allows the user to manage  brands.');
        INSERT INTO instation_role VALUES ('broadcast_message_administration', 'Allows the user to manage broadcast messages');
        INSERT INTO instation_role VALUES ('support_administration', 'Allows the user to manage support page.');
        INSERT INTO instation_role VALUES ('diagnostic_detail', 'Allows the user to use lower level instation diagnostic tools.');
        INSERT INTO instation_role VALUES ('instation_administration', 'Allows the user to manage instation.');
        INSERT INTO instation_role VALUES ('audit_trail_administration', 'Allows the user to view the audit trail.');
        INSERT INTO instation_role VALUES ('wiki_view', 'Allows the user to view wiki pages');
        INSERT INTO instation_role VALUES ('wiki_administration', 'Allows the user to add, delete and edit wiki pages');
        INSERT INTO instation_role VALUES ('statistics_report', 'statistics_report');
        INSERT INTO instation_role VALUES ('detector_status', 'detector_status');
        INSERT INTO instation_role VALUES ('fault_report', 'fault_report');
        INSERT INTO instation_role VALUES ('DATEX2', 'Allows the user to view the Datex2 feed');

        INSERT INTO instation_user_timezone VALUES ('Asia/Tashkent');
        INSERT INTO instation_user_timezone VALUES ('Europe/London');

        INSERT INTO instation_user VALUES (testUserName, testUserName, MD5(testUserPassword), 'SSL', 'Europe/London', true, testUserEmail, NULL, 30, now());
        INSERT INTO instation_user VALUES (userTestingTimeZoneChangeName, userTestingTimeZoneChangeName, MD5(userTestingTimeZoneChangePassword), 'SSL', 'Europe/London', true, userTestingTimeZoneChangeEmail, NULL, 30, now());
        INSERT INTO instation_user_role SELECT instation_user.username, instation_role.role_name FROM instation_user, instation_role;
        INSERT INTO instation_user_logical_group SELECT instation_user.username, 'Test Group' FROM instation_user;
        INSERT INTO instation_user_logical_group SELECT instation_user.username, 'Group'||testingDiagnostic FROM instation_user;

        INSERT INTO detector VALUES ('C', 'Detector C', 1, 'North', 'default', 51.141348999999998, -2.6977720000000001, NULL, 1, true);
        INSERT INTO detector VALUES ('D', 'Detector D', 1, 'North', 'default', 51.1321310085617, -2.7376556396484375, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('B', 'Detector B', 1, 'North', 'default', 51.139078559227812, -2.6768875122070313, NULL, 1, true);
        INSERT INTO detector VALUES ('A', 'Detector A', 0, 'North', 'default', 51.140586416645569, -2.6585197448730469, NULL, 1, true);
        INSERT INTO detector VALUES ('Detector1', 'Detector1', 3, 'North', 'default', 51.456066027013208, -2.5818300247192383, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector2', 'Detector2', 3, 'North', 'default', 51.44550204054292, -2.5846195220947266, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector3', 'Detector3', 3, 'North', 'default', 51.446143972759494, -2.6073646545410156, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector4', 'Detector4', 3, 'North', 'default', 51.440446508980081, -2.6272344589233394, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector5', 'Detector5', 3, 'North', 'default', 51.419121523698344, -2.7252960205078125, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector6', 'Detector6', 3, 'North', 'default', 51.373012723404933, -2.8088092803955078, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector7', 'Detector7', 3, 'North', 'default', 51.392404431921157, -2.8270483016967773, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingReportTrafficFlow||'1', 'Detector'||testingReportTrafficFlow||'1', 3, 'North', 'default', 51.43437378156989, -2.8535699844360347, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingAnalysisDetector||'1', 'Detector'||testingAnalysisDetector||'1', 3, 'North', 'default', 51.39920565355378, -3.4304809570312496, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingAnalysisSpan||'1', 'Detector'||testingAnalysisSpan||'1', 3, 'North', 'default', 51.524926028423387, -3.3643913269042969, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingAnalysisSpan||'2', 'Detector'||testingAnalysisSpan||'2', 3, 'North', 'default', 51.517929877202938, -3.330144882202148, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingAnalysisRoute||'1', 'Detector'||testingAnalysisRoute||'1', 3, 'North', 'default', 51.416016931959319, -3.2268047332763667, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingAnalysisRoute||'2', 'Detector'||testingAnalysisRoute||'2', 3, 'North', 'default', 51.409914188821666, -3.2652568817138672, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingAnalysisRoute||'3', 'Detector'||testingAnalysisRoute||'3', 3, 'North', 'default', 51.411413183661054, -3.3043098449707027, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingDiagnosticDetector||'1', 'Detector'||testingDiagnosticDetector||'1', 3, 'North', 'default', 51.34466033840012, -2.9812002182006836, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingDiagnosticDetector||'2', 'Detector'||testingDiagnosticDetector||'2', 3, 'North', 'default', 51.334633652165806, -2.983860969543457, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingDiagnosticDetector||'3', 'Detector'||testingDiagnosticDetector||'3', 3, 'North', 'default', 51.342998312125424, -2.9677677154541016, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('DetectorVersion3LiveOutStation', 'DetectorVersion3LiveOutStation', 3, 'North', 'default', 51.576002715553294, -2.979612350463867, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('DetectorVersion4LiveOutStation', 'DetectorVersion4LiveOutStation', 3, 'North', 'default', 51.59120256047754, -2.9903411865234375, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('DetectorTestingDisorderedDetections', 'DetectorTestingDisorderedDetections', 3, 'North', 'default', 51.31755106502268, -2.6771020889282227, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Detector'||testingReportIndependence, 'Detector'||testingReportIndependence, 3, 'North', 'default', 51.48672785116955, -2.7666664123535156, 'No detector information available', 1, true);

        INSERT INTO detector VALUES ('LPBT1913', 'LPBT1913', 3, 'North', 'default', 51.211346404002654, -3.0352306365966797, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Temp', 'Temp', 0, 'North', 'default', 51.1825174210091305, -3.44610214233398393, 'No detector information available', 1, true);
        INSERT INTO detector VALUES ('Temp2', 'Temp2', 0, 'North', 'default', 51.1940296634285659, -3.4558868408203125, 'No detector information available', 1, true);

        --Default configuration:
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
        WHERE detector_id IN (SELECT detector_id from detector);
        --Especial configuration:
        UPDATE detector_configuration
            SET "inquiryCycleDurationInSeconds" = '999999999' --?
        WHERE detector_id IN ('Detector'||testingAnalysisRoute||'1', 'Detector'||testingAnalysisRoute||'2', 'Detector'||testingAnalysisRoute||'3');
        UPDATE detector_configuration
            SET "freeFlowSpeedCyclesThreshold" = '20',
            "moderateSpeedCyclesThreshold" = '40',
            "slowSpeedCyclesThreshold" = '60',
            "verySlowSpeedCyclesThreshold" = '80',
            "backgroundLatchTimeThresholdInSeconds" = '360'
        WHERE detector_id IN ('DetectorVersion3LiveOutStation', 'DetectorVersion4LiveOutStation', 'DetectorTestingDisorderedDetections');

        -- All detector added to Test Group:
        INSERT INTO detector_logical_group SELECT detector_id, 'Test Group' FROM detector;
        INSERT INTO detector_logical_group VALUES ('Detector'||testingDiagnosticDetector||'1', 'Group'||testingDiagnostic);
        INSERT INTO detector_logical_group VALUES ('Detector'||testingDiagnosticDetector||'2', 'Group'||testingDiagnostic);
        INSERT INTO detector_logical_group VALUES ('Detector'||testingDiagnosticDetector||'3', 'Group'||testingDiagnostic);

        INSERT INTO span VALUES ('Span AB', 'A', 'B');
        INSERT INTO span VALUES ('Span BC', 'B', 'C');
        INSERT INTO span VALUES ('Span CD', 'C', 'D');
        INSERT INTO span VALUES ('Span12', 'Detector1', 'Detector2');
        INSERT INTO span VALUES ('Span23', 'Detector2', 'Detector3');
        INSERT INTO span VALUES ('Span34', 'Detector3', 'Detector4');
        INSERT INTO span VALUES ('Span45', 'Detector4', 'Detector5');
        INSERT INTO span VALUES ('Span56', 'Detector5', 'Detector6');
        INSERT INTO span VALUES ('Span67', 'Detector6', 'Detector7');
        INSERT INTO span VALUES ('Span'||testingAnalysisSpan||'12', 'Detector'||testingAnalysisSpan||'1', 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO span VALUES ('Span'||testingAnalysisRoute||'12', 'Detector'||testingAnalysisRoute||'1', 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO span VALUES ('Span'||testingAnalysisRoute||'23', 'Detector'||testingAnalysisRoute||'2', 'Detector'||testingAnalysisRoute||'3');

        UPDATE span_osrm
            SET "route_geometry" = '{jcwH~gfOd@tCfD|RVxAVxAl@vDDZB\Br@@r@?|C@`I?vREjIEjIItBYtFIhB',
            "total_distance" = 1279, "total_time" = 55
        WHERE span_name = 'Span AB';
        UPDATE span_osrm
            SET "route_geometry" = 'gccwHt{iOs@`Mu@rMMnCGxAChA?x@?lB@vG?~GN|P?hB?VATE\CXMt@SdAOn@Mh@i@lBo@xB[bAQr@S|@IrAIvAStDCh@U`FGlB',
            "total_distance" = 1475, "total_time" = 63
        WHERE span_name = 'Span BC';
        UPDATE span_osrm
            SET "route_geometry" = 'kpcwH`|mOe@lIAT[bHOnCKpAWtAY|AUlBMdAMhAK`A[~BWt@Qd@[h@Y\UTQRa@v@Wv@y@~Fk@tCG^w@`DMZg@pAbBrJ@D|@`Ff@`DFpADrAH`BTtCHbADd@ANAJJV@FB^Nt@VvBR|Ab@tCh@rDd@nD^zBb@hBHRRf@j@lA\t@Vf@VVh@Zd@ZRh@j@jAt@xA|@dBz@`Bt@|ANn@LpAL~ALtAJhAFx@Hz@D|@@vBB|ADdABl@Bv@FjAApB@v@JtAHx@NvALjATrBD`@@LBHDBD@D@L?XEj@Ex@IJ?L@FFDHBFBJBN@ZBb@v@Id@CB?jBGbA?n@Dz@HpBV`AH`ANp@X~@b@^PXHT@FEDAH?FBFBHPFZ@NAJNl@FRJPh@fAj@hALV',
            "total_distance" = 3662, "total_time" = 189
        WHERE span_name = 'Span CD';
        UPDATE span_osrm
            SET "route_geometry" = 'k_ayHfgwNKe@Gs@OgAGgABNN`@\bBLbAJfAZhCJj@DLX^JD`@Fz@Fr@Dd@JpAZvCt@x@Z\XvAXJ@\Fl@HLBTD\DJB~B^jANr@JZBD?H?XGHMLENCLBJHJJDLTLVBLEROT[\s@Rc@h@cADMVk@Zs@Zu@fAuBDGN[NSDGp@iAFKNMJGLEHAJBHNHV?@BXF\Hh@H\Tn@NX|AzC~@|Bl@~AJT',
            "total_distance" = 1630, "total_time" = 92
        WHERE span_name = 'Span12';
        UPDATE span_osrm
            SET "route_geometry" = 'w}~xHfywNRn@HXz@fDXfBN|AHfAFtABbAAv@Ad@G`AM|AQxAMlAObAK`AE\?T@RDRJ^@RAJAHAFCFABOLKFIDKLGLGRGZIpACr@EdA?`ABx@ZtJAn@RrDF`D@nAE~AEdACXUjBKhAc@bEc@fEWbDQrCKlCIrD@bDVzG',
            "total_distance" = 1640, "total_time" = 93
        WHERE span_name = 'Span23';
        UPDATE span_osrm
            SET "route_geometry" = 'qa_yH~f|NANJjE\bIXdHBZTpGJrBFp@Fl@V~AXnA^hAh@jAn@z@RXLP\d@DT?V?f@DbIBvDFzA?\?XBbADfDBd@F^Nl@V`@b@`@rAx@h@f@dFhD~@hAj@jA^xAtC|N',
            "total_distance" = 1642, "total_time" = 78
        WHERE span_name = 'Span34';
        UPDATE span_osrm
            SET "route_geometry" = 'w}}xHdc`OHz@BXXnB\rAl@hD\lAfBhFbHrVNh@tAtErAzDxArDtBdFnAvB`AnBp@jAlC|DpIpLbThY`NxQjGfI`HtJhB~BbBjCh@`Ah@hAf@hAf@hAh@tAh@xAjAdDj@jBf@lBh@nBd@rBj@nCh@pCd@rCb@vCZjCVfCj@tGVvDTlEJjEBfEGtEK`D[nO{Ad^}AdZe@bIU~C]tE]tEQrBOpB{A`Q[vDEnABjAP`CTbCRhCDdCS`CCTSnB[|CUrCIfBDlCPlBJvAFzAIhBErAFzALnBNrAb@dEPpEJnG?rAGp@OlBWfBCJM`CVhE@j@JhDLfBHrAD~AA|D?r@Bz@VnGLzF`@hEx@pHfAxHn@xDd@vBH^HZTp@',
            "total_distance" = 7861, "total_time" = 405
        WHERE span_name = 'Span45';
        UPDATE span_osrm
            SET "route_geometry" = 'uxyxHfhsO@Fb@nAz@bBbA|Bt@pBv@nCp@rCn@`Cb@lCn@lBr@jBRh@j@dB^dBDPZnBNb@PjBHvALbDNnDC\H`AHd@HZJVv@bAfBxCZp@d@|@r@nA@Dv@lBz@|Bz@jCLd@Pj@f@fBh@rBZpAPr@n@xBTn@Vh@\b@|@x@`BtAdA~@^^\\`AdBb@x@d@jAFN^jAVv@R|@F`AJhAPt@^l@V\RPb@Pn@HvABvAN|@TzAt@`Ax@nAfAfAx@rAjAfBjBlE|ExAlA~BbBpHjFtCtBjF|E`C`Cb@r@X~@PjAf@|FzBvYRtBPzBp@lDbArDV~@J^`@|ARbBLbANt@rCpKhCzI`@~@^`ATd@nBhEjDtFf@t@r@p@n@Z~@\j@`@j@v@f@lAp@bAl@f@hBlAdChC~@tAnAtBtBpEx@rBZ|@HV|@vCjA~Dv@vCt@nDb@jBb@jAr@vAn@lAv@bBvBtFHVxC`Iz@~BZx@\~@Rj@j@`BFPNd@`@lAZ`A@BPl@Vv@d@zA^hAf@vARd@Rb@JP@BLTJRHJLRf@v@HLd@r@X`@V\h@p@HHDDd@h@b@f@^`@FFLJZVXNZLfA^dA\p@T|@Zn@Td@N\NTJ\TPRPVJP@DFLLVFNN^x@|B`@jAd@rA\dAX~@Nd@H`@D\D\@V@V?B?P?h@?h@CvC?r@?p@Bt@@V?HDn@Dd@Ff@Hf@FZJh@Lh@L^Pd@N^LXVb@Xb@^d@\^RPRNTLRJRJXLRFFB@?',
            "total_distance" = 8151, "total_time" = 426
        WHERE span_name = 'Span56';
        UPDATE span_osrm
            SET "route_geometry" = 'ixpxHdrcPICGv@Ol@Sf@c@x@g@j@iAx@s@h@c@LSBO?]C_Aa@[UUSa@U]K[CQAWDe@Jk@No@Ng@Fq@BI?sACY?i@?cAGoCSu@Am@Fa@Lw@ZsA~@aAf@YLmBbAc@Rm@Re@Lo@NaAJsBNQ?M@G@G@GBEDGHEFGNGPQn@y@tC[~@k@hAEJc@`AW|@UlAUrAG`@Or@Mh@cAxBEJIVi@lBABKVMXGLWf@OZQb@a@fAg@nBKb@EXe@`CMb@Ql@u@|A_@x@q@rAc@z@i@jAi@nAO^Qd@YdA]|@O\OPc@`@c@Xy@j@mAz@g@\{@h@aAh@m@`@m@\UPOJMJg@b@GFEDiAvAy@nA',
            "total_distance" = 2733, "total_time" = 143
        WHERE span_name = 'Span67';
        UPDATE span_osrm
            SET "route_geometry" = 'smnyHvbpS}JlHUTK]XWlFcEnJmH~BeB|LgK[aCCKEa@I_@CmADmBX{BDQn@yCZaBPqBJeA?qA?GF{OSqESeCImBAsA?Q@sCJsDLmF?qA?oBEeDDwBJsAz@wEh@_EXqCZ}BPgBD_AJeBt@kDpAwEJ}AFaCBqAToFx@mJG{Jt@{C`@gA',
            "total_distance" = 3231, "total_time" = 171
        WHERE span_name = 'Span'||testingAnalysisSpan||'12';
        UPDATE span_osrm
            SET "route_geometry" = 'meyxHvfuR?Hr@fELj@nCrLJtAJBFFFLBT?TDn@?nAHnAP`A`AzBxA`Cj@~AZfCbA~Fn@dCj@bBp@bBJl@r@|DHRzAzFPpAPtB@tAC|AOpAUtAI^S~@]vAEVa@tBGf@?v@@pAFnAHdAPbAZfAXt@tAxEXbAnAzCxArBf@`@HMFIRANHHNBP@ZEREJKJEBM?GCKKoArCMb@Kp@FFBH?F?JCFEFE@ChA{@hFQ`AUxA?rBChBThGN`CB^PbCJdAL`BB\JbA~@lJXxDHH@B@H?FADADEDC@@pBBxAHdBZfCP~@',
            "total_distance" = 3053, "total_time" = 158
        WHERE span_name = 'Span'||testingAnalysisRoute||'12';
        UPDATE span_osrm
            SET "route_geometry" = 'y~wxHxv|RDXp@bDVpAbAvFf@zBdAbDlBxF{@fA]p@?z@hBtFe@l@ALGZXrDTnCDf@@LI|A_@jAa@f@gAnBYrAD^p@zByC|ChBrEv@nBPh@|A~Ej@Kp@]HnAl@dAz@rAh@zAHV|@~BZv@n@~Ax@xAZt@`ArAZfAbBLx@d@x@p@`AhAdCrEvApD_@vA_@x@W`@Wb@y@lAg@t@eDrFcHpKcAlCm@lBuAzDm@fBeAxC_DvIyAdE@d@A|@M^QJOKS[Sj@y@tAaBfDwBpDgAnBkAlBc@t@',
            "total_distance" = 3722, "total_time" = 192
        WHERE span_name = 'Span'||testingAnalysisRoute||'23';
--     INSERT INTO span VALUES ('TempSpan', 'Temp', 'Temp2');
--     UPDATE span_osrm
--         SET "route_geometry" = 'uqkwHx``TU@WIUKEGIg@SwAQkAGUKSIYI]Go@CSCEIASDs@HkAL}@JM?KBKDWDSAIEEEAEKWMa@KYAOGg@Go@Ga@Uk@i@sA]y@EQUm@eAsBmAiBGIIOq@gAcBfDEFmBvCyEdH}@rAoFxKgBhEu@tBcB`FkAhEuAhFgA`Fy@fEg@nD{@tGCj@Ah@@V@H@L?NA@CD',
--         "total_distance" = 2102, "total_time" = 123
--     WHERE span_name = 'TempSpan';
        -- All spans added to Test Group:
        INSERT INTO span_logical_group SELECT 'Test Group', span_name FROM span;

        INSERT INTO route VALUES ('Route AB', 'Route AB Description');
        INSERT INTO route VALUES ('Route17', 'Route17');
        INSERT INTO route VALUES ('Route'||testingAnalysisSpan||'12', 'Route'||testingAnalysisSpan||'12');
        INSERT INTO route VALUES ('Route'||testingAnalysisRoute||'13', 'Route'||testingAnalysisRoute||'13');
        INSERT INTO route_span VALUES ('Route AB', 'Span AB');
        INSERT INTO route_span VALUES ('Route17', 'Span12');
        INSERT INTO route_span VALUES ('Route17', 'Span23');
        INSERT INTO route_span VALUES ('Route17', 'Span34');
        INSERT INTO route_span VALUES ('Route17', 'Span45');
        INSERT INTO route_span VALUES ('Route17', 'Span56');
        INSERT INTO route_span VALUES ('Route17', 'Span67');
        INSERT INTO route_span VALUES ('Route'||testingAnalysisSpan||'12', 'Span'||testingAnalysisSpan||'12');
        INSERT INTO route_span VALUES ('Route'||testingAnalysisRoute||'13', 'Span'||testingAnalysisRoute||'12');
        INSERT INTO route_span VALUES ('Route'||testingAnalysisRoute||'13', 'Span'||testingAnalysisRoute||'23');
        INSERT INTO route_logical_group SELECT 'Test Group', route_name FROM route;


-- DETECTIONS: -----------------------------------------------------------------

        UPDATE span_statistic 
            SET "last_journey_detection_timestamp" = NOW(),
            "last_reported_journey_time" = '00:00:45',
            "last_reported_journey_time_strength" = 19
        WHERE span_name = 'Span AB';
        
        
        --Script1: Show different status in the map for one route
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0101', NOW()-'00:20:00'::interval, 'Detector1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0101', NOW(), 'Detector2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0102', NOW()-'00:10:00'::interval, 'Detector2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0102', NOW(), 'Detector3');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0103', NOW()-'00:05:00'::interval, 'Detector3');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0103', NOW(), 'Detector4');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0104', NOW()-'00:10:00'::interval, 'Detector4');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0104', NOW(), 'Detector5');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0105', NOW()-'00:10:00'::interval, 'Detector5');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0105', NOW(), 'Detector6');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0106', NOW()-'00:05:00'::interval, 'Detector6');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0106', NOW(), 'Detector7');

        --Script2: Show jourmey times for one span
        --Just in case: Mean is the average, Median is the middle value and mode the most repetead value. If there is no 
        --one middle number, because there are an even number of numbers, the Median is the Mean of the two middle numbers.
        --Mode may have one, none, or multiple values.
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0201', NOW()-'23:58:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0203', NOW()-'23:54:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0201', NOW()-'23:50:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0203', NOW()-'23:50:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        --8 and 4 should display: Mean: 6, Median: 6, Mode: none;
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0204', NOW()-'14:12:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0205', NOW()-'14:11:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device02061', NOW()-'14:08:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device02062', NOW()-'14:09:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0204', NOW()-'14:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0205', NOW()-'14:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device02061', NOW()-'14:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device02062', NOW()-'14:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        --12, 11, 9 and 8 should display: Mean: 10, Median: 10, Mode: none;
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0207', NOW()-'12:20:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0208', NOW()-'12:15:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0209', NOW()-'12:05:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0207', NOW()-'12:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0208', NOW()-'12:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0209', NOW()-'12:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        --20, 15 and 5 should display: Mean: 13.3, Median: 15, Mode: none;
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0210', NOW()-'10:15:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0211', NOW()-'10:05:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0212', NOW()-'10:04:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0210', NOW()-'10:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0211', NOW()-'10:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0212', NOW()-'10:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        --15, 5 and 4 should display: Mean: 8, Median: 5, Mode: none;
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0213', NOW()-'00:05:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0214', NOW()-'00:03:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0215', NOW()-'00:03:00'::interval, 'Detector'||testingAnalysisSpan||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0213', NOW()-'00:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0214', NOW()-'00:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0215', NOW()-'00:00:00'::interval, 'Detector'||testingAnalysisSpan||'2');
        --5, 3 and 3 should display: Mean: 3.6, Median: 3, Mode: 3;

        --Script3: Show jourmey times for one route:
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0301', NOW()-'23:10:00'::interval, 'Detector'||testingAnalysisRoute||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0301', NOW()-'23:05:00'::interval, 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0302', NOW()-'19:15:00'::interval, 'Detector'||testingAnalysisRoute||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0302', NOW()-'19:05:00'::interval, 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0303', NOW()-'15:05:00'::interval, 'Detector'||testingAnalysisRoute||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0303', NOW()-'15:00:00'::interval, 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0304', NOW()-'00:05:00'::interval, 'Detector'||testingAnalysisRoute||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0304', NOW()-'00:00:00'::interval, 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0305', NOW()-'23:10:00'::interval, 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0305', NOW()-'23:05:00'::interval, 'Detector'||testingAnalysisRoute||'3');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0306', NOW()-'10:05:00'::interval, 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0306', NOW()-'10:00:00'::interval, 'Detector'||testingAnalysisRoute||'3');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0307', NOW()-'5:15:00'::interval, 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0307', NOW()-'5:05:00'::interval, 'Detector'||testingAnalysisRoute||'3');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0308', NOW()-'00:05:00'::interval, 'Detector'||testingAnalysisRoute||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0308', NOW()-'00:00:00'::interval, 'Detector'||testingAnalysisRoute||'3');

        --Script4: Analysis of one detector
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0401', NOW()-'23:10:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0402', NOW()-'23:10:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0403', NOW()-'23:10:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0404', NOW()-'23:10:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0405', NOW()-'20:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0406', NOW()-'17:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0407', NOW()-'17:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0408', NOW()-'15:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0409', NOW()-'15:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0410', NOW()-'15:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0411', NOW()-'10:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0412', NOW()-'10:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0413', NOW()-'05:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0414', NOW()-'05:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0415', NOW()-'05:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0416', NOW()-'02:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0417', NOW()-'01:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0418', NOW()-'00:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0419', NOW()-'00:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0420', NOW()-'00:00:00'::interval, 'Detector'||testingAnalysisDetector||'1');

        --Script5: Diagnostic of three Detectors
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0501', NOW(), 'Detector'||testingDiagnosticDetector||'1');
        INSERT INTO occupancy VALUES (DEFAULT, 'Detector'||testingDiagnosticDetector||'1', NOW(), 1, 2, 3, 4, 5, NULL, NULL, 0);
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0502', NOW()-'00:40:00'::interval, 'Detector'||testingDiagnosticDetector||'2');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0503', NOW()-'24:00:00'::interval, 'Detector'||testingDiagnosticDetector||'3');

        --Script6: Just to make old detectors, spans and routes reporting        
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0605', NOW()-'00:10:00'::interval, 'A');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0605', NOW()-'00:05:00'::interval, 'B');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0605', NOW()-'00:02:00'::interval, 'C');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0605', NOW(), 'D');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0606', NOW()-'00:05:00'::interval, 'A');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0606', NOW()-'00:00:00'::interval, 'B');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0601', NOW(), 'A');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0602', NOW(), 'B');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0603', NOW(), 'C');
        INSERT INTO device_detection VALUES (DEFAULT, 'Device0604', NOW(), 'D');

        --Script7: Occupancy reports for one detector
        INSERT INTO occupancy VALUES (DEFAULT, 'Detector'||testingReportTrafficFlow||'1', NOW()-'20:00:00'::interval, 3, 1, 1, 1, 2, NULL, NULL, 0);
        INSERT INTO occupancy VALUES (DEFAULT, 'Detector'||testingReportTrafficFlow||'1', NOW()-'10:00:00'::interval, 1, 2, 1, 1, 1, NULL, NULL, 0);
        INSERT INTO occupancy VALUES (DEFAULT, 'Detector'||testingReportTrafficFlow||'1', NOW()-'01:00:00'::interval, 1, 1, 3, 1, 1, NULL, NULL, 0);
        INSERT INTO occupancy VALUES (DEFAULT, 'Detector'||testingReportTrafficFlow||'1', NOW()-'00:30:00'::interval, 1, 1, 1, 2, 1, NULL, NULL, 0);
        INSERT INTO occupancy VALUES (DEFAULT, 'Detector'||testingReportTrafficFlow||'1', NOW()-'00:00:00'::interval, 2, 1, 1, 1, 3, NULL, NULL, 0);
     
        INSERT INTO detector_unconfigured VALUES ('instation12345', NULL, '2013-02-08 12:29:26+00', NULL, NULL, NULL);

-- DETECTIONS ------------------------------------------------------------------

END$$;
