
# Distributed MapReduce Word Count System

**Suggested Project Name: `distributed-mapreduce-wordcount`**

This project implements a distributed MapReduce system for large-scale word counting operations using Java. The system follows the classic MapReduce paradigm with a master-worker architecture, utilizing FTP for data distribution and TCP sockets for coordination.

## Architecture Overview

The system consists of two main components:

### Master Node (`master/`)
- **Orchestrator**: Coordinates the entire MapReduce workflow
- **Data Distribution**: Splits large text files and distributes chunks to worker nodes via FTP
- **Phase Coordination**: Manages Map, Shuffle, and Reduce phases across all nodes
- **Performance Monitoring**: Tracks communication, computation, and synchronization times
- **Result Aggregation**: Collects final word count results and generates CSV reports

### Worker Nodes (`node/`)
- **FTP Server**: Receives data chunks from the master node
- **Map Phase**: Processes text data to generate word counts
- **Shuffle Phase**: Redistributes word counts based on hash partitioning
- **Reduce Phase**: Aggregates word counts from multiple nodes
- **Group Phase**: Organizes data for final reduction

## Key Features

- **Distributed Processing**: Scales across multiple machines for large datasets
- **Fault Tolerance**: Uses reliable FTP and TCP protocols for communication
- **Performance Analysis**: Built-in timing mechanisms for bottleneck identification
- **Flexible Deployment**: Python deployment script for easy cluster setup
- **Real-world Ready**: Designed to process CommonCrawl web data

## How It Works

1. **Data Splitting**: Master splits input file into chunks distributed across worker nodes
2. **Map Phase**: Each worker counts words in its assigned data chunk
3. **Shuffle Phase 1**: Word counts are redistributed based on hash partitioning
4. **Reduce Phase 1**: Initial aggregation of word counts per partition
5. **Group Phase**: Determines value ranges for final grouping
6. **Shuffle Phase 2**: Redistributes data based on value ranges
7. **Reduce Phase 2**: Final word count aggregation and result collection

## Build and Deployment

### Compilation

If you download the JAR from the repository, no compilation is needed. Otherwise, run this command in both `master/` and `node/` directories:

```bash
mvn clean compile assembly:single
```

### Using the Deployment Script

The deployment script automates the process of setting up the distributed system. Run it in a virtual environment for safety.

**IMPORTANT**: Edit the script to select the machines you want to use as nodes and master.

#### Setup Virtual Environment:

```bash
python3 -m venv slr207
source slr207/bin/activate
pip install paramiko scp
```

#### Deploy the System:

```bash
source slr207/bin/activate
python SendDeploy.py
```

The script will prompt for your network credentials. Ensure you're connected to the target network or VPN.


## Manual Deployment Steps

For manual deployment and testing:

### 1. Compile and Package
```bash
# In master/ directory
mvn clean compile assembly:single

# In node/ directory  
mvn clean compile assembly:single
```

### 2. Deploy to Remote Nodes
```bash
# Copy node JAR to remote machine
scp node-1-jar-with-dependencies.jar username@remote-host:/tmp/username/

# Copy master JAR to master machine
scp master-1-jar-with-dependencies.jar username@master-host:/tmp/username/
```

### 3. Execute on Remote Machines
```bash
# SSH to remote node and run
ssh username@remote-host
java -jar /tmp/username/node-1-jar-with-dependencies.jar

# SSH to master and run with server list
ssh username@master-host
java -jar /tmp/username/master-1-jar-with-dependencies.jar server1,server2,server3
```

## Performance Analysis

The system includes comprehensive performance monitoring:

- **Communication Time**: File transfer and data shuffling overhead
- **Computation Time**: Map and Reduce operation execution time  
- **Synchronization Time**: Coordination and waiting between phases

Results are automatically exported to `results.csv` and can be analyzed using the included Jupyter notebook (`analysis.ipynb`).

## File Structure

- `master/` - Master node implementation with coordination logic
- `node/` - Worker node implementation with MapReduce handlers  
- `SendDeploy.py` - Automated deployment script
- `analysis.ipynb` - Performance analysis and visualization
- `results.csv` - Execution timing results
- `nodes.txt` - List of available worker nodes

## Technical Details

- **Communication**: FTP for data transfer, TCP sockets for control messages
- **Data Processing**: Word counting with hash-based partitioning
- **Scalability**: Supports arbitrary number of worker nodes
- **Input Format**: Plain text files (designed for CommonCrawl data)
- **Output Format**: CSV with word counts and performance metrics