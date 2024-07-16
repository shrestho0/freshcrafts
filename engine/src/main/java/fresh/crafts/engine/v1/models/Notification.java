package fresh.crafts.engine.v1.models;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import lombok.Data;

@Data
@Document(collation = "notifications")
public class Notification {

    @Id
    private String id;

    private String message;

    // if any, optional
    private Object data;

    @CreatedDate
    private Date timestamp;

    public Notification(String message, Object data) {
        this.id = UlidGenerator.generate();
        this.message = message;
        this.data = data;
    }

    public Notification() {
        this.id = UlidGenerator.generate();
    }

}
