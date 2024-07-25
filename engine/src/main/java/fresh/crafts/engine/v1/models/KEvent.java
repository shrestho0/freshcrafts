package fresh.crafts.engine.v1.models;

import java.util.LinkedHashMap;

import org.apache.tomcat.util.json.JSONParser;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.entities.KEventPayloadInterface;
import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayload;
import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayloadFeedback;
import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;
import lombok.Data;

// OBSELETE
@Data
@Document(collation = "events")
public class KEvent {

    @Id
    String id;

    // WizardMySQLEventType eventType;
    KEventProducers eventSource;
    KEventProducers eventDestination;
    KEventPayloadInterface payload;

    public KEvent() {
        this.id = UlidGenerator.generate();
    }

    // Custom toJson, we'll replace with something else better when internet comes
    // :3

    public String toJson() {
        String json = "{";

        json += "\"id\":\"" + this.id + "\"" + ",";
        json += "\"eventSource\":\"" + this.eventSource + "\"" + ",";
        json += "\"eventDestination\":\"" + this.eventDestination + "\"" + ",";
        json += "\"payload\":" + this.payload.toJson() + "";

        json += "}";

        return json;
    }

    public static KEvent fromJson(String json) {
        KEvent kEvent = new KEvent();

        kEvent.setId(null);
        // System.out.println("\n============ KEvent Model / fromJson ============");

        try {
            JSONParser jsonParser = new JSONParser(json);
            LinkedHashMap<String, Object> objMap = jsonParser.object();

            // System.out.println("Before parsing, hashmap:" + objMap.toString() +
            // "eventDestination" + objMap.get("eventDestination"));
            // Common

            if (objMap.get("id") != null) {
                kEvent.setId((String) objMap.get("id"));
            }
            if (objMap.get("eventSource") != null) {
                kEvent.setEventSource(KEventProducers.valueOf((String) objMap.get("eventSource")));
            }

            if (objMap.get("eventDestination") != null) {
                kEvent.setEventDestination(KEventProducers.valueOf((String) objMap.get("eventDestination")));
            }

            if (objMap.get("payload") != null) {

                // TODO: Check payload type to be LinkedHashMap, throw for other types

                Object payloadObj = objMap.get("payload");

                LinkedHashMap<String, Object> objPayloadMap = (LinkedHashMap<String, Object>) payloadObj;

                // System.out.println("payload deserializing, payload obj class: " +
                // payloadObj.getClass()
                // + " objPayloadMap class: " + objPayloadMap.getClass() + " payload: " +
                // payloadObj
                // + " x objPayloadMap: " + objPayloadMap);

                assert (kEvent.getEventDestination() == null);
                // if (kEvent.getEventDestination() == null) {
                // throw new Exception("eventDestination must be set to provide/use payload");
                // }

                if (kEvent.getEventSource().equals(KEventProducers.ENGINE)) {
                    // System.out.println("Event with fromJson(),"
                    // + "Source: "
                    // + kEvent.getEventSource()
                    // + "; Dest: "
                    // + kEvent.getEventDestination()
                    // + "");

                    if (kEvent.getEventDestination().equals(KEventProducers.WIZARD_MYSQL)) {

                        // parse payload as WizardMySQLEventPayload

                        WizardMySQLKEventPayload payload = WizardMySQLKEventPayload
                                .fromHashMap(objPayloadMap);
                        kEvent.setPayload(payload);

                    } else if (kEvent.getEventDestination().equals(KEventProducers.WIZARD_POSTGRES)) {
                        // parse payload as WizardPostgresEventPayload
                        // throw new UnsupportedOperationException();
                    } else if (kEvent.getEventDestination().equals(KEventProducers.WIZARD_MONGO)) {
                        // parse payload as WizardMongoEventPayload

                    } else if (kEvent.getEventDestination().equals(KEventProducers.WIZARD_APP)) {
                        // parse payload as WizardAppEventPayload

                    } else if (kEvent.getEventDestination().equals(KEventProducers.WIZARD_NGINX)) {
                        // parse payload as WizardNGINXEventPayload
                    }

                } else if (kEvent.getEventSource().equals(KEventProducers.WIZARD_MYSQL)) {
                    // parse payload as WizardMySQLKEventPayloadFeedback
                    WizardMySQLKEventPayloadFeedback payload = WizardMySQLKEventPayloadFeedback
                            .fromHashMap(objPayloadMap);

                    kEvent.setPayload(payload);

                } else if (kEvent.getEventSource().equals(KEventProducers.WIZARD_POSTGRES)) {
                    // parse payload as WizardPostgresEventFeedback
                } else if (kEvent.getEventSource().equals(KEventProducers.WIZARD_MONGO)) {
                    // parse payload as WizardMongoEventFeedback
                } else if (kEvent.getEventSource().equals(KEventProducers.WIZARD_APP)) {
                    // parse payload as WizardAppEventFeedback
                } else if (kEvent.getEventSource().equals(KEventProducers.WIZARD_NGINX)) {
                    // parse payload as WizardNGINXEventFeedback
                }

                // parse payload from appropiate class that implements KEventPayloadInterface
            }

            // Check payload type will depend on eventSource
            // Map eventSource and payload types.

            // System.err.println("[DEBUG]: parsed kEvent: " + kEvent + "\n");
        } catch (Exception e) {
            System.out.println("=========  Failed to parse json from KEvent.fromJson() ========");
            e.getStackTrace();
            System.out.println(e.getMessage());
            System.out.println("====== ./ ======");
            return null;
        }

        // System.out.println("============ KEvent Model / fromJson ============\n");

        return kEvent;

    }

}
