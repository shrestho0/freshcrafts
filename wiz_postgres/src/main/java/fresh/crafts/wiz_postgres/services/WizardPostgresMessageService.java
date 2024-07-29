package fresh.crafts.wiz_postgres.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.wiz_postgres.config.MessageProducer;
import fresh.crafts.wiz_postgres.entities.KEventFeedbackPayload;
import fresh.crafts.wiz_postgres.entities.KEventWizardPostgresPayload;
import fresh.crafts.wiz_postgres.models.KEvent;

@Service
public class WizardPostgresMessageService {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private PostgreSQLService postgreSQLService;

    public void createUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {

        System.err.println("---------- service createUserAndDB  ----------");
        System.err.println("[DEBUG] createUserAndDB");
        // System.err.println("[DEBUG]: createUserAndDB; messageProducer-->" +
        // messageProducer);
        // System.err.println("[DEBUG]: createUserAndDB; postgreSQLService-->" +
        // postgreSQLService);
        // System.err.println("[DEBUG]: \n kEvent::" + kEvent + "\n feedbackKEvent::" +
        // feedbackKEvent + "\n");

        // Get the payloads from the events
        KEventWizardPostgresPayload requestPayload = (KEventWizardPostgresPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        // Create User And DB from payload
        try {
            // createDatabase(kEvent.getPayload());
            System.out.println("trying to create postgres db with" + requestPayload);

            // create db
            postgreSQLService.createDatabase(requestPayload.getDbName());
            // create user
            postgreSQLService.createUser(requestPayload.getDbUser(),
                    requestPayload.getDbPassword());

            // FIXME: Check if grantPrivileges is required with alterDatabaseOwner
            // grant privileges
            postgreSQLService.grantUserPrivileges(requestPayload.getDbName(),
                    requestPayload.getDbUser());

            // alter db owner, required to avoid permission issues
            postgreSQLService.alterDatabaseOwner(requestPayload.getDbName(), requestPayload.getDbUser());

            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage("DBMysql with dbName: " +
                    requestPayload.getDbName() + " created successfully");
            HashMap<String, Object> data = new HashMap<>();

            data.put("dbModelId", requestPayload.getDbModelId());
            feedbackPayload.setData(data);

            System.out.println("DB Created successfully");

        } catch (Exception e) {

            System.err.println("\n[DEBUG]: Error: WizardPostgresMessageService: createUserAndDB: " + e.getMessage());
            feedbackPayload.setSuccess(false);
            // TODO: check possible exceptions and set the message accordingly
            String message = e.getMessage().toString(); // unhandled exception message

            // if (message.contains("database exists")) {
            // message = "Database already exists";
            // } else if (message.contains("USER failed")) {
            // message = "User already exists";
            // }

            feedbackPayload.setMessage(message);
            System.out.println("Error: DB Creation failed" +
                    feedbackPayload.getMessage());
        }

        messageProducer.sendEvent(feedbackKEvent);
        System.err.println("---------- service createUserAndDB Ends ----------");
    }

    public void deleteUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {

        System.err.println("---------- service deleteUserAndDB ----------");

        // System.err.println("[DEBUG]: WizardPostgresMessageService");
        // System.err.println("[DEBUG]: deleteUserAndDB; messageProducer-->" +
        // messageProducer);
        // System.err.println("[DEBUG]: deleteUserAndDB; postgreSQLService-->" +
        // postgreSQLService);
        // System.err.println("[DEBUG]: \n kEvent::" + kEvent + "\n feedbackKEvent::" +
        // feedbackKEvent + "\n");

        // Get the payloads from the events
        KEventWizardPostgresPayload requestPayload = (KEventWizardPostgresPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        // The actual thing
        try {
            postgreSQLService.dropDatabase(requestPayload.getDbName());
            postgreSQLService.dropUser(requestPayload.getDbUser());
            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage("DBMysql with dbName: " +
                    requestPayload.getDbName() + " deleted successfully");
            HashMap<String, Object> data = new HashMap<>();
            data.put("dbModelId", requestPayload.getDbModelId());
            feedbackPayload.setData(data);

            System.out.println("DB Deleted successfully");

        } catch (Exception e) {
            //
            System.err.println("[DEBUG]: Error: service: deleteUserAndDB " + e.getMessage());
            feedbackPayload.setSuccess(false);

            // check possible exceptions and set the message accordingly
            String message = e.getMessage().toString(); // unhandled exception message
            // TODO: do the checking here

            feedbackPayload.setMessage(message);
            System.out.println("Error: DB Deletion failed" +
                    feedbackPayload.getMessage());

        }

        messageProducer.sendEvent(feedbackKEvent);

        System.err.println("---------- service deleteUserAndDB Ends ----------");
    }

    public void updateUserAndDB(KEvent kEvent, KEvent feedbackKEvent) {
        // NOTE: Partial update is possible

        System.err.println("---------- service updateUserAndDB ----------");

        // Get the payloads from the events
        KEventWizardPostgresPayload requestPayload = (KEventWizardPostgresPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

        System.out.println("requestPayload: " + requestPayload);

        /* The actual thing */

        HashMap<String, Object> data = new HashMap<>();
        try {

            String message = "DBPostgres updated. Changed fields: ";

            // update dbName if newDBName is present
            if (requestPayload.getNewDBName() != null) {
                System.out.println("[[[[[[[ trying to update db with" + requestPayload.getNewDBName());

                postgreSQLService.updateDatabaseName(requestPayload.getDbName(), requestPayload.getNewDBName());
                message += "dbName, ";
                data.put("updatedDBName", requestPayload.getNewDBName());
            }

            // update dbUser if newDBUser is present
            if (requestPayload.getNewDBUser() != null) {
                System.out.println("[[[[[[[ trying to update db with" + requestPayload.getNewDBUser());
                postgreSQLService.updateUserName(requestPayload.getDbUser(),
                        requestPayload.getNewDBUser());
                message += "dbUser, ";
                data.put("updatedDBUser", requestPayload.getNewDBUser());
            }

            // update dbPassword if newUserPassword is present
            if (requestPayload.getNewUserPassword() != null) {
                System.out.println("[[[[[[[ trying to update db with" +
                        requestPayload.getNewUserPassword());
                // update password
                postgreSQLService.updateUserPassword(
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
            System.err.println("[DEBUG]: Error: WizardPostgresMessageService: updateUserAndDB: " + e.getMessage());
            feedbackPayload.setSuccess(false);
            // check possible exceptions and set the message accordingly
            String message = e.getMessage().toString(); // unhandled exception message

            // temporary
            feedbackPayload.setMessage(message);
        }

        feedbackPayload.setData(data);
        messageProducer.sendEvent(feedbackKEvent);
        System.out.println("time to send event, " + feedbackKEvent);

        System.err.println("---------- service updateUserAndDB Ends ----------");

    }

}
