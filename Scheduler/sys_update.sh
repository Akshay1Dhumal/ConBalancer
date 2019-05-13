#Adding it to systemctl
hostname=$(hostname)
first2=":/home/"
first='/home/'
third='/criu-3.4/criu/'

#prefix=$(echo $PATH)
prefix="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
#prefix="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
pref="$prefix$first2$hostname$third"

echo "Adding this to Systemctl"
echo $pref
systemctl set-environment PATH=$pref

