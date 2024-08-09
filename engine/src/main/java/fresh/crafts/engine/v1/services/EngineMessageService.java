package fresh.crafts.engine.v1.services;

import java.time.Instant;

import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.entities.KEventFeedbackPayload;
import fresh.crafts.engine.v1.entities.KEventWizardMySQLPayload;
import fresh.crafts.engine.v1.entities.KEventWizardPostgresPayload;
import fresh.crafts.engine.v1.models.DBMysql;
import fresh.crafts.engine.v1.models.DBPostgres;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.models.Notification;
import fresh.crafts.engine.v1.utils.EnvProps;
import fresh.crafts.engine.v1.utils.enums.DBMysqlStatus;
import fresh.crafts.engine.v1.utils.enums.DBPostgresStatus;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsWizardMySQL;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsWizardPostgres;
import fresh.crafts.engine.v1.utils.enums.NotificationType;

/**
 * EngineMessageService
 *
 * @implNote This class is responsible for handling feedback from the wizard
 *           services
 * @apiNote REFACTOR_REQUIRED, too messy
 */
@Service
public class EngineMessageService {

    @Autowired
    private EnvProps envProps;

    @Autowired
    private DBMysqlService dbMysqlService;

    @Autowired
    private DBPostgresService dbPostgresService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private KEventService kEventService;

    /**
     * serveForWizardMySQL
     *
     * @param feedbackKEvent
     * @implNote This method is responsible for handling feedback from the
     *           wizard_mysql service
     */
    public void serveForWizardMySQL(KEvent feedbackKEvent) {
        System.err.println("---------- serveForWizardMySQL ----------");
        // System.err.println("[DEBUG]: [EngineMessageService] [[ serveForWizardMySQL ]]
        // Entered");
        // System.err.println("[DEBUG]: Feedback kEvent: " + feedbackKEvent.toJson());

        try {

            // // Check feedback payload
            KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

            // System.err.println("[TEMP_DEBUG]: feedbackPayload:" + feedbackPayload);

            if (feedbackPayload == null) {
                throw new Exception("Feedback payload is null");
            }

            // Create notification
            Notification notification = new Notification();

            try {
                // // requested KEvent
                KEvent requestedKEvent = kEventService.getById(feedbackPayload.getRequestEventId());

                if (requestedKEvent == null) {
                    throw new Exception(" Requested KEvent not found");
                }

                // System.err.println("[TEMP_DEBUG]: requestedKEvent: " + requestedKEvent);

                // requested payload
                KEventWizardMySQLPayload requestedPayload = (KEventWizardMySQLPayload) requestedKEvent.getPayload();

                if (requestedPayload == null) {
                    throw new Exception(" Requested KEvent payload not found");
                }

                // System.err.println("[TEMP_DEBUG]: requestedPayload: " + requestedPayload);

                // requested dbMysql
                DBMysql requestedDbMysql = dbMysqlService.getById(requestedPayload.getDbModelId());

                if (requestedDbMysql == null) {
                    throw new Exception(" DBMysql not found");
                }

                KEventCommandsWizardMySQL requestedCommand = requestedPayload.getCommand();

                // TODO: handle stuff based on more stuff
                if (requestedCommand.equals(KEventCommandsWizardMySQL.CREATE_USER_AND_DB)) {
                    System.err.println("[DEBUG]: CREATE_USER_AND_DB feedback from wizard");

                    //
                    notification.setActionHints("GOTO_DBMYSQL_" + requestedDbMysql.getId());

                    // System.err.println("[TEMP_DEBUG]: requestedDbMysql: " + requestedDbMysql);

                    if (feedbackPayload.getSuccess()) {
                        // db creation success
                        requestedDbMysql.setStatus(DBMysqlStatus.OK);

                        System.out.println("[DEBUG]: requestedDbMysql " + requestedDbMysql);

                        notification.setType(NotificationType.SUCCESS);
                        notification.setMessage(feedbackPayload.getMessage());

                    } else {
                        // db stuff
                        requestedDbMysql.setStatus(DBMysqlStatus.FAILED);
                        requestedDbMysql.setReasonFailed(feedbackPayload.getMessage());
                        // not success,
                        notification.setType(NotificationType.ERROR);
                        notification.setMessage("Failed to create Mysql database: " + requestedDbMysql.getDbName());
                    }

                    requestedDbMysql.setLastModifiedDate(Instant.now());
                    DBMysql updatedDB = dbMysqlService.update(requestedDbMysql);
                    System.out.println("[DEBUG]: updatedDB " + updatedDB);

                } else if (requestedCommand.equals(KEventCommandsWizardMySQL.UPDATE_USER_AND_DB)) {
                    // do stuff
                    System.out.println("---------- UPDATE_USER_AND_DB Feedback ----------");
                    System.out.println(feedbackPayload);

                    // remove update message
                    requestedDbMysql.setUpdateMessage(null);

                    // notify user about the action
                    notification.setActionHints("GOTO_DBMYSQL_" + requestedDbMysql.getId());

                    if (feedbackPayload.getSuccess()) {
                        /* Operation succeed */

                        if (feedbackPayload.getData() == null) {
                            throw new Exception("Feedback data is null");
                        }

                        // Checking if update succeeded
                        // errors can occur after each step,
                        // so, update the updated to the db
                        if (feedbackPayload.getData().get("updatedDBName") != null) {
                            requestedDbMysql.setDbName(feedbackPayload.getData().get("updatedDBName").toString());
                        }
                        if (feedbackPayload.getData().get("updatedDBUser") != null) {
                            requestedDbMysql.setDbUser(feedbackPayload.getData().get("updatedDBUser").toString());
                        }
                        if (feedbackPayload.getData().get("updatedUserPassword") != null) {
                            requestedDbMysql
                                    .setDbPassword(feedbackPayload.getData().get("updatedUserPassword").toString());
                        }

                        // Update db status
                        requestedDbMysql.setStatus(DBMysqlStatus.OK);

                        // notification
                        notification.setType(NotificationType.SUCCESS);
                        notification.setMessage(feedbackPayload.getMessage());

                    } else {
                        /* Operation failed */

                        // Update db status
                        requestedDbMysql.setStatus(DBMysqlStatus.UPDATE_FAILED);
                        requestedDbMysql.setReasonFailed(feedbackPayload.getMessage());

                        // set notification
                        notification.setType(NotificationType.ERROR);
                        notification.setMessage(feedbackPayload.getMessage());

                    }

                    // now
                    requestedDbMysql.setLastModifiedDate(Instant.now());
                    DBMysql updatedDB = dbMysqlService.update(requestedDbMysql);
                    System.out.println("[DEBUG]: updatedDB " + updatedDB);

                    System.out.println("---------- UPDATE_USER_AND_DB Feedback Ends ----------");
                } else if (requestedCommand.equals(KEventCommandsWizardMySQL.DELETE_USER_AND_DB)) {
                    System.err.println("[DEBUG]: DELETE_USER_AND_DB feedback from wizard");
                    // delete db
                    // to make frontend invalidate data
                    // SSE TO an invalid endpoint to make frontend invalidate data
                    notification.setActionHints("GOTO_DBMYSQL_" + requestedDbMysql.getId());

                    System.err.println("[DEBUG]: requestedDbMysql: " + requestedDbMysql);

                    if (feedbackPayload.getSuccess()) {
                        // delete db model
                        dbMysqlService.delete(requestedDbMysql);
                        System.out.println("[DEBUG]: requestedDbMysql deleted");
                        notification.setType(NotificationType.SUCCESS);
                        notification.setMessage(feedbackPayload.getMessage());

                    } else {
                        // not success,
                        notification.setType(NotificationType.ERROR);
                        notification.setMessage("Failed to delete Mysql database: " + requestedDbMysql.getDbName());
                    }

                }

            } catch (Exception e) {

                System.err.println("[DEBUG]: serveForWizardMySQL (Inner) Error: " + e.getMessage());

                notification.setType(NotificationType.ERROR);
                notification.setMessage(e.getMessage());
                // e.printStackTrace();
            }

            System.out.println("Feedback payload: " + feedbackPayload);

            String payloadString = notification.toJson();
            System.err.println("[DEBUG]: payloadString: " + payloadString);

            // Save feedback event to db
            kEventService.createOrUpdate(feedbackKEvent);

            // Save notification to db for future usage
            notificationService.createOrUpdate(notification);

            // Send Notification at the end
            requestNotificationSSE(payloadString);

        } catch (Exception e) {
            System.err.println("[DEBUG]: serveForWizardMySQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.err.println("---------- serveForWizardMySQL Ends ----------");
    }

    /**
     * serveForWizardPostgres
     *
     * @param kEvent
     * @implNote This method is responsible for handling feedback from the
     *           wizard_postgres service
     */
    public void serveForWizardPostgres(KEvent feedbackKEvent) {
        System.err.println("---------- serveForWizardPostgres ----------");
        try {

            // Check feedback payload
            KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

            // System.err.println("[TEMP_DEBUG]: feedbackPayload:" + feedbackPayload);

            if (feedbackPayload == null) {
                throw new Exception("Feedback payload is null");
            }

            // Create notification
            Notification notification = new Notification();

            try {
                // // requested KEvent
                KEvent requestedKEvent = kEventService.getById(feedbackPayload.getRequestEventId());

                if (requestedKEvent == null) {
                    throw new Exception(" Requested KEvent not found");
                }

                // System.err.println("[TEMP_DEBUG]: requestedKEvent: " + requestedKEvent);

                // requested payload
                KEventWizardPostgresPayload requestedPayload = (KEventWizardPostgresPayload) requestedKEvent
                        .getPayload();

                // TODO: create utility function
                // called throwExceptionIfNull(String name, Object obj) )
                if (requestedPayload == null) {
                    throw new Exception("Requested KEvent payload not found");
                }

                // System.err.println("[TEMP_DEBUG]: requestedPayload: " + requestedPayload);
                DBPostgres requestedDbPostgres = dbPostgresService.getById(requestedPayload.getDbModelId());

                if (requestedDbPostgres == null) {
                    throw new Exception(" DBPostgres not found");
                }

                KEventCommandsWizardPostgres requestedCommand = requestedPayload.getCommand();

                // TODO: handle stuff based on more stuff
                if (requestedCommand.equals(KEventCommandsWizardPostgres.CREATE_USER_AND_DB)) {
                    System.err.println("[DEBUG]: CREATE_USER_AND_DB feedback from wizard");

                    notification.setActionHints("GOTO_DBPOSTGRES_" + requestedDbPostgres.getId());

                    // System.err.println("[TEMP_DEBUG]: requestedDbPostgres: " +
                    // requestedDbPostgres);

                    if (feedbackPayload.getSuccess()) {
                        // db creation success
                        requestedDbPostgres.setStatus(DBPostgresStatus.OK);

                        // notification success
                        notification.setType(NotificationType.SUCCESS);
                        notification.setMessage(feedbackPayload.getMessage());

                    } else {
                        // db stuff
                        requestedDbPostgres.setStatus(DBPostgresStatus.FAILED);
                        requestedDbPostgres.setReasonFailed(feedbackPayload.getMessage());

                        // notification error
                        notification.setType(NotificationType.ERROR);
                        notification
                                .setMessage("Failed to create Postgres database: " + requestedDbPostgres.getDbName());
                    }

                    DBPostgres updatedDB = dbPostgresService.update(requestedDbPostgres);
                    System.out.println("[DEBUG]: updatedDB " + updatedDB);

                } else if (requestedCommand.equals(KEventCommandsWizardPostgres.UPDATE_USER_AND_DB)) {
                    // do stuff
                    System.out.println("---------- UPDATE_USER_AND_DB Feedback ----------");
                    // System.out.println(feedbackPayload);

                    // remove update message
                    requestedDbPostgres.setUpdateMessage(null);
                    // notify user about the action
                    notification.setActionHints("GOTO_DBPOSTGRES_" + requestedDbPostgres.getId());

                    if (feedbackPayload.getSuccess()) {
                        /* Operation succeed */

                        if (feedbackPayload.getData() == null) {
                            throw new Exception("Feedback data is null");
                        }

                        // Checking if update succeeded
                        // errors can occur after each step,
                        // so, update the updated to the db
                        if (feedbackPayload.getData().get("updatedDBName") != null) {
                            requestedDbPostgres.setDbName(feedbackPayload.getData().get("updatedDBName").toString());
                        }
                        if (feedbackPayload.getData().get("updatedDBUser") != null) {
                            requestedDbPostgres.setDbUser(feedbackPayload.getData().get("updatedDBUser").toString());
                        }
                        if (feedbackPayload.getData().get("updatedUserPassword") != null) {
                            requestedDbPostgres
                                    .setDbPassword(feedbackPayload.getData().get("updatedUserPassword").toString());
                        }

                        // Update db status
                        requestedDbPostgres.setStatus(DBPostgresStatus.OK);

                        // notification
                        notification.setType(NotificationType.SUCCESS);
                        notification.setMessage(feedbackPayload.getMessage());

                    } else {
                        /* Operation failed */

                        // Update db status
                        requestedDbPostgres.setStatus(DBPostgresStatus.UPDATE_FAILED);
                        requestedDbPostgres.setReasonFailed(feedbackPayload.getMessage());

                        // set notification
                        notification.setType(NotificationType.ERROR);
                        notification.setMessage(feedbackPayload.getMessage());

                    }

                    requestedDbPostgres.setLastModifiedDate(Instant.now());
                    DBPostgres updatedDB = dbPostgresService.update(requestedDbPostgres);
                    System.out.println("[DEBUG]: updatedDB " + updatedDB);

                    System.out.println("---------- UPDATE_USER_AND_DB Feedback Ends ----------");
                } else if (requestedCommand.equals(KEventCommandsWizardPostgres.DELETE_USER_AND_DB)) {
                    System.err.println("[DEBUG]: DELETE_USER_AND_DB feedback from wizard");
                    // delete db
                    // to make frontend invalidate data
                    // SSE TO an invalid endpoint to make frontend invalidate data
                    notification.setActionHints("GOTO_DBPOSTGRES_" + requestedDbPostgres.getId());

                    System.err.println("[DEBUG]: requestedDbPostgres: " + requestedDbPostgres);

                    if (feedbackPayload.getSuccess()) {
                        // delete db model
                        dbPostgresService.delete(requestedDbPostgres);
                        System.out.println("[DEBUG]: requestedDbPostgres deleted");
                        notification.setType(NotificationType.SUCCESS);
                        notification.setMessage(feedbackPayload.getMessage());

                    } else {
                        // not success,
                        notification.setType(NotificationType.ERROR);
                        notification
                                .setMessage("Failed to delete Postgres database: " + requestedDbPostgres.getDbName());
                    }

                }

            } catch (Exception e) {

                System.err.println("[DEBUG]: serveForWizardPostgres (Inner) Error: " + e.getMessage());

                notification.setType(NotificationType.ERROR);
                notification.setMessage(e.getMessage());
                // e.printStackTrace();
            }

            System.out.println("Feedback payload: " + feedbackPayload);

            String payloadString = notification.toJson();
            // System.err.println("[DEBUG]: payloadString: " + payloadString);

            // Save feedback event to db
            kEventService.createOrUpdate(feedbackKEvent);

            // Save notification to db for future usage
            notificationService.createOrUpdate(notification);

            // Send Notification at the end
            requestNotificationSSE(payloadString);

        } catch (Exception e) {
            System.err.println("[DEBUG]: serveForWizardPostgres Error (Outer): " + e.getMessage());
            e.printStackTrace();
        }

        System.err.println("---------- serveForWizardPostgres Ends ----------");

    }

    /**
     * requestNotificationSSE
     *
     * @param json
     * @implNote This method is responsible for sending notification to the frontend
     *           via SSE
     */
    private void requestNotificationSSE(String json) {
        System.err.println("[DEBUG]: EngineMessageService requestNotificationSSE");
        String url = envProps.getCockpitLocalUrl() + "/sse/notification";

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String resultContent = null;
            // HttpGet httpGet = new HttpGet(url);
            HttpPatch httpPatch = new HttpPatch(url);
            httpPatch.addHeader("Authorization", "Bearer " + envProps.getCockpitAuthorzationToken());
            httpPatch.addHeader("Content-Type", "application/json");

            httpPatch.setEntity(new StringEntity(json.toString()));

            try (CloseableHttpResponse response = httpclient.execute(httpPatch)) {
                HttpEntity entity = response.getEntity();
                // Get response information
                resultContent = EntityUtils.toString(entity);
                System.out.println("----------------------");
                System.out.println(resultContent);
                System.out.println("----------------------");
            }

        } catch (Exception e) {
            System.err.println("[DEBUG]: Error: " + e.getMessage());
        }

    }

}
