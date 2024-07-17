package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.WizardMySQLEventType;
import lombok.Data;

@Data
@Document(collation = "events")
public class WizardMySQLEvent {

    @Id
    String id;

    WizardMySQLEventType eventType;
    // String source

    public WizardMySQLEvent() {
        this.id = UlidGenerator.generate();
    }

}
