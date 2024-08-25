package us.freshbeans.manage_own_dbs.controllers;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import us.freshbeans.manage_own_dbs.services.ManageDBService;

 
@RestController
public class ManageDBController {

    @Autowired
    private ManageDBService manageDBService;

    ///// Root
    @GetMapping("/")
    public Object getTestData() {
        HashMap<String, Object> hello = new HashMap<>();

        hello.put("hello", "world");
        hello.put("ping", "pong");

        return hello;
    }

    @GetMapping("/dbs/")
    public Object getDBs(@RequestParam HashMap<String, Object> body){
        // get user's db
        HashMap<String, Object> retVal = new HashMap<>();
        System.out.print("REQUEST ASHCHE with: "+ body);

        Object username = body.get("username");
        if (username == null){
            retVal.put("success", false);
            retVal.put("message", "payload:username missing");
            return retVal;
        }

        try {
            List<String> dbsOfUser = manageDBService.getUsersDB((String) username);
            retVal.put("success", "true");
            retVal.put("databases", dbsOfUser);
            
        } catch (Exception e) {
            retVal.put("success", "false");
            retVal.put("err_msg", e.getMessage());
        }

        return retVal;
    }
 

    @PostMapping("/dbs")
    public Object createDB(@RequestBody HashMap<String, Object> body){
        HashMap<String, Object> dbs = new HashMap<>();
        

        String user_username = (String) body.get("username"); // temp, user's username
    

        System.out.println(body.get("dbName") + " " + body.get("dbUser") + " " + body.get("dbPass"));

        // generate dbname
        String new_dbName = user_username + "_" + body.get("dbName");
        String new_dbUser = user_username + "_" + body.get("dbUser");
        String new_dbPass = user_username + "_" + body.get("dbPass");

        // username_dbname will be actual dbname

        try {
            manageDBService.createDatabase(new_dbName);
            manageDBService.createUser(new_dbUser, new_dbPass);
            manageDBService.grantUserPrivileges(new_dbName, new_dbUser);
            dbs.put("success", true);
            dbs.put("message", "db created successfully");
        } catch (Exception e) {
            System.out.print(e);

            dbs.put("success", false);
            dbs.put("err_msg", e.getMessage());

            if (e instanceof UncategorizedSQLException){
                dbs.put("message", "database exists");
            }else{

                dbs.put("message", "failed to create db");
                dbs.put("err", e);
    }
    }

        
        // manageDBService.createDatabase(dbName);


        return dbs;
    }
}