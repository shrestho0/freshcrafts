#!/usr/bin/python3

import os
import sys 
import subprocess
from dotenv import dotenv_values
from rich.console import Console
from rich.progress import Progress
from rich.prompt import Prompt
from rich.status import Status
from __env import DOCKER_COMPOSE_COMMAND, DOCKER_COMPOSE_FILE, REQUIRED_OPEN_PORTS, JAVA_17_AVAILABLE,JAVA_17_DIR,SYSTEM_DEPS
from _utils.utils import parse_domain_from_env_file
from systemd_util import SystemDUtil
from setup_env import EnvSetup
console = Console()
progress = Progress(transient=True,)
 

class BarelyWorkingSetupWizard:    
    
    def __init__(self):
        # check_root_permission(console)
        if not self.can_use_sudo():
            console.print("User must have permission to use `sudo` or be a root user", style="red")
            sys.exit(1)
        self.sysd_util = SystemDUtil()
        # self.__take_consent()

    def can_use_sudo(self):
        try:
            subprocess.run("sudo ls", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            return True
        except Exception as e:
            return False
        
    def pre_install_stuff(self):
        # stop if things are running
        self.sysd_util.stop_and_disable_services()
        # docker compose down
        self.docker_compose_down()

        # check for dependencies
        self.check_dependencies_installed()
        # check if required ports are open (80,443 are handled by nginx, so, ignore them)
        self.check_ports_available()
        # make env files
        # self.make_env_files_from_root_env()
        # check env files are present
        self.check_env_files()


    def prepare_services(self):
        # build each service
        self.build_services()
        # run docker-compose up -d or docker compose up -d if (docker-compose fails)
        self.docker_compose_up()

    
    def install_system_services(self):
        # install system services
        self.sysd_util.install_services()
        # reload service files
        self.sysd_util.reload_services()
        console.log("Services reloaded", style="bold green")

    def install(self):
        self.pre_install_stuff()
        self.prepare_services()
        self.install_system_services()
        self.setup_cockpit_nginx()
        self.complete_process()
    def update(self):
        self.pre_install_stuff()
        self.prepare_services()
        self.install_system_services()
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
        for service, data in self.sysd_util.service_data.items():
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
        for k,v in self.sysd_util.service_data.items():
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
        file = self.sysd_util.get_absolute_path(DOCKER_COMPOSE_FILE)
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
        file = self.sysd_util.get_absolute_path(DOCKER_COMPOSE_FILE)
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




        nginx_template_file = self.sysd_util.service_data.get("cockpit").get("NGINX_CONFIG_TEMPLATE_FILE")
        service_name = self.sysd_util.service_data.get("cockpit").get("SYSTEMD_SERVICE_NAME")
        service_env_file = self.sysd_util.service_data.get("cockpit").get("ENV_FILE")

        SYSTEM_DOMAIN = parse_domain_from_env_file(service_env_file)
        

        nginx_server_name = f"{SYSTEM_DOMAIN} www.{SYSTEM_DOMAIN}"

        with open(nginx_template_file, "r") as f:
            nginx_template = f.read()
            console.log("Nginx template file read", style="blue")
            nginx_template = nginx_template.replace("{NGINX_SERVER_NAME}", nginx_server_name)
            # print(nginx_template)
            
            # write the nginx config file
            nginx_config_file = f"/etc/nginx/sites-available/{service_name}.conf"

            # dir = file dir
            os.chdir(os.path.dirname(__file__))

            with open(f"./fc_temp/{service_name}.conf", "w") as f:
                f.write(nginx_template)
                console.log("Temp Nginx config file written", style="blue")
            

            # keep a backup of the old file
            os.system(f"sudo cp /etc/nginx/sites-available/{service_name}.conf /etc/nginx/sites-available/{service_name}.conf.fcbak")
            # copy the file to /etc/nginx/sites-available
            os.system(f"sudo mv  ./fc_temp/{service_name}.conf /etc/nginx/sites-available/")

            # create a symlink
            # delete the old symlink if exists
            os.system(f"sudo rm -f /etc/nginx/sites-enabled/{service_name}.conf ")
            os.system(f"sudo ln -s /etc/nginx/sites-available/{service_name}.conf /etc/nginx/sites-enabled/")
            console.log("Nginx config symlink created", style="blue")
            # restart nginx
            os.system("sudo systemctl restart nginx")
            console.log("Nginx restarted", style="blue")
            console.log("Nginx setup complete", style="green")


        # import json
        # console.print_json(json.dumps(env_map))
    def setup_env(self):
        s = EnvSetup()
        s.setup_env()

    def uninstall(self):
        pass
    def version(self):
        pass
    
    def stop_service(self):
        # service names space separated from sys.argv
        services = sys.argv[2:]
        if len(services) == 0:
            console.print("No services provided to stop", style="red")
        for service in services:
            self.sysd_util.stop_service(service)
        pass
        
    def disable_service(self):
        # service names space separated from sys.argv
        services = sys.argv[2:]
        if len(services) == 0:
            console.print("No services provided to disable", style="red")
        for service in services:
            self.sysd_util.disable_service(service)
        pass

    def options(self):
        return {
            "--install": {
                "description": "Install freshcrafts from source",
                "function": self.install 
            },
            "--setup-env": {
                "description": "Setup environment files",
                "function": self.setup_env
            },
            "--update": {
                "description": "Update software from source",
                "function": self.update
            },
            "--uninstall": {
                "description": "Uninstall freshcrafts",
                "function": self.uninstall
            },
            "--stop": {
                "description": "Stop services (eg. --stop cockpit engine dep_wizard)",
                "function": self.stop_service
            },
            "--disable": {
                "description": "Stop services (eg. --stop cockpit engine dep_wizard)",
                "function": self.disable_service
            },
            "--help": {
                "description": "Show this help message",
                "function": self.usage
            },
            "--version": {
                "description": "Show version information",
                "function": self.version
            }

        }            

    def usage(self):
        options = self.options()
        console.print("Usage: python3 setup.py [option]", style="blue")
        console.print("Options:")
        # equal spacing
        
        for k,v in options.items():
            space_size = 16 - len(k)
            console.print(f"\t{k}{' '*space_size}; {v.get('description')}", style="blue")
        sys.exit(1)
    
 
if __name__ == "__main__":
    wizard = BarelyWorkingSetupWizard()

    prev_dir = os.getcwd()
    os.chdir(os.path.dirname(os.path.abspath(__file__)))


    # must have a flag --update, --install or --uninstall
    if len(sys.argv) < 2:
        wizard.usage()

    if sys.argv[1] not in wizard.options().keys():
        console.print("Provided options is not allowed", style="red")
        wizard.usage()
        sys.exit(1)
    
    # run the function
    wizard.options().get(sys.argv[1]).get("function")()


    os.chdir(prev_dir)

