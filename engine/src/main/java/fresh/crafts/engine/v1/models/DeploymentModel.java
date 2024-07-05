package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@Document(collation="deployments")
@AllArgsConstructor
public class DeploymentModel {
    
    @Id
    public String id;

    public DeploymentModel(){
        this.id = UlidGenerator.generate();
    }
    
}
