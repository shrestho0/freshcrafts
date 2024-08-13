#!/usr/bin/python3

import os
import sys 
import subprocess
from rich.console import Console
from rich.progress import Progress
from rich.prompt import Prompt
from __env import SYSTEM_DOMAIN, DOCKER_COMPOSE_COMMAND, DOCKER_COMPOSE_FILE, REQUIRED_OPEN_PORTS, JAVA_17_AVAILABLE,JAVA_17_DIR,SYSTEM_DEPS
from _utils.utils import check_root_permission
from systemd_services import SystemDServices
console = Console()
progress = Progress(transient=True,)
 

class BarelyWorkingInstaller:    
    def __init__(self):
        check_root_permission(console)
        self.systemD = SystemDServices()
        # self.__take_consent()

    def pre_install_stuff(self):
        # stop if things are running
        self.systemD.stop_and_disable_services()
        # docker compose down
        self.docker_compose_down()

        # check for dependencies
        self.check_dependencies_installed()
        # check if required ports are open (80,443 are handled by nginx, so, ignore them)
        self.check_ports_available()
        # check env files are present
        self.check_env_files()


    def prepare_services(self):
        # build each service
        self.build_services()
        # run docker-compose up -d or docker compose up -d if (docker-compose fails)
        self.docker_compose_up()


    
    def install_system_services(self):
        # install system services
        self.systemD.install_services()
        # reload service files
        self.systemD.reload_services()
        console.log("Services reloaded", style="bold green")

    def install(self):
        self.pre_install_stuff()
        self.prepare_services()
        self.install_system_services()
        self.setup_cockpit_nginx()
        self.complete_process()


    def complete_process(self):
        # do something
        xx = f"""
+------------------------------------------+
| 🎉 Installation completed successfully.  |
| 🚀 You can now start using FreshCrafts.  |
+------------------------------------------+
"""
        console.print(xx, style="blue_violet")
        sys.exit(0)

    def check_ports_available(self):
        x = 1
        for port in REQUIRED_OPEN_PORTS:
            if port == 80 or port == 443:
                console.log(f"✔ Port {port} is handled by nginx. Skipping...", style="blue")
                continue
            if self.port_available(port):
                console.log(f"✔ Port {port} is available.", style="green")
                x *= 1
            else:
                console.log(f"👹 Port {port} is not available. Please available port {port}.", style="red")
                x *= 0
        if x == 0:
            console.print("Please ensure required ports are available.", style="red")
            sys.exit(1)
        else: 
            console.print("All required ports are available.", style="green")
        return x
    
    def port_available(self, port): 
        cmd = f"netstat -tuln | grep :{port}"
        result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        # print(result)
        return bool(result.returncode)
    
    def check_env_files(self):
        x = 1
        # check if .env file or resources/application.properties file exists
        # for services 
        for service, data in self.systemD.service_data.items():
            envFile = data.get("ENV_FILE")
            if os.path.exists(envFile):
                console.log(f"✔ {service} environment is okay.", style="green"); x *= 1
            else:
                console.log(f"👹 {service} environment is not okay. Please check {data['ENV_FILE']}.", style="red")
                x *= 0
        if x == 0:
            console.print("Please ensure env files are present.", style="red")
            sys.exit(1)
        else:
            console.print("All env files are present.", style = "green")

        return x
    
    def build_services(self):
        # goto to directory
        # check if build dir exists
        x = 1
        for k,v in self.systemD.service_data.items():
            src_dir = v.get("SRC_DIR")
            isCockpit = v.get("COCKPIT")
            if os.path.exists(src_dir):
                console.log(f"✔ {k} source directory exists.", style="green")
                res,returnCode = self.build_service(src_dir, isCockpit)
                if not res:
                    console.log(f"👹 {k} build failed with exitcode {returnCode}", style="red")
                    x*=0
                else:
                    console.log(f"✔ {k} build successful.", style="green")
                x+=1
            else:
                console.log(f"👹 {k} source directory does not exist. Please check {src_dir}.", style="red")
                x*=0

        if x == 0:
            console.print("Failed to build services", style="red")
            sys.exit(1)
        else:
            console.print("Build successful", style="green")

                # self.build_service(k, v)
    def delete_old_builds(self, src_dir, isCockpit):
        delete_command = f"sudo rm -rf {src_dir}/target && sudo rm -rf {src_dir}/build"
        result = subprocess.run(delete_command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return result.returncode==0, result.returncode
        
    def build_service(self, src_dir, isCockpit):
        # TODO: delete old builds
        console.log("Deleting old builds", style="blue")
        res,returnCode = self.delete_old_builds(src_dir, isCockpit)
        if not res:
            console.log(f"👹 {src_dir} failed removing old builds with exitcode {returnCode}", style="red")
            return False, returnCode

        # build new
        console.log("Building service", src_dir)
        build_command = f""
        which_java = subprocess.run("which java", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        console.print(str(which_java.stdout.decode("utf-8")), str(which_java.stderr.decode("utf-8")), f"\n{which_java.returncode=}")
        if isCockpit:
            build_command = " /usr/bin/npm install && /usr/bin/npm run build"
        else:
            os.environ["JAVA_HOME"] = JAVA_17_DIR
            build_command = "sudo mvn clean -X && mvn package"
        # cd to src_dir &  run build command
        os.chdir(src_dir)
        thing_name = src_dir.split("/")[-1]
        with console.status(f"Building {thing_name}"):
            result = subprocess.run(build_command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            # console.print(str(result.stdout.decode("utf-8")), str(result.stderr.decode("utf-8")), f"\n{result.returncode=}")
            # return true if success else false along with exit code
            # back to the parent dir, freshcrafts dir
            # os.chdir(os.pardir)
        return result.returncode==0, result.returncode

    def docker_compose_up(self):
        # find the appropriate docker-compose file
        file = self.systemD.get_absolute_path(DOCKER_COMPOSE_FILE)
        console.log("docker-compose file locaton",file)
        os.chdir(os.path.dirname(file))
        console.log("Current working directory", os.getcwd())
        if not os.path.exists(file):
            console.print("docker-compose file not found", style="red")
            sys.exit(1)
        
        # run docker compose up -d
        command = DOCKER_COMPOSE_COMMAND
        result = subprocess.run(command, shell=True)

        if result.returncode == 0:
            console.print("Docker compose up successful", style="green")
        else:
            console.print("Docker compose up failed", style="red")
            # console.print(result.stderr.decode("utf-8"), style="red")
            sys.exit(1)
    def docker_compose_down(self):
        # find the appropriate docker-compose file
        file = self.systemD.get_absolute_path(DOCKER_COMPOSE_FILE)
        console.log("docker-compose file locaton",file)
        os.chdir(os.path.dirname(file))
        console.log("Current working directory", os.getcwd())
        if not os.path.exists(file):
            console.print("docker-compose file not found", style="red")
            sys.exit(1)
        
        # run docker compose down
        command = "docker compose down"
        result = subprocess.run(command, shell=True)

        if result.returncode == 0:
            console.print("Docker compose down successful", style="green")
        else:
            console.print("Docker compose down failed", style="red")
            # console.print(result.stderr.decode("utf-8"), style="red")
            sys.exit(1)
    
    def __take_consent(self):
        prompt = Prompt.ask("Do you want to install FreshCrafts ?", choices=["y", "n"], default="y")
        if prompt == "n":
            console.print("Installation cancelled by user.", style="red")
            sys.exit(69)
        return 1

    def check_dependencies_installed(self):
        x = 1
        for dep in SYSTEM_DEPS:

            package = dep.get("package")
            checker_command = dep.get("checker_command")
            required_version = dep.get("required_version")
            required_version_min = dep.get("required_version_min")

            # print(f"Checking {package}...; {checker_command} {required_version=} {required_version_min=}")

            try:
                res = subprocess.run(checker_command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
                assert res.returncode == 0, f"👹 {package} is not installed"
                # if required_version:
                #     assert required_version in res.stdout.decode("utf-8"), f"👹 {package} version is not {required_version}"
                # if required_version_min:
                console.log(f"✔ {package} is installed.", style="green")
                x *= 1
            except AssertionError as e:
                console.log(e, style="red")
                x*=0


        if x == 0:
            console.print("Please ensure dependencies are installed.", style="red")
            sys.exit(1)
        else:
            console.print("All dependencies are installed.", style="green")                


    def setup_cockpit_nginx(self):
        # get the nginx config template file

        nginx_server_name = f"{SYSTEM_DOMAIN} www.{SYSTEM_DOMAIN}"

        nginx_template_file = self.systemD.service_data.get("cockpit").get("NGINX_CONFIG_TEMPLATE_FILE")
        service_name = self.systemD.service_data.get("cockpit").get("SYSTEMD_SERVICE_NAME")

        with open(nginx_template_file, "r") as f:
            nginx_template = f.read()
            console.log("Nginx template file read", style="blue")
            nginx_template = nginx_template.replace("{NGINX_SERVER_NAME}", nginx_server_name)
            # print(nginx_template)
            
            # write the nginx config file
            nginx_config_file = f"/etc/nginx/sites-available/{service_name}.conf"
            with open(nginx_config_file, "w") as f:
                f.write(nginx_template)
                console.log("Nginx config file written", style="blue")
            # create a symlink
            # delete the old symlink if exists
            os.system(f"sudo rm -f /etc/nginx/sites-enabled/{service_name}.conf ")
            os.system(f"sudo ln -s /etc/nginx/sites-available/{service_name}.conf /etc/nginx/sites-enabled/")
            console.log("Nginx config symlink created", style="blue")
            # restart nginx
            os.system("sudo systemctl restart nginx")
            console.log("Nginx restarted", style="blue")
            console.log("Nginx setup complete", style="green")

 
if __name__ == "__main__":
    BarelyWorkingInstaller = BarelyWorkingInstaller()
    # we'll setup .env files from here too, but, not now
    BarelyWorkingInstaller.install()
    console.print(f"visit http://{SYSTEM_DOMAIN}/_/setup", style="green")
    # BarelyWorkingInstaller.setup_cockpit_nginx()
    # BarelyWorkingInstaller.check_dependencies_installed()

