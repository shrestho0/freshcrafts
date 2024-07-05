package fresh.crafts.engine.v1.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.SystemConfig;

@Repository
public interface SystemConfigRepository extends MongoRepository<SystemConfig, String> {
    SystemConfig findFirSystemConfigById(String id);
 }