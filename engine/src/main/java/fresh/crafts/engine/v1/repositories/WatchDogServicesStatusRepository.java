package fresh.crafts.engine.v1.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import fresh.crafts.engine.v1.models.WatchDogServicesStatus;
import java.util.List;

public interface WatchDogServicesStatusRepository extends MongoRepository<WatchDogServicesStatus, String> {

    public List<WatchDogServicesStatus> findAll();

}