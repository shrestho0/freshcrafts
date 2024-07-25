package fresh.crafts.engine.v1.entities;

import java.util.LinkedHashMap;

public interface KEventPayloadInterface {
    public String toJson();

    public static KEventPayloadInterface fromHashMap(LinkedHashMap<String, Object> objPayloadMap) {
        throw new UnsupportedOperationException("KEventPayloadInterface fromJson method not implemented");
    }

}
