package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// import fresh.crafts.engine.v1.entities.AIChatMessage;
import fresh.crafts.engine.v1.utils.UlidGenerator;
import lombok.Data;
import java.util.List;

@Data
@Document(collection = "AIChatHistory")
public class AIChatHistory {

    @Id
    String id;

    String chatName;

    Object messages;

    public AIChatHistory() {
        id = UlidGenerator.generate();
    }

}
