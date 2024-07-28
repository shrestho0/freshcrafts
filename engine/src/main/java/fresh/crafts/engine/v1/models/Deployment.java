package fresh.crafts.engine.v1.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.gson.Gson;

import fresh.crafts.engine.v1.utils.UlidGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Document(collection = "Deployments")
@AllArgsConstructor
public class Deployment {

    @Id
    public String id;

    public Deployment() {
        this.id = UlidGenerator.generate();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

}
