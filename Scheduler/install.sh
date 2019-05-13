#!/bin/bash
#IMP : APPEND to /etc/default/grub about the memory access (Offline)
#criu need to be sourced explicitly : not happening automatically
#Install CRIU to machine

id=4 # Id of the machine to be kept in /varzookeeper/data/myid file

sudo apt install -y libprotobuf-dev libprotobuf-c0-dev protobuf-c-compiler protobuf-compiler python-protobuf  libnet1-dev libcap-dev pkg-config  libaio-dev libnl-3-dev libcap-dev gcc make
echo "Installed Dependencies"
wget http://download.openvz.org/criu/criu-3.4.tar.bz2
tar -xvf criu-3.4.tar.bz2 
cd criu-3.4
make

#Adding it to bashrc
hostname=$(hostname)
first='/home/'
third='/criu-3.4/criu/'
string1="$first$hostname$third"

echo "Adding this to bashrc" $string1
final="export PATH=$PATH:"$string1
echo $final >> ~/.bashrc
source ~/.bashrc

#Adding it to systemctl

first2=":/home/"
#prefix=$(echo $PATH)
prefix="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
#prefix="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
pref="$prefix$first2$hostname$third"

echo "Adding this to Systemctl"
echo $pref
systemctl set-environment PATH=$pref
#systemctl show


#change permission of docker.sock

chmod 777 /var/run/docker.sock
#mkdir /var/zookeeper
#mkdir /var/zookeeper/data
#echo $id > /var/zoookeeper/data/myid



#APPEND to /etc/default/grub about the memory access (Offline)
