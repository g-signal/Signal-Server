#!/bin/bash
ID=`ps -ef | grep "SecureServer" | grep -v "$0" | grep -v "grep" | awk '{print $2}'`
echo $ID
echo "---------------"
for id in $ID
do
kill -9 $id
echo "killed $id"
done
echo "---------------"
nohup java -Dsecrets.bundle.filename=./service/config/sample-secrets-bundle.yml   -jar ./service/target/TextSecureServer-20250804.1.1-SNAPSHOT.jar server ./service/config/config.yml 2>&1 &
