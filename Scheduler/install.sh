#!/bin/bash
#
# INSTALL C-BALANCER FRAMEWORK DEPENDENCIES
#
# Description:
#   Installs all system-level dependencies required for C-Balancer:
#   - CRIU (Checkpoint-Restore in Userspace) 3.4 for container migration
#   - Required development libraries and headers
#   - Build tools and compilers
#   - Network and kernel utilities
#
# Key Components Installed:
#   - libprotobuf-dev, libprotobuf-c0-dev: Protocol buffer libraries (CRIU requirement)
#   - protobuf-c-compiler, protobuf-compiler: Protocol buffer code generators
#   - python-protobuf: Python protocol buffer support
#   - libnet1-dev: Netlink support for CRIU
#   - libcap-dev: Linux capabilities for privileged operations
#   - libaio-dev: Asynchronous I/O support
#   - libnl-3-dev: Netlink support
#   - gcc, make: Build tools for compiling CRIU
#
# Installation Steps:
#   1. Install all prerequisite libraries and tools
#   2. Download CRIU 3.4 from official repository
#   3. Extract and compile CRIU from source
#   4. Add CRIU binaries to default Bash PATH
#   5. Configure systemctl to find CRIU (optional)
#
# Machine ID Configuration:
#   - Edit variable 'id' in script before running
#   - This ID is stored in /var/zookeeper/data/myid
#   - Used by Zookeeper to identify this node in cluster
#   - Each cluster node must have unique ID
#
# Prerequisites:
#   - Ubuntu/Debian system with apt package manager
#   - sudo/root access
#   - Internet connectivity for downloading packages and CRIU source
#   - ~500MB free disk space for compilation
#
# Usage:
#   Edit the 'id' variable below for this machine
#   Then run: sudo ./install.sh
#
# Post-Installation Verification:
#   - Verify CRIU: criu --version
#   - Verify CRIU in PATH: which criu
#   - Check systemctl environment: systemctl show | grep CRIU
#
# Notes:
#   - This is a one-time setup script
#   - Must run on all nodes before starting C-Balancer
#   - CRIU compilation may take 5-10 minutes
#   - Requires internet access during installation
#

# Machine ID for this node (set unique ID for each node in cluster)
id=4 # Id of the machine to be kept in /var/zookeeper/data/myid file

# ===== INSTALL DEPENDENCIES =====
echo "Installing system dependencies for C-Balancer and CRIU..."
sudo apt install -y libprotobuf-dev libprotobuf-c0-dev protobuf-c-compiler protobuf-compiler python-protobuf libnet1-dev libcap-dev pkg-config libaio-dev libnl-3-dev libcap-dev gcc make
echo "Dependencies installation complete"

# ===== DOWNLOAD AND COMPILE CRIU =====
echo "Downloading CRIU 3.4..."
wget http://download.openvz.org/criu/criu-3.4.tar.bz2
tar -xvf criu-3.4.tar.bz2 
cd criu-3.4
echo "Building CRIU from source..."
make

# ===== CONFIGURE SYSTEM PATH =====
# Add CRIU to user's bash PATH
hostname_current=$(hostname)
criu_path_prefix='/home/'
criu_path_suffix='/criu-3.4/criu/'
criu_full_path="$criu_path_prefix$hostname_current$criu_path_suffix"

echo "Adding CRIU to PATH: $criu_full_path"
final_path_export="export PATH=$PATH:"$criu_full_path
echo $final_path_export >> ~/.bashrc
source ~/.bashrc

# ===== CONFIGURE SYSTEMCTL PATH (Optional) =====
# Add CRIU to system-wide PATH for systemctl and services
criu_systemd_prefix=":/home/"
default_system_path="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

echo "Installation complete!"
echo "Verify CRIU installation with: criu --version"
echo "Verify CRIU in PATH with: which criu"
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
