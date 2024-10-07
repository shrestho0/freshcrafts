package fresh.crafts.engine.v1.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import fresh.crafts.engine.v1.models.ProjectDeployment;

@Repository
public interface ProjectDeploymentRepository extends MongoRepository<ProjectDeployment, String> {

    // @Query("{ 'project.id' : ?0 }")
    List<ProjectDeployment> findByProjectId(String id);

    List<ProjectDeployment> findTop5ByOrderByIdDesc();

}