sudo apt-get update
sudo apt-key adv --keyserver hkp://pgp.mit.edu:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt-get install apt-transport-https -y
echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt-get update
sudo apt-cache policy docker-engine
sudo apt-get  install docker-engine=1.13.0-0~ubuntu-trusty 
echo '{"experimental": true,"storage-driver": "devicemapper","insecure-registries": ["registry:443"]}' > /etc/docker/daemon.json
systemctl restart docker
chmod 777 /var/run/docker.sock
