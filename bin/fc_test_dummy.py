
import time
import datetime
from rich.console import Console

console = Console()

while True:
    now = datetime.datetime.now()
    if now.second % 3 == 0:
        raise Exception("Random Error")
    now = now.replace(microsecond=0).isoformat()
    console.log(f"Current Time: {now}", style="bold cyan")

    time.sleep(2)


