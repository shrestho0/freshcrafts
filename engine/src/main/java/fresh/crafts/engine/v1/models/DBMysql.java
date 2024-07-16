package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.enums.DBMysqlCreationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "DBMysql")
public class DBMysql {

    @Id
    private String id;

    @Indexed(unique = true)
    private String dbName;

    @Indexed(unique = true)
    private String dbUser;

    private String dbPassword;

    private DBMysqlCreationType status;
    private String reasonFailed;

    // public DBMysql(String db_name, String db_user, String db_password) {
    // this.id = UlidGenerator.generate();
    // this.dbName = db_name;
    // this.dbUser = db_user;
    // this.dbPassword = db_password;
    // }

}
