import os
import sys 
def check_root_permission(console):
    """
    https://stackoverflow.com/questions/2806897/what-is-the-best-way-for-checking-if-the-user-of-a-script-has-root-like-privileg
    """
    if os.geteuid() != 0:
        console.print("You need to have root privileges to run this script.", style="bold red")
        sys.exit(1)
 
