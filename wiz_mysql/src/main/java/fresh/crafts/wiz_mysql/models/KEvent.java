package fresh.crafts.wiz_mysql.models;

import java.util.Map;

import com.google.gson.Gson;

import fresh.crafts.wiz_mysql.entities.KEventPayloadInterface;
import fresh.crafts.wiz_mysql.utils.UlidGenerator;
import fresh.crafts.wiz_mysql.utils.enums.KEventProducers;
import lombok.Data;

// OBSELETE
@Data
public class KEvent {

    String id;

    KEventProducers eventSource;
    KEventProducers eventDestination;
    KEventPayloadInterface payload;

    public KEvent() {
        this.id = UlidGenerator.generate();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static KEvent fromJson(String json, Class<?> payloadClass) {
        // System.err.println("\n\n [DEBUG]: KEvent/fromJson Starts ");
        KEvent tempE;
        try {
            Gson gson = new Gson();

            // map
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(json, Map.class);
            // System.out.println("\n Using Json map: \n\t" + map + "\n\n");
            if (map.getOrDefault("payload", null) == null) {
                System.err.println("[DEBUG]: Error: KEvent/fromJson: Payload is null");
                // return parseJson(json, KEvent.class);
                tempE = gson.fromJson(json, KEvent.class);
            } else {
                // payload ase
                String payloadJson = gson.toJson(map.get("payload"));
                KEventPayloadInterface payload = (KEventPayloadInterface) gson.fromJson(payloadJson, payloadClass);

                // delete payload from map
                map.remove("payload");
                // parse KEvent
                tempE = gson.fromJson(gson.toJson(map), KEvent.class);
                tempE.setPayload(payload);

            }

            return tempE;

        } catch (Exception e) {
            System.err.println("[DEBUG]: Error: KEvent/fromJson: " + e.getMessage());
            e.printStackTrace();
        }

        return null;

    }

}
