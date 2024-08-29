package fresh.crafts.engine.v1.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.entities.KEventFeedbackPayload;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.utils.CraftUtils;
import fresh.crafts.engine.v1.utils.enums.ProjectDeploymentStatus;

@Service
public class EngineDepwizMessageService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private KEventService kEventService;

    public void serve(KEvent kEvent) {
        System.err.println("[DEBUG]: Engine Depwiz Message Service");
        CraftUtils.jsonLikePrint(kEvent);

        System.err.println("[DEBUG]: Engine Depwiz Message Service: Payload");
        KEventFeedbackPayload payload = (KEventFeedbackPayload) kEvent.getPayload();
        CraftUtils.jsonLikePrint(payload);

        // save the event
        kEventService.createOrUpdate(kEvent);

        try {
            // Validations
            HashMap<String, Object> requiredValues = new HashMap<>();
            requiredValues.put("project_id", payload.getData().get("project_id"));
            requiredValues.put("deployment_id", payload.getData().get("deployment_id"));
            requiredValues.put("isPartial", payload.getIsPartial());
            CraftUtils.throwIfRequiredValuesAreNull(requiredValues);

            Project p = projectService.getProjectById(payload.getData().get("project_id").toString());
            ProjectDeployment pd = projectService
                    .getProjectDeploymentById2(payload.getData().get("deployment_id").toString());

            CraftUtils.throwIfNull(p, "Error: Project not found");
            CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

            boolean needToSavePD = false;
            if (payload.getIsPartial()) {
                System.err.println("[DEBUG]: Partial Event");
                if (payload.getData().get("partial_feedback") != null) {
                    pd.setPartialDeploymentMsg(payload.getData().get("partial_feedback").toString());
                    needToSavePD = true;
                }
                if (payload.getData().get("dep_status") != null) {
                    // check if that fails
                    pd.setStatus(ProjectDeploymentStatus.valueOf(payload.getData().get("dep_status").toString()));
                    needToSavePD = true;
                }
                // save partial data

                if (needToSavePD) {
                    ProjectDeployment pd_up = projectService.updateProjectDeployment(pd);
                    System.out.println("Updated pd");
                    CraftUtils.jsonLikePrint(pd_up);
                }

            } else {
                System.err.println("[DEBUG]: Full Event");
            }

        } catch (Exception e) {
            System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
                    + "\n=================== << Exceptions ===================");
        }
    }

}
