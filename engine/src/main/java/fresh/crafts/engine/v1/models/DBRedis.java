package fresh.crafts.engine.v1.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.DBRedisStatus;
import lombok.Data;

@Data
@Document(collection = "DBRedis")
public class DBRedis {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String password;

    @Indexed(unique = true)
    private String dbPrefix;

    private DBRedisStatus status;
    private String reasonFailed;
    private String updateMessage;

    private Instant lastModifiedDate;

    public DBRedis() {
        this.id = UlidGenerator.generate();
    }

}
