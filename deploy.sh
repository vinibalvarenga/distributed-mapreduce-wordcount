#!/bin/bash

# your list of hosts file that should contain each host in a separate line
computers=`cat nodes.txt | xargs -I {} echo {}`
# your password for the hosts MAJOR SEECURITY RISK :-DDDD
password="mUvpy3-jigfix-pykwaw" 
# your login 
login="alvarenga-23"
# skip host key checking MAJOR SECURITY RISK :-DD
sshopts="-o StrictHostKeyChecking=no -o UserKnownHostsFile=/tmp/null"

remoteFolder="/tmp/$login/"
cd node
mvn clean compile assembly:single
cd ..
fileName="node-1-jar-with-dependencies"
fileExtension=".jar"


for c in ${computers[@]}; do
  command0="sshpass -p '$password' ssh $sshopts $login@$c pkill -u $login"
  command1="sshpass -p '$password' ssh $sshopts $login@$c 'rm -rf $remoteFolder;mkdir $remoteFolder'"
  command2="sshpass -p '$password' scp $sshopts node/target/$fileName$fileExtension $login@$c:$remoteFolder$fileName$fileExtension"
  echo ${command0[*]}
  eval $command0
  echo ${command1[*]}
  eval $command1
  echo ${command2[*]}
  eval $command2
  command3="sshpass -p '$password' ssh $sshopts $login@$c 'cd $remoteFolder; java -jar $fileName$fileExtension'"
  echo ${command3[*]}
  eval $command3 &
done
