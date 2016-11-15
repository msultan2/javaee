#!/bin/bash


VM_NAME="CentOS 6.6"
ISO_IMAGE_NAME="CentOS-6.6-x86_64-bin-DVD1.iso"
VM_IMAGE="$HOME/VirtualBox/CentOS_6_6.vdi"
IMAGE_URL=http://www.mirrorservice.org/sites/mirror.centos.org/6.6/isos/x86_64/""
RDP_ADDRESS="127.0.0.1"

NUMBER_OF_CPUS=`grep -c ^processor /proc/cpuinfo`
MEMORY_IN_MB=2048

#Parse all options provided
while getopts ":c:i:m:n:t:u:v:" opt; do
	case $opt in
		c) NUMBER_OF_CPUS="$OPTARG"
		;;
		i) ISO_IMAGE_NAME="$OPTARG"
		;;
		m) MEMORY_IN_MB="$OPTARG"
		;;
		n) VM_NAME="$OPTARG"
		;;
        t) RDP_ADDRESS="$OPTARG"
		;;
		u) IMAGE_URL="$OPTARG"
		;;
		v) VM_IMAGE="$OPTARG"
		;;
		\?) echo "Invalid option -$OPTARG" >&2
        exit 1
		;;
	esac
done
shift $((OPTIND-1)) #This tells getopts to move on to the next argument.


function help {
	printf "Usage: $0 [OPTION] COMMAND\n"
        printf "\n"
	printf "Options:\n"
        printf "  -i ISO image name\n"
        printf "  -n name\n"
        printf "  -v VM disk image (full path)\n"
        printf "  -u download URL of dvd image to be used for installation excluding the ISO image name\n"
        printf "\n"
        printf "  -c number of CPUs to be used when installing VM\n"
        printf "  -m amount of memory in MB to allocate to the VM when installing it\n"
        printf "  -t rdp IP address\n"
        printf "\n"
	printf "Commands:\n"
        printf "  install - install VM\n"
        printf "  run,start - start VM\n"
        printf "  poweroff,stop - poweroff VM\n"
        printf "  detach_dvd - remove dvd drive from VM\n"
        printf "  get_ip_address - get IP address of the running VM\n"
        printf "  rdp - run remote desktop client\n"
        printf "  show - show if VM is running"
	exit 1
}

function install {
	#Get ISO image
	ISO_IMAGE="iso/$ISO_IMAGE_NAME"
	if [ ! -f $ISO_IMAGE ]
	then
		mkdir -p iso
		wget -nc $IMAGE_URL/$ISO_IMAGE_NAME -P iso
        [ $? -eq 0 ] || exit $?; # exit for none-zero return code

		echo "The ISO image '$ISO_IMAGE' has been downloaded"
else
		echo "The ISO image '$ISO_IMAGE' exists. Skipping..."
	fi

	#Set default folder for virtual machines
	VBoxManage setproperty machinefolder $HOME/VirtualBox

	IS_VM_INSTALLED=`VBoxManage list vms | sed -e 's/{[^{}]*}//g' | grep "$VM_NAME"`
	if [ -z "$IS_VM_INSTALLED" ]
	then
		VBoxManage createvm --name "$VM_NAME" --register
		echo "The virtual machine '$VM_NAME' created"
	else
		echo "The virtual machine '$VM_NAME' already exists and will not be created. Skipping..."
	fi

	VBoxManage modifyvm "$VM_NAME" --ostype Linux --cpus $NUMBER_OF_CPUS --memory $MEMORY_IN_MB --vram 16 --acpi on --boot1 dvd --nic1 bridged --bridgeadapter1 eth0
	VBoxManage modifyvm "$VM_NAME" --ioapic on

	IDE_CONTROLLER_NAME="IDE Controller"
	IS_CONTROLLER_INSTALLED=`VBoxManage showvminfo "$VM_NAME" | grep "$IDE_CONTROLLER_NAME"`
	if [ -z "$IS_CONTROLLER_INSTALLED" ]
	then
		VBoxManage storagectl "$VM_NAME" --name "$IDE_CONTROLLER_NAME" --add ide
		echo "The $IDE_CONTROLLER_NAME for '$VM_NAME' created"
	fi
	if [ ! -f $VM_IMAGE ]
	then
		VBoxManage createhd --filename $VM_IMAGE --size 30000
		echo "The hard disk '$VM_IMAGE' for '$VM_NAME' created"
	else
		echo "The hard disk '$VM_IMAGE' for '$VM_NAME' already exists and will not be created. Skipping..."
	fi

	IS_STORAGE_ATTACHED=`VBoxManage list hdds | grep -e ^Location:* | sed -e 's/^Location:\ *//' | grep "$VM_IMAGE"`
	if [ -z "$IS_STORAGE_ATTACHED" ]
	then
		VBoxManage storageattach "$VM_NAME" --storagectl "IDE Controller" --port 0 --device 0 --type hdd --medium $VM_IMAGE
		echo "The $IDE_CONTROLLER_NAME for '$VM_NAME' attached"
	else
		echo "The storage '$VM_IMAGE' is already attached. Skipping..."
	fi

	IS_DVD_ATTACHED=`VBoxManage showvminfo "$VM_NAME" | grep "$ISO_IMAGE"`
	if [ -z "$IS_DVD_ATTACHED" ]
	then
		if [ -f $ISO_IMAGE ]
		then
			VBoxManage storageattach "$VM_NAME" --storagectl "IDE Controller" --port 1 --device 0 --type dvddrive --medium $ISO_IMAGE
			echo "Thw ISO image $ISO_IMAGE for '$VM_NAME' attached"
		else
		echo "The '$ISO_IMAGE' does not exist. Exiting."
		exit 1
		fi
	else
		echo "The storage '$ISO_IMAGE' is already attached. Skipping..."
	fi

	VBoxManage modifyvm "$VM_NAME" --vrde on


	#Additionally install extension pack if required
	IS_EXTENSION_PACK_INSTALLED=`sudo VBoxManage list extpacks | grep 'Oracle VM VirtualBox Extension Pack'`
	if [ -z "$IS_EXTENSION_PACK_INSTALLED" ]
	then
		echo "Installing Oracle VM VirtualBox Extension Pack"
		cd /tmp
		wget -nc http://download.virtualbox.org/virtualbox/4.3.18/Oracle_VM_VirtualBox_Extension_Pack-4.3.18.vbox-extpack
		sudo VBoxManage extpack install Oracle_VM_VirtualBox_Extension_Pack-4.3.18.vbox-extpack
		sudo VBoxManage setproperty vrdeextpack "Oracle VM VirtualBox Extension Pack"
                #Restart service to apply extensions
                sudo service virtualbox restart
	else
		echo "Oracle VM VirtualBox Extension Pack is already installed. Skipping..."
	fi

	#If the dvd is no more required remove it by uncommenting the following line
	#VBoxManage storageattach "$VM_NAME" --storagectl "IDE Controller" --port 1 --device 0 --type dvddrive --medium none

	echo "Job done!"
}

function run {
	IS_RUNNING=`VBoxManage list runningvms | grep "$VM_NAME"`
	if [ -z "$IS_RUNNING" ]
	then
		printf "Deploying the instance of Guest OS in a separate session. To access it type:\n"
		printf "    screen -x vbox\n\n"
		screen -d -m -S vbox VBoxHeadless --startvm "$VM_NAME"
		sleep 2

		#Check if it is running
		IS_RUNNING=`VBoxManage list runningvms | grep "$VM_NAME"`
		if [ -z "$IS_RUNNING" ]
		then
		echo "Error. The image is not running..."
		exit 1
		else
		echo "Done."
		fi
	else
		echo "The Guest OS is already running. Skipping..."
	fi
}

function stop {
	IS_RUNNING=`VBoxManage list runningvms | grep "$VM_NAME"`
	if [ -n "$IS_RUNNING" ]
	then
		printf "Shutting down the instance of Guest OS\n"
		VBoxManage controlvm "$VM_NAME" poweroff
		sleep 5

		#Check if it is running
		IS_RUNNING=`VBoxManage list runningvms | grep "$VM_NAME"`
		if [ -n "$IS_RUNNING" ]
		then
		echo "Error. The image is still running..."
		exit 1
		else
		echo "Done."
		fi
	else
		echo "The Guest OS is already stopped. Skipping..."
	fi
}


function detach_dvd {
	VBoxManage storageattach "$VM_NAME" --storagectl "IDE Controller" --port 1 --device 0 --type dvddrive --medium none
}

function get_ip_address {
	MAC_ADDRESS=`VBoxManage showvminfo "$VM_NAME" | grep MAC | \
		awk '{ printf "%s:%s:%s:%s:%s:%s\n", \
			substr($4,1,2), \
			substr($4,3,2), \
			substr($4,5,2), \
			substr($4,7,2), \
			substr($4,9,2), \
			substr($4,11,2) }'`
	echo 'Scanning network for MAC address' $MAC_ADDRESS

	NETWORK_INTERFACES_ADDRESSES=`ifconfig | sed -r -n 's/(inet\ addr:)(([0-9]+\.){3}([0-9]+))(.*)/\2/p'`
	for address in $NETWORK_INTERFACES_ADDRESSES
	do
		for j in {1..254}; do ping -c 1 $address.$j & done &> /dev/null
	done

	FOUND_STRING=`arp -a | grep -i $MAC_ADDRESS | sed -r -n 's/((...)(([0-9]+\.){3}([0-9]+))(.*))/\3/p'`
	if [ ! -z "$FOUND_STRING" ]
	then
		echo "Interface of MAC address" $MAC_ADDRESS "has been found at" $FOUND_STRING
	else
		echo "Interface of MAC address" $MAC_ADDRESS "has not been found"
	fi
}

function run_rdp {
	rdesktop -a 16 -N $RDP_ADDRESS &
}

function run_show {
    #Check if it is running
	IS_RUNNING=`VBoxManage list runningvms | grep "$VM_NAME"`
	if [ -z "$IS_RUNNING" ]
	then
        echo "The Guest OS $VM_NAME is not running"
	else
		echo "The Guest OS $VM_NAME is running"
	fi
}


if [ $# == 0 ]; then
	printf "No action specified.\n"
        help
elif [ $1 == "install" ]; then
    install
elif [[ $1 == "run" || $1 == "start" ]]; then
	run
elif [[ $1 == "poweroff" || $1 == "stop" ]]; then
	stop
elif [ $1 == "detach_dvd" ]; then
	detach_dvd
elif [ $1 == "get_ip_address" ]; then
	get_ip_address
elif [ $1 == "rdp" ]; then
	run_rdp
elif [ $1 == "show" ]; then
	run_show
else
	echo "Unknown action '"$1"'. Exiting..."
        help
fi
