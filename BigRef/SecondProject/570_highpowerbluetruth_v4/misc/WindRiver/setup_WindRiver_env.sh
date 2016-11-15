#!/bin/bash

/usr/tools/WindRiver/WRL4_3/wrenv.sh -p wrlinux-4
export SYSROOT_PATH=`pwd`/common_pc_VDX-glibc_small/x86-linux2
export PATH=$SYSROOT_PATH:$PATH
