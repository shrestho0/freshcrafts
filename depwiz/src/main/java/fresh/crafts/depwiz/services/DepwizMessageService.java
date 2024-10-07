package fresh.crafts.depwiz.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.depwiz.entities.Project;
import fresh.crafts.depwiz.entities.ProjectDeployment;
import fresh.crafts.depwiz.enums.DepWizKEventCommands;
import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;
import fresh.crafts.depwiz.utils.CraftUtils;

@Service
public class DepwizMessageService {

        @Autowired
        private DepwizMessageServiceHelper helper;

        public void firstDeploy(KEvent kEvent, KEvent feedbackKEvent) {
                // check/validations
                // build
                // ecosystem file
                // run project using pm2
                // setup nginx
                // ssl
                // feedback

                KEventDepWizardPayload requestPayload = (KEventDepWizardPayload) kEvent.getPayload();
                KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload) feedbackKEvent.getPayload();

                ProjectDeployment pd = requestPayload.getCurrentDeployment();
                Project p = requestPayload.getProject();

                try {
                        // Validations
                        HashMap<String, Object> notNullables = new HashMap<>() {
                                {
                                        put("project", requestPayload.getProject());
                                        put("deployment", pd);
                                        put("deployment.src", pd.getSrc());
                                        put("deployment.depCommands", pd.getDepCommands());
                                        put("deployment.depCommands.build",
                                                        pd.getDepCommands().getBuild());
                                }
                        };

                        CraftUtils.throwIfRequiredValuesAreNull(notNullables);

                        // build
                        // check if projectRoot exists
                        helper.deploy(feedbackKEvent, requestPayload, pd, p);

                        // finally
                        // we'll update partially from this project entity
                        feedbackPayload.setSuccess(true);
                        feedbackPayload.setMessage("[SUCCESS] Deployment completed for project: " + p.getUniqueName());
                        feedbackPayload.setIsPartial(false);

                } catch (Exception e) {
                        // e.printStackTrace();

                        feedbackPayload.setSuccess(false);
                        feedbackPayload.setIsPartial(false);

                        feedbackPayload.setMessage("[FAILURE] " +
                                        (e.getMessage() != null ? e.getMessage()
                                                        : "Deployment completed for project: " + p.getUniqueName()));

                }

                // for all case we'll send feedback
                helper.sendFeedback(feedbackKEvent);

        }

        public void deleteDeployments(KEvent kEvent, KEvent feedbackKEvent) {
                System.err.println("[DEBUG] DepWizardMessageService: deleteDeployments");

                KEventDepWizardPayload requestPayload = (KEventDepWizardPayload) kEvent.getPayload();
                KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload) feedbackKEvent.getPayload();
                feedbackPayload.setSuccess(true);

                Project p = requestPayload.getProject();
                // List<ProjectDeployment> deploymentList = requestPayload.getDeploymentList();
                // String errorTraceback = "";

                CraftUtils.jsonLikePrint(requestPayload);
                System.err.println("\n=========================[DEBUG] Deployment List: =========================\n");
                // CraftUtils.jsonLikePrint(deploymentList);

                /**
                 * For each deployment; send one message per deployment
                 * Delete nginx conf file
                 * Delete pm2 process
                 * Delete projectDir (just check if directory name matches project id)
                 * 
                 * Finally, send a feedback
                 */

                try {

                        HashMap<String, Object> notNullables = new HashMap<>() {
                                {
                                        put("project", p);
                                        put("deploymentList", requestPayload.getDeploymentList());

                                }
                        };

                        CraftUtils.throwIfRequiredValuesAreNull(notNullables);

                        helper.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Deployment deletion started");

                        for (ProjectDeployment pd : requestPayload.getDeploymentList()) {
                                helper.undeployAndDelete(pd, p);
                        }

                        // delete nginx conf
                        // file name = project_id.conf

                        // after all done, finally

                        // m.put("partial_feedback", "[SUCCESS] Deployment deletion completed");
                        feedbackPayload.setMessage("[SUCCESS] Deployment deletion completed");
                        feedbackPayload.setIsPartial(false);
                        // feedbackPayload.setData(m);

                } catch (Exception e) {

                        String message = e.getMessage();
                        if (message == null)
                                message = "Someting went wrong";
                        message = "[FAILURE] " + message;

                        // HashMap<String, Object> m = new HashMap<>();
                        // m.put("project_id", requestPayload.getProject().getId());
                        // m.put("error_traceback", errorTraceback);
                        // m.put("partial_feedback", "[FAILURE] " + message);
                        System.err.println("[ERROR] (DepwizMessageService/deleteDeployments) " + message);
                        feedbackPayload.setSuccess(false);
                        feedbackPayload.setIsPartial(false);
                        // feedbackPayload.setData(m);
                        feedbackPayload.setMessage(message);

                }

                // for all case we'll send feedback
                helper.sendFeedback(feedbackKEvent);
        }

        ////////// send partial feedback //////////

        // public void reDeploy(KEvent kEvent, KEvent feedbackKEvent) {
        // // delete previous deployment
        // // build
        // // ecosystem file
        // // run project using pm2
        // // setup nginx
        // // ssl
        // // feedback

        // KEventDepWizardPayload requestPayload = (KEventDepWizardPayload)
        // kEvent.getPayload();
        // KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload)
        // feedbackKEvent.getPayload();

        // ProjectDeployment pd = requestPayload.getCurrentDeployment();
        // Project p = requestPayload.getProject();

        // try {

        // // remove previous deployment
        // helper.sendPartialFeedback(feedbackKEvent, "[RUNNING] Removing previous
        // deployment");
        // helper.undeploy(pd, p);
        // helper.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Previous deployment
        // removed");

        // helper.deploy(feedbackKEvent, requestPayload, pd, p);
        // // TODO: Setup ssl

        // // finally
        // // we'll update partially from this project entity
        // feedbackPayload.setSuccess(true);
        // feedbackPayload.setMessage(
        // "[SUCCESS] Re-deployment completed for project: " + p.getUniqueName());
        // feedbackPayload.setIsPartial(false);

        // } catch (Exception e) {
        // // e.printStackTrace();

        // feedbackPayload.setSuccess(false);
        // feedbackPayload.setIsPartial(false);

        // // : "Re-deployment failed for project: " + p.getUniqueName())
        // feedbackPayload.setMessage("[FAILURE] " + e.getMessage());

        // }

        // // for all case we'll send feedback
        // helper.sendFeedback(feedbackKEvent);
        // }

        public void reDeploy(KEvent kEvent, KEvent feedbackKEvent) {
                // undeploy active
                // deploy current

                System.err.println("[DEBUG] DepWizardMessageService: reDeploy");

                KEventDepWizardPayload requestPayload = (KEventDepWizardPayload) kEvent.getPayload();
                KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload) feedbackKEvent.getPayload();

                DepWizKEventCommands cmd = requestPayload.getCommand();
                System.err.println("[DEBUG] DepWizardMessageService: cmd" + cmd);

                ProjectDeployment currentDeployment = requestPayload.getCurrentDeployment();
                ProjectDeployment activeDeployment = requestPayload.getActiveDeployment();

                Project p = requestPayload.getProject();

                HashMap<DepWizKEventCommands, String> initMessages = new HashMap<>() {
                        {
                                put(DepWizKEventCommands.RE_DEPLOY, "Re-deployment started");
                                put(DepWizKEventCommands.UPDATE_DEPLOYMENT, "Deployment update started");
                                put(DepWizKEventCommands.ROLLBACK, "Rollback started");
                                put(DepWizKEventCommands.ROLLFORWARD, "Rollforward started");
                        }
                };

                HashMap<DepWizKEventCommands, String> finishMessages = new HashMap<>() {
                        {
                                put(DepWizKEventCommands.RE_DEPLOY, "Re-deployment completed");
                                put(DepWizKEventCommands.UPDATE_DEPLOYMENT, "Deployment update completed");
                                put(DepWizKEventCommands.ROLLBACK, "Rollback completed");
                                put(DepWizKEventCommands.ROLLFORWARD, "Rollforward completed");
                        }
                };

                try {
                        // Validations
                        HashMap<String, Object> notNullables = new HashMap<>() {
                                {
                                        put("project", requestPayload.getProject());
                                }
                        };

                        if (cmd == DepWizKEventCommands.RE_DEPLOY) {
                                notNullables.put("currentDeployment", currentDeployment);
                                CraftUtils.throwIfRequiredValuesAreNull(notNullables);
                        } else if( cmd== DepWizKEventCommands.ROLLBACK) {
                                notNullables.put("currentDeployment", currentDeployment);
                                CraftUtils.throwIfRequiredValuesAreNull(notNullables);

                                if(activeDeployment != null) {
                                        helper.sendPartialFeedback(feedbackKEvent, "[RUNNING] Removing previous deployment");
                                        helper.undeploy(activeDeployment, p);
                                        helper.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Previous deployment removed");
                                }

                               
                        }else  {
                                notNullables.put("currentDeployment", currentDeployment);
                                notNullables.put("activeDeployment", activeDeployment);
                                CraftUtils.throwIfRequiredValuesAreNull(notNullables);

                                // undeploy
                                helper.sendPartialFeedback(feedbackKEvent, "[RUNNING] Removing previous deployment");
                                helper.undeploy(activeDeployment, p);
                                helper.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Previous deployment removed");
                        }

                        helper.sendPartialFeedback(feedbackKEvent, "[SUCCESS] " + initMessages.get(cmd));
                        // System.err.println("[DEBUG] First ffedback " + initMessages.get(cmd));
                        // // remove active deployment

                        // // build
                        // // TODO\: refactor helper.deploy later
                        helper.deploy(feedbackKEvent, requestPayload, currentDeployment, p);

                        // // feedbackPayload.setMessage(
                        // // "[SUCCESS] Deployment update completed for project: " +
                        // p.getUniqueName());

                        // // helper.sendPartialFeedback(feedbackKEvent, "[SUCCESS] " +
                        // // finishMessages.get(cmd));

                        // final message
                        feedbackPayload.setSuccess(true);
                        feedbackPayload.setMessage("[SUCCESS] " + finishMessages.get(cmd));
                        feedbackPayload.setIsPartial(false);

                } catch (Exception e) {
                        System.out.println("[ERROR] Encountered Exception " + e.getMessage());
                        feedbackPayload.setSuccess(false);
                        feedbackPayload.setIsPartial(false);
                        feedbackPayload.setMessage("[FAILURE] " + e.getMessage());
                }

                helper.sendFeedback(feedbackKEvent);

        }

        public void modifyDomains(KEvent kEvent, KEvent feedbackKEvent) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'modifyDomains'");
        }

}
