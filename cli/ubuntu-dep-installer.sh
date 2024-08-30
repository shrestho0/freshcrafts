#!/usr/bin/bash

# set -e


 
GO_VERSION_ARCH=go1.23.0.linux-amd64
NODE_VERSION=20
OUTPUT_LOG=./ubuntu-dep-installer.log


if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi


function install_common(){
  echo "Installing common dependencies..."
  sudo apt install -y wget curl git unzip > $OUTPUT_LOG
  echo "Common dependencies installed successfully"
}

function install_go(){
  echo "Installing Go..."
  wget https://go.dev/dl/$GO_VERSION_ARCH.tar.gz -O $GO_VERSION_ARCH.tar.gz >> $OUTPUT_LOG 2>&1
  rm -rf /usr/local/go && tar -C /usr/local -xzf $GO_VERSION_ARCH.tar.gz && rm $GO_VERSION_ARCH.tar.gz 
  # export PATH=$PATH:/usr/local/go/bin
  echo "export PATH=$PATH:/usr/local/go/bin" >> /etc/profile
  source /etc/profile
  go version >> $OUTPUT_LOG 2>&1
  echo "Go installed successfully"
}

function install_python(){

  echo "Installing python3, python3-pip"

  sudo apt install python3 python3-pip -y >> $OUTPUT_LOG 2>&1
  python3 -m pip install rich --no-warn-script-location >> $OUTPUT_LOG 2>&1
  python3 -m pip install python-dotenv --no-warn-script-location >> $OUTPUT_LOG 2>&1

  echo "Python installed successfully" 
}



function install_docker(){
  echo "Installing Docker..."
  # Docker
  for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do sudo apt-get remove $pkg >> $OUTPUT_LOG 2>&1 ; done

  # Add Docker's official GPG key:
  sudo apt-get update >> $OUTPUT_LOG 2>&1
  sudo apt-get install ca-certificates curl >> $OUTPUT_LOG 2>&1
  sudo install -m 0755 -d /etc/apt/keyrings >> $OUTPUT_LOG 2>&1
  sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc >> $OUTPUT_LOG 2>&1
  sudo chmod a+r /etc/apt/keyrings/docker.asc >> $OUTPUT_LOG 2>&1

  # Add the repository to Apt sources:
  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

  sudo apt-get update >> $OUTPUT_LOG 2>&1

  sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y >> $OUTPUT_LOG 2>&1

  echo "Docker installed successfully"
}

function install_java17(){
  # Installing java 17
  echo "Installing Java 17..."
  sudo apt install libc6-i386 libc6-x32 libxi6 libxtst6 -y >> $OUTPUT_LOG 2>&1
  wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.deb >> $OUTPUT_LOG 2>&1
  sudo dpkg -i jdk-17_linux-x64_bin.deb >> $OUTPUT_LOG 2>&1
  java -version >> $OUTPUT_LOG 2>&1
  echo "Java 17 installed successfully"
}


function install_nodejs(){
  echo "Installing NodeJS..."

  sudo apt-get install -y curl >> $OUTPUT_LOG 2>&1
  curl -fsSL https://deb.nodesource.com/setup_$NODE_VERSION.x -o nodesource_setup.sh >> $OUTPUT_LOG 2>&1
  sudo -E bash nodesource_setup.sh >> $OUTPUT_LOG 2>&1
  sudo apt-get install -y nodejs >> $OUTPUT_LOG 2>&1
  node -v >> $OUTPUT_LOG 2>&1
  echo "NodeJS installed successfully"  
}

function install_maven(){

  echo "Installing Maven..."
  sudo apt install maven -y >> $OUTPUT_LOG 2>&1
  mvn -v >> $OUTPUT_LOG 2>&1
  echo "Maven installed successfully"

}


function install_nginx(){
  echo "Installing NGINX..."
  sudo apt install nginx -y >> $OUTPUT_LOG 2>&1
  nginx -v >> $OUTPUT_LOG 2>&1
  echo "NGINX installed successfully"
}

function install_certbot(){
  echo "Installing Certbot..."
  sudo apt install certbot python3-certbot-nginx -y >> $OUTPUT_LOG 2>&1
  echo "Certbot installed successfully" 
}

function welcome(){
  echo "===== FreshCrafts System Dependency Installer ====="
  echo "================= Ubuntu Edition =================="
  echo -e "\033[1mNote: This is an unstable installer made from instructions of the package's website\033[0m"
  echo -e "\033[1mNote: This script is explicitly written for Ubuntu based systems\033[0m"
  echo " ___________                     .__    _________                _____  __          "
  echo " \_   _____/______   ____   _____|  |__ \_   ___ \____________ _/ ____\/  |_  ______"
  echo "  |    __) \_  __ \_/ __ \ /  ___/  |  \/    \  \/\_  __ \__  \\   __\\   __\/  ___/"
  echo "  |     \   |  | \/\  ___/ \___ \|   Y  \     \____|  | \// __ \|  |   |  |  \___ \ "
  echo "  \___  /   |__|    \___  >____  >___|  /\______  /|__|  (____  /__|   |__| /____  >"
  echo "      \/                \/     \/     \/        \/            \/                 \/ "
}

function update_system(){
  echo "Updating system..."
  # output should go on ./ubuntu-dep-installer.log
  sudo apt update -y > ./ubuntu-dep-installer.log
  sudo apt upgrade -y >> ./ubuntu-dep-installer.log
}

welcome
update_system

echo "Installing dependencies..."


install_common

install_go
install_python
install_docker
install_java17
install_nodejs
install_maven
install_nginx
install_certbot

echo "All dependencies installed successfully"
