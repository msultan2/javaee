#!/bin/bash


check_for_root_permissions() {
    if [[ $UID != 0 ]]
    then
        echo "Please start the script as root or sudo!"
        exit 1
    fi
}


installerCheck()
{
    ## check if BRDF is running
    ps ax | grep brdf | grep -v ".*brdf.*" > /dev/null
    if [ $? -eq 0 ]; then
        echo Please close all instances of BRDF before running this installer
        exit 1
    fi

    if [ ${noLibraryCheck:-0} -eq 1 ]; then
        return 0 
    fi

    ## check bitness of system
    bit=`getconf LONG_BIT`
    if [ $bit -eq 64 ]; then
        echo "64 Bit system, OK"
    else
        echo "Error! This program can be only run on 64 bit Linux"
        exit 1
    fi
}


main() {
    echo "Installing BRDF"

    check_for_root_permissions
    installerCheck

    INSTALL_DIR=/opt/brdf

    # Instal program files
    install -d --mode=755 $INSTALL_DIR/{bin,etc,lib,share}
    install --mode=755 bin/brdf $INSTALL_DIR/bin
    install --mode=755 bin/deleteoldestfile.sh $INSTALL_DIR/bin
    install --mode=755 bin/uninstall.sh $INSTALL_DIR/bin
    install --mode=644 etc/brdfconfiguration.xml $INSTALL_DIR/etc
    install --mode=644 etc/brdfconfiguration.xsd $INSTALL_DIR/etc
    install --mode=644 share/database.js $INSTALL_DIR/share
    install --mode=755 lib/libboost_* $INSTALL_DIR/lib
    install --mode=755 bin/checklogfiles.sh /etc/cron.hourly

    # Set external interface ip address in the configuration file
    IP_ADDRESS=`hostname -I | xargs`
    sed -i "s/.*<host>.*<\/host>/\t\t<host>$IP_ADDRESS<\/host>/g" $INSTALL_DIR/etc/brdfconfiguration.xml

    # Create working directories
    install -d --mode=777 /var/cache/brdf
    install -d --mode=777 /var/log/brdf

    # Add service to startup and start it
    install --mode=755 bin/brdfd /etc/init.d
    /sbin/chkconfig --add brdfd
    service brdfd start

    echo "INFO: Review the contents of configuration file /opt/brdf/etc/brdfconfiguration.xml"
    echo "      and restart the service with command:"
    echo "      sudo service brdfd restart"

    echo "Done!"
}


main "$@"
