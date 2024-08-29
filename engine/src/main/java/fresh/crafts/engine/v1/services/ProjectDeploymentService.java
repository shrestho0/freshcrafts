package fresh.crafts.engine.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.repositories.ProjectDeploymentRepository;

@Service
public class ProjectDeploymentService {

    @Autowired
    private ProjectDeploymentRepository projectDeploymentRepository;

    public ProjectDeployment getProjectDeploymentById(String id, Boolean includeProject) {
        if (id == null)
            return null;

        Optional<ProjectDeployment> pd = projectDeploymentRepository.findById(id);
        if (pd.isPresent()) {
            if (!includeProject) {
                pd.get().setProject(null);
            }
            return pd.get();
        }
        return null;
    }

    public ProjectDeployment save(ProjectDeployment d) {
        return projectDeploymentRepository.save(d);
    }

    public void delete(String currentDeploymentId) {
        projectDeploymentRepository.deleteById(currentDeploymentId);
    }

    public void delete(ProjectDeployment pd) {
        projectDeploymentRepository.delete(pd);
    }

}
