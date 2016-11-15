#!/bin/bash

#ifup eth0
sed -i 's/ONBOOT=no/ONBOOT=yes/' /etc/sysconfig/network-scripts/ifcfg-eth0

yum -y update

useradd -m engineer
echo "engineer:ssl1324" | chpasswd
usermod -a -G wheel engineer
# Enable wheel group do execute sudo command
sed -i 's|#\s*%wheel\s*ALL=(ALL)\s*ALL|%wheel ALL=(ALL) ALL|' /etc/sudoers

# Enable sftp login
sed -i 's/\(Subsystem\s*sftp\s*\/usr\/libexec\/openssh\/sftp-server\)/#\1\nSubsystem sftp internal-sftp/' /etc/ssh/sshd_config

yum install -y wget openssh-clients git man

echo "Done"
