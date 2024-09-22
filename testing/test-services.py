
from TestDBServices import TestDBServices
from rich.console import Console
from time import sleep


if __name__ == "__main__":
    console = Console()

    # console.print("Running tests for MySQL API Wizard (ENGINE API)...", style="bold cyan2")
    # new_test = TestDBServices(ENDPOINT = "http://localhost:10000/api/v1/db-mysql")
    # new_test.run_tests()

    # sleep(1)

    # console.print("Running tests for Postgres Wizard (ENGINE API)...", style="bold cyan2")
    # new_test = TestDBServices(ENDPOINT = "http://localhost:10000/api/v1/db-postgres")
    # new_test.run_tests()
    
    # sleep(1)

    # console.print("Running tests for Mongo Wizard (ENGINE API)...", style="bold cyan2")
    # new_test = TestDBServices(ENDPOINT = "http://localhost:10000/api/v1/db-mongo", IGNORE=[2,6])
    # new_test.run_tests()

    # sleep(1)

    console.print("Running tests for Mongo Wizard (ENGINE API)...", style="bold cyan2")
    new_test = TestDBServices(ENDPOINT = "http://localhost:10000/api/v1/db-redis", IGNORE=[2,3,5,6])
    new_test.run_tests()


    