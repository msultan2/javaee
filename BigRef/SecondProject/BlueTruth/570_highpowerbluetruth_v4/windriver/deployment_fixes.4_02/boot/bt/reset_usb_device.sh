#!/bin/sh

DEVICE_ID=0a12:0001

echo "Resetting USB device"
echo "Switching OFF ..."
echo -en '\x00' | dd of=/dev/port bs=1 count=1 seek=153 >& /dev/null
sleep 2
CHECK_DEVICE_RESULT=`lsusb | grep -i $DEVICE_ID`
if [ -z "$CHECK_DEVICE_RESULT" ]; then
    echo "Device $DEVICE_ID is gone. Good so far!"
else
    echo "Device $DEVICE_ID seems to be still there... ERROR"
    exit 1
fi

echo "Switching ON ..."
echo -en '\x80' | dd of=/dev/port bs=1 count=1 seek=153 >& /dev/null
sleep 2
CHECK_DEVICE_RESULT=`lsusb | grep -i $DEVICE_ID`
if [ -z "$CHECK_DEVICE_RESULT" ]; then
    echo "Device $DEVICE_ID seems to be missing... ERROR"
    exit 1
else
    echo "Device $DEVICE_ID is back. Good!"
    echo "Resetting USB device done"
fi
