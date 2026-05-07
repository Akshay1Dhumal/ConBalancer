#!/bin/bash
#
# DEPLOY TEST WORKLOAD CONTAINERS
#
# Description:
#   Deploys Docker containers with CPU-intensive or network-intensive workloads.
#   Used for testing C-Balancer optimization under various load conditions.
#   Multiple lines (some commented) allow selection of different test workloads.
#
# Configured Workloads:
#
#   1. CPU STRESS (Matrix Product Computation):
#      - Stress-NG with CPU-intensive matrix operations
#      - 2 CPU cores per container
#      - 300-second duration
#      - Best for testing CPU-bound optimization
#
#   2. NETWORK STRESS (iPerf):
#      - Network bandwidth/latency testing
#      - Requires iPerf server endpoint configuration
#      - Tests network I/O optimization
#
#   3. READAHEAD STRESS:
#      - Read-ahead cache testing
#      - 50MB readahead buffer
#      - Tests storage I/O optimization
#
#   4. CRYPTOGRAPHY STRESS:
#      - CPU-intensive crypto operations
#      - 2 cores, 120-second duration
#      - Tests high-CPU load handling
#
#   5. BINARY SEARCH STRESS:
#      - Memory-intensive search operations
#      - 4MB search space per container
#      - Tests memory optimization
#
# Usage:
#   ./run.sh
#   - Uncomment desired workload lines
#   - Comment out unneeded workloads
#   - Containers run in background
#
# Prerequisites:
#   - Docker installed and running
#   - Workload images pulled (deployable/stress, akshay1dhumal/iperf)
#   - Network connectivity to iPerf servers (if using network tests)
#
# Notes:
#   - Edit IP addresses for network test endpoints
#   - Adjust CPU cores and duration based on needs
#   - View running containers: docker ps
#   - View container stats: docker stats
#

# ===== CPU STRESS (ACTIVE WORKLOAD) =====
# Matrix product computation using 2 CPU cores for 300 seconds
docker run -d deployable/stress:latest --cpu 2 --timeout 300s  --cpu-method matrixprod --metrics-brief &
docker run -d deployable/stress:latest --cpu 2 --timeout 300s --cpu-method matrixprod --metrics-brief & 

# ===== NETWORK STRESS (DISABLED) =====
#docker run -d akshay1dhumal/iperf:latest 10.21.229.203 500 120 &
#docker run -d akshay1dhumal/iperf:latest 10.21.237.195 500 120 &

# ===== READAHEAD STRESS (DISABLED) =====
#docker run -d deployable/stress:latest --readahead 2 --readahead-bytes 50m  --metrics-brief --timeout 300s &
#docker run -d deployable/stress:latest --readahead 2 --readahead-bytes 50m  --metrics-brief --timeout 300s &

# ===== CRYPTOGRAPHY STRESS (DISABLED) =====
#docker run -d deployable/stress:latest --crypt 2 --timeout 120s --metrics-brief &
#docker run -d deployable/stress:latest --crypt 2 --timeout 120s --metrics-brief &

# ===== BINARY SEARCH STRESS (DISABLED) =====
#docker run -d deployable/stress:latest --bsearch 2 --bsearch-size 4m --timeout 120s --metrics-brief &
#docker run -d deployable/stress:latest --bsearch 2 --bsearch-size 4m --timeout 120s --metrics-brief &

# ===== NETWORK IPERF CLIENT (DISABLED) =====
# Note: Configure server endpoints (10.21.229.203 = scheduler node)
#docker run -d  oneclient:latest 10.21.229.203 500 20 &
#docker run -d  oneclient:latest 10.21.237.195 500 20 &



#VM STRESSOR
#docker run -d deployable/stress:latest --vm 2 --vm-bytes 512m -t 2m --metrics-brief &
#docker run -d deployable/stress:latest --vm 2 --vm-bytes 512m -t 2m --metrics-brief &


#TSEARCH
#docker run -d deployable/stress:latest --tsearch 2 --tsearch-size 4m --timeout 300s --metrics-brief &
#docker run -d deployable/stress:latest --tsearch 2 --tsearch-size 4m --timeout 300s --metrics-brief &

#qsort
#docker run -d deployable/stress:latest --qsort 2 --qsort-size 4m --timeout 300s --metrics-brief &
#docker run -d deployable/stress:latest --qsort 2 --qsort-size 4m --timeout 300s --metrics-brief &


