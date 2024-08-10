package fresh.crafts.wiz_mongo.services;

import java.util.HashMap;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import fresh.crafts.wiz_mongo.config.MessageProducer;
import fresh.crafts.wiz_mongo.entities.KEventFeedbackPayload;
import fresh.crafts.wiz_mongo.entities.KEventWizardMongoPayload;
import fresh.crafts.wiz_mongo.models.KEvent;

/**
 * WizardMongoMessageService
 * 
 */
@Service
public class WizardMongoMessageService {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private MongoService mongoService;

    public void createUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {

        System.err.println("[DEBUG]: WizardMongoMessageService");
        System.err.println("[DEBUG]: createUserAndDB; messageProducer-->" + messageProducer);
        System.err.println("[DEBUG]: createUserAndDB; mysqlService-->" + mongoService);
        System.err.println("[DEBUG]: \n kEvent::" + kEvent + "\n feedbackKEvent::" + feedbackKEvent + "\n");

        // Get the payloads from the events
        KEventWizardMongoPayload requestPayload = (KEventWizardMongoPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        // the actual thing
        // Create User And DB from payload
        try {
            System.out.println("trying to create db with" + requestPayload);

            // check if db already exists
            // create db
            // MongoDatabase db = mongoService.createDatabaseAndUser(
            // requestPayload.getDbName(),
            // requestPayload.getDbUser(),
            // requestPayload.getDbPassword());
            // System.out.println("DB Created maybe:" + db);

            MongoDatabase db = mongoService.createDatabase(requestPayload.getDbName());
            mongoService.confirmDatabaseCreation(db);

            // create user and assign to db
            mongoService.createAndAssignDBOwner(
                    requestPayload.getDbName(),
                    requestPayload.getDbUser(),
                    requestPayload.getDbPassword());

            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage("DBMongo with dbName: " + requestPayload.getDbName() + " created successfully");
            HashMap<String, Object> data = new HashMap<>();
            data.put("dbModelId", requestPayload.getDbModelId());
            feedbackPayload.setData(data);

            // System.out.println("DB Created successfully");

        } catch (Exception e) {
            feedbackPayload.setSuccess(false);

            String message = e.getMessage().toString(); // unhandled exception message

            // TODO: check possible exceptions and set the message accordingly

            feedbackPayload.setMessage(message);
            System.out.println("Error: DB Creation failed" +
                    feedbackPayload.getMessage());
        }

        messageProducer.sendEvent(feedbackKEvent);

    }

    public void deleteUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {
        System.out.println("---------- DELETE USER AND DB ----------");
        // Get the payloads from the events
        KEventWizardMongoPayload requestPayload = (KEventWizardMongoPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        // The actual thing
        try {

            mongoService.deleteDatabaseAndUser(
                    requestPayload.getDbName(),
                    requestPayload.getDbUser());

            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage("DBMysql with dbName: " +
                    requestPayload.getDbName() + " deleted successfully");
            HashMap<String, Object> data = new HashMap<>();
            data.put("dbModelId", requestPayload.getDbModelId());
            feedbackPayload.setData(data);

            System.out.println("DB Deleted successfully");

        } catch (MongoException e) {
            System.err.println("---------------deleteUserAndDB MongoException-------------: ");
            System.err.println(e.getMessage());
            // Handle Mongo Exceptions
            int err_code = e.getCode();

            String message = e.getMessage().toString(); // unhandled exception message
            feedbackPayload.setMessage(message);

            if (e.getCode() == 11) {
                message = "DBUser" + requestPayload.getDbUser() + "does not exist.";
            }

            System.err.println("---------------deleteUserAndDB MongoException Ends-------------: ");

            feedbackPayload.setSuccess(false);

        } catch (Exception e) {
            System.err.println("---------------deleteUserAndDB Exception-------------: ");
            System.err.println(e.getMessage());
            System.err.println("---------------deleteUserAndDB Exception Ends-------------: ");

            feedbackPayload.setSuccess(false);

            // check possible exceptions and set the message accordingly
            String message = e.getMessage().toString(); // unhandled exception message
            // TODO: Check message and set error

            feedbackPayload.setMessage(message);

        }

        System.out.println("---------- DELETE USER AND DB Ends ----------");

        messageProducer.sendEvent(feedbackKEvent);

    }

    public void updateUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {
        System.out.println("---------- UPDATE USER AND DB ----------");
        // Get the payloads from the events
        KEventWizardMongoPayload requestPayload = (KEventWizardMongoPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        // The actual thing
        HashMap<String, Object> data = new HashMap<>();
        try {
            String message = "DBMongo updated. Changed fields: ";

            // if (requestPayload.getNewDBName() != null && requestPayload.getDbUser() !=
            // null) {
            // //
            // mongoService.updateDatabaseName(requestPayload.getDbUser(),
            // requestPayload.getNewDBName());
            // Document doc = mongoService.updateDatabaseName(
            // requestPayload.getDbName(),
            // requestPayload.getNewDBUser());
            // // some more feedback
            // // to minimize risk of accidental partial change
            // message += "dbName, ";
            // data.put("updatedDBName", requestPayload.getNewDBName());

            // // mutate the thing, niche latest db name lagbe
            // requestPayload.setDbName(
            // requestPayload.getNewDBName());
            // }

            // update user
            // if (requestPayload.getNewUserPassword() != null ||
            // requestPayload.getNewDBUser() != null) {

            // // drop the current user no matter what
            // mongoService.dropUser(requestPayload.getDbName(),
            // requestPayload.getDbUser());

            // // new user name thakle valo nahle same user ee new user
            // String newDbUserName = requestPayload.getNewDBUser() != null ?
            // requestPayload.getNewDBUser()
            // : requestPayload.getDbUser();

            // String newDbUserPassword = requestPayload.getNewUserPassword() != null
            // ? requestPayload.getNewUserPassword()
            // : requestPayload.getDbPassword();

            // mongoService.createAndAssignDBOwner(requestPayload.getDbName(),
            // newDbUserName, newDbUserPassword);

            // if (requestPayload.getNewDBUser() != null) {
            // message += "dbUser, ";
            // data.put("updatedDBUser", newDbUserName);
            // }

            // if (requestPayload.getNewUserPassword() != null) {
            // message += "dbPassword, ";
            // data.put("updatedUserPassword", newDbUserPassword);
            // }

            // }

            // 1. Delete Old User
            mongoService.deleteDatabaseAndUser(
                    requestPayload.getDbName(),
                    requestPayload.getDbUser());

            // 2. set new user details

            String newUserName = requestPayload.getNewDBUser() != null ? requestPayload.getNewDBUser()
                    : requestPayload.getDbUser();
            System.out.println("\nnewUserName: " + newUserName + "\n");
            String newUserPassword = requestPayload.getNewUserPassword() != null ? requestPayload.getNewUserPassword()
                    : requestPayload.getDbPassword();
            System.out.println("\nnewUserPassword: " + newUserPassword + "\n");
            // 3. Create new user
            mongoService.createAndAssignDBOwner(
                    requestPayload.getDbName(),
                    newUserName,
                    newUserPassword);

            // 4. Update the request payload

            data.put("updatedDBUser", newUserName);
            data.put("updatedUserPassword", newUserPassword);

            message = message.substring(0, message.length() - 2);
            data.put("dbModelId", requestPayload.getDbModelId());

            feedbackPayload.setData(data);
            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage(message);

            System.out.println("DB Updated successfully");

        } catch (MongoException e) {
            System.err.println("--------------- updateUserAndDB MongoException-------------: ");
            System.err.println(e.getMessage());
            int err_code = e.getCode();

            String message = e.getMessage().toString(); // unhandled exception message

            // Handle Mongo Exceptions
            if (err_code == 11) {
                message = "DBUser" + requestPayload.getDbUser() + "does not exist.";
            }

            System.err.println("---------------updateUserAndDB MongoException Ends-------------: ");

            feedbackPayload.setMessage(message);
            feedbackPayload.setSuccess(false);

        } catch (Exception e) {
            System.err.println("---------------updateUserAndDB Exception-------------: ");
            System.err.println(e.getMessage());
            System.err.println("---------------updateUserAndDB Exception Ends-------------: ");

            feedbackPayload.setSuccess(false);

            // check possible exceptions and set the message accordingly
            String message = e.getMessage().toString(); // unhandled exception message
            // TODO: Check message and set error

            feedbackPayload.setMessage(message);

        }

        System.out.println("---------- UPDATE USER AND DB Ends ----------");

        messageProducer.sendEvent(feedbackKEvent);
    }

}
