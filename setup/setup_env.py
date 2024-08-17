#

import os
from time import sleep
from dotenv import dotenv_values
from rich.status import Status
from rich.console import Console

from systemd_util import SystemDUtil

console = Console()
class EnvSetup:
    def __init__(self) -> None:
        self.sysd_util = SystemDUtil()

    def make_env_files_from_root_env(self):
        # #parent dir of this
        # root_dir = os.path.dirname(os.path.abspath(__file__))
        
        # root_env_file = os.path.join(root_dir, ".env")
        # env file
        env_file = ".env"
        env_path = os.path.abspath(env_file)
        env_vals = dotenv_values(env_path)
        env_map ={
            "cockpit": [],
            "wiz_postgres": [],
            "wiz_mongo": [],
            "wiz_mysql": [],
            "engine": [],
            "all_spring": [], # only if key,val are same
            "group_g": [], # stuff for cockpit and engine, jwt and common stuff basically
        }
        all_spring = ["engine","wiz_postgres", "wiz_mongo", "wiz_mysql",]
        group_g = ["cockpit", "engine"]
        remove_prefix = lambda x: x.split("_", 1)[1]
        for x,val in env_vals.items():
            for k in env_map.keys():
                if x.startswith(k.upper().replace("_", "")+"_"):
                    env_map[k].append({remove_prefix(x):val})
        
        # all spring to specific services
        for k in all_spring:
            env_map[k]+= env_map["all_spring"]
        env_map.pop("all_spring")

        for k in group_g:
            env_map[k]+= env_map["group_g"]
        env_map.pop("group_g")
        
        import json
        # console.print_json(json.dumps(env_map))

        for service_name, env_data in env_map.items():
            env_file_loc = self.sysd_util.service_data.get(service_name).get("ENV_FILE")
            # print(f"Writing to {env_file_loc}")
            # write temp files
            with open(f"fc_temp/{service_name}_env", "w+") as f:
                f.write("### This file is auto generated. Feel free to edit ###\n")
                f.write("### Before that, check `freshcrafts/setup/README.md` and make use of common env ###\n")

                for env in env_data:
                    for k,v in env.items():
                        if "-----BEGIN RSA PRIVATE KEY-----" in v:
                            f.write(f'{k}="{v}"\n')
                        else:
                            f.write(f'{k}={v}\n')

            # move to actual location
            try:
                x = os.system(f"cp fc_temp/{service_name}_env {env_file_loc}")
                console.log(f"Env file {service_name} written with exitcode:{x}", style="blue")
            except Exception as e:
                console.log(f"Error writing env file {service_name} with error: {e}", style="red")

if __name__ == "__main__":
    # save current dir
    previous_dir = os.getcwd()
    # get the path of this file
    file_path = os.path.abspath(__file__)
    # cd to this directory
    os.chdir(os.path.dirname(file_path))
    with Status("Setting up environment files..."):
        EnvSetup().make_env_files_from_root_env()
        sleep(0.5)
    console.print("Environment files added services.", style="bold green")
    # cd back to previous dir
    os.chdir(previous_dir)