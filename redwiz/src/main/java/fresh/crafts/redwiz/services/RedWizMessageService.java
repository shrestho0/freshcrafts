package fresh.crafts.redwiz.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.redwiz.config.MessageProducer;
import fresh.crafts.redwiz.entities.KEvent;
import fresh.crafts.redwiz.entities.KEventFeedbackPayload;
import fresh.crafts.redwiz.entities.KEventRedWizPayload;
import fresh.crafts.redwiz.enums.KEventCommandsRedWiz;

/**
 * WizardMongoMessageService
 * 
 */
@Service
public class RedWizMessageService {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private RedisService redisService;

    public void serveStuff(KEvent kEvent, KEvent feedbackKEvent) {

        System.err.println("[DEBUG]: WizardMongoMessageService");

        // Get the payloads from the events
        KEventRedWizPayload requestPayload = (KEventRedWizPayload) kEvent.getPayload();
        KEventCommandsRedWiz command = requestPayload.getCommand();

        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();
        System.out.println("Serving: " + command);
        // the actual thing
        // Create User And DB from payload

        HashMap<KEventCommandsRedWiz, String> commandMap = new HashMap<>();
        commandMap.put(KEventCommandsRedWiz.ALLOW_PREFIX_TO_USER, "created");
        commandMap.put(KEventCommandsRedWiz.REVOKE_ACCESS_FROM_USER, "deleted");
        commandMap.put(KEventCommandsRedWiz.UPDATE_USER_PASSWORD, "updated");

        try {
            System.err.println("[DEBUG]: Assign DB to user Service");
            String username = requestPayload.getUsername();
            String password = requestPayload.getPassword();
            String dbPrefix = requestPayload.getDbPrefix();

            if (command == KEventCommandsRedWiz.ALLOW_PREFIX_TO_USER) {

                if (redisService.userExists(username)) {
                    System.err.println("[DEBUG]: User already exists");
                    throw new Exception("User already exists");
                }

                redisService.createUserAndAssignPrefix(username, password, dbPrefix);
            } else if (command == KEventCommandsRedWiz.REVOKE_ACCESS_FROM_USER) {

                if (!redisService.userExists(username)) {
                    System.err.println("[DEBUG]: User does not exist");
                    throw new Exception("User does not exist");
                }

                redisService.deleteUser(username);
            } else if (command == KEventCommandsRedWiz.UPDATE_USER_PASSWORD) {
                if (!redisService.userExists(username)) {
                    System.err.println("[DEBUG]: User does not exist");
                    throw new Exception("User does not exist");
                }

                redisService.updateUserPassword(username, password);

            } else {
                System.err.println("[DEBUG]: Invalid command: " + command);
                throw new Exception("Invalid command: " + command);
            }

            feedbackPayload.setSuccess(true);
            feedbackPayload.setMessage(
                    "DBRedis with dbName: " + requestPayload.getDbPrefix() + " " + commandMap.get(command)
                            + " successfully");
            HashMap<String, Object> data = new HashMap<>();
            data.put("dbModelId", requestPayload.getDbModelId());
            data.put("dbPrefix", requestPayload.getDbPrefix());
            data.put("username", requestPayload.getUsername());
            data.put("password", requestPayload.getPassword());
            feedbackPayload.setData(data);
        } catch (Exception e) {
            System.err.println("[DEBUG]: Exception: " + e.getMessage());
        }

        messageProducer.sendEvent(feedbackKEvent);

    }

}
