#!/bin/bash 

#CHECK if $path is properly set. If set just run the file. Else, for setting bashrc path and systemcl path, use the install.sh file.
hostname=$(hostname)
first='/home/'
third='/criu-3.4/criu/'
string1="$first$hostname$third"

#echo "Adding this to bashrc" $string1
final="export PATH=$PATH:"$string1
#echo $final >> ~/.bashrc
#source ~/.bashrc
#Adding it to systemctl
#first2=":/home/"
prefix=$(echo $PATH)
pref="$prefix"

echo "Adding this to Systemctl"
echo $pref
systemctl set-environment PATH=$pref
systemctl show

chmod 777 /var/run/docker.sock

