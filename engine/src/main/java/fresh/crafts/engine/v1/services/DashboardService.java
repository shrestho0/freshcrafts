package fresh.crafts.engine.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.models.ProjectDeployment;

import java.util.HashMap;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private DBMongoService dbMongoService;

    @Autowired
    private DBMysqlService dbMysqlService;

    @Autowired
    private DBPostgresService dbPostgresService;

    @Autowired
    private DBRedisService dbRedisService;

    @Autowired
    private ProjectDeploymentService projectDeploymentService;

    @Autowired
    private ProjectService projectService;

    public void getDeployments(CommonResponseDto res) {

        try {

            List<ProjectDeployment> deps = projectDeploymentService.getLast5DeploymentsWithProject();

            if (deps == null) {
                throw new Exception("getDeployments.deps is null");
            }

            if (deps != null && deps.size() > 0) {
                for (ProjectDeployment pd : deps) {
                    pd.setProject(projectService.getProjectById(pd.getProjectId()));
                }
            }

            res.setPayload(deps);
            res.setSuccess(true);
            res.setStatusCode(200);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(500);
            res.setMessage(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }

    }

    public void getActiveDBs(CommonResponseDto res) {
        try {

            HashMap<String, Object> payload = new HashMap<>();

            payload.put("activeMySqlCount", dbMysqlService.getActiveDBCount());
            payload.put("activePostgresCount", dbPostgresService.getActiveDBCount());
            payload.put("activeMongoCount", dbMongoService.getActiveDBCount());
            payload.put("activeRedisCount", dbRedisService.getActiveDBCount());

            res.setPayload(payload);
            res.setSuccess(true);
            res.setStatusCode(200);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(500);
            res.setMessage(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }

    }

}
