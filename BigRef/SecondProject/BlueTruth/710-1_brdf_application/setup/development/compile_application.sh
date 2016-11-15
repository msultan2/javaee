#!/bin/bash

# This script compiles the BRDF application from scratch
cd ../..
echo "Running autoreconf -i ..."
autoreconf -i
[ $? -eq 0 ] || exit $?; # exit for none-zero return code

cd build
echo "Building the application ..."
./build_anew.sh
[ $? -eq 0 ] || exit $?; # exit for none-zero return code

cd ../setup/development
echo "Application has been build!"
