#!/bin/bash

# This script creates and deploys scripts that purge mongo brdf database
# detection records that are older than 2 hour. The script is deployed 
# into /etc/cron.hourly to be run on an hourly basis.


check_for_root_permissions() {
    if [[ $UID != 0 ]]
    then
        echo "Please start the script as root or sudo!"
        exit 1
    fi
}


create_purge_scripts() {
    # Create purge_outdated_records.js
    cat > /opt/brdf/share/purge_outdated_records.js <<'_EOF'
/**
 * This scripts purges outdated record
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
var currentPosixTimeInSeconds=0;
var obsolesencePeriodInSeconds=7200; //period after which the records should be removed
var timeThresholdInSeconds=currentPosixTimeInSeconds - obsolesencePeriodInSeconds;
db.detections.remove({"time": {$lt: timeThresholdInSeconds}})
print("All outdated records have been removed");
_EOF

    # Create purge_outdated_records.sh
    cat > /opt/brdf/bin/purge_outdated_records.sh <<'_EOF'
#!/bin/bash

JAVA_SCRIPT_DIR=/opt/brdf/share

POSIX_TIME=`date +%s`
sed -i 's/var\scurrentPosixTimeInSeconds=[0-9]*/var currentPosixTimeInSeconds='$POSIX_TIME'/' $JAVA_SCRIPT_DIR/purge_outdated_records.js
mongo $JAVA_SCRIPT_DIR/purge_outdated_records.js
_EOF
    chmod 755 /opt/brdf/bin/purge_outdated_records.sh
}


install_purge_script() {
    echo "Installing script to purge outdated records"
    
    # Install program files
    if [ -d /etc/cron.hourly ]
    then
        install --mode=755 /opt/brdf/bin/purge_outdated_records.sh /etc/cron.hourly
    else
        echo "ERROR: Directory /etc/cron.hourly does not exist. Is cron program installed?"
    fi
}


main() {
    check_for_root_permissions

    create_purge_scripts
    install_purge_script
    
    echo "Done!"
}


main "$@"
