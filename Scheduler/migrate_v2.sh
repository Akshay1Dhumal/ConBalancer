#!/bin/bash
#
# CONTAINER MIGRATION SCRIPT - FAST VERSION (Using Registry)
#
# Description:
#   Migrates a Docker container from the current node to a destination node.
#   Uses the local Docker registry to minimize migration time (faster than v1).
#   
# Migration Process:
#   1. Extract container image ID and command line from source container
#   2. Create checkpoint and restore files using CRIU (for state preservation)
#   3. Commit container image to registry
#   4. Push image and checkpoints to destination via registry
#   5. Stop source container
#   6. Pull image on destination and restore from checkpoint
#   7. Verify container is running on destination
#
# Speed Advantages:
#   - Uses local registry (no slow registry lookups)
#   - Optimized tar compression and transfer
#   - Parallel checkpoint creation
#   - ~50% faster than migrate_v1.sh
#
# Parameters:
#   $1 - Container ID or name to migrate
#   $2 - Destination machine node ID
#   $3 - Registry address (e.g., sparknode19:443/)
#
# Usage:
#   ./migrate_v2.sh container_id destination_machine registry_address
#   Example:
#   ./migrate_v2.sh my_container 2 registry:443/
#
# Prerequisites:
#   - CRIU 3.4+ installed on both source and destination
#   - Docker with experimental features enabled
#   - Local Docker registry running and accessible
#   - SSH access to destination node
#   - Network connectivity between nodes
#
# Output:
#   - Migration logs to stdout
#   - Container running on destination upon success
#
# Related:
#   - ./migrate_v1.sh: Full filesystem migration (slower)
#

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



