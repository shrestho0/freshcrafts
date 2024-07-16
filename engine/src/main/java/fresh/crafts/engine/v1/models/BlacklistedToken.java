package fresh.crafts.engine.v1.models;

import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "BlacklistedTokens")
public class BlacklistedToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    public BlacklistedToken(String token) {
        this.id = UlidGenerator.generate();
        this.token = token;
    }

}
