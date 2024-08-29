package fresh.crafts.engine.v1.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.AIChatHistory;

@Repository
public interface AIChatHistoryRepository extends MongoRepository<AIChatHistory, String> {

}