------------------------------------------------------------------

# High Power BlueTruth (HPBT) v.4  #
------------------------------------------------------------------

## HOW TO COMPILE THE PACKAGE USING WINDRIVER ##

The project contains 3 project:
- outstation,
- outstation_gui,
- google-test test framework.

The 'outstation' project is the main one, other projects are used for development.

To create the DOM bluetruth image do the following:

1) Prepare windriver environment for cross compilation.
- mount the windriver development directories (windriver/scripts/mount_windriver.sh):

    sudo mount 192.168.0.43:/usr/tools/WindRiver/ /usr/tools/WindRiver/

Mounting should be done only once for any PC restart.
- for each shell from which compilation will take place configure the environment (windriver/scripts/prepare_env.sh):

    /usr/tools/WindRiver/WRL4_3/wrenv.sh -p wrlinux-4
    export SYSROOT_PATH=`pwd`/common_pc_VDX-glibc_small/x86-linux2
    export PATH=$SYSROOT_PATH:$PATH

2) Compile the executable file:
- generate configuration files from outstation directory:

    cd outstation
    autoreconf -i

- compile the project:

    cd outstation/build
    windriver_build_anew.sh

3) Create DOM image:
- Configure the OutStation core configuration file (core_configuration.xml) that must be contained in the DOM image.
In particular verify the following entries:
* site identifier - must be unique accross all deployed OutStations communicating to the same InStation
* serial number - unique accross all OutStations and assigned during production
* InStation SSH connection parameters: address, login, password. These parameters 
will define how reverse SSH connection will be done when requested by the 
InStation. Leave password blank if public/private key pair is to be used.
The contents of this section must be agreed with InStation maintainer.
* Functional configuration URL: path, prefix and suffix. 
The contents of this section must be agreed with InStation maintainer.
The current version of the InStation creates a separate configuration file for each OutStation and the suffix should contain entry <site identifier>_ini.txt
- Create compressed image of BlueTruth application bt.tgz (compress_image.sh)

    cd windriver/scripts
    rm -f bt.tgz
    cp -f ../../outstation/build/outstation bin
    tar cvfz bt.tgz bin etc

4) Copy the created bt.tgz to /boot/bt directory of the OutStation. To do it:
- upload the file bt.tgz to /tmp via scp command

    scp -P <reverse port number> bt.tgz bt@localhost:/tmp

   The entire copy proces may last a bit so be patient!
- log in to the OutStation as bt user over ssh connection.
- change to engineer account:

    login engineer

- become a root:

    su

- remount disk as writeable:

    mount /dev/hda1 - o remount,rw

- verify the integrity of the copied file, move the file to the /boot/bt directory and restart the system

    gzip -t /tmp/bt.tgz
    cp /tmp/bt.tgz /boot/bt/bt.tgz
    sync
    reboot
