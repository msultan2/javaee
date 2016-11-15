#!/bin/bash

TMP_DIR=/tmp/brdf

BOOST_DIR=/usr/local/lib
BOOST_VERSION=1.57

MONGO_DIR=/usr/local/lib

#Prepare temporary directory and copy installation files inside
rm -rf $TMP_DIR
mkdir -p --mode 755 $TMP_DIR/{bin,etc,lib,init.d,share}
cp ../../build/brdf  $TMP_DIR/bin
cp ../../misc/brdfd  $TMP_DIR/bin
cp ./setup.sh  $TMP_DIR/bin
cp ./uninstall.sh  $TMP_DIR/bin
cp ../../misc/checklogfiles.sh  $TMP_DIR/bin
cp ../../misc/deleteoldestfile.sh  $TMP_DIR/bin
cp ../../misc/brdfconfiguration.xml  $TMP_DIR/etc
cp ../../misc/brdfconfiguration.xsd  $TMP_DIR/etc
cp ../../mongo/database.js  $TMP_DIR/share
cp $BOOST_DIR/libboost_chrono.so.$BOOST_VERSION.0 $TMP_DIR/lib
cp $BOOST_DIR/libboost_date_time.so.$BOOST_VERSION.0 $TMP_DIR/lib
cp $BOOST_DIR/libboost_filesystem.so.$BOOST_VERSION.0 $TMP_DIR/lib
cp $BOOST_DIR/libboost_log_setup.so.$BOOST_VERSION.0 $TMP_DIR/lib
cp $BOOST_DIR/libboost_log.so.$BOOST_VERSION.0 $TMP_DIR/lib
cp $BOOST_DIR/libboost_program_options.so.$BOOST_VERSION.0 $TMP_DIR/lib
cp $BOOST_DIR/libboost_regex.so.$BOOST_VERSION.0 $TMP_DIR/lib
cp $BOOST_DIR/libboost_system.so.$BOOST_VERSION.0 $TMP_DIR/lib
cp $BOOST_DIR/libboost_thread.so.$BOOST_VERSION.0 $TMP_DIR/lib


#Create compressed binary instalation file
makeself $TMP_DIR brdf_setup.run "BlueTruth InStation Raw Data Feed (BRDF)" bin/setup.sh

#Remove installation files
rm -rf $TMP_DIR
