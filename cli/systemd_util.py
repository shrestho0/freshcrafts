
"""
1. Ensure creating log files
"""

import os
import subprocess
from __env import FC_SERVICES_INFO,LOG_DIR, SYSTEMD_SERVICE_DIRECTORY,TEMP_DIR
from rich.console import Console

console = Console()

class SystemDUtil:
    def __init__(self) -> None:
        self.service_data = self.with_abs_path(FC_SERVICES_INFO)
        # console.log(self.service_data)
        self.log_dir = self.get_log_dir()
        self.temp_dir = self.get_temp_dir()
        self.systemd_dir = SYSTEMD_SERVICE_DIRECTORY
        self.installer_log = self.ensure_file(self.log_dir + "/installer.log")
        self.temp_service_files = []
    
    def get_log_dir(self):
        # self.ensure_directory(self.get_absolute_path(os.path.abspath(__file__)+LOG_DIR))
        # current file dir
        return self.ensure_directory(os.path.dirname(os.path.abspath(__file__))+"/"+LOG_DIR) 
    def get_temp_dir(self):
        return self.ensure_directory(os.path.dirname(os.path.abspath(__file__))+"/"+TEMP_DIR)
        
    def with_abs_path(self, FC_SERVICES_INFO):
        # all path of properties with _FILE or _DIR should be absolute for each service
        pwd = os.getcwd()
        os.chdir(os.path.dirname(os.path.abspath(__file__)))

        for k,v in FC_SERVICES_INFO.items():
            for key, value in v.items():
                if key.endswith("_FILE") or key.endswith("_DIR"):
                    FC_SERVICES_INFO[k][key] = os.path.abspath(value) 
        os.chdir(pwd)
        return FC_SERVICES_INFO

    def pre_install_stuff(self):
        self.check_stuff()
        # self.stop_and_disable_services()

    def install_services(self):
        self.pre_install_stuff()
        self.generate_service_files()
        self.copy_service_files_to_systemd()
        self.enable_services()
        self.start_services()
        console.log("Services Installation complete", style="bold green")
 
    def copy_service_files_to_systemd(self, single:str=''):

        # print(self.temp_service_files) # works
        
        xTempFileList  = self.temp_service_files
        if single:
            for f in self.temp_service_files:
                if single in f:
                    xTempFileList = [f]
                    break

        for service_file in xTempFileList:
            # check if file exists
            # if self.check_file_exists(service_file):
            #     console.log("service temp file exists", service_file, style="bold green")
            # else:
            #     console.log("service temp file does not exists", service_file, style="bold red")
            #     exit(69)
            if not self.check_file_exists(service_file):
                console.log("service temp file does not exists", service_file, style="bold red")
                exit(69)
            
            try:
                # console.log("Copying service file to systemd", service_file)
                os.system(f"sudo cp -r {service_file} /lib/systemd/system/")
                # console.log("Service file copied to systemd", service_file)
            except Exception as e:
                console.log("Failed to copy service file ")
                print(e)        
        # success
            


    def enable_dev_service(self, service_name):      
        self.perform_service_action_on_service("enable", service_name, self.is_service_not_enabled, "Enabling")
        self.deamon_reload()

    def start_dev_service(self, service_name):
        self.perform_service_action_on_service("start", service_name, self.is_service_not_running, "Starting")

    def enable_services(self):
        console.log("Enabling services...", style="bold green")
        for k,v in self.service_data.items():
            # service_name = v.get("SYSTEMD_SERVICE_NAME")
            service_name = f"{k}"
            # os.system(f"sudo systemctl enable {service_name}.service")
            # console.log("Service enabled", service_name, style="bold green")
            self.perform_service_action_on_service("enable", service_name, self.is_service_not_enabled, "Enabling")

    
    def start_services(self):
        console.log("Starting services...", style="bold green")
        for k,v in self.service_data.items():
            # service_name = v.get("SYSTEMD_SERVICE_NAME")
            # service_name = f"fc_{k}"
            service_name = f"{k}"
            # os.system(f"sudo systemctl start {service_name}.service")
            self.perform_service_action_on_service("start", service_name, self.is_service_not_running, "Starting")
            
            # console.log("Service started", service_name, style="bold green")

    def stop_and_disable_services(self):
        console.log("Stopping and disabling services...", style="bold green")
        for k,v in self.service_data.items():
            # service_name = v.get("SYSTEMD_SERVICE_NAME")
            # service_name = f"fc_{k}"
            service_name = f"{k}"
            self.stop_service(service_name)
            self.disable_service(service_name)
            # os.system(f"sudo systemctl stop {service_name}.service")
            # os.system(f"sudo systemctl disable {service_name}.service")
            # console.log("Service stopped and disabled", service_name, style="bold green")
        console.log("Services stopped and disabled", style="bold green")
        
    def deamon_reload(self):      
        self.perform_service_action_on_service("sudo systemctl daemon-reload", "", lambda x: True, "Reloading", raw=True)

    def restart_service(self, service):
        # # restart the service
        # console.log("Restarting service...", service, style="bold green")
        # os.system(f"sudo systemctl restart {service}.service")
        # console.log("Service restarted", service, style="bold green")
        # check function handles the negative case
        self.perform_service_action_on_service("restart", service, lambda x: True, "Restarting")
                
    def restart_nginx(self):
        self.perform_service_action_on_service(action="sudo systemctl restart nginx", fc_service_name=None, check_function=lambda x: True, action_name="Restarting", raw=True)

    def delete_temp_service_files(self):
        #
        for f in self.temp_service_files:
            try:
                os.remove(f)
            except Exception as e:
                pass

    def generate_service_files(self, single:str=''):
        
        self.delete_temp_service_files()

        xxd = self.service_data.items()
        for k,v in xxd:
            
            if single != None and single != k:
                continue
                
             
            service_name = f"fc_{k}"

            # access_log_file = self.log_dir+"/"+service_name + "_access.log"
            access_log, error_log =  self.ensure_service_logs(service_name)



            isCockpit = v.get("COCKPIT") == True
            isGopher = v.get("GOPHER") == True
            # console.log(k, isCockpit)
            
            service_template = v.get("SYSTEMD_SERVICE_TEMPLATE_FILE")
            service_exec_command = v.get("SYSTEMD_EXEC_COMMAND")
            service_file_temp = self.temp_dir + "/" + service_name + ".service"
    
            service_executable = v.get("SYSTEMD_EXECUTABLE_FILE")

            with open(service_template, "r+") as f:
                file_content = f.read()
                file_content = file_content.replace("{SERVICE_NAME}", service_name)

                env_file = self.get_absolute_path(v.get("ENV_FILE"))
                
                execStart = ""
                if isCockpit:
                    build_dir = self.get_absolute_path(v.get("SYSTEMD_BUILD_DIR"))
                    # service_exec_command
                    service_exec_command = service_exec_command.replace("{ENV}", env_file)
                    service_exec_command = service_exec_command.replace("{BUILD_DIR}", build_dir)
                    execStart = service_exec_command
                elif isGopher:
                    file_content = file_content.replace("{{SERVICE_ENV_FILE}}", env_file)
                    execStart = f"{service_executable}"
                    
                else:
                    execStart = f"{service_exec_command} {service_executable}"

                file_content = file_content.replace("{{SERVICE_EXEC_START_COMMAND}}", execStart)

                file_content = file_content.replace("{{SERVICE_OUTPUT_LOG}}", access_log)
                file_content = file_content.replace('{{SERVICE_ERROR_LOG}}', error_log)

                # console.log("Service file content", file_content, style="bold green")

                with open(service_file_temp, "w") as f:
                    f.write(file_content)
                    self.temp_service_files.append(service_file_temp)
                    console.log("Service (temp) file created ", service_file_temp, style="bold green")

    def check_stuff(self):

        x  =  1
        for k,v in self.service_data.items():
            # console.log(k, v.get("SYSTEMD_EXECUTABLE_FILE"))

            executable_file = v.get("SYSTEMD_EXECUTABLE_FILE")
            if executable_file:
                executable_exists = self.check_file_exists(executable_file)
                x *= executable_exists
                console.log("Checking file:",executable_file, "OK:", executable_exists)


            build_dir = v.get("SYSTEMD_BUILD_DIR")
            if build_dir:
                # build_directory_abs_path = self.get_absolute_path(build_directory_rel_path)
                build_directory_exists = self.check_file_exists(build_dir)
                # mutate to get abs path
                # self.service_data[k]["SYSTEMD_BUILD_DIR"] = build_directory_abs_path
                x *= build_directory_exists
                console.log("Checking file:",build_dir, "OK:", build_directory_exists)

            service_template_file = v.get("SYSTEMD_SERVICE_TEMPLATE_FILE")
            if service_template_file:
                service_template_exists = self.check_file_exists(service_template_file)
                x *= service_template_exists
                console.log("Checking file:",service_template_file, "OK:", service_template_exists)

        
        if x == 0:
            console.print("Try to build the all services, exiting...", style="bold red")
            exit(1)
        else:
            console.print("All files exist, continuing...", style="bold green")

        


    def get_absolute_path(self, relative_path):
        return os.path.abspath(relative_path)

    def check_file_exists(self, file_path):
        if not file_path:
            raise Exception("File path not provided")
        return os.path.exists(file_path)
    

    def ensure_file(self, f):
        if not f:
            raise Exception("File path not provided")
        
        if not self.check_file_exists(f):
            console.log("Log file does not exist, creating...", {f}, style="bold green")
            with open(f, "w") as fx:
                fx.write("")
        return f
    
    def ensure_directory(self, directory):
        directory = self.get_absolute_path(directory)

        if not os.path.exists(directory):
            os.makedirs(directory)
        return directory
    

    def ensure_service_logs(self, service_name):
        access_log = f"{self.log_dir}/{service_name}_access.log"
        error_log = f"{self.log_dir}/{service_name}_error.log"

        self.ensure_file(access_log)
        self.ensure_file(error_log)

        return access_log, error_log
    
    
    # def stop_service(self,service_name):
    #     # check if service exists
    #     if os.system(f"systemctl status fc_{service_name}.service") == 0:
    #         os.system(f"sudo systemctl stop fc_{service_name}.service")
    #         console.log("Service stopped", service_name, style="bold green")
    #     # if exists, stop it

    def is_service_running(self, service_name):
        # Check if the service is active (running)
        result = subprocess.run(["systemctl", "is-active", service_name], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return result.returncode == 0

    def is_service_enabled(self, service_name):
        # Check if the service is enabled
        result = subprocess.run(["systemctl", "is-enabled", service_name], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return result.returncode == 0

    def is_service_not_running(self, service_name):
        return not self.is_service_running(service_name)
    def is_service_not_enabled(self, service_name):
        return not self.is_service_enabled(service_name)
 
    def stop_service(self, service_name):
        self.perform_service_action_on_service("stop", service_name, self.is_service_running, "Stopping")

    def disable_service(self, service_name):
        self.perform_service_action_on_service("disable", service_name, self.is_service_enabled, "Disabling")
       



    def perform_service_action_on_service(self, action, fc_service_name, check_function, action_name, raw=False):

        if fc_service_name: fc_service_name = f"fc_{fc_service_name}"

        # Check if the service meets the condition for the action
        if not check_function(fc_service_name):
            console.log(f"Service {fc_service_name} is already in the desired state for {action_name}.", style="bold yellow")
            with open(self.installer_log, "a") as f:
                f.write(f"Service {fc_service_name} is already in the desired state for {action_name}.\n")
                f.write("------------------------------------------------\n\n")
            return

        try:
            with open(self.installer_log, "a") as f:
                f.write(f"{action_name} systemd service {fc_service_name}\n")
                f.write("------------------------------------------------\n\n")
                if raw:
                    # action = action.split(" ")
                    subprocess.run(action, check=True, stdout=f, stderr=f, shell=True)
                else:
                    subprocess.run(["sudo", "systemctl", action, fc_service_name], check=True, stdout=f, stderr=f)
                console.log(f"Service {fc_service_name} {action_name.lower()} successfully.", style="bold green")
        except subprocess.CalledProcessError:
            console.log(f"Error {action_name.lower()} systemd service {fc_service_name}.", style="bold red")
            with open(self.installer_log, "a") as f:
                f.write(f"Error {action_name.lower()} systemd service {fc_service_name}.\n")
                f.write("------------------------------------------------\n\n")
        except Exception as e:
            console.log(f"Unexpected error {action_name.lower()} systemd service {fc_service_name}: {e}", style="bold red")
            with open(self.installer_log, "a") as f:
                f.write(f"Unexpected error {action_name.lower()} systemd service {fc_service_name}: {e}\n")
                f.write("------------------------------------------------\n\n")


# for testing
# if __name__ == "__main__":
#     installer = ServiceUnitStuff()
#     installer.install_services()
#     pass