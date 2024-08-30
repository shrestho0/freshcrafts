
"""
1. Ensure creating log files
"""

import os
from __env import FC_SERVICES_INFO,LOG_DIR, SYSTEMD_SERVICE_DIRECTORY,TEMP_DIR
from rich.console import Console

console = Console()

class SystemDUtil:
    def __init__(self) -> None:
        self.service_data = self.with_abs_path(FC_SERVICES_INFO)
        # console.log(self.service_data)
        self.log_dir = self.ensure_directory(LOG_DIR)
        self.temp_dir = self.ensure_directory(TEMP_DIR)
        self.systemd_dir = SYSTEMD_SERVICE_DIRECTORY
        self.temp_service_files = []
        
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
 
    def copy_service_files_to_systemd(self):
        for service_file in self.temp_service_files:
            console.log("Copying service file to systemd", service_file)
            os.system(f"sudo cp -r {service_file} /lib/systemd/system/")
            console.log("Service file copied to systemd", service_file)

            # check if file exists
            if self.check_file_exists(service_file):
                console.log("Service file exists", service_file, style="bold green")


    def enable_services(self):
        console.log("Enabling services...", style="bold green")
        for k,v in self.service_data.items():
            service_name = v.get("SYSTEMD_SERVICE_NAME")
            os.system(f"sudo systemctl enable {service_name}.service")
            console.log("Service enabled", service_name, style="bold green")

    
    def start_services(self):
        console.log("Starting services...", style="bold green")
        for k,v in self.service_data.items():
            service_name = v.get("SYSTEMD_SERVICE_NAME")
            os.system(f"sudo systemctl start {service_name}.service")
            console.log("Service started", service_name, style="bold green")

    def stop_and_disable_services(self):
        console.log("Stopping and disabling services...", style="bold green")
        for k,v in self.service_data.items():
            service_name = v.get("SYSTEMD_SERVICE_NAME")
            os.system(f"sudo systemctl stop {service_name}.service")
            os.system(f"sudo systemctl disable {service_name}.service")
            console.log("Service stopped and disabled", service_name, style="bold green")

        
    def reload_services(self):
        console.log("Reloading services...", style="bold green")
        os.system("sudo systemctl daemon-reload ")
        console.log("Services reloaded", style="bold green")        

    def restart_services(self, service):
        # restart the service
        console.log("Restarting service...", service, style="bold green")
        os.system(f"sudo systemctl restart {service}.service")
        console.log("Service restarted", service, style="bold green")
                


    def generate_service_files(self):


        for k,v in self.service_data.items():
            service_name = v.get("SYSTEMD_SERVICE_NAME")

            # access_log_file = self.log_dir+"/"+service_name + "_access.log"
            access_log, error_log =  self.ensure_service_logs(service_name)



            isCockpit = v.get("COCKPIT") == True
            console.log(k, isCockpit)
            
            service_template = v.get("SYSTEMD_SERVICE_TEMPLATE_FILE")
            service_exec_command = v.get("SYSTEMD_EXEC_COMMAND")
            service_file_temp = self.temp_dir + "/" + service_name + ".service"
    
            service_executable = v.get("SYSTEMD_EXECUTABLE_FILE")

            with open(service_template, "r+") as f:
                file_content = f.read()
                file_content = file_content.replace("{SERVICE_NAME}", service_name)

                execStart = ""
                if isCockpit:
                    build_dir = self.get_absolute_path(v.get("SYSTEMD_BUILD_DIR"))
                    env_file = self.get_absolute_path(v.get("ENV_FILE"))
                    # service_exec_command
                    service_exec_command = service_exec_command.replace("{ENV}", env_file)
                    service_exec_command = service_exec_command.replace("{BUILD_DIR}", build_dir)
                    execStart = service_exec_command
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
        return os.path.exists(file_path)
    

    def ensure_log_file(self, log_file):
        if self.check_file_exists(log_file):
            console.log("Log file exists", log_file, style="bold green")
        else:
            console.log("Log file does not exist, creating...", log_file, style="bold green")
            with open(log_file, "w") as f:
                f.write("")

    def ensure_directory(self, directory):
        directory = self.get_absolute_path(directory)

        if not os.path.exists(directory):
            os.makedirs(directory)
        return directory

    def ensure_service_logs(self, service_name):
        access_log = f"{self.log_dir}/{service_name}_access.log"
        error_log = f"{self.log_dir}/{service_name}_error.log"

        self.ensure_log_file(access_log)
        self.ensure_log_file(error_log)

        return access_log, error_log
    
    def stop_service(self,service_name):
        # check if service exists
        if os.system(f"systemctl status fc_{service_name}.service") == 0:
            os.system(f"sudo systemctl stop fc_{service_name}.service")
            console.log("Service stopped", service_name, style="bold green")
        # if exists, stop it
    def disable_service(self,service_name):
        # check if service exists
        if os.system(f"systemctl status fc_{service_name}.service") == 0:
            os.system(f"sudo systemctl disable fc_{service_name}.service")
            console.log("Service disabled", service_name, style="bold green")
        # if exists, stop it



# for testing
# if __name__ == "__main__":
#     installer = ServiceUnitStuff()
#     installer.install_services()
#     pass