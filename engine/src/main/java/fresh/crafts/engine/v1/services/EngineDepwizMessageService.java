package fresh.crafts.engine.v1.services;

import java.util.HashMap;

import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.entities.KEventFeedbackPayload;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.models.Notification;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.utils.CraftUtils;
import fresh.crafts.engine.v1.utils.EnvProps;
import fresh.crafts.engine.v1.utils.enums.NotificationType;
import fresh.crafts.engine.v1.utils.enums.ProjectDeploymentStatus;
import fresh.crafts.engine.v1.utils.enums.ProjectStatus;

@Service
public class EngineDepwizMessageService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private KEventService kEventService;

    @Autowired
    private EnvProps envProps;

    @Autowired
    private NotificationService notificationService;

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

            // notification
            Notification notification = new Notification();

            boolean needToSavePD = false;
            boolean needToSaveP = false;
            if (payload.getIsPartial()) {
                System.err.println("[DEBUG]: Partial Event");
                if (payload.getData().get("partial_feedback") != null) {
                    String message = pd.getPartialDeploymentMsg() + "\n" + payload.getData().get("partial_feedback");
                    pd.setPartialDeploymentMsg(message);
                    notification.setMessage(payload.getData().get("partial_feedback").toString());

                    needToSavePD = true;
                }
                if (payload.getData().get("dep_status") != null) {
                    // check if that fails
                    pd.setStatus(ProjectDeploymentStatus.valueOf(payload.getData().get("dep_status").toString()));
                    needToSavePD = true;
                }
                // save partial data
                notification.setType(NotificationType.INFO);

            } else {
                needToSaveP = true;
                needToSavePD = true;

                if (payload.getSuccess()) {
                    // success

                    p.setTotalVersions(p.getTotalVersions());
                    p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
                    p.setStatus(ProjectStatus.ACTIVE);

                    p.setPortAssigned(Integer.decode((payload.getData().get("port_assigned")).toString()));

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
                    pd.setPartialDeploymentMsg(
                            pd.getPartialDeploymentMsg() + "\n" + payload.getData().get("partial_feedback"));
                    notification.setType(NotificationType.SUCCESS);
                    notification.setMessage(payload.getData().get("partial_feedback").toString());
                } else {
                    // failure
                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                    p.setStatus(ProjectStatus.INACTIVE);
                    pd.setPartialDeploymentMsg(
                            pd.getPartialDeploymentMsg() + "\n" + payload.getData().get("partial_feedback"));
                    pd.setErrorTraceback(payload.getData().get("error_traceback").toString());
                    notification.setType(NotificationType.ERROR);
                    notification.setMessage(payload.getData().get("partial_feedback").toString());

                }

                System.err.println("[DEBUG]: Full Event");
            }

            if (needToSavePD) {
                ProjectDeployment pd_up = projectService.updateProjectDeployment(pd);
                System.out.println("Updated pd");
                CraftUtils.jsonLikePrint(pd_up);
            }

            if (needToSaveP) {
                Project p_up = projectService.updateProject(p);
                System.out.println("Updated p");
                CraftUtils.jsonLikePrint(p_up);
            }

            // save notification
            notificationService.createOrUpdate(notification);

            requestNotificationSSE(notification.toJson(), p.getId());

        } catch (Exception e) {
            System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
                    + "\n=================== << Exceptions ===================");
        }
    }

    /**
     * requestNotificationSSE
     *
     * @param json
     * @implNote This method is responsible for sending notification to the frontend
     *           via SSE
     */
    private void requestNotificationSSE(String json, String projectId) {
        System.err.println("[DEBUG]: EngineMessageService requestNotificationSSE");
        String url = envProps.getCockpitLocalUrl() + "/sse/projects/" + projectId;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String resultContent = null;
            // HttpGet httpGet = new HttpGet(url);
            HttpPatch httpPatch = new HttpPatch(url);
            httpPatch.addHeader("Authorization", "Bearer " + envProps.getCockpitAuthorizationToken());
            httpPatch.addHeader("Content-Type", "application/json");

            httpPatch.setEntity(new StringEntity(json.toString()));

            try (CloseableHttpResponse response = httpclient.execute(httpPatch)) {
                HttpEntity entity = response.getEntity();
                // Get response information
                resultContent = EntityUtils.toString(entity);
                System.out.println("----------------------");
                System.out.println(resultContent);
                System.out.println("----------------------");
            }

        } catch (Exception e) {
            System.err.println("[DEBUG]: Error: " + e.getMessage());
        }

    }

}
