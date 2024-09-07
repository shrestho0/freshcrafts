package fresh.crafts.engine.v1.models;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "WatchDogServicesStatus")
@Data
public class WatchDogServicesStatus {

    String ID;
    String timestamp;

    String primary_kafka;
    String primary_mongo;
    String secondary_mongo;
    String secondary_mysql;
    String secondary_postgres;

    String cockpit;
    String depwiz;
    String engine;
    String wizard_mongo;
    String wizard_mysql;
    String wizard_postgres;
}
