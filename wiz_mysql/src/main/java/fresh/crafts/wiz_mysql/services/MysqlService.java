package fresh.crafts.wiz_mysql.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * MysqlService
 * 
 * @apiNote This service handles MySQL Database operations using
 *          JdbcTemplate
 * @apiNote REFACTOR_REQUIRED, this is a bad design as this is a poc
 *
 */

@Service
public class MysqlService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void flushPrivileges() {
        String sql = "FLUSH PRIVILEGES";
        jdbcTemplate.execute(sql);
    }

    public void createDatabase(String dbName) {
        String sql = "CREATE DATABASE " + dbName;
        jdbcTemplate.execute(sql);
    }

    // check if db exists
    public Boolean checkDatabaseExists(String dbName) {
        String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + dbName + "'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("SCHEMA_NAME")).size() > 0;
    }

    public void createUser(String username, String password) {
        // do some validation here and before
        String sql = "CREATE USER '" + username + "'@'%' IDENTIFIED BY '" + password + "'";
        jdbcTemplate.execute(sql);
    }

    public void grantUserPrivileges(String dbName, String username) {

        String sql = "GRANT ALL PRIVILEGES ON " + dbName + ".*" + " TO '" + username + "'@'%' WITH GRANT OPTION";
        jdbcTemplate.execute(sql);
    }

    // Check User Exists or not
    public void checkUserExists(String username) {
        String sql = "SELECT User FROM mysql.user WHERE User = '" + username + "'";
        jdbcTemplate.execute(sql);
    }

    public void deleteDatabase(String dbName) {

        String sql = "DROP DATABASE " + dbName;
        jdbcTemplate.execute(sql);

    }

    public void deleteUser(String dbUser) {
        // delete mysql user
        String sql = "DROP USER '" + dbUser + "'@'%'";
        jdbcTemplate.execute(sql);
    }

    public void updateDatabaseName(String oldDbName, String newDbName) {
        // String sql = "ALTER DATABASE " + oldDbName + " RENAME TO " + newDbName;

        String sql = "RENAME DATABASE " + oldDbName + " TO " + newDbName;

        jdbcTemplate.execute(sql);
    }

    public void updateUserName(String oldUserName, String newUserName) {
        String sql = "RENAME USER '" + oldUserName + "'@'%' TO '" + newUserName + "'@'%'";
        jdbcTemplate.execute(sql);
    }

    public void updateUserPassword(String username, String newPassword) {
        // String sql = "SET PASSWORD FOR '" + username + "'@'%' = PASSWORD('" +
        // newPassword + "')";
        String sql = "ALTER USER '" + username + "'@'%' IDENTIFIED BY '" + newPassword + "'";
        jdbcTemplate.execute(sql);
    }

}
