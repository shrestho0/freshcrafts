package fresh.crafts.engine.v1.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.AICodeDoc;

@Repository
public interface AICodeDocRepository extends MongoRepository<AICodeDoc, String> {

    // @Query("{ 'projectId' : ?0 }")
    @Query(value = "{ 'projectId' : ?0 }", sort = "{ '_id': -1 }")
    List<AICodeDoc> findByProjectId(String projectId);

}