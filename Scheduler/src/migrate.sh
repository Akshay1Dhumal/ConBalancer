echo $1 #Container id
echo $2 #Destination machine
extension=".tar.gz"
loc="/opt/kafka_2.11-0.9.0.0"
r_loc=":/opt/kafka_2.11-0.9.0.0/restore/"

#change the directory to checkpoint
cd $loc/checkpoint
x=$(docker checkpoint create --checkpoint-dir=$loc/checkpoint $1 $1)
y=$(docker checkpoint ls $1)

file=$1$extension
remote_loc=$2$r_loc

#tar the checkpoint
#x=$(tar -cvzf   $loc/checkpoint/$file $loc/checkpoint/$1)
x=$(tar -cvzf  $file -P $1)

#SCP to the destination machine
x=$(scp $file $remote_loc)
x=$(ssh -t $2 "ls")





#rm -rf * 
#x=$(docker checkpoint rm $1 $1)

