

import os
from time import sleep
from dotenv import dotenv_values
from rich.status import Status
from rich.console import Console

from __env import ROOT_ENV_FILE, DOCKER_COMPOSE_FILE
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
        env_file = ROOT_ENV_FILE
        env_path = os.path.abspath(env_file)
        env_vals = dotenv_values(env_path)
        
        # check if values are present
        if len(env_vals) == 0: 
            console.print("Make sure you have .env in the root and it has values", style="red")
            exit(1)

        self.make_docker_compose_file(env_vals)

        env_map ={
            "cockpit": [],
            "wiz_postgres": [],
            "wiz_mongo": [],
            "wiz_mysql": [],
            "depwiz":[],
            "redwiz": [],
            "engine": [],
            "gopher": [],
            "all_spring": [], # only if key,val are same
            "group_g": [], # stuff for cockpit and engine, jwt and common stuff basically
            "group_w":[], # stuff for cockpit, and gopher
        }
        all_spring = ["engine","wiz_postgres", "wiz_mongo", "wiz_mysql","depwiz", "redwiz"]
        group_g = ["cockpit", "engine"]
        group_w = ["cockpit", "gopher"]
        remove_prefix = lambda x: x.split("_", 1)[1]
        
        for x,val in env_vals.items():
            if x.startswith("_"):
                continue
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
        
        for k in group_w:
            env_map[k]+= env_map["group_w"]
        env_map.pop("group_w")
        
        
        # import json
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
                
    def make_docker_compose_file(self, env_vals: dict[str, str]):
        
        # values we need 
        # For mongo:
        # _PRIMARY_MONGO_PORT, _PRIMARY_MONGO_USERNAME, _PRIMARY_MONGO_PASSWORD, 
        # _SECONDARY_MONGO_PORT, _SECONDARY_MONGO_USERNAME, _SECONDARY_MONGO_PASSWORD
        # For redis: 
        # _SECONDARY_REDIS_PORT, _SECONDARY_REDIS_PASSWORD,
        # For mysql: 
        # _SECONDARY_MYSQL_PORT, _SECONDARY_MYSQL_USERNAME, _SECONDARY_MYSQL_PASSWORD
        # For postgres: 
        # _SECONDARY_POSTGRES_PORT, _SECONDARY_POSTGRES_USERNAME, _SECONDARY_POSTGRES_PASSWORD
        
        template = ""
        with open("./templates/docker-compose.yml.template", "r") as f:
            template = f.read()
        
        replacements = [
            "_PRIMARY_MONGO_PORT",
            "_PRIMARY_MONGO_USERNAME",
            "_PRIMARY_MONGO_PASSWORD",
            "_SECONDARY_MONGO_PORT",
            "_SECONDARY_MONGO_USERNAME",
            "_SECONDARY_MONGO_PASSWORD",
            "_SECONDARY_MYSQL_PORT",
            "_SECONDARY_MYSQL_PASSWORD",
            "_SECONDARY_POSTGRES_PORT",
            "_SECONDARY_POSTGRES_USERNAME",
            "_SECONDARY_POSTGRES_PASSWORD",
            "_SECONDARY_REDIS_PASSWORD",
            "_SECONDARY_REDIS_PORT",
        ]
        
        with open(DOCKER_COMPOSE_FILE, "w+") as f:
            for k,v in env_vals.items():
                if k in replacements:
                    template = template.replace(k, v)
            
            f.write(template)
            
                    
                    
        
        
        

    def setup_env(self):
        # save current dir
        previous_dir = os.getcwd()
        # get the path of this file
        file_path = os.path.abspath(__file__)
        # cd to this directory
        os.chdir(os.path.dirname(file_path))
        with Status("Setting up environment files..."):
            self.make_env_files_from_root_env()
            sleep(0.5)
        console.print("Environment files added services.", style="bold green")
        # cd back to previous dir
        os.chdir(previous_dir)