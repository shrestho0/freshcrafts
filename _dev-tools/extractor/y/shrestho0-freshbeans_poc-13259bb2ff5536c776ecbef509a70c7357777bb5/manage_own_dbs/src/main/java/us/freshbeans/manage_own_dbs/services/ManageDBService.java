package us.freshbeans.manage_own_dbs.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ManageDBService {
   
     
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createDatabase(String dbName) {
            String sql = "CREATE DATABASE " + dbName;
            jdbcTemplate.execute(sql);
            
    }

    public void createUser(String username, String password) {
        String sql = "CREATE USER '" + username + "'@'%' IDENTIFIED BY '" + password + "'";
        jdbcTemplate.execute(sql);
    }

    public void grantUserPrivileges(String dbName, String username){
        
        String sql = "GRANT ALL PRIVILEGES ON " +  dbName + ".*" + " TO '" + username + "'@'%' WITH GRANT OPTION";
        jdbcTemplate.execute(sql);
    }

    public List<String> getUsersDB(String username){
        String sql = "show databases where Database LIKE '" + username + "%'";
        
        return jdbcTemplate.queryForList(sql, String.class);
    }

}
