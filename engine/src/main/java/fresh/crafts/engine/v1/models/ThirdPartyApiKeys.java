package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "ThirdPartyApiKeys")
public class ThirdPartyApiKeys {

    @Id
    String id;
    Boolean isAzureChatApiSet;
    String azureChatApiEndpoint;
    String azureChatApiKey;

    // temporary solution
    // String xEndpoint
    // String xApiKey

}
