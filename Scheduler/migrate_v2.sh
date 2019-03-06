#!/bin/bash
#In this script, we try to migrate the docker container using docker commit and creating a registry. Using registry we minimise the time for docker push  and pull. 

echo $1 #Container id 
echo $2 #Destination machine
echo $3 #Registry id like sparknode19:443/
extension_cr="_cr.tar.gz" # extension  for checkpoint restore
extension_fs="_fs.tar" #extension  for file system checkpoint
loc="/opt/kafka_2.11-0.9.0.0"
r_loc=":/opt/kafka_2.11-0.9.0.0/restore/"
con_ext=":new"



#get image id from container name
image_id=$(docker container ls | grep $1 | awk '{print $2}')

#get cmd line for container 
cmd_line_length=$(docker inspect -f '{{.Config.Cmd}}' $1 | wc -m)
cmd_line_length=$(expr $cmd_line_length - 2)
cmd_line="CMD "$(docker inspect -f '{{.Config.Cmd}}' $1 |  cut -c2-$cmd_line_length)

echo $cmd_line

#extensions for files 
file_cr=$1$extension_cr
file_fs=$1$extension_fs
remote_loc=$2$r_loc

#change the directory to checkpoint
echo "Docker checkpointime "
cd $loc/checkpoint
x=$(time docker checkpoint create --checkpoint-dir=$loc/checkpoint $1 $1)
#y=$(docker checkpoint ls $1)

#tar the checkpoint
echo "docker checkpoint tar time"
#x=$(tar -cvzf   $loc/checkpoint/$file $loc/checkpoint/$1)
x=$(time tar -cvzf  $file_cr -P $1)


#create a random new container name 
con_name=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 8 | head -n 1)
echo "Con name "$con_name
image_name_new=$3$con_name$con_ext


#commit the  file system of container as an docker image
echo "docker commit time"
x=$(time docker commit $1 $image_name_new)
echo "docker pushing to local registry"
x=$(time docker push $image_name_new)

#x=$(time docker container export --output="$file_fs" $1)


#create a random name for new container 
#con_name=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 8 | head -n 1)
#echo "Con name "$con_name
#image_name_new=$con_name$con_ext

#SCP to the destination machine
echo "chck and fs scp time"
x=$(time scp $file_cr $remote_loc) #checkpoint scp
#x=$(time scp $file_fs $remote_loc) #file system scp 

#ssh -t $2 "ls /opt/; docker images"
#ssh -t $2 "cd $loc/restore/; tar -zxvf $loc/restore/$file_cr; cat $file_fs | docker import --change "CMD $cmd_line" - $image_name_new; && docker create --name $con_name $image_id; docker start --checkpoint-dir=$loc/restore --checkpoint $1 $con_name"  # to retrive the same image
ssh -t $2 "cd $loc/restore/; time tar -zxvf $loc/restore/$file_cr;"
echo "time :docker pull and docker create  and docker start $1 $con_name"
ssh -t $2 "criu --version"
ssh -t $2 "time docker pull $image_name_new ; time docker create --name $con_name $image_name_new ;"
ssh -t $2 "time docker start --checkpoint-dir=$loc/restore --checkpoint=$1 $con_name;"



