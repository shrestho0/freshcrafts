import random
import requests
from time import sleep
from rich.progress import Progress, BarColumn, TextColumn  
from rich.console import Console


# ENDPOINT = "http://localhost:10000/api/v1/db-mysql"
console = Console()
class TestDBServices:

    def __init__(self, ENDPOINT, IGNORE=[]):
        self.id = self.dbName = self.dbUser = self.dbPassword = "fc_test"+ str(random.randint(1,69))
        self.ENDPOINT = ENDPOINT
        self.IGNORE=IGNORE
        self.isRedis = "redis" in ENDPOINT


    def run_tests(self):
        # test cases:
        # 1. Create DB
        # 2. Update DB
        # 2.1 Update DB Name
        # 2.2 Update DB User
        # 2.3 Update DB Password
        # 2.4 Update All Together
        # 3. Delete DB
        arr = [
            {"case": 1, "name": "create_db", "func": self.test_create_db, "validate": True},
            {"case": 2, "name": "update_dbname", "func": self.test_update_dbname, "validate": True},
            {"case": 3, "name": "update_dbuser", "func": self.test_update_dbuser, "validate": True},
            {"case": 4, "name": "update_dbpass", "func": self.test_update_dbpass, "validate": True},
            {"case": 5, "name": "update_db_user_pass", "func": self.test_update_db_user_pass, "validate": True},
            {"case": 6, "name": "update_dball", "func": self.test_update_dball, "validate": True},
            {"case": 7,"name": "delete_db", "func": self.test_delete_db, "validate": True, "inverted": True},
 
        ]

        with Progress(
            TextColumn("[bold blue]{task.description}"),
            BarColumn(),
            TextColumn("[bold green]Checking..."),
            console=console,
            transient=True,
        ) as progress:
            
            tasks = {i['case']: progress.add_task(f"Test {i['case']} {i['name']}", total=1) for i in arr}

            for i in arr:
                progress.update(tasks[i['case']], completed=0)


                if i['case'] in self.IGNORE:
                    progress.update(tasks[i['case']], completed=1)
                    console.print(f"Test {i['case']} {i['name']}: [bold blue]IGNORED")
                    continue


                success, res_msg = i["func"]()             

                if success:
                    if i["validate"]:
                        sleep(1)
                        validated_success, validated_msg = self.validate_from_server()

                        # inverse for delete cases
                        if i.get("inverted", False): validated_success = not validated_success

                        if validated_success:
                            # success
                            progress.update(tasks[i['case']], completed=1)
                            console.print(f"Test {i['case']} {i['name']}: [bold green]Passed+Validated")

                        else:
                            # validation failed 
                            console.print(f"Test {i['case']} {i['name']}: [bold red]Failed")
                            console.print(f"Message: {validated_msg}")
                            return

                    else:
                        progress.update(tasks[i['case']], completed=1)
                        console.print(f"Test {i['case']} {i['name']}: [bold green]Passed")
                else:
                    console.print(f"Test {i['case']} {i['name']}: [bold red]Failed")
                    console.print(f"Message: {res_msg}")
                    return
                    

                # if val and not i["validate"]:
                #     progress.update(tasks[i['case']], completed=1)
                #     console.print(f"Test {i['case']} {i['name']}: [bold green]Passed")
                    
                # elif val and i["validate"]:
                #     sleep(1)
                #     val, msg = self.validate_from_server()
                #     print(val, msg)
                #     if val:
                #         progress.update(tasks[i['case']], completed=1)
                #         console.print(f"Test {i['case']} {i['name']}: [bold green]Passed")
                #     else:
                #         console.print(f"Test {i['case']} {i['name']}: [bold red]Failed")
                #         console.print(f"Message: {msg}")
                #         return
                # else:
                #     console.print(f"Test {i['case']} {i['name']}: [bold red]Failed")
                #     console.print(f"Message: {msg}")
                #     return
                    


    def test_create_db(self):
        # create db
        # check if created
        payload = {
            "id": self.id,
            "dbName": self.dbName,
            "dbUser": self.dbUser,
            "dbPassword": self.dbPassword,
            
            # redis vals.
            "dbPrefix": self.dbName,
            "username": self.dbUser,
            "password": self.dbPassword,
        }
        response = requests.post(self.ENDPOINT, json=payload)

        return response.json()["success"], response.json()["message"]
    
    def test_update_dbname(self):
        self.dbName = self.dbName + "_u"
        payload = {
            "newDBName": self.dbName,
        }
        response = requests.patch(self.ENDPOINT + "/" + self.id, json=payload)
        # print(response.json())
        return response.json()["success"], response.json()["message"]

    def test_update_dbuser(self):
        self.dbUser = self.dbUser + "_u"
        payload = {
            "newDBUser": self.dbUser,
        }
        response = requests.patch(self.ENDPOINT + "/" + self.id, json=payload)
        # print(response.json())
        return response.json()["success"], response.json()["message"]

    def test_update_dbpass(self):
        self.dbPassword = self.dbPassword + "_u"
        payload = {
            "newUserPassword": self.dbPassword,
        }
        if self.isRedis:
            payload = {
                "username": self.dbUser,
                "password": self.dbPassword
            }
        
        response = requests.patch(self.ENDPOINT + "/" + self.id, json=payload)
        # print(response.json())
        return response.json()["success"], response.json()["message"]
    
    def test_update_db_user_pass(self):
        # self.dbName = self.dbName + "_u"
        self.dbUser = self.dbUser + "_u"
        self.dbPassword = self.dbPassword + "_u"
        payload = {
            "newDBUser": self.dbUser,
            "newUserPassword": self.dbPassword,
        }
        response = requests.patch(self.ENDPOINT + "/" + self.id, json=payload)
        # print(response.json())
        return response.json()["success"], response.json()["message"]


    def test_update_dball(self):
        self.dbName = self.dbName + "_u"
        self.dbUser = self.dbUser + "_u"
        self.dbPassword = self.dbPassword + "_u"
        payload = {
            "newDBName": self.dbName,
            "newDBUser": self.dbUser,
            "newUserPassword": self.dbPassword,
        }
        response = requests.patch(self.ENDPOINT + "/" + self.id, json=payload)
        # print(response.json())
        return response.json()["success"], response.json()["message"]
    

    def test_delete_db(self):
        response = requests.delete(self.ENDPOINT + "/" + self.id, headers={
            "Content-Type": "application/json"
        })
        
        return response.json()["success"], response.json()["message"]


    def validate_from_server(self):
        # get latest data from server by id
        response = requests.get(self.ENDPOINT + "/" + self.id, headers={
            "Content-Type": "application/json"
        })
        
        # print(response.json())
        # check with currently stored data

        # success = False
        # msg = "request error"

        # print(response.json())
        

        success = response.json()["success"]
        msg = response.json()["message"]


        if not success:
            return success, msg
        
        try:
            if self.isRedis:
                assert response.json()["payload"]["dbPrefix"] == self.dbName, "dbPrefix does not match"
                assert response.json()["payload"]["username"] == self.dbUser, "username does not match"
                assert response.json()["payload"]["password"] == self.dbPassword, "password does not match"
                return True, ""
            
            assert response.json()["payload"]["dbName"] == self.dbName, "dbName does not match"
            assert response.json()["payload"]["dbUser"] == self.dbUser, "dbUser does not match"
            assert response.json()["payload"]["dbPassword"] == self.dbPassword, "dbPassword does not match"
            return True, ""
        except AssertionError as e:
            # print(str(e))
            # print("Failed to validate from server")
            return False, str(e)    




        