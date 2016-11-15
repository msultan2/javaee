
DO $$
    DECLARE  
        databaseAddress varchar;
        dataBaseName varchar;
        databaseAddressFound varchar;
        dataBaseNameFound varchar;        
    BEGIN
        SELECT current_setting('testSettings.databaseAddress') INTO databaseAddress;
        SELECT current_setting('testSettings.dataBaseName') INTO dataBaseName;        
        SELECT inet_server_addr() INTO databaseAddressFound;
        SELECT current_database() INTO dataBaseNameFound;        

        IF (databaseAddress = databaseAddressFound AND dataBaseName = dataBaseNameFound) THEN
            RAISE INFO 'Check Server: Script is running in the expected database, with address % and name %.', databaseAddress , dataBaseName;
        ELSE
            RAISE EXCEPTION 'Check Server: Excepted database with address % and name %, but found database with addres % and name %. As a security measure, this script will only execute in the database with address % and name %. Change the values of databaseAddress and dataBaseName at SettingsStartUp if needed.', databaseAddress, dataBaseName, databaseAddressFound, dataBaseNameFound, databaseAddress, dataBaseName;
        END IF;
    END
$$;


