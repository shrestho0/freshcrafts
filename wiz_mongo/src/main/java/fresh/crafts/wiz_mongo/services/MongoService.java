package fresh.crafts.wiz_mongo.services;

import java.util.Arrays;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/*
 * MongoService
 * @apinote REFACTOR REQUIRED
 * Each functionality should 
 * @description whatever
 * 
 */
@Service
public class MongoService {

    @Autowired
    private MongoClient mongoClient;

    public Document updateDatabaseName(String oldDbName, String newDBName) throws Exception {
        throw new Exception("Not implemented, requires command line stuff for this");
    }
    // public MongoDatabase createDatabaseAndUser(String dbName, String dbUser,
    // String dbPassword) {

    // // for putting the access stuff
    // MongoDatabase adminDB = mongoClient.getDatabase("admin");

    // // getDatabase also creates database
    // MongoDatabase db = mongoClient.getDatabase(dbName);

    // String role = "readWrite";

    // Document createUserCommand = new Document("createUser", dbUser)
    // .append("pwd", dbPassword)
    // .append("roles", Arrays.asList(new Document("role", role).append("db",
    // dbName)));

    // adminDB.runCommand(createUserCommand);
    // return db;
    // }

    public MongoDatabase createDatabase(String dbName) {
        // creates database
        MongoDatabase db = mongoClient.getDatabase(dbName);

        return db;
    }

    public void dropUser(String dbName, String dbUser) {

        MongoDatabase db = mongoClient.getDatabase(dbName);

        Document dropUserCommand = new Document("dropUser", dbUser);

        db.runCommand(dropUserCommand);
    }

    public void createAndAssignDBOwner(String dbName, String dbUser, String dbPassword) {
        // MongoDatabase adminDB = mongoClient.getDatabase("admin");
        MongoDatabase db = mongoClient.getDatabase(dbName);

        String role = "readWrite";
        Document createUserCommand = new Document("createUser", dbUser)
                .append("pwd", dbPassword)
                .append("roles", Arrays.asList(new Document("role", role).append("db",
                        dbName)));

        db.runCommand(createUserCommand);
    }

    public void deleteDatabaseAndUser(String dbName, String dbUser) {

        // // for putting the access stuff
        // MongoDatabase adminDB = mongoClient.getDatabase("admin");

        // getDatabase also creates database
        MongoDatabase db = mongoClient.getDatabase(dbName);
        // dropping the user
        Document dropUserCommand = new Document("dropUser", dbUser);

        // Drop the user, maybe unnecessary
        db.runCommand(dropUserCommand);

        // Drop the database
        db.drop();

        // return adminDB.runCommand(dropUserCommand);

    }

    public void confirmDatabaseCreation(MongoDatabase db) {
        MongoCollection<Document> collection = db.getCollection("sampleCollection");
        // confirming it will show on mongo shell
        Document sampleDocument = new Document("name", "sample")
                .append("value", "This is a sample document");
        collection.insertOne(sampleDocument);
        // delete inserted collection
        collection.deleteOne(sampleDocument);
    }

}
