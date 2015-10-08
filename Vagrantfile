# -*- mode: ruby -*-
# vi: set ft=ruby :

$script = <<SCRIPT

#sudo sed -i 's/us.//g' /etc/apt/sources.list

sudo apt-get update

sudo apt-get install software-properties-common vim curl libgd-dev -y

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections && sudo add-apt-repository -y ppa:webupd8team/java && sudo apt-get update && sudo apt-get install -y oracle-java8-installer

sudo apt-get install imagemagick git nginx python-pip -y

wget -P /tmp https://nodejs.org/dist/v4.1.2/node-v4.1.2-linux-x64.tar.gz

sudo tar xvf /tmp/node-v4.1.2-linux-x64.tar.gz -C /opt/

sudo ln -sf /opt/node-v4.1.2-linux-x64/bin/npm /usr/bin/npm

sudo ln -sf /opt/node-v4.1.2-linux-x64/bin/node /usr/bin/node

sudo pip install awscli

SCRIPT

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.provision "shell", inline: $script

  config.vm.synced_folder ".", "/home/vagrant/grid"

  config.vm.define "node_1" do |node_1|
    node_1.vm.box = "opscode_ubuntu-14.04_provisionerless"
    node_1.vm.box_url = "http://opscode-vm-bento.s3.amazonaws.com/vagrant/virtualbox/opscode_ubuntu-14.04_chef-provisionerless.box"

    node_1.vm.hostname = "node1"

    node_1.vm.provider :virtualbox do |vb|
      vb.customize ["modifyvm", :id, "--memory", "4096"]
    end
  end
end
