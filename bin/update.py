#!/usr/bin/python3

from rich.console import Console
from rich.progress import Progress
from _utils.utils import check_root_permission

class Updater:
    def __init__(self) -> None:
        self.console = Console()
        # self.datafile = self.get_data_file()
        self.progress = Progress(transient=True,)
        self.print_queue = []
        self.clear_console()
        check_root_permission(self.console)

    
    def update(self):
        self.console.log("Updating FreshCrafts...", style="bold cyan")
        self.console.log("Updating FreshCrafts...", style="bold cyan")
        """
        what is does:
        1. 
        2. build each service
        3. reload service files
        """
    def clear_console(self):
        self.console.clear()
        self.console.print("========== FreshCrafts (v0.1) Updater ==========\n", style="bold cyan")

    

if __name__ == "__main__":
    updater = Updater()
    updater.update()