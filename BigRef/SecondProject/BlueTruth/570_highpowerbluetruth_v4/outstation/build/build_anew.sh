#!/bin/sh
if [ -f "Makefile" ]
then 
    make maintainer-clean
fi
../configure --enable-desktop --prefix=$HOME/bt CFLAGS="-g -O0" CXXFLAGS="-g -O0" && \
make -j`grep -c ^processor /proc/cpuinfo` && \
echo "Done!"

