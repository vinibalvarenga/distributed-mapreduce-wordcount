
# Using the deployment script in a venv:

### Create and Activate a Virtual Environment:

```
python3 -m venv slr207
source slr207/bin/activate
```

### Install paramiko and scp in the Virtual Environment:

```
pip install paramiko scp
```

### Run Your Script:
Ensure your script is using the virtual environment's Python interpreter. You can run the script directly while the virtual environment is activated.

```
source slr207/bin/activate
python SendDeploy.py
```



### OBS.: SSH connection
to open
```
	ssh alvarenga-23@tp-1a201-22
```

to quit
```
	exit
```


### Enviar os arquivos para o no remoto

[scp] dentro da pasta target local

node:
```
	scp node-1-jar-with-dependencies.jar alvarenga-23@tp-m5-00:/tmp/alvarenga-23/node-1-jar-with-dependencies.jar
```
master:



### Rodar o arquivo no remoto (requer ssh)

[ssh] para rodar o arquivo dentro da pasta correta do remoto
node:
```
	java -jar node-1-jar-with-dependencies.jar
```

master:
```
	java -jar master-1-jar-with-dependencies.jar
```

# Apendix

### General notes
List of computers

https://tp.telecom-paris.fr/

All web pages text with splits
First task - separate the data into different machines. Do the splits.
	Main (orchestrator)
		takes diffeent splits and distribute them to the local disks of nodes (SN1,CN1 - SN2,CN2, ...)
		TDLR: main copies parts of the files and distribute it into different computers.

Implementation using File Transfer Protocol (FTP):

We use FTP servers and FTP clients

main (orchestrator) - FTP client
SN1,CN1 - S... are the FTP servers.

Deploy Script 
	Take a list of nodes
	Copy the FTP server
	Start it

How to create the executable to deploy:
Use maven - pom.xml file available. 
To execute it, use the maven pluting. 
	Go to maven, myftpserver, plugins, assembly, single - this will create an executable file.
		first compile, then create the executable file using the command - mvn package (I think)


### First class
Steps to deploy manually
1.⁠ ⁠Compile the server (Assembly)
    mvn compile
2.⁠ ⁠Generate the JAR (JAVA archive) using the Maven plugin Assembly "single"
	mvn clean compile assembly:single
3.1 create /tmp/you_login with SSH on the local disk of each computer 
3.2⁠ ⁠Copy the JAR on N1 N2 N3 using SCP  /temp/your_login/____.jar
4.⁠ ⁠Execute the Jar using SSH ( java -jar ___.jar)
5. Modify the FTP client to connect to the 3 FTB servers
and send S1 S2 and S3 then try with S4 S5 S6 (create manual simple strings)

