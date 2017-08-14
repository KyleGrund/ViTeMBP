# update the system
sudo apt-get update
sudo apt-get -y dist-upgrade

# install additional programs
sudo apt-get install -y ant

# manually install java

# donwnload source from github
mkdir ViTeMBP_src
cd ViTeMBP_src/
git clone https://github.com/KyleGrund/ViTeMBP-BEJava.git

# build source with ant
export JAVA_HOME=/opt/jdk1.8.0_144/
ant -Dnb.internal.action.name=build jar
# this is usually performed by the ide, so the first fat jar build will fail, but the second compile will succeed
cp libs/* dist/lib/
ant -Dnb.internal.action.name=build jar

# copy the output jar to the home dir
cp dist/ViTeMBP_Embedded.jar ~/
cd ~/

# create a startup script
echo "sudo /opt/jdk1.8.0_144/bin/java -jar ViTeMBP_Embedded.jar" > start_ViTeMBP.sh
sudo chmod +x start_ViTeMBP.sh

# add script to run at start-up

# install credentials ?

# set M3 core to run accelerometer sketch

# remove uneeded udoo packages