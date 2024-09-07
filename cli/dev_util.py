
import json
import subprocess
class DevUtil:
    def __init__(self):
        pass

    def start_cockpit(self,service):
        print("Starting Cockpit")
        src_dir = service.get("SRC_DIR")
        cmd = "npm run dev"
        try:
            subprocess.run(cmd,shell=True, cwd=src_dir)
        except Exception as e:
            print("Error running cockpit")
            print(e)
    
    def start_with_build_and_run_file(self,service):
        print("Starting service with build and run file")   
        src_dir = service.get("SRC_DIR")

        cmd = f"{src_dir}/build_and_run.sh"
    
        try:
            subprocess.run(cmd,shell=True, cwd=src_dir, check=True)
        except Exception as e:
            print("Error running build and run file")
        
    def start_gopher(self,service):
        print("Starting gopher with build and run file")   
        src_dir = service.get("SRC_DIR")

        cmd = f"{src_dir}/run.sh"
    
        try:
            subprocess.run(cmd,shell=True, cwd=src_dir, check=True)
        except Exception as e:
            print("Error running build and run file")
        