package fresh.crafts.depwiz.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.depwiz.config.MessageProducer;
import fresh.crafts.depwiz.entities.CommandServiceResult;
import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;
import fresh.crafts.depwiz.entities.Project;
import fresh.crafts.depwiz.entities.ProjectDeployment;
import fresh.crafts.depwiz.utils.CraftUtils;
import java.util.*;

@Service
public class DepwizMessageServiceHelper {

        @Autowired
        private CommandService commandService;

        @Autowired
        private FileOpsService fileOpsService;

        @Autowired
        private MessageProducer messageProducer;

        @Autowired
        private SystemServices systemServices;

        /////////////////////////////////// Helpers ///////////////////////////////////

        ////////// do pre build checks //////////
        public void doPreSetupChecks(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        ProjectDeployment pd) throws Exception {

                this.sendPartialFeedback(feedbackKEvent, "[RUNNING] Checking files and installing dependencies");

                String rootDir = pd.getSrc().getRootDirAbsPath();
                Boolean rootDirPathExists = fileOpsService.checkDirectoryExists(rootDir);
                CraftUtils.throwIfFalseOrNull(rootDirPathExists, "Project root directory does not exist");

                Boolean prevBuildRemoved = fileOpsService.ensurePreviousBuildFilesRemoved(pd); // throws

                CraftUtils.throwIfFalseOrNull(prevBuildRemoved, "Could not remove previous build files");

                this.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Checking files and installing done");
        }

        // public void sendPartialDeploymentFeedback(KEvent fE, KEventDepWizardPayload
        // rP, String pM) {

        // KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload)
        // fE.getPayload();

        // feedbackPayload.setIsPartial(true);
        // feedbackPayload.setSuccess(true); // doen't matter

        // // success is null for partial feedback
        // HashMap<String, Object> m = new HashMap<>();

        // feedbackPayload.setCurrentDeployment(rP.getCurrentDeployment());
        // feedbackPayload.setProject(rP.getProject());

        // // m.put("project_id", requestPayload.getProject().getId());
        // // // m.put("deployment_id", requestPayload.getDeployment().getId());
        // // m.put("partial_feedback", partialMsg);
        // // feedbackPayload.setData(m);

        // messageProducer.sendEvent(fE);
        // }

        public void sendPartialFeedback(KEvent feedbackEvent, String partialMsg) {
                KEventDepWizardPayload payload = (KEventDepWizardPayload) feedbackEvent.getPayload();

                payload.setIsPartial(true);
                payload.setSuccess(true); // doen't matter
                payload.setMessage(partialMsg);

                // rest are set while feedback event creation

                this.sendFeedback(feedbackEvent);
        }

        // public void sendPartialDeletionFeedback(
        // KEvent feedbackKEvent,
        // KEventDepWizardPayload requestPayload,
        // String deploymentId,
        // String partialMsg

        // ) {

        // KEventDepWizardPayload feedbackPayload = (KEventDepWizardPayload)
        // feedbackKEvent.getPayload();
        // feedbackPayload.setIsPartial(true);
        // // success is null for partial feedback
        // HashMap<String, Object> m = new HashMap<>();

        // m.put("project_id", requestPayload.getProject().getId());
        // m.put("deployment_id", deploymentId);
        // m.put("partial_feedback", partialMsg);

        // feedbackPayload.setData(m);
        // messageProducer.sendEvent(feedbackKEvent);
        // }

        ////////// install dependencies //////////
        public void doInstallDeps(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload, ProjectDeployment pd)
                        throws Exception {

                // install deps
                this.sendPartialFeedback(feedbackKEvent, "[RUNNING] Installing dependencies");

                CommandServiceResult installDepsResult = commandService.freshInstallDeps(pd);

                // set error traceback if any
                pd.setErrorTraceback(installDepsResult.getError());

                CraftUtils.throwIfFalseOrNull(installDepsResult.getExitCode() == 0,
                                installDepsResult.getShortError() != null ? installDepsResult.getShortError()
                                                : "Failed to install dependencies");

                this.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Dependencies installed");

        }

        ////////// build project //////////
        public void doBuildProject(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        ProjectDeployment pd) throws Exception {
                // only build if build command is provided

                this.sendPartialFeedback(feedbackKEvent, "[RUNNING] Building project");

                CommandServiceResult buildResult = commandService.freshBuildProject(pd);

                // set error traceback if any
                pd.setErrorTraceback(buildResult.getError());

                CraftUtils.throwIfFalseOrNull(buildResult.getExitCode() == 0,
                                buildResult.getShortError() != null ? buildResult.getShortError()
                                                : "Failed to build project");

                this.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Project build complete");
        }

        ////////// post install //////////
        public void doPostInstall(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        ProjectDeployment pd) throws Exception {
                // post install

                this.sendPartialFeedback(feedbackKEvent, "[RUNNING] Running post install command");

                CommandServiceResult postInstallResult = commandService.postInstallCommands(pd);

                // set error traceback if any

                pd.setErrorTraceback(postInstallResult.getError());

                CraftUtils.throwIfFalseOrNull(postInstallResult.getExitCode() == 0,
                                postInstallResult.getShortError() != null ? postInstallResult.getShortError()
                                                : "Failed to run post install commands");

                this.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Post install commands executed");
        }

        ////////// setup pm2 //////////

        public void doRunApplication(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
                        ProjectDeployment pd, Project p) throws Exception {

                this.sendPartialFeedback(feedbackKEvent, "[RUNNING] Binding Setting up application process ");
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
                // CommandServiceResult pm2ListResult = commandService.savePM2();
                commandService.savePM2();

                pd.setErrorTraceback(pm2StartResult.getError());

                CraftUtils.throwIfFalseOrNull(pm2StartResult.getExitCode() == 0,
                                pm2StartResult.getShortError() != null ? pm2StartResult.getShortError()
                                                : "Failed to start project with pm2");

                this.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Application process setup complete");
        }

        ////////// setup nginx //////////

        public void doSetupNginx(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload, ProjectDeployment pd,
                        Project p) throws Exception {

                this.sendPartialFeedback(feedbackKEvent, "[RUNNING] Setting up nginx");

                // move nginx conf file to /etc/nginx/sites-available and create symlink

                CommandServiceResult moveNginxConfResult = commandService
                                .enableNginxSite(pd.getProdFiles().getNginxConfFileAbsPath());
                pd.setErrorTraceback(moveNginxConfResult.getError());
                CraftUtils.throwIfFalseOrNull(moveNginxConfResult.getExitCode() == 0,
                                moveNginxConfResult.getShortError() != null ? moveNginxConfResult.getShortError()
                                                : "Failed to setup nginx");

                this.sendPartialFeedback(feedbackKEvent, "[SUCCESS] Nginx setup complete");

        }

        public Boolean deleteProjectDir(Project p) {
                // delete projectDir
                return fileOpsService.deleteProjectDir(p.getId(), p.getProjectDir().getAbsPath());
        }

        public void sendFeedback(KEvent feedbackKEvent) {
                messageProducer.sendEvent(feedbackKEvent);
        }

        public void undeploy(ProjectDeployment pd, Project p) {

                // all graceful, no freking checks
                CommandServiceResult deleteNginxConfResult = commandService
                                .disableNginxSite(p.getId());

                CraftUtils.jsonLikePrint(deleteNginxConfResult);

                // delete pm2 process
                CommandServiceResult deletePM2Result = commandService.stopAndDeletePM2(
                                pd.getProdFiles().getEcoSystemFileAbsPath(), p.getId());
                CraftUtils.jsonLikePrint(deletePM2Result);
        }

        public void undeployAndDelete(ProjectDeployment pd, Project p) {
                this.undeploy(pd, p);
                // delete project directory
                Boolean deleteProjectDirResult = this.deleteProjectDir(p);
                CraftUtils.jsonLikePrint(deleteProjectDirResult);

        }

        public void deploy(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload, ProjectDeployment pd,
                        Project p) throws Exception {

                this.doPreSetupChecks(feedbackKEvent, requestPayload, pd);

                this.doInstallDeps(feedbackKEvent, requestPayload, pd);

                //////// build project ////////
                if (pd.getDepCommands().getBuild() != null
                                && !pd.getDepCommands().getBuild().trim().isEmpty()) {

                        this.doBuildProject(feedbackKEvent, requestPayload, pd);
                }
                if (pd.getDepCommands().getPostInstall() != null) {
                        this.doPostInstall(feedbackKEvent, requestPayload, pd);
                }

                this.doRunApplication(feedbackKEvent, requestPayload, pd, p);
                this.doSetupNginx(feedbackKEvent, requestPayload, pd, p);

        }

}
