#!/bin/bash

# This script installs necessary packages, libraries and tools
# required to compile BRDF application

AUTOCONF_VERSION=2.69
AUTOMAKE_VERSION=1.15
LIBTOOL_VERSION=2.4.6
FLEX_VERSION=2.5.39
BISON_VERSION=3.0.4
BOOST_VERSION=1_57_0
MONGO_CXX_DRIVER_VERSION=legacy-0.0-26compat-2.6.9
MAKESELF_VERSION=release-2.2.0

STAMPS=./stamps
DOWNLOADS=./downloads

INSTALL_DIR=/usr/local

# Create stamps directory if it does not exist
mkdir -p $STAMPS


# Install compilation packages and utilities
if [ ! -f $STAMPS/compilation_tools.build ]
then
    sudo yum -y install wget gcc gcc-c++ perl git bzip2-devel python-devel make scons vim mc
    echo "Compilation tools have been installed!"
    touch $STAMPS/compilation_tools.build
else
    echo "Compilation tools have already been installed. Skipping..."
fi


function download {
    FILE=$1
    LOCATION=$2

    if [ ! -f $DOWNLOADS/$FILE ]
    then
        wget -c -P $DOWNLOADS $LOCATION/$FILE
        if [ ! -f "$DOWNLOADS/$FILE" ]
        then
            echo "Unable to download the file $FILE. Exitting..."
            exit 1
        fi
    fi

    unset FILE
    unset LOCATION
}

function compile_and_install {
    FILE=$1
    FILE_VERSION=$2

    NAME=`echo $FILE | sed  's/\([a-zA-Z]*\).*/\1/'`
    FILE_WITHOUT_EXTENSION=`echo $FILE | sed -e 's/\.tar\.gz//g'`

    if [ ! -f ${STAMPS}/${FILE_WITHOUT_EXTENSION}.build ]
    then
        echo "Installing $NAME version $FILE_VERSION..."

        tar -zxvf $DOWNLOADS/$FILE
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

        cd $FILE_WITHOUT_EXTENSION

        ./configure --prefix $INSTALL_DIR && make
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

        sudo make install
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

        cd ..

        touch "${STAMPS}/${FILE_WITHOUT_EXTENSION}.build"
        echo "$NAME version $FILE_VERSION has been installed!"
    else
        echo "$NAME version $FILE_VERSION has already been installed. Skipping..."
    fi

    unset FILE
    unset FILE_VERSION
    unset NAME
    unset FILE_WITHOUT_EXTENSION
}

function update_clone_from_git_repository {
    LOCATION=$1
    DIRECTORY=$2
    BRANCH=$3

    if [ ! -d $DIRECTORY ]
    then
        git clone $LOCATION
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    ls
        if [ ! -d "$DIRECTORY" ]
        then
        echo "Unable to clone the repository $LOCATION. Exitting..."
        exit 1
        fi

    echo "Repository $LOCATION has been cloned"
    cd $DIRECTORY
    git checkout $BRANCH
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    echo "Switched to $BRANCH branch of $LOCATION repository"
    cd ..
    else
    cd $DIRECTORY
    git fetch
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    git fetch --tags
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    git checkout $BRANCH
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    TAG_QUERY_RESULT=`git describe --exact-match --tags $BRANCH 2>&1`
    if [ $BRANCH = "$TAG_QUERY_RESULT" ]
    then
        IS_TAG=true
    else
        IS_TAG=false
    fi
    unset TAG_QUERY_RESULT

    if [ $IS_TAG = true ]
    then
            echo "Using git tag $BRANCH. No pull required"
        else
            echo "Using git branch $BRANCH. Pulling changes"
        git pull
            [ $? -eq 0 ] || exit $?; # exit for none-zero return code
            echo "git branch $BRANCH changes pulled"
        fi
        unset IS_TAG

    echo "Repository $LOCATION has been updated"
    echo "Switched to $BRANCH branch of $LOCATION repository"
    cd ..
    fi

    unset LOCATION
    unset DIRECTORY
    unset BRANCH
}


# Install autoconf
download autoconf-$AUTOCONF_VERSION.tar.gz http://ftp.gnu.org/gnu/autoconf
compile_and_install autoconf-$AUTOCONF_VERSION.tar.gz $AUTOCONF_VERSION


# Install automake
download automake-$AUTOMAKE_VERSION.tar.gz http://ftp.gnu.org/gnu/automake
compile_and_install automake-$AUTOMAKE_VERSION.tar.gz $AUTOMAKE_VERSION


# Install libtool
download libtool-$LIBTOOL_VERSION.tar.gz http://ftp.gnu.org/gnu/libtool
compile_and_install libtool-$LIBTOOL_VERSION.tar.gz $LIBTOOL_VERSION

# Install flex
download flex-$FLEX_VERSION.tar.gz http://sourceforge.net/projects/flex/files
compile_and_install flex-$FLEX_VERSION.tar.gz $FLEX_VERSION

# Install bison
download bison-$BISON_VERSION.tar.gz http://ftp.gnu.org/gnu/bison
compile_and_install bison-$BISON_VERSION.tar.gz $BISON_VERSION

# Install BOOST LIBRARY
download boost_$BOOST_VERSION.tar.gz http://sourceforge.net/projects/boost/files/boost/`echo $BOOST_VERSION | sed 's/_/./g'`
FILE=boost_$BOOST_VERSION.tar.gz
NAME=`echo $FILE | sed  's/\([a-zA-Z]*\).*/\1/'`
VERSION=$BOOST_VERSION
FILE_WITHOUT_EXTENSION=`echo $FILE | sed -e 's/\.tar\.gz//g'`

if [ ! -f ${STAMPS}/${FILE_WITHOUT_EXTENSION}.build ]
then
    echo "Installing $NAME version $VERSION..."

    tar -zxvf $DOWNLOADS/$FILE
    [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    cd $FILE_WITHOUT_EXTENSION

    ./bootstrap.sh
    [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    ./b2
    [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    sudo ./b2 install
    [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    cd ..
    touch "${STAMPS}/${FILE_WITHOUT_EXTENSION}.build"
    echo "$NAME version $VERSION has been installed!"
else
    echo "$NAME version $VERSION has already been installed. Skipping..."
fi


# Install mongo C++ driver
VERSION=$MONGO_CXX_DRIVER_VERSION
if [ ! -f ${STAMPS}/mongo_driver-$VERSION.build ]
then
    update_clone_from_git_repository https://github.com/mongodb/mongo-cxx-driver mongo-cxx-driver $VERSION

    cd mongo-cxx-driver
    sudo scons --prefix=$INSTALL_DIR --full --use-system-boost
    [ $? -eq 0 ] || exit $?; # exit for none-zero return code

    cd ..
    # Restore ownership because compilation was done as superuser
    sudo chown -R $USER mongo-cxx-driver

    touch "${STAMPS}/mongo_driver-$VERSION.build"
    echo "mongo driver version $VERSION has been installed!"
else
    echo "mongo driver version $VERSION has already been installed. Skipping..."
fi


# Install makeself
VERSION=$MAKESELF_VERSION
if [ ! -f ${STAMPS}/makeself-$VERSION.build ]
then
    update_clone_from_git_repository https://github.com/megastep/makeself makeself $VERSION

    cd makeself
    sudo cp -f *.sh $INSTALL_DIR/bin
    sudo ln -fs $INSTALL_DIR/bin/makeself.sh $INSTALL_DIR/bin/makeself
    cd ..
    touch "${STAMPS}/makeself-$VERSION.build"
    echo "makeself version $VERSION has been installed!"
else
    echo "makeself version $VERSION has already been installed. Skipping..."
fi
