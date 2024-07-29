import random
import requests
from time import sleep

ENDPOINT = "http://localhost:10000/api/v1/db-mysql"

# Update DB Name Not Implemented

# Make Another for MySQL
class TestApiWizPostgres:

    def __init__(self):
        self.endpoint = ENDPOINT    
        self.dbName = self.dbUser = self.dbPassword = "fc_test"
        self.id = self.dbName
        # + str(random.randint(1,69))

    def wait_for_update(self, second: int = 1):
        sleep(second)

    def create_database(self):
        """
        Create a new database
        Post Request to the endpoint
        With the following data as payload:

            ```json
        {
        "id": "some_string",
        "dbName": "some_string",
        "dbUser": "some_string",
        "dbPassword": "some_string",
        }

        ```

        """

        payload = {
            "id": self.id,
            "dbName": self.dbName,
            "dbUser": self.dbUser,
            "dbPassword": self.dbPassword,
        }

        # check if already exists
        # delete if so

        try:
            response = requests.post(self.endpoint, json=payload)
            print(response.json())

            res_dic = response.json()
            assert res_dic["success"] == True, "Failed to create database"

            # check if created
            self.wait_for_update()
            response = requests.get(self.endpoint + "/" + self.id, headers={
                "Content-Type": "application/json"
            })

            res_dic = response.json()
            assert res_dic["payload"]["dbName"]     == self.dbName, "create_db: dbName does not match"
            assert res_dic["payload"]["dbUser"]     == self.dbUser, "create_db: dbUser does not match"
            assert res_dic["payload"]["dbPassword"] == self.dbPassword, "create_db: dbPassword does not match"

            return True

        except Exception as e:
            print(str(e))
            return False
    
    def update_database(self):
        u = "u_"
        # check one by one
        try:
            # TODO: Uncomment this when update dbName is implemented
            # # update dbName
            # self.dbName = u + self.dbName
            # payload = {
            #     "newDBName": self.dbName,
            # }

            # response = requests.patch(self.endpoint + "/" + self.id, json=payload)
            # res_dic = response.json()
            # print(res_dic)
            # assert res_dic["success"] == True, "Failed to update database"


            # # check if updated
            # self.wait_for_update(2)
            # response = requests.get(self.endpoint + "/" + self.id, headers={
            #     "Content-Type": "application/json"
            # })
            # res_dic = response.json()
            # assert res_dic["payload"]["dbName"] == self.dbName, "dbName does not match"

            # update dbUser

            self.dbUser = u + self.dbUser

            payload = {
                "newDBUser": self.dbUser,
            }
            response = requests.patch(self.endpoint + "/" + self.id, json=payload)
            res_dic = response.json()
            print(res_dic)
            assert res_dic["success"] == True, "Failed to update database"
            
            # check if updated
            self.wait_for_update()
            response = requests.get(self.endpoint + "/" + self.id, headers={
                "Content-Type": "application/json"
            })

            res_dic = response.json()
            assert res_dic["payload"]["dbUser"] == self.dbUser, "dbUser does not match"

            # update dbPassword

            self.dbPassword = u + self.dbPassword

            payload = {"newUserPassword": self.dbPassword}

            response = requests.patch(self.endpoint + "/" + self.id, json=payload)
            res_dic = response.json()
            assert res_dic["success"] == True, "Failed to update database"


            # # check if updated
            self.wait_for_update(2)
            response = requests.get(self.endpoint + "/" + self.id, headers={
                "Content-Type": "application/json"
            })

            res_dic = response.json()
            assert res_dic["payload"]["dbPassword"] == self.dbPassword, "dbPassword does not match"



            # # check all together
            # self.dbName = u + self.dbName
            self.dbUser = u + self.dbUser
            self.dbPassword = u + self.dbPassword

            payload = {
                # "newDBName": self.dbName,
                "newDBUser": self.dbUser,
                "newUserPassword": self.dbPassword,
            }
            
            response = requests.patch(self.endpoint + "/" + self.id, json=payload)
            res_dic = response.json()

            assert res_dic["success"] == True, "Failed to update database"

            # check if updated
            self.wait_for_update(2)
            response = requests.get(self.endpoint + "/" + self.id, headers={"Content-Type": "application/json"})
            res_dic = response.json()

            assert res_dic["payload"]["dbName"] == self.dbName, "dbName does not match"
            assert res_dic["payload"]["dbUser"] == self.dbUser, "dbUser does not match"
            assert res_dic["payload"]["dbPassword"] == self.dbPassword, "dbPassword does not match"


            return True
        
    
        except AssertionError as e:
            print(str(e))
            return False




        

    def delete_database(self):
        try:
            response = requests.delete(self.endpoint + "/" + self.id, headers={
                "Content-Type": "application/json"
            })
            res_dic = response.json()
            print(res_dic)
            assert res_dic["success"] == True, "Failed to delete database"

            # check if deleted
            self.wait_for_update(3) # delete takes a bit time
            response = requests.get(self.endpoint + "/" + self.id, headers={
                "Content-Type": "application/json"
            })
            res_dic = response.json()
            assert res_dic["success"] == False, "Database still exists"

            return True
        except Exception as e:
            print(str(e))
            return False

    def run_test(self):
        boolToMsg = lambda x: "Passed" if x else "Failed"
        test_cases = {
            "create": self.create_database(),
            "update": self.update_database(),
            "delete": self.delete_database(),
        }

        print("-"*20)
        i=0
        for test_name, test_func in test_cases.items():
            print(f"Test {i+1} {test_name} {boolToMsg(test_func)}")
            i+=1
        print("-"*20)
        
        if(all(test_cases.values())):
            print("All test cases passed")
        

    

        
    

    

def run_test():
    new_test = TestApiWizPostgres()
    new_test.run_test()


if __name__ == "__main__":
    # TODO: Check if all necessary connections are ok, like db, kafka, etc

    # Func 1: Create DB with 
    run_test()
 