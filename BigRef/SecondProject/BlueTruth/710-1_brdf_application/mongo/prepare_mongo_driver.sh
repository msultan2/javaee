#!/bin/sh

MONGO_DRIVER_DIR=mongo-cxx-driver
TAG=legacy-0.0-26compat-2.6.7
OPTIONS=--use-system-boost
INSTALL_OPTIONS=--full
#TAG=legacy-1.0.0
#OPTIONS=--prefix=/usr/local 
#INSTALL_OPTIONS=install

# Clone repository if not cloned yet
if [ ! -d "$MONGO_DRIVER_DIR" ]
then
    git clone -b 26compat https://github.com/mongodb/mongo-cxx-driver
fi
cd $MONGO_DRIVER_DIR

# Update and checkout a specific tag
git fetch --tags
git checkout $TAG

# Compile the package
scons $OPTIONS
# Install the package
sudo rm -rf /usr/local/include/mongo /usr/local/lib/libmongoclient.a
sudo scons $OPTIONS $INSTALL_OPTIONS
