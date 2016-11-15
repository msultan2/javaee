
\i lib/CheckServer.sql

DO $$
    DECLARE  
        
    BEGIN        
        RAISE INFO 'Delete Data';

        DELETE FROM device_detection;
        DELETE FROM device_detection_historic;
        DELETE FROM span_journey_detection;
        DELETE FROM span_journey_detection_analytics;
        DELETE FROM span_journey_detection_cache;
        DELETE FROM detector_unconfigured;
        DELETE FROM occupancy;
        DELETE FROM detector_performance;
        DELETE FROM fault_message;
        DELETE FROM fault_report;
        DELETE FROM statistics_device;
        DELETE FROM statistics_report;
        DELETE FROM audit_trail;

        DELETE FROM audit_trail_action;
        DELETE FROM branding;
        DELETE FROM branding_contact_details;
        DELETE FROM broadcast_message;
        DELETE FROM logical_group;
        DELETE FROM instation_role;
        DELETE FROM instation_user_timezone;
        DELETE FROM instation_user;
        DELETE FROM instation_user_role;    
        DELETE FROM instation_user_logical_group;
        DELETE FROM span_journey_average_duration;
        DELETE FROM detector_configuration;
        DELETE FROM detector_statistic;
        DELETE FROM detector_last_rnd;
        DELETE FROM detector_status; 
        DELETE FROM detector;
        DELETE FROM detector_logical_group;
        DELETE FROM span;
        DELETE FROM span_osrm;
        DELETE FROM span_logical_group;
        DELETE FROM route_datex2;
        DELETE FROM route;
        DELETE FROM route_span;
        DELETE FROM route_logical_group;
        DELETE FROM detector_statistic;
        DELETE FROM detector_status;
        DELETE FROM detector_last_rnd;
    END
$$;
