package fresh.crafts.engine.v1.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.DBPostgresStatus;
import lombok.Data;

@Data
@Document(collection = "DBPostgres")
public class DBPostgres {

    @Id
    private String id;

    @Indexed(unique = true)
    private String dbName;

    @Indexed(unique = true)
    private String dbUser;

    private String dbPassword;

    private DBPostgresStatus status;
    private String reasonFailed;
    private String updateMessage;

    @LastModifiedDate
    private Instant lastModifiedDate;

    public DBPostgres() {
        this.id = UlidGenerator.generate();
    }

    public DBPostgres(String db_name, String db_user, String db_password) {
        this.id = UlidGenerator.generate();
        this.dbName = db_name;
        this.dbUser = db_user;
        this.dbPassword = db_password;
    }

}
