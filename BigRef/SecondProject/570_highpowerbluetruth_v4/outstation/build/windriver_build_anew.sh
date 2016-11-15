#!/bin/sh

#Fix this location to match the actual setup
export SYSROOT_PATH=$HOME/windriver/bluetruth_build/host-cross/i586-wrs-linux-gnu/x86-linux2
export PATH=$SYSROOT_PATH:$PATH

if [ -f "Makefile" ]
then 
    make maintainer-clean
fi

echo "Fixing a bug in boost library file locks.hpp..."
patch $SYSROOT_PATH/../sysroot/usr/include/boost/thread/locks.hpp -i locks.hpp.patch --forward

CC=x86_32-target-linux-gnu-gcc CXX=x86_32-target-linux-gnu-g++ ../configure --prefix=/opt/bt --localstatedir=/var CFLAGS="-g -O2" CXXFLAGS="-g -O2" && \
make && \
echo "Done!"
