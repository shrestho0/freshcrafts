// package fresh.crafts.engine.v1.services;

// import java.util.HashMap;

// import org.apache.hc.client5.http.classic.methods.HttpDelete;
// import org.apache.hc.client5.http.classic.methods.HttpPatch;
// import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
// import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
// import org.apache.hc.client5.http.impl.classic.HttpClients;
// import org.apache.hc.core5.http.HttpEntity;
// import org.apache.hc.core5.http.io.entity.EntityUtils;
// import org.apache.hc.core5.http.io.entity.StringEntity;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import fresh.crafts.engine.v1.entities.KEventPayloadDepWiz;
// import fresh.crafts.engine.v1.models.KEvent;
// import fresh.crafts.engine.v1.models.Notification;
// import fresh.crafts.engine.v1.models.Project;
// import fresh.crafts.engine.v1.models.ProjectDeployment;
// import fresh.crafts.engine.v1.utils.CraftUtils;
// import fresh.crafts.engine.v1.utils.EnvProps;
// import fresh.crafts.engine.v1.utils.enums.NotificationType;
// import fresh.crafts.engine.v1.utils.enums.ProjectDeploymentStatus;
// import fresh.crafts.engine.v1.utils.enums.ProjectStatus;

// @Service
// public class EngineDepwizMessageService {

//     @Autowired
//     private ProjectService projectService;

//     @Autowired
//     private ProjectDeploymentService projectDeploymentService;

//     @Autowired
//     private KEventService kEventService;

//     @Autowired
//     private EnvProps envProps;

//     @Autowired
//     private NotificationService notificationService;

//     @Autowired
//     private EngineMessageService engineMessageService;

//     public void serveFirstDepFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {

//         // save the event
//         kEventService.createOrUpdate(kEvent);

//         try {
//             // Validations
//             HashMap<String, Object> requiredValues = new HashMap<>();
//             requiredValues.put("project_id", payload.getProject().getId());
//             requiredValues.put("deployment_id", payload.getCurrentDeployment().getId());
//             requiredValues.put("isPartial", payload.getIsPartial());

//             CraftUtils.throwIfRequiredValuesAreNull(requiredValues);

//             Project p = projectService.getProjectById(payload.getProject().getId());
//             ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

//             CraftUtils.throwIfNull(p, "Error: Project not found");
//             CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

//             // notification
//             Notification notification = new Notification();

//             System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK =================");
//             CraftUtils.jsonLikePrint(payload);
//             System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK ENDS =================");

//             // first iteration as first successful deployment
//             // every update is a new iteration

//             if (payload.getIsPartial()) {
//                 System.err.println("[DEBUG]: Partial Event");

//                 p.getPartialMessageList().add(payload.getMessage());
//                 // notification.setMessage(payload.getMessage());
//                 notification.setType(NotificationType.INFO);

//             } else {

//                 pd.setIteration(pd.getIteration() + 1);

//                 p.setActiveVersion(p.getTotalVersions()); // latest one should be the active version

//                 if (payload.getSuccess()) {
//                     // success

//                     // on successful deployment
//                     // p.setTotalVersions(p.getTotalVersions() + 1); // as succeed, total version
//                     // should ++

//                     // pd.setVersion(p.getTotalVersions()); // set from service

//                     p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
//                     p.setCurrentDeploymentId(null); // set the current deployment id to null
//                     p.setStatus(ProjectStatus.ACTIVE);
//                     p.setPortAssigned(payload.getProject().getPortAssigned());

//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);

//                     notification.setType(NotificationType.SUCCESS);
//                     notification.setActionHints("GOTO_PROJECTS_" + p.getId());
//                     engineMessageService.requestNotificationSSE(notification.toJson());

//                 } else {
//                     // failure
//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
//                     p.setStatus(ProjectStatus.INACTIVE);
//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");
//                     // p.setPortAssigned(null); // should be already null

//                     pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

//                     notification.setType(NotificationType.ERROR);
//                     notification.setActionHints("GOTO_PROJECTS_" + p.getId());
//                     engineMessageService.requestNotificationSSE(notification.toJson());

//                 }

//                 System.err.println("[DEBUG]: Full Event");
//             }

//             // save stuff
//             // ProjectDeployment pd_up = projectService.updateProjectDeployment(pd);
//             // Project p_up = projectService.updateProject(p);

//             projectService.updateProjectDeployment(pd);
//             projectService.updateProject(p);

//             // save notification if not partial
//             if (!payload.getIsPartial()) {
//                 // notification.setMessage(notification.getMessage() + " [REVALIDATE]");
//                 // p.getPartialMessageList().add(" [REVALIDATE]");
//                 notificationService.createOrUpdate(notification);

//             }

//             notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
//             requestProjectIDSSE(notification.toJson(), p.getId());
//             requestProjectIDSSEDataDelete(p.getId());

//         } catch (Exception e) {
//             System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
//                     + "\n=================== << Exceptions ===================");
//         }
//     }

//     public void serveDeleteDepsFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {
//         try {
//             HashMap<String, Object> requiredValues = new HashMap<>();
//             requiredValues.put("project", payload.getProject());
//             requiredValues.put("isPartial", payload.getIsPartial());
//             requiredValues.put("partial_feedback", payload.getMessage());
//             CraftUtils.throwIfRequiredValuesAreNull(requiredValues);

//             // Project p =
//             // projectService.getProjectById(payload.getData().get("project_id").toString());
//             Project p = projectService.getProjectById(payload.getProject().getId());

//             // CraftUtils.throwIfNull(p, "Error: Project not found");

//             // // notification
//             Notification notification = new Notification();

//             // p.getPartialMessageList().add(payload.getData().get("partial_feedback").toString());
//             p.getPartialMessageList().add(payload.getMessage());

//             if (payload.getIsPartial()) {
//                 // partial event
//                 notification.setType(NotificationType.INFO);
//                 projectService.updateProject(p);

//             } else {

//                 // full event
//                 if (payload.getSuccess()) {
//                     notification.setType(NotificationType.INFO);
//                     // success
//                     notification.setType(NotificationType.SUCCESS);
//                     // notification.setMessage( ); // do it later
//                     projectService.deleteProjectAndDeployments(p.getId());
//                     notification.setMessage("[REVALIDATE] Project `" + p.getUniqueName() + "` Deleted Successfully");
//                     notification.setActionHints("REDIRECT_PROJECTS_" + p.getId()); //
//                     engineMessageService.requestNotificationSSE(notification.toJson());
//                 } else {
//                     // failure
//                     notification.setType(NotificationType.ERROR);
//                     notification.setMessage("[FAILURE] Failed to delete project: " + p.getUniqueName());
//                     notification.setActionHints("REDIRECT_PROJECTS_" + p.getId());
//                     engineMessageService.requestNotificationSSE(notification.toJson());

//                 }

//                 notificationService.createOrUpdate(notification);
//                 // return;

//             }

//             requestProjectIDSSE(notification.toJson(), p.getId());

//         } catch (Exception e) {
//             System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
//                     + "\n=================== << Exceptions ===================");

//         }

//     }

//     public void serveReDeployFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {
//         System.err.println("[DEBUG]: EngineDepwizMessageService serveReDeployFeedback");
//         // save the event
//         kEventService.createOrUpdate(kEvent);

//         try {

//             Project p = projectService.getProjectById(payload.getProject().getId());
//             ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

//             CraftUtils.throwIfNull(p, "Error: Project not found");
//             CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

//             // notification
//             Notification notification = new Notification();

//             System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK =================");
//             CraftUtils.jsonLikePrint(payload);
//             System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK ENDS =================");

//             if (payload.getIsPartial()) {
//                 System.err.println("[DEBUG]: Partial Event");

//                 p.getPartialMessageList().add(payload.getMessage());
//                 // notification.setMessage(payload.getMessage());
//                 notification.setType(NotificationType.INFO);
//                 System.out.println("\nPARTIAL MESSAGE: " + payload.getMessage() + "\n");

//             } else {
//                 pd.setIteration(pd.getIteration() + 1);

//                 if (payload.getSuccess()) {
//                     // success

//                     // on successful deployment
//                     // p.setTotalVersions(p.getTotalVersions()); // as succeed, total version should
//                     // ++
//                     // p.setActiveVersion(p.getTotalVersions()); // latest one should be the active
//                     // version

//                     p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
//                     p.setCurrentDeploymentId(null); // set the current deployment id to null
//                     p.setStatus(ProjectStatus.ACTIVE);
//                     p.setPortAssigned(payload.getProject().getPortAssigned());

//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED); // should be redep completed
//                     pd.setVersion(p.getTotalVersions());

//                     notification.setType(NotificationType.SUCCESS);
//                     // notification.setMessage(payload.getMessage());

//                     // all or nothing
//                     // notification.setMessage(p.getPartialMessageList().toString());

//                 } else {
//                     // failure
//                     p.setStatus(ProjectStatus.INACTIVE);
//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

//                     p.setPortAssigned(null);

//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED); // should be redep failed
//                     pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

//                     notification.setType(NotificationType.ERROR);

//                 }

//                 System.err.println("[DEBUG]: Full Event");
//             }

//             // save stuff
//             // ProjectDeployment pd_up = projectService.updateProjectDeployment(pd);
//             // Project p_up = projectService.updateProject(p);
//             projectService.updateProjectDeployment(pd);
//             projectService.updateProject(p);

//             // save notification if not partial
//             if (!payload.getIsPartial()) {
//                 // notification.setMessage(notification.getMessage() + " [REVALIDATE]");
//                 // notification.setMessage(p.getPartialMessageList().toString());

//                 notificationService.createOrUpdate(notification);
//             }

//             notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
//             requestProjectIDSSE(notification.toJson(), p.getId());
//             requestProjectIDSSEDataDelete(p.getId());

//         } catch (Exception e) {
//             System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
//                     + "\n=================== << Exceptions ===================");
//         }
//     }

//     public void serveUpdateDepFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {
//         System.err.println("[DEBUG]: EngineDepwizMessageService serveUpdateDepFeedback");
//         // save the event
//         kEventService.createOrUpdate(kEvent);

//         try {

//             Project p = projectService.getProjectById(payload.getProject().getId());
//             ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

//             // update should not occur if active deployment is not found
//             ProjectDeployment ad = projectService.getDeploymentById(p.getActiveDeploymentId());

//             CraftUtils.throwIfNull(p, "Error: Project not found");
//             CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

//             // notification
//             Notification notification = new Notification();

//             System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK =================");
//             CraftUtils.jsonLikePrint(payload);
//             System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK ENDS =================");

//             if (payload.getIsPartial()) {
//                 System.err.println("[DEBUG]: Partial Event");

//                 p.getPartialMessageList().add(payload.getMessage());
//                 // notification.setMessage(payload.getMessage());
//                 notification.setType(NotificationType.INFO);
//                 System.out.println("\nPARTIAL MESSAGE: " + payload.getMessage() + "\n");

//             } else {
//                 System.out.println("\nFULL MESSAGE: " + "iteration:" + pd.getIteration());
//                 // pd.setIteration(pd.getIteration() + 1);

//                 pd.setIteration(ad.getIteration() + 1);

//                 // we don't need previous active deployment as current deployment is a copy of
//                 // previous one
//                 projectDeploymentService.delete(p.getActiveDeploymentId());

//                 if (payload.getSuccess()) {
//                     // success

//                     // on successful deployment
//                     // but, no need to change version for update
//                     // only iteration should be updated

//                     p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
//                     p.setCurrentDeploymentId(null); // set the current deployment id to null

//                     p.setStatus(ProjectStatus.ACTIVE);
//                     p.setPortAssigned(payload.getProject().getPortAssigned());

//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED); // actually updated
//                     // pd.setVersion(p.getTotalVersions()); // update ee version gonna be the same

//                     notification.setType(NotificationType.SUCCESS);
//                     // notification.setMessage(payload.getMessage());

//                     // all or nothing
//                     // notification.setMessage(p.getPartialMessageList().toString());

//                 } else {
//                     // failure
//                     p.setStatus(ProjectStatus.INACTIVE);
//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");
//                     p.setPortAssigned(null);

//                     p.setActiveDeploymentId(null); // as no active deployment,

//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
//                     pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

//                     notification.setType(NotificationType.ERROR);

//                 }

//                 System.err.println("[DEBUG]: Full Event");
//             }

//             // save stuff
//             // ProjectDeployment pd_up = projectService.updateProjectDeployment(pd);
//             // Project p_up = projectService.updateProject(p);
//             projectService.updateProjectDeployment(pd);
//             projectService.updateProject(p);

//             // save notification if not partial
//             if (!payload.getIsPartial()) {
//                 // notification.setMessage(notification.getMessage() + " [REVALIDATE]");
//                 // notification.setMessage(p.getPartialMessageList().toString());

//                 notificationService.createOrUpdate(notification);
//             }

//             notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
//             requestProjectIDSSE(notification.toJson(), p.getId());
//             requestProjectIDSSEDataDelete(p.getId());

//         } catch (Exception e) {
//             System.err.println("[DEBUG]: =================== Exceptions >> ===================\n" + e.getMessage()
//                     + "\n=================== << Exceptions ===================");
//         }

//     }

//     public void serveRollforwardFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {

//         System.err.println("[DEBUG]: EngineDepwizMessageService serveRollforwardFeedback");
//         kEventService.createOrUpdate(kEvent);

//         try {
//             // if partial, do like others,
//             // if full and success, update versions, deps, set active dep, remove current
//             // if fail, user have to redeploy

//             Project p = projectService.getProjectById(payload.getProject().getId());
//             ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

//             // update should not occur if active deployment is not found
//             ProjectDeployment ad = projectService.getDeploymentById(p.getActiveDeploymentId());

//             CraftUtils.throwIfNull(p, "Error: Project not found");
//             CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

//             // notification
//             Notification notification = new Notification();

//             System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK =================");
//             CraftUtils.jsonLikePrint(payload);
//             System.err.println("================= PAYLOAD SERVE DEPLOY FEEDBACK ENDS =================");

//             if (payload.getIsPartial()) {
//                 System.err.println("[DEBUG]: Partial Event");

//                 p.getPartialMessageList().add(payload.getMessage());
//                 // notification.setMessage(payload.getMessage());
//                 notification.setType(NotificationType.INFO);
//                 // System.out.println("\nPARTIAL MESSAGE: " + payload.getMessage() + "\n");

//             } else {

//                 if (payload.getSuccess()) {

//                     // successs

//                     // latest version should be active version
//                     p.setActiveVersion(p.getTotalVersions());

//                     // current is the new active
//                     p.setActiveDeploymentId(p.getCurrentDeploymentId());

//                     p.setCurrentDeploymentId(null);
//                     p.setRollforwardDeploymentId(null);

//                     p.setStatus(ProjectStatus.ACTIVE);
//                     p.setPortAssigned(payload.getProject().getPortAssigned());
//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
//                     // pd.setVersion(pd.getVersion()); // already updated before initiating
//                     // rollforward to depwiz

//                     notification.setType(NotificationType.SUCCESS);

//                     ad.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);

//                 } else {
//                     // failure

//                     p.setStatus(ProjectStatus.INACTIVE);
//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");
//                     p.setPortAssigned(null);

//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
//                     pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

//                     notification.setType(NotificationType.ERROR);

//                     ad.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);

//                 }
//                 notificationService.createOrUpdate(notification);
//             }

//             // save stuff
//             projectService.updateProjectDeployment(pd);
//             projectService.updateProject(p);
//             projectService.updateProjectDeployment(ad);

//             notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
//             requestProjectIDSSE(notification.toJson(), p.getId());
//             requestProjectIDSSEDataDelete(p.getId());

//         } catch (Exception e) {
//             System.out.println(">>>>>>>>>> Exception: <<<<<<<<<<");
//             System.out.println(e.getMessage());
//         }

//     }

//     public void serveRollbackFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {

//         System.err.println("[DEBUG]: EngineDepwizMessageService serveRollbackFeedback");
//         kEventService.createOrUpdate(kEvent);

//         try {

//             Project p = projectService.getProjectById(payload.getProject().getId());
//             ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

//             // if exists, previously active dep status should be updated to
//             // "READY_FOR_DEPLOYMENT"

//             ProjectDeployment ad = null;

//             if (payload.getActiveDeployment() != null) {
//                 ad = projectService.getDeploymentById(payload.getActiveDeployment().getId());
//             }

//             Project payloadP = payload.getProject();
//             ProjectDeployment payloadPD = payload.getCurrentDeployment();

//             // update should not occur if active deployment is not found
//             // ProjectDeployment ad =
//             // projectService.getDeploymentById(p.getActiveDeploymentId());

//             CraftUtils.throwIfNull(p, "Error: Project not found");
//             CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

//             // notification
//             Notification notification = new Notification();

//             System.err.println("================= PAYLOAD ROLLBACK FEEDBACK =================");
//             CraftUtils.jsonLikePrint(payload);
//             System.err.println("================= PAYLOAD ROLLBACK FEEDBACK ENDS =================");

//             if (payload.getIsPartial()) {
//                 System.err.println("[DEBUG]: Partial Event");

//                 p.getPartialMessageList().add(payload.getMessage());
//                 // notification.setMessage(payload.getMessage());
//                 notification.setType(NotificationType.INFO);
//                 // System.out.println("\nPARTIAL MESSAGE: " + payload.getMessage() + "\n");

//             } else {

//                 if (payload.getSuccess()) {

//                     // updated prod info
//                     p.setActiveDeploymentId(payloadPD.getId());
//                     p.setPortAssigned(payloadP.getPortAssigned());
//                     p.setActiveVersion(payloadPD.getVersion());

//                     // no current deployment
//                     p.setCurrentDeploymentId(null);
//                     p.setRollbackDeploymentId(null);

//                     // ensuring statuses and what user gets
//                     p.setStatus(ProjectStatus.ACTIVE);
//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
//                     pd.setVersion(payloadPD.getVersion());

//                     // ensuring new port

//                     notification.setType(NotificationType.SUCCESS);
//                     notification.setActionHints("GOTO_PROJECTS_" + p.getId());

//                 } else {
//                     // failure

//                     p.setStatus(ProjectStatus.INACTIVE);
//                     p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");
//                     p.setPortAssigned(null);

//                     // for some reason, this is null
//                     p.setCurrentDeploymentId(p.getRollbackDeploymentId());
//                     // so, there shouldn't be any active deployment
//                     p.setActiveDeploymentId(null);

//                     // FIX ME change to more appropiate here if requires
//                     pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
//                     pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

//                     notification.setType(NotificationType.ERROR);

//                 }

//                 if (ad != null) {
//                     ad.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);
//                 }

//                 notificationService.createOrUpdate(notification);
//             }

//             // save stuff
//             projectService.updateProjectDeployment(pd);
//             projectService.updateProject(p);

//             notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
//             requestProjectIDSSE(notification.toJson(), p.getId());
//             requestProjectIDSSEDataDelete(p.getId());

//         } catch (Exception e) {
//             System.out.println(">>>>>>>>>> Exception: <<<<<<<<<<");
//             System.out.println(e.getMessage());
//         }

//     }

//     /**
//      * requestProjectIDSSE
//      *
//      * @param json
//      * @implNote This method is responsible for sending notification to the frontend
//      *           via SSE
//      */
//     private void requestProjectIDSSE(String json, String projectId) {
//         System.err.println("[DEBUG]: EngineMessageService requestProjectIDSSE /sse/projects/");
//         String url = envProps.getCockpitLocalUrl() + "/sse/projects/" + projectId;

//         try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
//             String resultContent = null;
//             // HttpGet httpGet = new HttpGet(url);
//             HttpPatch httpPatch = new HttpPatch(url);
//             httpPatch.addHeader("Authorization", "Bearer " + envProps.getCockpitAuthorizationToken());
//             httpPatch.addHeader("Content-Type", "application/json");

//             httpPatch.setEntity(new StringEntity(json.toString()));

//             try (CloseableHttpResponse response = httpclient.execute(httpPatch)) {
//                 HttpEntity entity = response.getEntity();
//                 // Get response information
//                 resultContent = EntityUtils.toString(entity);
//                 System.out.println("----------------------");
//                 System.out.println(resultContent);
//                 System.out.println("----------------------");
//             }

//         } catch (Exception e) {
//             System.err.println("[DEBUG]: Error: " + e.getMessage());
//         }

//     }

//     private void requestProjectIDSSEDataDelete(String projectId) {
//         System.err.println("[DEBUG]: EngineMessageService requestProjectIDSSE /sse/projects/");
//         String url = envProps.getCockpitLocalUrl() + "/sse/projects/" + projectId;

//         try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
//             String resultContent = null;
//             // HttpGet httpGet = new HttpGet(url);
//             HttpDelete httpPatch = new HttpDelete(url);
//             httpPatch.addHeader("Authorization", "Bearer " + envProps.getCockpitAuthorizationToken());
//             httpPatch.addHeader("Content-Type", "application/json");

//             // httpPatch.setEntity(new StringEntity(json.toString()));

//             try (CloseableHttpResponse response = httpclient.execute(httpPatch)) {
//                 HttpEntity entity = response.getEntity();
//                 // Get response information
//                 resultContent = EntityUtils.toString(entity);
//                 System.out.println("----------------------");
//                 System.out.println(resultContent);
//                 System.out.println("----------------------");
//             }

//         } catch (Exception e) {
//             System.err.println("[DEBUG]: Error: " + e.getMessage());
//         }

//     }

// }

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

import fresh.crafts.engine.v1.entities.KEventPayloadDepWiz;
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
    private ProjectDeploymentService projectDeploymentService;

    @Autowired
    private KEventService kEventService;

    @Autowired
    private EnvProps envProps;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EngineMessageService engineMessageService;

    public void serveFirstDepFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {

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

            if (payload.getIsPartial()) {
                System.err.println("[DEBUG]: Partial Event");

                p.getPartialMessageList().add(payload.getMessage());
                // notification.setMessage(payload.getMessage());
                notification.setType(NotificationType.INFO);

            } else {

                pd.setIteration(pd.getIteration() + 1);

                p.setActiveVersion(p.getTotalVersions()); // latest one should be the active version

                if (payload.getSuccess()) {
                    // success

                    // on successful deployment
                    // p.setTotalVersions(p.getTotalVersions() + 1); // as succeed, total version
                    // should ++

                    // pd.setVersion(p.getTotalVersions()); // set from service

                    p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
                    p.setCurrentDeploymentId(null); // set the current deployment id to null
                    p.setStatus(ProjectStatus.ACTIVE);
                    p.setPortAssigned(payload.getProject().getPortAssigned());

                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);

                    notification.setType(NotificationType.SUCCESS);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Deployed Successfully");
                    engineMessageService.requestNotificationSSE(notification.toJson());

                } else {
                    // failure
                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                    p.setStatus(ProjectStatus.INACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");
                    // p.setPortAssigned(null); // should be already null

                    pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

                    notification.setType(NotificationType.ERROR);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Deployment Failed");

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

    public void serveDeleteDepsFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {
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
                    notification.setActionHints("REDIRECT_PROJECTS_all"); //
                    notification.setMessage("Project `" + p.getUniqueName() + "` Deleted Successfully");
                    engineMessageService.requestNotificationSSE(notification.toJson());
                } else {
                    // failure
                    notification.setType(NotificationType.ERROR);
                    notification.setMessage("[FAILURE] Failed to delete project: " + p.getUniqueName());
                    notification.setActionHints("REDIRECT_PROJECTS_all");
                    notification.setMessage("Failed to delete project: " + p.getUniqueName());
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

    public void serveReDeployFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {
        System.err.println("[DEBUG]: EngineDepwizMessageService serveReDeployFeedback");
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
                    // p.setTotalVersions(p.getTotalVersions()); // as succeed, total version should
                    // ++
                    // p.setActiveVersion(p.getTotalVersions()); // latest one should be the active
                    // version

                    p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
                    p.setCurrentDeploymentId(null); // set the current deployment id to null
                    p.setStatus(ProjectStatus.ACTIVE);
                    p.setPortAssigned(payload.getProject().getPortAssigned());

                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED); // should be redep completed
                    pd.setVersion(p.getTotalVersions());

                    notification.setType(NotificationType.SUCCESS);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Re-deployed Successfully");
                    // notification.setMessage(payload.getMessage());

                    // all or nothing
                    // notification.setMessage(p.getPartialMessageList().toString());

                } else {
                    // failure
                    p.setStatus(ProjectStatus.INACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    p.setPortAssigned(null);

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED); // should be redep failed
                    pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

                    notification.setType(NotificationType.ERROR);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Re-deployment Failed");

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

    public void serveUpdateDepFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {
        System.err.println("[DEBUG]: EngineDepwizMessageService serveUpdateDepFeedback");
        // save the event
        kEventService.createOrUpdate(kEvent);

        try {

            Project p = projectService.getProjectById(payload.getProject().getId());
            ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

            // update should not occur if active deployment is not found
            ProjectDeployment ad = projectService.getDeploymentById(p.getActiveDeploymentId());

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
                System.out.println("\nFULL MESSAGE: " + "iteration:" + pd.getIteration());
                // pd.setIteration(pd.getIteration() + 1);

                pd.setIteration(ad.getIteration() + 1);

                // we don't need previous active deployment as current deployment is a copy of
                // previous one
                projectDeploymentService.delete(p.getActiveDeploymentId());

                if (payload.getSuccess()) {
                    // success

                    // on successful deployment
                    // but, no need to change version for update
                    // only iteration should be updated

                    p.setActiveDeploymentId(p.getCurrentDeploymentId()); // set the current deployment id to active
                    p.setCurrentDeploymentId(null); // set the current deployment id to null

                    p.setStatus(ProjectStatus.ACTIVE);
                    p.setPortAssigned(payload.getProject().getPortAssigned());

                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED); // actually updated
                    // pd.setVersion(p.getTotalVersions()); // update ee version gonna be the same

                    notification.setType(NotificationType.SUCCESS);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Updated Successfully");
                    // notification.setMessage(payload.getMessage());

                    // all or nothing
                    // notification.setMessage(p.getPartialMessageList().toString());

                } else {
                    // failure
                    p.setStatus(ProjectStatus.INACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");
                    p.setPortAssigned(null);

                    p.setActiveDeploymentId(null); // as no active deployment,

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                    pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

                    notification.setType(NotificationType.ERROR);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Update Failed");

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

    public void serveRollforwardFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {

        System.err.println("[DEBUG]: EngineDepwizMessageService serveRollforwardFeedback");
        kEventService.createOrUpdate(kEvent);

        try {
            // if partial, do like others,
            // if full and success, update versions, deps, set active dep, remove current
            // if fail, user have to redeploy

            Project p = projectService.getProjectById(payload.getProject().getId());
            ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

            // update should not occur if active deployment is not found
            ProjectDeployment ad = projectService.getDeploymentById(p.getActiveDeploymentId());

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
                // System.out.println("\nPARTIAL MESSAGE: " + payload.getMessage() + "\n");

            } else {

                if (payload.getSuccess()) {

                    // successs

                    // latest version should be active version
                    p.setActiveVersion(p.getTotalVersions());

                    // current is the new active
                    p.setActiveDeploymentId(p.getCurrentDeploymentId());

                    p.setCurrentDeploymentId(null);
                    p.setRollforwardDeploymentId(null);

                    p.setStatus(ProjectStatus.ACTIVE);
                    p.setPortAssigned(payload.getProject().getPortAssigned());
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
                    // pd.setVersion(pd.getVersion()); // already updated before initiating
                    // rollforward to depwiz

                    notification.setType(NotificationType.SUCCESS);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Rollforwarded Successfully");

                    ad.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);

                } else {
                    // failure

                    p.setStatus(ProjectStatus.INACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");
                    p.setPortAssigned(null);
                    p.setActiveDeploymentId(null); // no active deploymentif failed

                    // as, it's in current deployment, so, user only can redeploy
                    p.setRollforwardDeploymentId(null);

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                    pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

                    notification.setType(NotificationType.ERROR);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Rollforward Failed");

                    ad.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);

                }
                notificationService.createOrUpdate(notification);
            }

            // save stuff
            projectService.updateProjectDeployment(pd);
            projectService.updateProject(p);
            projectService.updateProjectDeployment(ad);

            notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
            requestProjectIDSSE(notification.toJson(), p.getId());
            requestProjectIDSSEDataDelete(p.getId());

        } catch (Exception e) {
            System.out.println(">>>>>>>>>> Exception: <<<<<<<<<<");
            System.out.println(e.getMessage());
        }

    }

    public void serveRollbackFeedback(KEvent kEvent, KEventPayloadDepWiz payload) {

        System.err.println("[DEBUG]: EngineDepwizMessageService serveRollbackFeedback");
        kEventService.createOrUpdate(kEvent);

        try {

            Project p = projectService.getProjectById(payload.getProject().getId());
            ProjectDeployment pd = projectService.getDeploymentById(payload.getCurrentDeployment().getId());

            // if exists, previously active dep status should be updated to
            // "READY_FOR_DEPLOYMENT"

            ProjectDeployment ad = null;

            if (payload.getActiveDeployment() != null) {
                ad = projectService.getDeploymentById(payload.getActiveDeployment().getId());
            }

            Project payloadP = payload.getProject();
            ProjectDeployment payloadPD = payload.getCurrentDeployment();

            // update should not occur if active deployment is not found
            // ProjectDeployment ad =
            // projectService.getDeploymentById(p.getActiveDeploymentId());

            CraftUtils.throwIfNull(p, "Error: Project not found");
            CraftUtils.throwIfNull(pd, "Error: Project Deployment not found");

            // notification
            Notification notification = new Notification();

            System.err.println("================= PAYLOAD ROLLBACK FEEDBACK =================");
            CraftUtils.jsonLikePrint(payload);
            System.err.println("================= PAYLOAD ROLLBACK FEEDBACK ENDS =================");

            if (payload.getIsPartial()) {
                System.err.println("[DEBUG]: Partial Event");

                p.getPartialMessageList().add(payload.getMessage());
                // notification.setMessage(payload.getMessage());
                notification.setType(NotificationType.INFO);
                // System.out.println("\nPARTIAL MESSAGE: " + payload.getMessage() + "\n");

            } else {

                if (payload.getSuccess()) {

                    // updated prod info
                    p.setActiveDeploymentId(payloadPD.getId());
                    p.setPortAssigned(payloadP.getPortAssigned());
                    p.setActiveVersion(payloadPD.getVersion());

                    // no current deployment
                    p.setCurrentDeploymentId(null);
                    p.setRollbackDeploymentId(null);

                    // ensuring statuses and what user gets
                    p.setStatus(ProjectStatus.ACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");

                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
                    pd.setVersion(payloadPD.getVersion());

                    p.setRollbackDeploymentId(null);

                    // ensuring new port

                    notification.setType(NotificationType.SUCCESS);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Rollbacked Successfully");

                } else {
                    // failure

                    p.setStatus(ProjectStatus.INACTIVE);
                    p.getPartialMessageList().add(payload.getMessage() + " __REVALIDATE__");
                    p.setPortAssigned(null);

                    // for some reason, this is null
                    p.setCurrentDeploymentId(p.getRollbackDeploymentId());
                    // so, there shouldn't be any active deployment
                    p.setActiveDeploymentId(null);
                    p.setRollbackDeploymentId(null);

                    // FIX ME change to more appropiate here if requires
                    pd.setStatus(ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                    pd.setErrorTraceback(payload.getCurrentDeployment().getErrorTraceback());

                    notification.setType(NotificationType.ERROR);
                    notification.setActionHints("GOTO_PROJECTS_" + p.getId());
                    notification.setMessage("Project `" + p.getUniqueName() + "` Rollback Failed");

                }

                if (ad != null) {
                    ad.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);
                }

                notificationService.createOrUpdate(notification);
            }

            // save stuff
            projectService.updateProjectDeployment(pd);
            projectService.updateProject(p);

            notification.setMessage(CraftUtils.toJson(p.getPartialMessageList()));
            requestProjectIDSSE(notification.toJson(), p.getId());
            requestProjectIDSSEDataDelete(p.getId());

        } catch (Exception e) {
            System.out.println(">>>>>>>>>> Exception: <<<<<<<<<<");
            System.out.println(e.getMessage());
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
