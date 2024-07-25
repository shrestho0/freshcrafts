package fresh.crafts.engine.v1.models;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TestTestModel {

    @Id
    String id;
    String testTitle;

    @CreatedDate
    private Date created;

}
