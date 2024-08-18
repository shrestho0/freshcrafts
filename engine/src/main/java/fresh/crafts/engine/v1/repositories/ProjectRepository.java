package fresh.crafts.engine.v1.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.Project;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByUniqueName(String id);

}