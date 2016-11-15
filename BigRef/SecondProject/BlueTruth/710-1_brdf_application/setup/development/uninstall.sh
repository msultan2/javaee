#!/bin/bash


INSTALL_DIR=/opt/brdf

echo "Uninstalling BRDF"

#First stop brdf
service brdfd stop
/sbin/chkconfig --del brdfd

#Remove program files
rm -rf $INSTALL_DIR/bin/brdf
rm -rf $INSTALL_DIR/bin/deleteoldestfile.sh
rm -rf $INSTALL_DIR/bin/uninstall.sh
rm -rf $INSTALL_DIR/etc/brdfconfiguration.xml
rm -rf $INSTALL_DIR/etc/brdfconfiguration.xsd
rm -rf $INSTALL_DIR/share/database.js
rm $INSTALL_DIR/lib/libboost_*

rm -f /etc/init.d/brdfd
rm -r /etc/cron.hourly/checklogfiles.sh

# Remove directories if empty
[ "$(ls -A /opt/brdf/bin)" ] || rmdir /opt/brdf/bin
[ "$(ls -A /opt/brdf/etc)" ] || rmdir /opt/brdf/etc
[ "$(ls -A /opt/brdf/lib)" ] || rmdir /opt/brdf/lib
[ "$(ls -A /opt/brdf/share)" ] || rmdir /opt/brdf/share

#Remove working directories
rm -rf /var/cache/brdf
rm -rf /var/log/brdf

echo "Done!"
