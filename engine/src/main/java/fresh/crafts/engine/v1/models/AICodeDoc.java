package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// import fresh.crafts.engine.v1.entities.AIChatMessage;
import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.AICodeDocStatus;
import lombok.Data;
import java.util.List;

@Data
@Document(collection = "AICodeDoc")
public class AICodeDoc {

    @Id
    String id;

    String prompt;
    String projectId;
    String content;
    AICodeDocStatus status;
    String message;

    public AICodeDoc() {
        id = UlidGenerator.generate();
    }

}
