package fresh.crafts.engine.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.repositories.ProjectDeploymentRepository;

@Service
public class ProjectDeploymentService {

    @Autowired
    private ProjectDeploymentRepository projectDeploymentRepository;

    public ProjectDeployment getProjectDeploymentById(String id) {
        if (id == null)
            return null;

        Optional<ProjectDeployment> pd = projectDeploymentRepository.findById(id);
        if (pd.isPresent()) {
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

    public List<ProjectDeployment> getDeploymentsByProjectId(String id) {
        // find all deployments with project id
        return projectDeploymentRepository.findByProjectId(id);

    }

    public void deleteAllByProjectId(String id) {
        try {

            List<ProjectDeployment> pds = this.getDeploymentsByProjectId(id);
            for (ProjectDeployment pd : pds) {
                this.delete(pd);
            }
        } catch (Exception e) {
            System.out.println("Error deleting deployments for project id: " + id);
            // e.printStackTrace();
        }

    }

}
