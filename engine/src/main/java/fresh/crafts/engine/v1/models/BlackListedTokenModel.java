package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Document(collection="BlackListedTokens")
public class BlackListedTokenModel {

    @Id
    private String id;

    private String token;

    public BlackListedTokenModel(){
        this.id = UlidGenerator.generate();
    }

    public BlackListedTokenModel(String token){
        this.id = UlidGenerator.generate();
        this.token = token;
    }



    
}
