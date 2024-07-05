package fresh.crafts.engine.v1.models;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;



@Data
@Document(collection="SystemConfig")
public class SystemConfig {
    


    @Id
    @Indexed(unique = true)
    private String id;


    private String setupKey;

    
    private Boolean systemSetupComplete;

    private String systemUserEmail;
    private String systemUserPasswordHash;

    private Object systemUserOauthProviders;
    

    @CreatedDate
    private Date created;
    @LastModifiedDate
    private Date updated;
     
}
