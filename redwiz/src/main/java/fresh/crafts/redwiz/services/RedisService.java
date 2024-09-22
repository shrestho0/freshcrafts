package fresh.crafts.redwiz.services;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.redwiz.utils.EnvProps;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.resps.AccessControlUser;

@Service
public class RedisService {

    @Autowired
    private EnvProps envProps;

    private JedisPool jedisPool;

    private JedisPool ensureConnectionPool() {
        jedisPool = new JedisPool(envProps.getRedisHost(), Integer.parseInt(envProps.getRedisPort()));
        jedisPool.getResource().auth(envProps.getRedisPassword());
        return jedisPool;
    }

    private JedisPool getConnectionPool() {
        if (jedisPool == null) {
            jedisPool = ensureConnectionPool();
        }
        return jedisPool;
    }

    /**
     * Create User and Permit to use db{id}:*
     */

    public boolean createUserAndAssignPrefix(String username, String password, String dbPrefix) {
        try (Jedis jedis = getConnectionPool().getResource()) {
            // jedis.auth(envProps.getRedisPassword());
            // String aclCommand = String.format(">%s ~%s:* +@all -@dangerous", password,
            // dbPrefix);
            // String aclCommand = String.format("resetpass >%s ~%s:* +@all -@dangerous",
            // password, dbPrefix);
            // String aclCommand = String.format("resetpass >%s ~%s:* +@all -@dangerous on",
            // password, dbPrefix);

            String aclCommand = String.format("on >%s ~%s:* +@all", password, dbPrefix);

            String result = jedis.aclSetUser(username, aclCommand.split(" "));
            System.out.println("ACL SETUSER result: " + result);
            return true;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * userExists
     * 
     * @apiNote Check if the user exists in Redis
     */
    public boolean userExists(String username) {
        try (Jedis jedis = getConnectionPool().getResource()) {
            jedis.auth(envProps.getRedisPassword());
            AccessControlUser user = jedis.aclGetUser(username);
            System.out.println("User " + username + " exists: " + (user != null));
            rewriteConfig();
            return user != null;
        } catch (Exception e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * deleteUser
     */

    public boolean deleteUser(String username) {
        try (Jedis jedis = getConnectionPool().getResource()) {
            jedis.auth(envProps.getRedisPassword());
            long result = jedis.aclDelUser(username);
            System.out.println("ACL DELUSER result: " + result);
            rewriteConfig();
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * rewriteConfig
     */

    public boolean rewriteConfig() {
        try (Jedis jedis = getConnectionPool().getResource()) {
            // jedis.auth(envProps.getRedisPassword());
            String result = jedis.configRewrite();
            System.out.println("CONFIG REWRITE result: " + result);
            return true;
        } catch (Exception e) {
            System.err.println("Error rewriting Redis config: " + e.getMessage());
            return false;
        }
    }

    /**
     * updateUserPassword
     */

    public boolean updateUserPassword(String username, String password) {
        try (Jedis jedis = getConnectionPool().getResource()) {
            // String result = jedis.aclSetUser(username, "resetpass", password);
            String result = jedis.aclSetUser(username, "resetpass", String.format(">%s", password));

            System.out.println("ACL SETUSER result: " + result);
            rewriteConfig();
            return true;
        } catch (Exception e) {
            System.err.println("Error updating user password: " + e.getMessage());
            return false;
        }
    }

    // /**
    // * Set Redis ACL for a user with a specific username and password, restricting
    // * them to a database.
    // *
    // * @param username The Redis username.
    // * @param password The Redis password for the user.
    // * @param db The Redis database number (e.g., 0, 1, 2...).
    // * @return Result of the ACL SETUSER command.
    // * @throws Exception If the Redis connection fails.
    // */
    // public String setAcl(String username, String password, int db) throws
    // Exception {

    // Jedis jedis = getConnectionPool().getResource();
    // jedis.auth(envProps.getRedisPassword()); // Authenticate to Redis server

    // // Set the user to be enabled ("on"), with a specific password and access to
    // a
    // // single database.
    // // String aclCommand = String.format("on >%s ~* &db{%d} +@all", password,
    // db);
    // String aclCommand = String.format(">%s ~db%d:* +@all -@dangerous", password,
    // db);
    // // Set the ACL user with the specified username, password, and access to the
    // // specified database
    // String result = jedis.aclSetUser(username, aclCommand.split(" "));
    // System.out.println("ACL SETUSER result: " + result);
    // rewriteConfig();
    // return result;
    // }

    // public void rewriteConfig() {
    // try (Jedis jedis = getConnectionPool().getResource()) {
    // jedis.auth(envProps.getRedisPassword()); // Use this if Redis requires
    // authentication

    // // Rewrite the Redis configuration file
    // String result = jedis.configRewrite();
    // System.out.println("CONFIG REWRITE result: " + result);
    // } catch (Exception e) {
    // System.err.println("Error rewriting configuration: " + e.getMessage());
    // // throw e; // Optionally rethrow the exception to propagate it
    // }
    // }

    // public long deleteUser(String username, int db) throws Exception {
    // // Obtain the Jedis pool connection

    // try (Jedis jedis = getConnectionPool().getResource()) { // Automatically
    // close the resource when done
    // jedis.auth(envProps.getRedisPassword()); // Authenticate to Redis server

    // // Delete the user ACL from Redis
    // long result = jedis.aclDelUser(username);
    // System.out.println("ACL DELUSER result: " + result);

    // // Free up the database after user deletion
    // freeDatabase(db);

    // return result; // Return the result of the ACL DELUSER command
    // } catch (Exception e) {
    // // Log or rethrow the exception if necessary
    // System.err.println("Error deleting user: " + e.getMessage());
    // throw e; // Optionally rethrow the exception to propagate it
    // }
    // }

    // public Integer getFreeDatabase() {
    // try (Jedis jedis = getConnectionPool().getResource()) {
    // jedis.auth(envProps.getRedisPassword()); // Use this if Redis requires
    // authentication

    // System.out.println("Checking for free databases...");
    // System.out.println("Redis server info fetched.");

    // // number of user = freeDatabaseId
    // // get number of user
    // List<String> userList = jedis.aclList();

    // int newDbId = userList.size() + 1;

    // System.out.println("Used databases: " + userList);

    // // check if ~db{idDb}:* exists in any user
    // for (String user : userList) {
    // AccessControlUser aclRules = jedis.aclGetUser(user);
    // if (aclRules == null) {
    // continue;
    // }
    // for (String rule : aclRules.getChannels()) {
    // if (rule.contains("~db" + newDbId + ":*")) {
    // newDbId++;
    // break;
    // }
    // }
    // }

    // // Fetch the total number of databases

    // return newDbId;

    // }

    // }

    // public void freeDatabase(int db) {

    // try (Jedis jedis = getConnectionPool().getResource()) {
    // jedis.auth(envProps.getRedisPassword()); // Use this if Redis requires
    // authentication

    // // Remove the database from the set of used databases
    // jedis.srem("db:used", String.valueOf(db));
    // System.out.println("Database " + db + " freed.");
    // } catch (Exception e) {
    // System.err.println("Error freeing database: " + e.getMessage());
    // throw e; // Optionally rethrow the exception to propagate it
    // }
    // }

    // public void freeAllDatabase() {
    // try (Jedis jedis = getConnectionPool().getResource()) {
    // jedis.auth(envProps.getRedisPassword()); // Use this if Redis requires
    // authentication

    // // Remove the database from the set of used databases
    // jedis.del("db:used");
    // System.out.println("All Databases freed.");
    // } catch (Exception e) {
    // System.err.println("Error freeing database: " + e.getMessage());
    // throw e; // Optionally rethrow the exception to propagate it
    // }
    // }

    // public boolean userExists(String username) {
    // // Check if the user exists in Redis

    // try (Jedis jedis = getConnectionPool().getResource()) {
    // jedis.auth(envProps.getRedisPassword()); // Use this if Redis requires
    // authentication

    // // Fetch the set of users
    // AccessControlUser user = jedis.aclGetUser(username);

    // System.out.println("User " + username + " exists: " + (user != null));

    // // Check if the user exists
    // return user != null;
    // }
    // }

    // public boolean userHasPermission(String username, int db) {
    // // Check if the user has permission to access the specified database
    // try (Jedis jedis = getConnectionPool().getResource()) {
    // jedis.auth(envProps.getRedisPassword()); // Authenticate to Redis server

    // // Fetch the ACL information for the user as a list of ACL rules
    // AccessControlUser aclRules = jedis.aclGetUser(username);

    // System.out.println("ACL rules for user " + username + ": " + aclRules);

    // if (aclRules == null) {
    // return false;
    // }

    // // Build the database access pattern for this specific database
    // String dbPattern = "&db{" + db + "}";

    // // Check if the ACL rules contain the specific database access pattern
    // for (String rule : aclRules.getChannels()) {
    // if (rule.contains(dbPattern)) {
    // return true; // User has access to the specified database
    // }
    // }

    // return false; // User does not have access to the specified database
    // } catch (Exception e) {
    // System.err.println("Error checking user permission: " + e.getMessage());
    // throw e; // Optionally rethrow the exception to propagate it
    // }
    // }

}
