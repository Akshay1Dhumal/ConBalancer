#!/bin/bash
#
# INSTALL DOCKER ENGINE WITH EXPERIMENTAL FEATURES
#
# Description:
#   Installs Docker Engine version 1.13.0 with experimental features enabled.
#   Configures daemon to support:
#   - Container checkpointing and restoration (required for container migration)
#   - Devicemapper storage driver
#   - Insecure registries for local testing (e.g., registry:443)
#
# Installation Steps:
#   1. Update package manager cache
#   2. Add Docker repository GPG key
#   3. Enable Docker repository
#   4. Install Docker Engine 1.13.0 (specific version required)
#   5. Enable experimental features in daemon.json
#   6. Restart Docker daemon
#   7. Grant socket permissions for non-root container management
#
# Specific Requirements:
#   - Docker 1.13.0 build 49bf474 (required for C-Balancer container migration)
#   - Experimental mode enabled (for checkpoint/restore functionality)
#   - Devicemapper storage driver (for CRIU integration)
#   - Insecure registry configuration (for local registry access)
#
# Prerequisites:
#   - Ubuntu Trusty (or compatible Debian-based system)
#   - sudo/root access
#   - Internet connectivity
#   - apt package manager
#
# Usage:
#   ./install_docker.sh
#
# Post-Installation:
#   - Verify: docker version  (should show 1.13.0)
#   - Verify: docker --version
#   - Check experimental: docker version | grep Experimental
#
# Notes:
#   - This must complete before running C-Balancer
#   - Docker daemon will restart during installation
#   - May temporarily disrupt any running containers
#

# Update package manager
sudo apt-get update

# Add Docker repository GPG key
sudo apt-key adv --keyserver hkp://pgp.mit.edu:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D

# Add Docker  repository
echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" | sudo tee /etc/apt/sources.list.d/docker.list

# Install package managers for repository support
sudo apt-get install apt-transport-https -y

# Update package list with Docker repository
echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt-get update

# Show available Docker versions
sudo apt-cache policy docker-engine

# Install specific Docker Engine version (1.13.0 required for C-Balancer)
sudo apt-get install docker-engine=1.13.0-0~ubuntu-trusty

# Configure Docker with experimental features and local registry support
echo '{"experimental": true,"storage-driver": "devicemapper","insecure-registries": ["registry:443"]}' > /etc/docker/daemon.json

# Restart Docker daemon to apply new configuration
systemctl restart docker

# Grant socket permissions for non-root container management
chmod 777 /var/run/docker.sock
