#!/bin/bash

yum -y install gcc kernel-devel kernel-headers dkms make bzip2 perl wget

VIRTUALBOX_VERSION=4.3.10
wget -nc http://dlc-cdn.sun.com/virtualbox/${VIRTUALBOX_VERSION}/VBoxGuestAdditions_${VIRTUALBOX_VERSION}.iso
mkdir -p /mnt/guest
mount -o loop VBoxGuestAdditions_${VIRTUALBOX_VERSION}.iso /mnt/guest
cd /mnt/guest
./VBoxLinuxAdditions.run
