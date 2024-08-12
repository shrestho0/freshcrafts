#!/usr/bin/python3

import os
import sys 
import subprocess
from rich.console import Console
from rich.progress import Progress
from rich.prompt import Prompt
from __env import DATA_FILE_LOCATION, FC_SERVICES_INFO, LOG_DIR, LOG_FILE_NAME, REQUIRED_OPEN_PORTS
from _utils.utils import check_root_permission
from install_systemd_services import ServiceUnitStuff

console = Console()
progress = Progress(transient=True,)

# class Installer:

#     def __init__(self):
#         self.print_queue = []
#         check_root_permission(console)
     
 
#     def install(self):
#         from time import sleep
#         self.__take_consent()
#         final_x = 1

#         proc_list = [
#             {
#                 "func": self.__check_dependencies_installed,
#                 "success_message": "💀 Dependencies are installed.",
#                 "error_message": "👹 Dependencies are not installed.",
#             },
#             {
#                 "func": self.__check_fc_ports_available,
#                 "success_message": "💀 Required Ports are available.",
#                 "error_message": "👹 Required ports are not available.",
#             },
#             {
#                 "func": self.__check_fc_services_environment,
#                 "success_message": "💀 FreshCrafts services environment is okay.",
#                 "error_message": "👹 FreshCrafts services environment is not okay.",
#             }
#         ]

#         for proc in proc_list:
#             x= proc["func"]()
#             final_x *= x
#             sleep(.5)

#             self.print_queue.append({"message": proc["success_message"] if x ==1 else proc["error_message"], "style":"blue" if x == 1 else "red" })

#             self.do_print_queue()

#         if final_x == 0:
#             console.print("\n`Please ensure requirements are filled.`", style="bold red")
#             sys.exit(1)
 

#         xx = f"""
# +------------------------------------------+
# | 🎉 Installation completed successfully.  |
# | 🚀 You can now start using FreshCrafts.  |
# +------------------------------------------+
# """
 
#         console.print(xx, style="blue_violet")
        
#         sys.exit(0)

#     def do_print_queue(self):
#         for item in self.print_queue:
#             console.print(item["message"], style=item["style"])

    

#     def __take_consent(self):
#         prompt = Prompt.ask("Do you want to install FreshCrafts ?", choices=["y", "n"], default="y")
#         if prompt == "n":
#             console.print("Installation cancelled by user.", style="red")
#             sys.exit(69)
#         return 1

#     def __check_dependencies_installed(self):
#         """
#         Checks if all the dependencies are installed
#         Exits on error if graceful=False
#         """
#         x = 1
#         x *= self.__check_dependency("docker -v", name="docker",  )
#         x *= self.__check_dependency("docker-compose -v",name="docker-compose",  )
#         x *= self.__check_dependency("nginx -v", name="nginx", )
#         x *= self.__check_dependency("java --version", name="java", )
#         x *= self.__check_dependency("javac --version", name="javac",)
#         x *= self.__check_dependency("mvn -v", name="mvn", )
#         x *= self.__check_dependency("psql -V", name="postgres client", )
#         x *= self.__check_dependency("mysql -V", name="mysql client", )
#         x *= self.__check_dependency("mysqldump -V", name="mysqldump client", )
#         x *= self.__check_dependency("mongo --version", name="mongodb shell", )

#         # x *= self.__check_dependency("mvnxx -v", name="mvnxx", )

#         if x == 0:
#             console.print("\n`Install the dependencies first.`", style="bold red")
#             sys.exit(1)
#         return x
        
        
#     def __check_dependency(self, command="", name="", graceful=True):
#         try:
#             if command == "":
#                 raise Exception("Command not provided")
            
#             # code = os.system(command)
#             # code, output = commands.getstatusoutput("cat /etc/services")
#             sp = subprocess.Popen(command+" 1> /dev/null ; echo $?", shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
#             out, err = sp.communicate()

#             err_code = int(out.decode("utf-8").split("\n")[0])
            
#             if err_code != 0:
#                 console.print(f"{name} is not installed. Please install {name} first.", style="red")
#                 console.error(f"☠ Dependency {name} is not installed.")
#                 if(not graceful):
#                     console.error(f"☠ Terminating installation process.")
#                     sys.exit(1)
            
#             else:
#                 console.print(f"✔ {name} is installed.", style="green", )
#                 return 1
#         except Exception as e:
#             console.print(f"Error while checking {command}: {e}", style="red")
#             if(not graceful):
#                 console.error(f"Error while checking {name}: {e}")
#                 # sys.exit(1)

#         return 0
    
#     def __check_fc_ports_available(self):
#         x = 1
#         for port in REQUIRED_OPEN_PORTS:
#             if self.__check_port(port):
#                 console.print(f"Port {port} is available.", style="green")
#                 x *= 1
#             else:
#                 console.print(f"Port {port} is not available. Please available port {port}.", style="red")
#                 x *= 0
#         return x
    
#     def __check_port(self, port): 

#         cmd = f"netstat -tuln | grep :{port}"
#         result = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        
#         # If the output is empty, the port is available
#         if result.stdout:
#             return False
#         else:
#             return True
        
#     def __check_fc_services_environment(self):
#         x = 1

#         # check if .env file or resources/application.properties file exists
#         # for services 
#         for service, data in FC_SERVICES_INFO.items():
#             envFile = data.get("ENV_FILE")
#             if os.path.exists(envFile):
#                 console.print(f"✔ {service} environment is okay.", style="green")
#                 x *= 1
#             else:
#                 console.print(f"👹 {service} environment is not okay. Please check {data['ENV_FILE']}.", style="red")
#                 x *= 0
#         return x

class Installer:    
    def __init__(self):
        check_root_permission(console)

    def build_stuff(self):
        # check if required ports are open (80,443 are handled by nginx, so, ignore them)
        # check env files are present
        # build each service
        # check if all build passed
        # run docker-compose up -d or docker compose up -d if (docker-compose fails)
        # install system services
        # reload service files


        pass
    def install_system_services(self):
        ServiceUnitStuff().install_services()

if __name__ == "__main__":
    installer = Installer()
    installer.install_system_services()


