package fresh.crafts.engine.v1.services;

import java.util.HashMap;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
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

    public void serveFirstDepFeedback(KEvent kEvent, DepWizKEventPayload payload) {

        // save the event
        kEventService.createOrUpdate(kEvent);

        try {
            // Validations
            HashMap<String, Object> requiredValues = new HashMap<>();
            requiredValues.put("project_id", payload.getProject().getId());
            requiredValues.put("deployment_id", payload.getCurrentDeployment().getId());
            requiredValues.put("isPartial", payload.getIsPartial());

            CraftUtils.throwIfRequiredValuesAreNull(requiredValues);

            Project p = projectService.getProjectById(payload.getProject().getId());
            ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

            CraftUtils.throwIfNull(p, "Error: Project not found");
            CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

            // notification
            Notification notification = new Notification();

            System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK =================");
            CraftUtils.jsonLikePrint(payload);
            System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK ENDS =================");

            // first iteration as first successful deployment
            // every update is a new iteration
            pd.setIteration(pd.getIteration() + 1);

            if (payload.getIsPartial()) {
                System.err.println("[DEBUG]: Partial Event");

                p.getPartialMessageList().add(payload.getMessage());
                // notification.setMessage(payload.getMessage());
                notification.setType(NotificationType.INFO);

            } else {

                if (payload.getSuccess()) {
                    // success

                    // on successful deployment
                    p.setTotalVersions(p.getTotalVersions() + 1); // as succeed, total version should ++
                    p.setActiveVersion(p.getTotalVersions()); // latest one should be the active version
                    p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
                    p.setCurrentDeploymentId(null); // set the current deployment id to null
                    p.setStatus(ProjectStatus.ACTIVE);
                    p.setPortAssigned(payload.getProject().getPortAssigned());

                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
                    pd.setVersion(p.getTotalVersions());

                    notification.setType(NotificationType.SUCCESS);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    engineMessageService.requestNotificationSSE(notification.toJson());

                } else {
                    // failure
                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                    p.setStatus(ProjectStatus.INACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

                    notification.setType(NotificationType.ERROR);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    engineMessageService.requestNotificationSSE(notification.toJson());

                }

                System.err.println("[DEBUG]: Full Event");
            }

            // save stuff
            // ProjectDeployment pd_up = projectService.updateProjectDeployment(pd);
            // Project p_up = projectService.updateProject(p);

            projectService.updateProjectDeployment(pd);
            projectService.updateProject(p);

            // save notification if not partial
            if (!payload.getIsPartial()) {
                // notification.setMessage(notification.getMessage() + " [REVALIDATE]");
                // p.getPartialMessageList().add(" [REVALIDATE]");
                notificationService.createOrUpdate(notification);

            }

            notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
            requestProjectIDSSE(notification.toJson(), p.getId());
            requestProjectIDSSEDataDelete(p.getId());

        } catch (Exception e) {
            System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
                    + "\n=================== << Exceptions ===================");
        }
    }

    public void serveDeleteDepsFeedback(KEvent kEvent, DepWizKEventPayload payload) {
        try {
            HashMap<String, Object> requiredValues = new HashMap<>();
            requiredValues.put("project", payload.getProject());
            requiredValues.put("isPartial", payload.getIsPartial());
            requiredValues.put("partial_feedback", payload.getMessage());
            CraftUtils.throwIfRequiredValuesAreNull(requiredValues);

            // Project p =
            // projectService.getProjectById(payload.getData().get("project_id").toString());
            Project p = projectService.getProjectById(payload.getProject().getId());

            // CraftUtils.throwIfNull(p, "Error: Project not found");

            // // notification
            Notification notification = new Notification();

            // p.getPartialMessageList().add(payload.getData().get("partial_feedback").toString());
            p.getPartialMessageList().add(payload.getMessage());

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

    public void serveReDepFeedback(KEvent kEvent, DepWizKEventPayload payload) {
        System.err.println("[DEBUG]: EngineDepwizMessageService serveReDepFeedback");
        // save the event
        kEventService.createOrUpdate(kEvent);

        try {

            Project p = projectService.getProjectById(payload.getProject().getId());
            ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

            CraftUtils.throwIfNull(p, "Error: Project not found");
            CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

            // notification
            Notification notification = new Notification();

            System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK =================");
            CraftUtils.jsonLikePrint(payload);
            System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK ENDS =================");

            if (payload.getIsPartial()) {
                System.err.println("[DEBUG]: Partial Event");

                p.getPartialMessageList().add(payload.getMessage());
                // notification.setMessage(payload.getMessage());
                notification.setType(NotificationType.INFO);
                System.out.println("\nPARTIAL MESSAGE: " + payload.getMessage() + "\n");

            } else {
                pd.setIteration(pd.getIteration() + 1);

                if (payload.getSuccess()) {
                    // success

                    // on successful deployment
                    p.setTotalVersions(p.getTotalVersions()); // as succeed, total version should ++
                    p.setActiveVersion(p.getTotalVersions()); // latest one should be the active version
                    p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
                    p.setCurrentDeploymentId(null); // set the current deployment id to null
                    p.setStatus(ProjectStatus.ACTIVE);
                    p.setPortAssigned(payload.getProject().getPortAssigned());

                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED); // should be redep completed
                    pd.setVersion(p.getTotalVersions());

                    notification.setType(NotificationType.SUCCESS);
                    // notification.setMessage(payload.getMessage());

                    // all or nothing
                    // notification.setMessage(p.getPartialMessageList().toString());

                } else {
                    // failure
                    p.setStatus(ProjectStatus.INACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED); // should be redep failed
                    pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

                    notification.setType(NotificationType.ERROR);

                }

                System.err.println("[DEBUG]: Full Event");
            }

            // save stuff
            // ProjectDeployment pd_up = projectService.updateProjectDeployment(pd);
            // Project p_up = projectService.updateProject(p);
            projectService.updateProjectDeployment(pd);
            projectService.updateProject(p);

            // save notification if not partial
            if (!payload.getIsPartial()) {
                // notification.setMessage(notification.getMessage() + " [REVALIDATE]");
                // notification.setMessage(p.getPartialMessageList().toString());

                notificationService.createOrUpdate(notification);
            }

            notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
            requestProjectIDSSE(notification.toJson(), p.getId());
            requestProjectIDSSEDataDelete(p.getId());

        } catch (Exception e) {
            System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
                    + "\n=================== << Exceptions ===================");
        }
    }

    public void serveUpdateDepFeedback(KEvent kEvent, DepWizKEventPayload payload) {
        System.err.println("[DEBUG]: EngineDepwizMessageService serveUpdateDepFeedback");
        // save the event
        kEventService.createOrUpdate(kEvent);

        try {

            Project p = projectService.getProjectById(payload.getProject().getId());
            ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

            CraftUtils.throwIfNull(p, "Error: Project not found");
            CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

            // notification
            Notification notification = new Notification();

            System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK =================");
            CraftUtils.jsonLikePrint(payload);
            System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK ENDS =================");

            if (payload.getIsPartial()) {
                System.err.println("[DEBUG]: Partial Event");

                p.getPartialMessageList().add(payload.getMessage());
                // notification.setMessage(payload.getMessage());
                notification.setType(NotificationType.INFO);
                System.out.println("\nPARTIAL MESSAGE: " + payload.getMessage() + "\n");

            } else {
                pd.setIteration(pd.getIteration() + 1);

                if (payload.getSuccess()) {
                    // success

                    // on successful deployment
                    p.setTotalVersions(p.getTotalVersions()); // as succeed, total version should ++
                    p.setActiveVersion(p.getTotalVersions()); // latest one should be the active version

                    p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
                    p.setCurrentDeploymentId(null); // set the current deployment id to null

                    p.setStatus(ProjectStatus.ACTIVE);
                    p.setPortAssigned(payload.getProject().getPortAssigned());

                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED); // actually updated
                    pd.setVersion(p.getTotalVersions());

                    notification.setType(NotificationType.SUCCESS);
                    // notification.setMessage(payload.getMessage());

                    // all or nothing
                    // notification.setMessage(p.getPartialMessageList().toString());

                } else {
                    // failure
                    p.setStatus(ProjectStatus.INACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                    pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

                    notification.setType(NotificationType.ERROR);

                }

                System.err.println("[DEBUG]: Full Event");
            }

            // save stuff
            // ProjectDeployment pd_up = projectService.updateProjectDeployment(pd);
            // Project p_up = projectService.updateProject(p);
            projectService.updateProjectDeployment(pd);
            projectService.updateProject(p);

            // save notification if not partial
            if (!payload.getIsPartial()) {
                // notification.setMessage(notification.getMessage() + " [REVALIDATE]");
                // notification.setMessage(p.getPartialMessageList().toString());

                notificationService.createOrUpdate(notification);
            }

            notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
            requestProjectIDSSE(notification.toJson(), p.getId());
            requestProjectIDSSEDataDelete(p.getId());

        } catch (Exception e) {
            System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
                    + "\n=================== << Exceptions ===================");
        }

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

    private void requestProjectIDSSEDataDelete(String projectId) {
        System.err.println("[DEBUG]: EngineMessageService requestProjectIDSSE /sse/projects/");
        String url = envProps.getCockpitLocalUrl() + "/sse/projects/" + projectId;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String resultContent = null;
            // HttpGet httpGet = new HttpGet(url);
            HttpDelete httpPatch = new HttpDelete(url);
            httpPatch.addHeader("Authorization", "Bearer " + envProps.getCockpitAuthorizationToken());
            httpPatch.addHeader("Content-Type", "application/json");

            // httpPatch.setEntity(new StringEntity(json.toString()));

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
