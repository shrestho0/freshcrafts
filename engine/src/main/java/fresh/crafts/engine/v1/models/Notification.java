package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.gson.Gson;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.NotificationType;
import lombok.Data;

/**
 * Notification [Model]
 */
@Data
@Document(collection = "Notifications")
public class Notification {

    @Id
    private String id;

    private String message;

    private String actionHints;
    private Boolean markedAsRead;
    private NotificationType type;

    public Notification() {
        this.id = UlidGenerator.generate();
        this.markedAsRead = false;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

}