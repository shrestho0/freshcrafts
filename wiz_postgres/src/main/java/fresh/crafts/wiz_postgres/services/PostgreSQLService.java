package fresh.crafts.wiz_postgres.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/*
 * PostgreSQLService
 */
@Service
public class PostgreSQLService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void flushPrivileges() {
    }

    public void createDatabase(String dbName) {
        // sanitize dbName
        // if dbName is not valid, throw exception('that, ei somossha')

        String sql = "CREATE DATABASE " + dbName;
        jdbcTemplate.execute(sql);
    }

    public void createUser(String username, String password) {
        // sanitize username and password
        // if username or password is not valid, throw exception('that, ei somossha')

        // with login option
        String sql = "CREATE USER " + username + " WITH LOGIN PASSWORD '" + password + "'";
        jdbcTemplate.execute(sql);

    }

    public void grantUserPrivileges(String dbName, String username) {

        String sql = "GRANT ALL PRIVILEGES ON DATABASE " + dbName + " TO " + username;
        jdbcTemplate.execute(sql);

    }

    public void alterDatabaseOwner(String dbName, String username) {
        // ALTER DATABASE testdb OWNER TO testuser
        String sql = "ALTER DATABASE " + dbName + " OWNER TO " + username;
        jdbcTemplate.execute(sql);
    }

    // Check User Exists or not
    public void checkUserExists(String username) {
    }

    public void dropDatabase(String dbName) {
        String sql = "DROP DATABASE " + dbName;
        jdbcTemplate.execute(sql);
    }

    public void dropUser(String dbUser) {
        // user must not have any database
        String sql = "DROP USER " + dbUser;
        jdbcTemplate.execute(sql);
    }

    public void updateDatabaseName(String oldDbName, String newDbName) {
        String sql = "ALTER DATABASE " + oldDbName + " RENAME TO " + newDbName;
        jdbcTemplate.execute(sql);

    }

    public void updateUserName(String oldUserName, String newUserName) {
        String sql = "ALTER USER " + oldUserName + " RENAME TO " + newUserName;
        jdbcTemplate.execute(sql);

    }

    public void updateUserPassword(String username, String newPassword) {
        // sanitize username and password
        // ALTER USER username WITH PASSWORD 'new_password';
        String sql = "ALTER USER " + username + " WITH PASSWORD '" + newPassword + "'";

        jdbcTemplate.execute(sql);
    }

}
