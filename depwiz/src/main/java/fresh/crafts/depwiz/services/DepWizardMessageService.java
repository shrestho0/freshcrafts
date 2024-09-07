package fresh.crafts.depwiz.services;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.depwiz.config.MessageProducer;
import fresh.crafts.depwiz.entities.CommandServiceResult;
import fresh.crafts.depwiz.entities.Project;
import fresh.crafts.depwiz.entities.ProjectDeployment;
import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;
import fresh.crafts.depwiz.enums.ProjectDeploymentStatus;
import fresh.crafts.depwiz.utils.CraftUtils;

@Service
public class DepWizardMessageService {

        @Autowired
        private MessageProducer messageProducer;

        @Autowired
        private CommandService commandService;

        @Autowired
        private FileOpsService fileOpsService;

        @Autowired
        private SystemServices systemServices;

        // public String sayHello(KEvent kEvent, KEvent feedbacKEvent) {

        // CommandServiceResult cr = commandService.lsDir("/home/");
        // // System.err.println("[DEBUG] CommandServiceResult: " + cr);

        // feedbacKEvent.setEventSource(KEventProducers.DEP_WIZ);
        // feedbacKEvent.setEventDestination(KEventProducers.ENGINE);
        // KEventFeedbackPayload payload = (KEventFeedbackPayload)
        // feedbacKEvent.getPayload();
        // HashMap<String, Object> payloadData = new HashMap<>();
        // payloadData.put("message", "Hello, World!");
        // payload.setData(payloadData);

        // feedbacKEvent.setPayload(payload);
        // messageProducer.sendEvent(feedbacKEvent);

        // return "Hello, World!";
        // }

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

                ProjectDeployment pd = requestPayload.getDeployment();
                Project p = requestPayload.getProject();

                String errorTraceback = "";

                try {
                        // Validations
                        // check
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

                        this.doPreSetupChecks(feedbackKEvent, requestPayload, pd);

                        this.doInstallDeps(feedbackKEvent, requestPayload, pd);

                        //////// build project ////////
                        if (pd.getDepCommands().getBuild() != null
                                        && !pd.getDepCommands().getBuild().trim().isEmpty()) {
                                this.doBuildProject(feedbackKEvent, requestPayload, pd);
                        }
                        //////// post install ////////
                        if (pd.getDepCommands().getPostInstall() != null) {
                                this.doPostInstall(feedbackKEvent, requestPayload, pd);
                        }

                        //////////// application setup
                        this.doRunApplication(feedbackKEvent, requestPayload, pd, p);

                        // \n[RUNNING] Reloading nginx
                        // TODO: Setup nginx
                        this.doSetupNginx(feedbackKEvent, requestPayload, pd, p);

                        // TODO: Setup ssl

                        // finally
                        feedbackPayload.setSuccess(true);
                        feedbackPayload.setIsPartial(false);
                        HashMap<String, Object> m = new HashMap<>();
                        m.put("project_id", requestPayload.getProject().getId());
                        m.put("deployment_id", requestPayload.getDeployment().getId());
                        m.put("dep_status", ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
                        m.put("port_assigned", "" + p.getPortAssigned() + "");
                        m.put("partial_feedback", "[SUCCESS] Deployment completed");
                        feedbackPayload.setData(m);
                } catch (Exception e) {
                        // e.printStackTrace();
                        feedbackPayload.setSuccess(false);
                        feedbackPayload.setIsPartial(false);

                        HashMap<String, Object> m = new HashMap<>();
                        m.put("project_id", requestPayload.getProject().getId());
                        m.put("deployment_id", requestPayload.getDeployment().getId());
                        m.put("dep_status", ProjectDeploymentStatus.DEPLOYMENT_FAILED);
                        m.put("partial_feedback", "[FAILURE] " + e.getMessage());
                        m.put("error_traceback", pd.getErrorTraceback());
                        feedbackPayload.setData(m);

                        String message = e.getMessage();
                        if (message == null)
                                message = "Someting went wrong";

                        feedbackPayload.setMessage(message);

                }

                // for all case we'll send feedback
                messageProducer.sendEvent(feedbackKEvent);

        }

        public void deleteDeployments(KEvent kEvent, KEvent feedbackKEvent) {
                System.err.println("[DEBUG] DepWizardMessageService: deleteDeployments");

                KEventDepWizardPayload requestPayload = (KEventDepWizardPayload) kEvent.getPayload();
                KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload) feedbackKEvent.getPayload();
                feedbackPayload.setSuccess(true);

                Project p = requestPayload.getProject();
                List<ProjectDeployment> deploymentList = requestPayload.getDeploymentList();
                String errorTraceback = "";

                CraftUtils.jsonLikePrint(requestPayload);
                System.err.println("\n=========================[DEBUG] Deployment List: =========================\n");
                CraftUtils.jsonLikePrint(deploymentList);

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
                                        put("deploymentList", deploymentList);

                                }
                        };

                        CraftUtils.throwIfRequiredValuesAreNull(notNullables);

                        this.sendPartialDeletionFeedback(feedbackKEvent, requestPayload, p.getId(),
                                        "[SUCCESS] Deployment deletion started");

                        for (ProjectDeployment pd : deploymentList) {
                                // delete nginx conf
                                // file name = project_id.conf
                                errorTraceback += "\n========== DepID: " + pd.getId() + "Error Starts ==========\n";

                                // all graceful, no freking checks
                                List<CommandServiceResult> deleteNginxConfResult = commandService
                                                .disableNginxSite(p.getId());

                                errorTraceback += "\n ========== Nginx Conf Delete Result ==========\n";
                                for (CommandServiceResult res : deleteNginxConfResult) {
                                        if (res.getError() != null)
                                                errorTraceback += "\n" + res.getError() + "\n";
                                }

                                // delete pm2 process
                                List<CommandServiceResult> deletePM2Result = commandService.stopAndDeletePM2(
                                                pd.getProdFiles().getEcoSystemFileAbsPath());

                                errorTraceback += "\n ========== PM2 Delete Result ==========\n";
                                for (CommandServiceResult res : deletePM2Result) {
                                        if (res.getError() != null)
                                                errorTraceback += "\n" + res.getError() + "\n";
                                }

                                // delete projectDir
                                Boolean deleteProjectDirResult = fileOpsService
                                                .deleteProjectDir(p.getId(), p.getProjectDir().getAbsPath());

                                errorTraceback += "\n ========== Project Dir Delete Result ==========\n";
                                errorTraceback += "\n" + deleteProjectDirResult + "\n";

                                errorTraceback += "\n========== DepID: " + pd.getId() + "Error Ends ==========\n";

                                this.sendPartialDeletionFeedback(feedbackKEvent, requestPayload, p.getId(),
                                                "[SUCCESS] Deployment X deleted");

                                messageProducer.sendEvent(feedbackKEvent);
                        }

                        System.err.println("\n" + "loop er baire" + "\n");
                        System.err.println("\n" + errorTraceback + "\n");
                        System.err.println("\n" + "loop er baire ends" + "\n");

                        // delete nginx conf
                        // file name = project_id.conf

                        // after all done, finally
                        // feedbackPayload.setSuccess(true);
                        // feedbackPayload.setIsPartial(false);
                        HashMap<String, Object> m = new HashMap<>();
                        m.put("project_id", requestPayload.getProject().getId());
                        m.put("partial_feedback", "[SUCCESS] Deployment deletion completed");
                        feedbackPayload.setIsPartial(false);
                        feedbackPayload.setData(m);

                } catch (Exception e) {

                        String message = e.getMessage();
                        if (message == null)
                                message = "Someting went wrong";

                        HashMap<String, Object> m = new HashMap<>();
                        m.put("project_id", requestPayload.getProject().getId());
                        m.put("error_traceback", errorTraceback);
                        m.put("partial_feedback", "[FAILURE] " + message);

                        feedbackPayload.setSuccess(false);
                        feedbackPayload.setIsPartial(false);
                        feedbackPayload.setData(m);
                        feedbackPayload.setMessage(message);

                }

                // for all case we'll send feedback
                messageProducer.sendEvent(feedbackKEvent);
        }

        /////////////////////////////////// Helpers ///////////////////////////////////

        ////////// do pre build checks //////////
        private void doPreSetupChecks(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        ProjectDeployment pd) throws Exception {

                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[RUNNING] Checking files and installing dependencies",
                                ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);

                String rootDir = pd.getSrc().getRootDirAbsPath();
                Boolean rootDirPathExists = fileOpsService.checkDirectoryExists(rootDir);
                CraftUtils.throwIfFalseOrNull(rootDirPathExists, "Project root directory does not exist");

                Boolean prevBuildRemoved = fileOpsService.ensurePreviousBuildFilesRemoved(pd); // throws

                CraftUtils.throwIfFalseOrNull(prevBuildRemoved, "Could not remove previous build files");

                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[SUCCESS] Checking files and installing done",
                                ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);
        }

        ////////// install dependencies //////////
        private void doInstallDeps(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload, ProjectDeployment pd)
                        throws Exception {

                // install deps
                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[RUNNING] Installing dependencies",
                                ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);

                CommandServiceResult installDepsResult = commandService.freshInstallDeps(pd);

                // set error traceback if any
                pd.setErrorTraceback(installDepsResult.getError());

                CraftUtils.throwIfFalseOrNull(installDepsResult.getExitCode() == 0,
                                installDepsResult.getShortError() != null ? installDepsResult.getShortError()
                                                : "Failed to install dependencies");

                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[SUCCESS] Dependencies installed",
                                ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);

        }

        ////////// build project //////////
        private void doBuildProject(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        ProjectDeployment pd) throws Exception {
                // only build if build command is provided
                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null, "[RUNNING] Building project",
                                null);
                CommandServiceResult buildResult = commandService.freshBuildProject(pd);

                // set error traceback if any
                pd.setErrorTraceback(buildResult.getError());

                CraftUtils.throwIfFalseOrNull(buildResult.getExitCode() == 0,
                                buildResult.getShortError() != null ? buildResult.getShortError()
                                                : "Failed to build project");
                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[SUCCESS] Project build complete",
                                null);
        }

        ////////// post install //////////
        private void doPostInstall(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        ProjectDeployment pd) throws Exception {
                // post install
                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[RUNNING] Running post install command",
                                null);
                CommandServiceResult postInstallResult = commandService.postInstallCommands(pd);

                // set error traceback if any

                pd.setErrorTraceback(postInstallResult.getError());

                CraftUtils.throwIfFalseOrNull(postInstallResult.getExitCode() == 0,
                                postInstallResult.getShortError() != null ? postInstallResult.getShortError()
                                                : "Failed to run post install commands");

                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[SUCCESS] Post install commands executed",
                                ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);
        }

        ////////// setup pm2 //////////

        private void doRunApplication(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        ProjectDeployment pd, Project p) throws Exception {
                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[RUNNING] Binding Setting up application process ", null);
                // Find and set port to ecosystem file

                int frekingPort = systemServices.nextFreePort(50000, 60000);
                Boolean portSet = fileOpsService.setPortToProdFiles(pd.getProdFiles(),
                                frekingPort);
                CraftUtils.throwIfFalseOrNull(portSet, "Failed to set port to ecosystem file");

                p.setPortAssigned(frekingPort);

                /////////// Run project using pm2
                CommandServiceResult pm2StartResult = commandService
                                .startPM2(pd.getProdFiles().getEcoSystemFileAbsPath());

                // ignore it
                CommandServiceResult pm2ListResult = commandService.savePM2();

                pd.setErrorTraceback(pm2StartResult.getError());

                CraftUtils.throwIfFalseOrNull(pm2StartResult.getExitCode() == 0,
                                pm2StartResult.getShortError() != null ? pm2StartResult.getShortError()
                                                : "Failed to start project with pm2");

                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[SUCCESS] Application process setup complete",
                                ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);
        }

        ////////// setup nginx //////////

        private void doSetupNginx(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload, ProjectDeployment pd,
                        Project p) throws Exception {
                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null, "[RUNNING] Setting up nginx",
                                null);

                // move nginx conf file to /etc/nginx/sites-available and create symlink

                CommandServiceResult moveNginxConfResult = commandService
                                .enableNginxSite(pd.getProdFiles().getNginxConfFileAbsPath());
                pd.setErrorTraceback(moveNginxConfResult.getError());
                CraftUtils.throwIfFalseOrNull(moveNginxConfResult.getExitCode() == 0,
                                moveNginxConfResult.getShortError() != null ? moveNginxConfResult.getShortError()
                                                : "Failed to setup nginx");

                this.sendPartialDeploymentFeedback(feedbackKEvent, requestPayload, null,
                                "[SUCCESS] Nginx setup complete",
                                ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);

        }

        ////////// send partial feedback //////////

        private void sendPartialDeploymentFeedback(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        HashMap<String, Object> fullData, String partialMsg,
                        ProjectDeploymentStatus partialStatus) {

                KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload) feedbackKEvent.getPayload();
                feedbackPayload.setIsPartial(true);
                // success is null for partial feedback
                HashMap<String, Object> m = new HashMap<>();
                m.put("project_id", requestPayload.getProject().getId());
                m.put("deployment_id", requestPayload.getDeployment().getId());
                if (fullData != null) {
                        m.putAll(fullData);
                } else {
                        m.put("partial_feedback", partialMsg);
                        m.put("dep_status", partialStatus);
                }

                feedbackPayload.setData(m);
                messageProducer.sendEvent(feedbackKEvent);
        }

        private void sendPartialDeletionFeedback(
                        KEvent feedbackKEvent,
                        KEventDepWizardPayload requestPayload,
                        String deploymentId,
                        String partialMsg

        ) {

                KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload) feedbackKEvent.getPayload();
                feedbackPayload.setIsPartial(true);
                // success is null for partial feedback
                HashMap<String, Object> m = new HashMap<>();

                m.put("project_id", requestPayload.getProject().getId());
                m.put("deployment_id", deploymentId);
                m.put("partial_feedback", partialMsg);

                feedbackPayload.setData(m);
                messageProducer.sendEvent(feedbackKEvent);
        }

        public void reDeploy(KEvent kEvent, KEvent feedbackKEvent) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'reDeploy'");
        }

}
