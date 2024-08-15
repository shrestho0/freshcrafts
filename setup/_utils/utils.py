import os
import subprocess
import sys 
from dotenv import dotenv_values, load_dotenv

def check_root_permission(console):
    """
    https://stackoverflow.com/questions/2806897/what-is-the-best-way-for-checking-if-the-user-of-a-script-has-root-like-privileg
    """
    if os.geteuid() != 0:
        console.print("You need to have root privileges to run this script.", style="bold red")
        sys.exit(1)
 
def find_java17():
    """
    @description Finds the java 17 installation directory
    @returns available:boolean, path:string
    """
    
    java_home = os.environ.get('JAVA_HOME')
    java_paths = [java_home] if java_home else []
    java_paths.extend([
        "/usr/lib/jvm/java-17-openjdk-amd64",
        "/usr/lib/jvm/java-17-openjdk",
        "/usr/java/jdk-17",
        "/usr/lib/jvm/java-17-oracle",
        #...more paths if required, later
    ])

    for path in java_paths:
        java_bin = os.path.join(path, "bin", "java")
        if os.path.exists(java_bin):
            try:
                version = subprocess.check_output([java_bin, "-version"], stderr=subprocess.STDOUT).decode()
                if '17' in version:
                    # print(f"Java 17 found at: {path}")
                    return True, path
            except subprocess.CalledProcessError:
                continue

    return False, ""


def parse_domain_from_env_file(file_path:str)->str:
    """
    @depricated
    Depricate it,
    Get values from .env file while splitting env data to service specific files
    """
    # x = load_dotenv(dotenv_path=file_path)
    env_path = os.path.abspath(file_path)
    env_vals = dotenv_values(env_path)
    # origin = os.getenv("ORIGIN")
    origin = env_vals.get("ORIGIN")
    if not origin:
        raise ValueError("ORIGIN not found in .env file")
    
    domain = origin.split("//")[-1]
    return domain
