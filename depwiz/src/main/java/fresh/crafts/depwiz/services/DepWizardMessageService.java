package fresh.crafts.depwiz.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.depwiz.config.MessageProducer;
import fresh.crafts.depwiz.entities.CommandServiceResult;
import fresh.crafts.depwiz.entities.KEventFeedbackPayload;
import fresh.crafts.depwiz.entities.Project;
import fresh.crafts.depwiz.entities.ProjectDeployment;
import fresh.crafts.depwiz.entities.KEvent;
import fresh.crafts.depwiz.entities.KEventDepWizardPayload;
import fresh.crafts.depwiz.enums.KEventProducers;
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

    public String sayHello(KEvent kEvent, KEvent feedbacKEvent) {

        CommandServiceResult cr = commandService.lsDir("/home/");
        // System.err.println("[DEBUG] CommandServiceResult: " + cr);

        feedbacKEvent.setEventSource(KEventProducers.DEP_WIZ);
        feedbacKEvent.setEventDestination(KEventProducers.ENGINE);
        KEventFeedbackPayload payload = (KEventFeedbackPayload) feedbacKEvent.getPayload();
        HashMap<String, Object> payloadData = new HashMap<>();
        payloadData.put("message", "Hello, World!");
        payload.setData(payloadData);

        feedbacKEvent.setPayload(payload);
        messageProducer.sendEvent(feedbacKEvent);

        return "Hello, World!";
    }

    public void firstDeploy(KEvent kEvent, KEvent feedbackKEvent) {
        // check/validations
        // build
        // ecosystem file
        // run project using pm2
        // setup nginx
        // ssl
        // feedback

        KEventDepWizardPayload requestPayload = (KEventDepWizardPayload) kEvent.getPayload();
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();

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

            this.sendPartialFeedback(feedbackKEvent, requestPayload, null,
                    "[RUNNING] Checking files and installing dependencies",
                    ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);

            String rootDir = pd.getSrc().getRootDirAbsPath();
            Boolean rootDirPathExists = fileOpsService.checkDirectoryExists(rootDir);
            CraftUtils.throwIfFalseOrNull(rootDirPathExists, "Project root directory does not exist");

            Boolean prevBuildRemoved = fileOpsService.ensurePreviousBuildFilesRemoved(pd); // throws
            CraftUtils.throwIfFalseOrNull(prevBuildRemoved, "Could not remove previous build files");

            this.sendPartialFeedback(feedbackKEvent, requestPayload, null,
                    "[SUCCESS] Checking files and installing done\n [RUNNING] Installing dependencies",
                    ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);

            // install deps

            // this.sendPartialFeedback(feedbackKEvent, requestPayload, null,
            // "[RUNNING] Installing dependencies",
            // ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);
            CommandServiceResult installDepsResult = commandService.freshInstallDeps(pd);
            CraftUtils.throwIfFalseOrNull(installDepsResult.getExitCode() == 0,
                    installDepsResult.getError() != null ? installDepsResult.getError()
                            : "Failed to install dependencies");

            this.sendPartialFeedback(feedbackKEvent, requestPayload, null,
                    "[SUCCESS] Dependencies installed",
                    ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);

            // build project
            if (pd.getDepCommands().getBuild() != null) {
                // only build if build command is provided
                this.sendPartialFeedback(feedbackKEvent, requestPayload, null, "[RUNNING] Building project", null);
                CommandServiceResult buildResult = commandService.freshBuildProject(pd);
                CraftUtils.throwIfFalseOrNull(buildResult.getExitCode() == 0,
                        buildResult.getError() != null ? buildResult.getError() : "Failed to build project");
                this.sendPartialFeedback(feedbackKEvent, requestPayload, null, "[SUCCESS] Project build complete",
                        null);
            }

            if (pd.getDepCommands().getPostInstall() != null) {
                // post install
                this.sendPartialFeedback(feedbackKEvent, requestPayload, null, "[RUNNING] Running post install command",
                        null);
                CommandServiceResult postInstallResult = commandService.postInstallCommands(pd);
                if (postInstallResult.getError() != null) {
                    errorTraceback = postInstallResult.getError();
                }

                CraftUtils.throwIfFalseOrNull(postInstallResult.getExitCode() == 0,
                        postInstallResult.getShortError() != null ? postInstallResult.getShortError()
                                : "Failed to run post install commands");

                this.sendPartialFeedback(feedbackKEvent, requestPayload, null,
                        "[SUCCESS] Post install commands executed",
                        ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);
            }

            //////////// application setup
            this.sendPartialFeedback(feedbackKEvent, requestPayload, null,
                    "[RUNNING] Binding Setting up application process ", null);
            // Find and set port to ecosystem file

            int frekingPort = systemServices.nextFreePort(50000, 60000);
            Boolean portSet = fileOpsService.setPortToProdFiles(pd.getProdFiles(),
                    frekingPort);
            CraftUtils.throwIfFalseOrNull(portSet, "Failed to set port to ecosystem file or nginx conf");

            /////////// Run project using pm2
            CommandServiceResult pm2StartResult = commandService.startPM2(pd.getProdFiles().getEcoSystemFileAbsPath());
            if (pm2StartResult.getError() != null) {
                errorTraceback = pm2StartResult.getError();
            }
            CraftUtils.throwIfFalseOrNull(pm2StartResult.getExitCode() == 0,
                    pm2StartResult.getShortError() != null ? pm2StartResult.getShortError()
                            : "Failed to start project with pm2");

            this.sendPartialFeedback(feedbackKEvent, requestPayload, null,
                    "[SUCCESS] Application process setup complete\n[RUNNING] Reloading nginx",
                    ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);
            // TODO: Setup nginx

            // TODO: Setup ssl

            // finally
            feedbackPayload.setSuccess(true);
            feedbackPayload.setIsPartial(false);
            HashMap<String, Object> m = new HashMap<>();
            m.put("project_id", requestPayload.getProject().getId());
            m.put("deployment_id", requestPayload.getDeployment().getId());
            m.put("dep_status", ProjectDeploymentStatus.DEPLOYMENT_COMPLETED);
            m.put("port_assigned", "" + frekingPort + "");
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
            m.put("error_traceback", errorTraceback);
            feedbackPayload.setData(m);

            String message = e.getMessage();
            if (message == null)
                message = "Someting went wrong";

            feedbackPayload.setMessage(message);

        }

        // for all case we'll send feedback
        messageProducer.sendEvent(feedbackKEvent);

    }

    private void sendPartialFeedback(KEvent feedbackKEvent, KEventDepWizardPayload requestPayload,
            HashMap<String, Object> fullData, String partialMsg,
            ProjectDeploymentStatus partialStatus) {
        KEventFeedbackPayload feedbackPayload = (KEventFeedbackPayload) feedbackKEvent.getPayload();
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

}
