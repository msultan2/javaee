#!/bin/bash

# This script creates and deploys scripts that periodically compact mongo brdf database
# The script is deployed into /etc/cron.daily to be run on a daily basis.


check_for_root_permissions() {
    if [[ $UID != 0 ]]
    then
        echo "Please start the script as root or sudo!"
        exit 1
    fi
}


create_scripts() {
    # Create compact_brdf_database.js
    cat > /opt/brdf/share/compact_brdf_database.js <<'_EOF'
/**
* This scripts compacts the database
* The script must be run when the database is run in authenticated mode (see
* auth parameter in /etc/mongodb.conf).
*/

//Connect to database in authenticated mode
conn = new Mongo();

databaseAddress="localhost";
databasePort="27017";

db = connect(databaseAddress + ":" + databasePort + "/brdf");
db.auth("brdf_admin","ssl1324");


// Get a the current collection size.
var storage = db.detections.storageSize();
var total = db.detections.totalSize();

print('Storage Size: ' + tojson(storage));
print('TotalSize: ' + tojson(total));

print('-----------------------');
print('Running db.repairDatabase()');
print('-----------------------');

// Run repair
db.repairDatabase()

// Get new collection sizes.
var storage_a = db.detections.storageSize();
var total_a = db.detections.totalSize();

print('Storage Size: ' + tojson(storage_a));
print('TotalSize: ' + tojson(total_a));
_EOF

    # Create compact_brdf_database.sh
    cat > /opt/brdf/bin/compact_brdf_database.sh <<'_EOF'
#!/bin/bash

JAVA_SCRIPT_DIR=/opt/brdf/share

mongo $JAVA_SCRIPT_DIR/compact_brdf_database.js
_EOF
    chmod 755 /opt/brdf/bin/compact_brdf_database.sh
}


install_script() {
    echo "Installing script to compact brdf database"

    # Install program files
    if [ -d /etc/cron.daily ]
    then
        install --mode=755 /opt/brdf/bin/compact_brdf_database.sh /etc/cron.daily
    else
        echo "ERROR: Directory /etc/cron.daily does not exist. Is cron program installed?"
    fi
}


main() {
    check_for_root_permissions

    mkdir -p /opt/brdf/{bin,share}
    create_scripts
    install_script

    echo "Done!"
}


main "$@"
