#!/bin/bash

# Expect the deploy package to be in the ~/temp/wowza-plugin folder of the unpacked digitv-package.
# The script is supposed to run on the server where Wowza resides as the larm-user.

PACKAGE_NAME=LARM-CHAOS-Wowza-package.zip
SERVER_DEPLOY_PACKAGE_LIB=~/temp/chaos-wowza-plugin
SERVER_DEPLOY_SETUP_LIB=chaos-deploy-setup


echo Deploying Wowza plugin on server

cd $SERVER_DEPLOY_PACKAGE_LIB

echo - Removing previous deploy-setup folder...
rm -r $SERVER_DEPLOY_SETUP_LIB

echo - Creating new deploy-setup folder...
mkdir $SERVER_DEPLOY_SETUP_LIB

echo - Extracting install package...
unzip -q $PACKAGE_NAME -d $SERVER_DEPLOY_SETUP_LIB

echo - Copy jar-files to /usr/local/WowzaMediaServer/lib ...
cp -r $SERVER_DEPLOY_SETUP_LIB/lib/* /usr/local/WowzaMediaServer/lib

echo - Copy VHost files to /home/larm/wowza_vhost_chaos
cp -r $SERVER_DEPLOY_SETUP_LIB/chaosVHost/* /home/larm/wowza_vhost_chaos


# Wowza restart script cannot be called remote
# echo - Restarting server...
# echo ====== Server log : Start ======
# sudo /usr/local/sbin/wowzainit.sh restart
# echo ====== Server log : End ======
echo - Restart server locally on the machine using the command: "sudo /usr/local/sbin/wowzainit.sh restart"

echo Finished deploying Wowza plugin on server
