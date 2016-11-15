/**
 * This scripts defines the mongo database structure and access control of BRDF
 * (BlueTruth Raw Data Feed). The concept of BRDF has been defined in SSL4253.
 * The script must be run when the database is run in unauthenticated mode on
 * localhost and port 27017.
 *
 *
 * Database structure description:
 *
 * There are 3 collections: owners, detectors and detections.
 * owners - defines detector owners, its name, URL to which to report
 *     data and if to send the data,
 * detectors - defines existing detectors (OutStations) and their membership
 *     in owner collection,
 * detections - defines the data to be passed to the BRDF server (as specified
 *     in SSL4253).
 *
 * There are 3 users defined:
 * - instation and brdf_client - both can read and write records,
 * - brdf_admin - owns the database
 *
 * InStation and BRDF client responsibilities:
 *
 * The actions are split between the InStation and BRDF client.
 * The Instation should:
 * - add detections,
 * - regularly delete obsoleted records.
 *
 * The BRDF client should:
 * - retrieve the BRDF data (detections) filtered by owner and sorted by
 *     time and pass them to the BRDF server,
 * - delete the BRDF data after having sent.
 *
 * This mongo sript:
 * 1. creates 3 collections: detectors, owners and detections,
 * 2. creates all the corresponding indexes to optimise deletion of obsoleted
 *     records and retrieving the BRDF data (detections) filtered by owner and sorted by time,
 * 3. creates database users.
*/


//Connect to database
conn = new Mongo();

databaseAddress="localhost";
databasePort="27017";

// Remove test database
db = connect(databaseAddress + ":" + databasePort + "/test");
db.dropDatabase();

//Change authentication schema so that we can access database with password
db = connect(databaseAddress + ":" + databasePort + "/admin");
db.system.version.update( {"_id": "authSchema"}, {"currentVersion": 3} );

// Add admin user, a user which can create users and assign roles
db.dropAllUsers();
db.createUser( { "user": "admin",  "pwd": "ssl1324", "roles": [ { role: "userAdminAnyDatabase", db: "admin" } ] } );
db.auth("admin","ssl1324");

// Connect to the brdf database
db = connect(databaseAddress + ":" + databasePort + "/brdf");

db.owners.drop();
db.detectors.drop();
db.detections.drop();


//Create index on the name field of owners collection
db.owners.createIndex({name: 1}, { unique: true });

//Create index on the owner field of the detectors collection
db.detectors.createIndex({name: 1}, { unique: true });

//Create index on the "time" and "outstationId" fields
db.detections.createIndex({"outstationId": 1, "time" : 1}, {unique: true });
db.detections.createIndex({"time" : 1});

db.dropAllUsers();
db.createUser( { "user": "brdf_admin",  "pwd": "ssl1324", "roles": [ { role: "dbOwner", db: "brdf" } ] } );
db.createUser( { "user": "instation",   "pwd": "ssl1324", "roles": [ { role: "readWrite", db: "brdf" } ] } );
db.createUser( { "user": "brdf_client", "pwd": "ssl1324", "roles": [ { role: "readWrite", db: "brdf" } ] } );


/*
 * From now on connect to brdf database in the following way:
 * mongo -u brdf_client -p ssl1324 --authenticationDatabase brdf
 * or
 * mongo -u instation -p ssl1324 --authenticationDatabase brdf
 *
 * To change any user of drop database:
 * mongo -u brdf_admin -p ssl1324 --authenticationDatabase brdf
 */
