package fresh.crafts.engine.v1.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.DBMysqlStatus;
import lombok.Data;

@Data
@Document(collection = "DBMysql")
public class DBMysql {

    @Id
    private String id;

    @Indexed(unique = true)
    private String dbName;

    @Indexed(unique = true)
    private String dbUser;

    private String dbPassword;

    private DBMysqlStatus status;
    private String reasonFailed;
    private String updateMessage;

    @LastModifiedDate
    private Instant lastModifiedDate;

    public DBMysql() {
        this.id = UlidGenerator.generate();
    }

    public DBMysql(String db_name, String db_user, String db_password) {
        this.id = UlidGenerator.generate();
        this.dbName = db_name;
        this.dbUser = db_user;
        this.dbPassword = db_password;
    }

}
