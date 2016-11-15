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

databaseAddress="82.109.252.45";
databasePort="30005";
//databaseAddress="localhost";
//databasePort="27017";

db = connect(databaseAddress + ":" + databasePort + "/brdf");
db.auth("instation","ssl1324");

//Remove all existing records in the database
db.owners.remove({});
db.detectors.remove({});
db.detections.remove({});


//Insert owners into the collection
db.owners.insert({name: "ssl", full_name: "Simulation Systems Ltd"})
db.owners.insert({name: "cv", full_name: "ClearView", report_url: "http://127.0.0.1/path_to_report:2000", send_reports: 1 })
db.owners.insert({name: "ha", full_name: "Highways Agency"})
db.owners.insert({name: "void", full_name: "Void User"})


//Insert detectors into the collection
db.detectors.insert({name: "A60PD-1", owner: "cv"});
db.detectors.insert({name: "A60PD-2", owner: "cv"});
db.detectors.insert({name: "A60PD-3", owner: "cv"});
db.detectors.insert({name: "A60PD-4", owner: "ssl"});
db.detectors.insert({name: "A60SL-4", owner: "ssl"});
db.detectors.insert({name: "A60SL-5", owner: "ssl"});
db.detectors.insert({name: "A60SL-6", owner: "ssl"});
db.detectors.insert({name: "M4/3665A", owner: "ha"});
db.detectors.insert({name: "M4/3670A", owner: "ha"});
db.detectors.insert({name: "M4/3675A", owner: "ha"});


//Create index on the "time" and "outstationId" fields
db.detections.createIndex({"outstationId": 1, "time" : 1}, {unique: true });
db.detections.createIndex({"time" : 1});
//Insert a few records of data
db.detections.insert({
	"outstationId":"A60PD-1","time":1424297947,"duration":60, "status":"OK",
	"detections": [
		{"id":"0123456789ABCDEF","startTime":1424297947,"refTime":10,"endTime":20},
		{"id":"FEDCBA9876543210","startTime":1424297951,"refTime":15,"endTime":30}
	]});
db.detections.insert({
	"outstationId":"A60PD-1","time":1424298947,"duration":60, "status":"OK",
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
print("Owners:");
cursor = db.owners.find();
while ( cursor.hasNext() ) {
   printjsononeline( cursor.next() );
}
print("----------");
print("Detectors:");
cursor = db.detectors.find();
while ( cursor.hasNext() ) {
   printjsononeline( cursor.next() );
}
print("----------");
print("Detections:");
cursor = db.detections.find();
while ( cursor.hasNext() ) {
   printjsononeline( cursor.next() );
}
print("----------");


var ownerName="cv";
var timeThreshold=1424297000;
var numberOfRecordsPerBatch=1;
print("++++++++++");
print("Sending records for owner \"" + ownerName + "\" with time greater than " + timeThreshold);
print("++++++++++");


//Remove outdated records
db.detections.remove({"time": {$lt: timeThreshold}})
print("All outdated records have been removed");


//Check if specified owner exists
owner = db.owners.findOne({name: ownerName}, {name: 1, _id: 0});
if (!owner)
	throw new Error("Owner \"" + ownerName + "\" not found");

print("Owner \"" + ownerName + "\" found:")
printjsononeline(owner);


//Retrieve all detectors to be reported
ownerDetectors = db.detectors.find(
	{owner: ownerName}, {name: 1, _id: 0}
	).toArray().map(function(doc) { return doc['name']});
if (ownerDetectors.length == 0)
	throw new Error("No sensors found for owner \"" + ownerName + "\"");

print("Detectors belonging to owner\"" + ownerName + "\"");
printjsononeline(ownerDetectors);


//Find detections for the owner detectors
/*
//Implementation giving all results in an array which can be expensive if
//amount of records is big.

detectionIndices = db.detections.find(
	{"outstationId": {$in: ownerDetectors}, "time": {$gte: timeThreshold}}, {_id: 1}
	).toArray().map(function(doc) { return doc['_id']});
if (detectionIndices.length == 0)
	throw new Error("No detections found for owner \"" + ownerName + "\"");

print("Indices of records to be sent:");
printjsononeline(detectionIndices);


//Print the records to be sent
print("Records to be sent:");
cursor = db.detections.find({"_id": {$in: detectionIndices}});
while ( cursor.hasNext() ) {
   printjsononeline( cursor.next() );
}
*/

while (true)
{
	var detectionIndices = [];
	cursor = db.detections.find(
		{"outstationId": {$in: ownerDetectors}}
		).sort({"time": 1}).limit(numberOfRecordsPerBatch);

	var numberOfRecordsToProcess = cursor.objsLeftInBatch();
	if (numberOfRecordsToProcess == 0)
	{
		//No more records
		break;
	}
	//else continue

	print(numberOfRecordsToProcess + " records to be sent:");
	while ( cursor.hasNext() ) {
	   detection = cursor.next();
	   printjsononeline(detection);

	   detectionIndices.push(detection._id);
	}

	print(detectionIndices);

	//After sending of records remove them
	db.detections.remove({"_id": {$in: detectionIndices}});
	print("All sent records have been removed");
}


//Print what is left in the data base
print("Records that have been left in database:");
cursor = db.detections.find();
while ( cursor.hasNext() ) {
   printjsononeline( cursor.next() );
}

print("++++++++++");
