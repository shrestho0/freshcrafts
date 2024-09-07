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

import fresh.crafts.engine.v1.entities.DepWizKEventPayload;
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

    @Autowired
    private EngineMessageService engineMessageService;

    public void serveDeployFeedback(KEvent kEvent, DepWizKEventPayload payload) {
        // System.err.println("[DEBUG]: Engine Depwiz Message Service");
        // CraftUtils.jsonLikePrint(kEvent);

        // System.err.println("[DEBUG]: Engine Depwiz Message Service: Payload");
        // KEventFeedbackPayload payload = (KEventFeedbackPayload) kEvent.getPayload();
        // CraftUtils.jsonLikePrint(payload);

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
                    // String message = pd.getPartialDeploymentMsg() + "\n" +
                    // payload.getData().get("partial_feedback");
                    p.getPartialMessageList().add(payload.getData().get("partial_feedback").toString());
                    needToSaveP = true;
                    // pd.setPartialDeploymentMsg(message);
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

                    // on successful deployment
                    p.setTotalVersions(p.getTotalVersions() + 1); // as succeed, total version should ++
                    p.setActiveVersion(p.getTotalVersions()); // latest one should be the active version
                    p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
                    p.setCurrentDeploymentId(null); // set the current deployment id to null
                    p.setStatus(ProjectStatus.ACTIVE);

                    p.setPortAssigned(Integer.decode((payload.getData().get("port_assigned")).toString()));

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
                    pd.setVersion(p.getTotalVersions());
                    // pd.setPartialDeploymentMsg(
                    // pd.getPartialDeploymentMsg() + "\n" +
                    // payload.getData().get("partial_feedback"));

                    p.getPartialMessageList().add(payload.getData().get("partial_feedback").toString());
                    needToSaveP = true;

                    notification.setType(NotificationType.SUCCESS);
                    notification.setMessage(payload.getData().get("partial_feedback").toString());
                } else {
                    // failure
                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                    p.setStatus(ProjectStatus.INACTIVE);
                    // pd.setPartialDeploymentMsg(
                    // pd.getPartialDeploymentMsg() + "\n" +
                    // payload.getData().get("partial_feedback"));

                    p.getPartialMessageList().add(payload.getData().get("partial_feedback").toString());

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

            // save notification if not partial
            if (!payload.getIsPartial()) {
                notification.setMessage(notification.getMessage() + " [REVALIDATE]");
                notificationService.createOrUpdate(notification);
            }

            requestProjectIDSSE(notification.toJson(), p.getId());

        } catch (Exception e) {
            System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
                    + "\n=================== << Exceptions ===================");
        }
    }

    public void serveDeleteFeedback(KEvent kEvent, DepWizKEventPayload payload) {
        try {
            HashMap<String, Object> requiredValues = new HashMap<>();
            requiredValues.put("project_id", payload.getData().get("project_id"));
            requiredValues.put("isPartial", payload.getIsPartial());
            requiredValues.put("partial_feedback", payload.getData().get("partial_feedback"));
            CraftUtils.throwIfRequiredValuesAreNull(requiredValues);

            Project p = projectService.getProjectById(payload.getData().get("project_id").toString());

            CraftUtils.throwIfNull(p, "Error: Project not found");

            // // notification
            Notification notification = new Notification();

            p.getPartialMessageList().add(payload.getData().get("partial_feedback").toString());

            if (payload.getIsPartial()) {
                // partial event
                notification.setType(NotificationType.INFO);
                projectService.updateProject(p);

            } else {

                // full event
                if (payload.getSuccess()) {
                    notification.setType(NotificationType.INFO);
                    // success
                    notification.setType(NotificationType.SUCCESS);
                    // notification.setMessage( ); // do it later
                    projectService.deleteProjectAndDeployments(p.getId());
                    notification.setMessage("[REVALIDATE] Project `" + p.getUniqueName() + "` Deleted Successfully");
                    notification.setActionHints("REDIRECT_PROJECTS_" + p.getId()); //
                    engineMessageService.requestNotificationSSE(notification.toJson());
                } else {
                    // failure
                    notification.setType(NotificationType.ERROR);
                    notification.setMessage("[FAILURE] Failed to delete project: " + p.getUniqueName());
                    notification.setActionHints("REDIRECT_PROJECTS_" + p.getId());
                    engineMessageService.requestNotificationSSE(notification.toJson());

                }

                notificationService.createOrUpdate(notification);
                // return;

            }

            requestProjectIDSSE(notification.toJson(), p.getId());

        } catch (Exception e) {
            System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
                    + "\n=================== << Exceptions ===================");

        }

    }

    public void serveReDeployFeedback(KEvent kEvent, DepWizKEventPayload payload) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'serveReDeployFeedback'");
    }

    /**
     * requestProjectIDSSE
     *
     * @param json
     * @implNote This method is responsible for sending notification to the frontend
     *           via SSE
     */
    private void requestProjectIDSSE(String json, String projectId) {
        System.err.println("[DEBUG]: EngineMessageService requestProjectIDSSE /sse/projects/");
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
