#!/bin/sh

#This script is executing some actions depending on the value returned from the bluetruth main program

BLUETRUTH_PROGRAM_NAME=/opt/bt/bin/outstation
FINISH=false
REBOOT_SYSTEM=true

while ! $FINISH; do
    sh -c $BLUETRUTH_PROGRAM_NAME; STATUS=$?
    echo "Program exited with code: $STATUS"

    case $STATUS in
    0)
        echo "Normal exit"
        ;;
    1)
        echo "Fatal error"
        sleep 10
        ;;
    16)
        echo "Program restart required"
        ;;
    17)
        echo "System reboot required"
        REBOOT_SYSTEM=true
        FINISH=true
        ;;
    *)
        echo "Unknown return code"
    esac
done

if $REBOOT_SYSTEM; then
    echo "About to reboot the system..."
    reboot
fi
