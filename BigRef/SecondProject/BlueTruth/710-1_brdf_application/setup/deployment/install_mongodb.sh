#!/bin/sh

# This script installs mongodb on CentOS for BRDF application. 
# Additionally it installs brdf database, its users and changes
# ip address and authentication to on

check_for_root_permissions() {
    if [[ $UID != 0 ]]
    then
        echo "Please start the script as root or sudo!"
        exit 1
    fi
}

install_mongodb() {
    # Add mongo repository
    MONGO_REPO_FILE=/etc/yum.repos.d/mongodb-org-3.0.repo
    if [ ! -f $MONGO_REPO_FILE ]
    then
        mkdir -p /etc/yum.repos.d/
        cat > $MONGO_REPO_FILE << '_EOF'
[mongodb-org-3.0]
name=MongoDB Repository
baseurl=http://repo.mongodb.org/yum/redhat/6/mongodb-org/3.0/x86_64/
gpgcheck=0
enabled=1
_EOF
    fi

    # Install mongo database
    yum update
    yum install -y mongodb-org

    # Start mongo service
    service mongod start

    # Sleep a bit to be sure that the service has fully restarted
    sleep 5
}

create_logrotate_entry_for_mongodb() {
    # Create logrotate entry to manage mongodb log directory
    mkdir -p /etc/logrotate.d
    cat > /etc/logrotate.d/mongo << '_EOF'
/var/log/mongodb/*.log {
    daily
    rotate 7
    compress
    dateext
    missingok
    notifempty
    sharedscripts
    copytruncate
    postrotate
        /bin/kill -SIGUSR1 `cat /var/lib/mongo/mongod.lock 2> /dev/null` 2> /dev/null || true
    endscript
}
_EOF
}


create_mongodb_brdf_users() {
    # Create a stipped down version of users.js file
    cat > /opt/brdf/share/users.js <<'_EOF'
/**
users.js - for full description look at /opt/brdf/etc/users.js
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
db.system.version.insert( {"_id": "authSchema", "currentVersion": 3} );

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

_EOF

    # Install brdf database and define its users
    mongo /opt/brdf/share/users.js
    [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    rm -f users.js
    echo "BRDF database has been initialised"
}

secure_mongodb() {
    # Enable authorisation
    sed -i 's/#\s*auth.*=.*true.*/auth = true/g' /etc/mongod.conf

    # Set mongo to work on eth0
    sed -i 's/\(bind_ip.*\)/#\1/g' /etc/mongod.conf

    # Restart mongo service
    service mongod restart

    # Sleep a bit to be sure that the service has fully restarted
    sleep 5
}


verify_mongodb_connectivity() {
    # Create test_db_connection.js
    cat > /opt/brdf/share/test_db_connection.js <<'_EOF'
//Connect to database and show system version and all users

print("System version contents:");
cursor = db.system.version.find();
while ( cursor.hasNext() ) {
   printjsononeline( cursor.next() );
}

print("System users:");
cursor = db.system.users.find();
while ( cursor.hasNext() ) {
   printjsononeline( cursor.next() );
}
_EOF

    # Create test_db_connection.sh
    cat > /opt/brdf/bin/test_db_connection.sh <<'_EOF'
#!/bin/bash

# Connect to mongo database as brdf_client with password and run test.js script
mongo --username admin --password ssl1324 --host `hostname -I` admin /opt/brdf/share/test_db_connection.js
_EOF
    chmod 755 /opt/brdf/bin/test_db_connection.sh

    # Run test_db_connection.sh script and check if everything is OK
    /opt/brdf/bin/test_db_connection.sh
    [ $? -eq 0 ] || exit $?; # exit for none-zero return code
}


main() {
    check_for_root_permissions

    install_mongodb
    create_logrotate_entry_for_mongodb

    mkdir -p /opt/brdf/{bin,share}
    create_mongodb_brdf_users
    secure_mongodb
    verify_mongodb_connectivity

    echo "Done!"
}


main "$@"
