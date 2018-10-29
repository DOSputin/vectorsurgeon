#!/bin/bash

# figure out where we are
DIR=`dirname $0`

if [ -z "$DIR" ]
then
	DIR=.
else
	cd $DIR
fi

# copy jars into place

cp vs-mac.jar VectorSurgeonMac/vs.jar
cp vs.jar VectorSurgeonWindows
cp vs.jar VectorSurgeonLinux

zip -r VectorSurgeonMac.zip VectorSurgeonMac
zip -r VectorSurgeonWindows.zip VectorSurgeonWindows
zip -r VectorSurgeonLinux.zip VectorSurgeonLinux

mac=VectorSurgeonMac.zip
windows=VectorSurgeonWindows.zip
linux=VectorSurgeonLinux.zip

scp -i ~/ec2/Redfish.pem $mac $windows $linux ec2-user@redfish.net:prophetvs.com/editor/


