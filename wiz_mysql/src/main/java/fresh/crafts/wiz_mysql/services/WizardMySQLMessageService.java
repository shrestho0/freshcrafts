package fresh.crafts.wiz_mysql.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.wiz_mysql.config.MessageProducer;
import fresh.crafts.wiz_mysql.entities.KEventFeedbackPayload;
import fresh.crafts.wiz_mysql.entities.KEventWizardMySQLPayload;
import fresh.crafts.wiz_mysql.models.KEvent;

/**
 * WizardMySQLMessageService
 * 
 * @description whatever
 * 
 * @apiNote currently checking exceptions by checking the message string, we'll
 *          do it by using error code later
 */
@Service
public class WizardMySQLMessageService {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private MysqlService mysqlService;

    public void createUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {

        System.err.println("[DEBUG]: WizardMySQLMessageService");
        System.err.println("[DEBUG]: createUserAndDB; messageProducer-->" + messageProducer);
        System.err.println("[DEBUG]: createUserAndDB; mysqlService-->" + mysqlService);
        System.err.println("[DEBUG]: \n kEvent::" + kEvent + "\n feedbackKEvent::" + feedbackKEvent + "\n");

        // Get the payloads from the events
        KEventWizardMySQLPayload requestPayload = (KEventWizardMySQLPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        // Create User And DB from payload
        try {
            // createDatabase(kEvent.getPayload());
            System.out.println("trying to create db with" + requestPayload);

            // check if db already exists
            // create db
            mysqlService.createDatabase(requestPayload.getDbName());
            // check if user already exists
            // create user
            mysqlService.createUser(requestPayload.getDbUser(), requestPayload.getDbPassword());

            mysqlService.grantUserPrivileges(requestPayload.getDbName(), requestPayload.getDbUser());

            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage("DBMysql with dbName: " + requestPayload.getDbName() + " created successfully");
            HashMap<String, Object> data = new HashMap<>();

            data.put("dbModelId", requestPayload.getDbModelId());
            feedbackPayload.setData(data);

            System.out.println("DB Created successfully");

        } catch (Exception e) {
            System.err.println("[DEBUG]: Error: WizardMySQLMessageService: createUserAndDB: " + e.getMessage());
            feedbackPayload.setSuccess(false);
            // check possible exceptions and set the message accordingly
            String message = e.getMessage().toString(); // unhandled exception message

            if (message.contains("database exists")) {
                message = "Database already exists";
            } else if (message.contains("USER failed")) {
                message = "User already exists";
            }

            feedbackPayload.setMessage(message);
            System.out.println("Error: DB Creation failed" + feedbackPayload.getMessage());
        }

        messageProducer.sendEvent(feedbackKEvent);

    }

    public void deleteUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {

        System.err.println("[DEBUG]: WizardMySQLMessageService");
        System.err.println("[DEBUG]: deleteUserAndDB; messageProducer-->" + messageProducer);
        System.err.println("[DEBUG]: deleteUserAndDB; mysqlService-->" + mysqlService);
        System.err.println("[DEBUG]: \n kEvent::" + kEvent + "\n feedbackKEvent::" + feedbackKEvent + "\n");

        // Get the payloads from the events
        KEventWizardMySQLPayload requestPayload = (KEventWizardMySQLPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        // The actual thing
        try {
            mysqlService.deleteDatabase(requestPayload.getDbName());
            mysqlService.deleteUser(requestPayload.getDbUser());
            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage("DBMysql with dbName: " + requestPayload.getDbName() + " deleted successfully");
            HashMap<String, Object> data = new HashMap<>();
            data.put("dbModelId", requestPayload.getDbModelId());
            feedbackPayload.setData(data);

            System.out.println("DB Deleted successfully");

        } catch (Exception e) {
            //
            System.err.println("[DEBUG]: Error: WizardMySQLMessageService: deleteUserAndDB: " + e.getMessage());
            feedbackPayload.setSuccess(false);

            // check possible exceptions and set the message accordingly
            String message = e.getMessage().toString(); // unhandled exception message
            // TODO: do the checking here
            // DB doesn't exist
            // "ERROR 1008 (HY000): Can't drop database 'test__69'; database doesn't exist"
            // User doesn't exist
            // ERROR 1396 (HY000): Operation DROP USER failed for 'test__14'@'%'

            if (message.contains("database doesn't exist")) {
                message = "Database doesn't exist";
            } else if (message.contains("Operation DROP USER failed")) {
                message = "User doesn't exist";
            }

            feedbackPayload.setMessage(message);
            System.out.println("Error: DB Deletion failed" + feedbackPayload.getMessage());

        }

        messageProducer.sendEvent(feedbackKEvent);

    }

    public void updateUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {
        // NOTE: Partial update is possible

        System.err.println("[DEBUG]: WizardMySQLMessageService");
        System.err.println("[DEBUG]: updateUserAndDB; messageProducer-->" + messageProducer);
        System.err.println("[DEBUG]: updateUserAndDB; mysqlService-->" + mysqlService);
        // System.err.println("[DEBUG]: \n kEvent::" + kEvent + "\n feedbackKEvent::" +
        // feedbackKEvent + "\n");

        // Get the payloads from the events
        KEventWizardMySQLPayload requestPayload = (KEventWizardMySQLPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        System.out.println("requestPayload: " + requestPayload);

        HashMap<String, Object> data = new HashMap<>();

        // The actual thing
        try {

            String message = "DBMysql updated. Changed fields: ";

            // update dbName if newDBName is present
            if (requestPayload.getNewDBName() != null) {
                throw new Exception("Not implemented yet. Please try others");
                // System.out.println("[[[[[[[ trying to update db with" +
                // requestPayload.getNewDBName());
                // mysqlService.updateDatabaseName(requestPayload.getDbName(),
                // requestPayload.getNewDBName());
                // message += "dbName, ";
                // data.put("updatedDBName", requestPayload.getNewDBName());
            }

            // update dbUser if newDBUser is present
            if (requestPayload.getNewDBUser() != null) {
                System.out.println("[[[[[[[ trying to update db with" + requestPayload.getNewDBUser());
                mysqlService.updateUserName(requestPayload.getDbUser(),
                        requestPayload.getNewDBUser());
                message += "dbUser, ";
                data.put("updatedDBUser", requestPayload.getNewDBUser());
            }

            // update dbPassword if newUserPassword is present
            if (requestPayload.getNewUserPassword() != null) {
                System.out.println("[[[[[[[ trying to update db with" + requestPayload.getNewUserPassword());
                // update password
                mysqlService.updateUserPassword(
                        requestPayload.getNewDBUser() != null ? requestPayload.getNewDBUser()
                                : requestPayload.getDbUser(),
                        requestPayload.getNewUserPassword());
                message += "dbPassword, ";
                data.put("updatedUserPassword", requestPayload.getNewUserPassword());
            }

            message = message.substring(0, message.length() - 2);
            data.put("dbModelId", requestPayload.getDbModelId());

            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage(message);

            System.out.println("DB Updated successfully");

        } catch (Exception e) {
            System.err.println("[DEBUG]: Error: WizardMySQLMessageService: updateUserAndDB: " + e.getMessage());
            feedbackPayload.setSuccess(false);
            // check possible exceptions and set the message accordingly
            String message = e.getMessage().toString(); // unhandled exception message

            // temporary
            feedbackPayload.setMessage(message);
        }

        feedbackPayload.setData(data);
        messageProducer.sendEvent(feedbackKEvent);
        System.out.println("time to send event, " + feedbackKEvent);

    }

}
