/**
 * This scripts defines the mongo database activities of BRDF (BlueTruth Raw
 * Data Feed). The concept of BRDF has been defined in SSL4253.
 * The script must be run when the database is run in authenticated mode (see
 * auth parameter in /etc/mongodb.conf).
 *
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
 *
 * InStation and BRDF client responsibilities:
 *
 * The actions are split between the InStation and BRDF client.
 * The Instation should:
 * - regularly delete obsoleted records.
 *
 * The BRDF client should:
 * - retrieve the BRDF data (detections) filtered by owner and sorted by
 *     time and pass them to the BRDF server,
 * - delete the BRDF data after having sent.
 *
 * This mongo sript:
 * 1. inserts a few exemplary data records,
 * 2. prints database contents,
 * 3. removes outdated detections,
 * 4. verifies if owner of a defined name exists,
 * 5. retrieves the owner detectors and verifies if any sensors exist,
 * 6. retrieves the detections of owner detectors in time ascending order
 *    in batches and prints and deletes them,
 * 7. prints what is left in the database.
 * Actions 1 and 3 should be performed by the InStation.
 * Actions 6, 7 and 8 should be performed by the BRDF client.
 */

//Connect to database
conn = new Mongo();

//databaseAddress="82.109.252.45";
//databasePort="30005";
databaseAddress="localhost";
databasePort="27017";

db = connect(databaseAddress + ":" + databasePort + "/brdf");
db.auth("instation","ssl1324");


db.detections.remove({});

//Insert a few records of data
db.detections.insert({
	"outstationId":"A60PD-1","time":1424297947,"duration":60, "status":"OK",
	"detections": [
		{"id":"0123456789ABCDEF","startTime":1424297947,"refTime":10,"endTime":20},
		{"id":"FEDCBA9876543210","startTime":1424297951,"refTime":15,"endTime":30}
	]});
db.detections.insert({
	"outstationId":"A60PD-1","time":1424298957,"duration":60, "status":"OK",
	"detections": [
		{"id":"0123456789ABCDEF","startTime":1424297947,"refTime":10,"endTime":20},
		{"id":"FEDCBA9876543210","startTime":1424297951,"refTime":15,"endTime":30}
	]});
db.detections.insert({
	"outstationId":"A60PD-4","time":1424298947,"duration":60, "status":"OK",
	"detections": [
		{"id":"0123456789ABCDEF","startTime":1424297947,"refTime":10,"endTime":20},
		{"id":"FEDCBA9876543210","startTime":1424297951,"refTime":15,"endTime":30}
	]});


//Print what is in the data base
print("Records that are stored in database:");
print("----------");
print("Detections:");
cursor = db.detections.find();
while ( cursor.hasNext() ) {
   printjsononeline( cursor.next() );
}
print("----------");


print("++++++++++");
