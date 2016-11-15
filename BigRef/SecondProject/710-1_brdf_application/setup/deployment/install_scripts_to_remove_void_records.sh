#!/bin/bash

# This script creates and deploys scripts that removes mongo brdf database
# detection records of detectors for which we do not send reports.
# The script is deployed  into /etc/cron.hourly to be run on an hourly basis.


check_for_root_permissions() {
    if [[ $UID != 0 ]]
    then
        echo "Please start the script as root or sudo!"
        exit 1
    fi
}


create_remove_void_records_scripts() {
    # Create remove_void_records.js
    cat > /opt/brdf/share/remove_void_records.js <<'_EOF'
/**
 * This scripts removes records that of detectors for which we do not send reports.
 * The script must be run when the database is run in authenticated mode (see
 * auth parameter in /etc/mongodb.conf).
 */

//Connect to database in authenticated mode
conn = new Mongo();

databaseAddress="localhost";
databasePort="27017";

db = connect(databaseAddress + ":" + databasePort + "/brdf");
db.auth("instation","ssl1324");


//Remove outdated records
var detectorsFound = [];
db.detectors.find({}).forEach(function(doc) {
    if (doc.send_reports != '1') {
        detectorsFound.push(doc.name)
    } })

db.detections.remove({"outstationId": { $in: detectorsFound }})
print("All BRDF detections belonging to detectors with reporting disabled have been removed");
_EOF

    # Create remove_void_records.sh
    cat > /opt/brdf/bin/remove_void_records.sh <<'_EOF'
#!/bin/bash

JAVA_SCRIPT_DIR=/opt/brdf/share

mongo $JAVA_SCRIPT_DIR/remove_void_records.js
_EOF
    chmod 755 /opt/brdf/bin/remove_void_records.sh
}


install_remove_void_records_script() {
    echo "Installing script to remove void records"

    # Install cron file
    if [ -d /etc/cron.d ]
    then
        FILE=root_cron.tmp
        crontab -l > $FILE
        echo "*/5 * * * * /opt/brdf/bin/remove_void_records.sh" >> $FILE
        crontab $FILE
    else
        echo "ERROR: Directory /etc/cron.d does not exist. Is cron program installed?"
    fi
}


main() {
    check_for_root_permissions

    create_remove_void_records_scripts
    install_remove_void_records_script

    echo "Done!"
}


main "$@"
