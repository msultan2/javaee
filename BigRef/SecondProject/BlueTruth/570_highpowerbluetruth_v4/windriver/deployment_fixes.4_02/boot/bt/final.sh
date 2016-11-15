#!/bin/sh

echo "Running final.sh"

cp /boot/bt/check_for_unresolved_bugs.sh /etc/crontabs/
echo "0,10,20,30,40,50 *  *  *  * nice /etc/crontabs/check_for_unresolved_bugs.sh >& /tmp/check_for_unresolved_bugs.txt" >> /etc/crontabs/root

echo "sh /boot/bt/reset_usb_device.sh" >> /etc/rcS.d/S95runbt


echo "Running final.sh finished"
