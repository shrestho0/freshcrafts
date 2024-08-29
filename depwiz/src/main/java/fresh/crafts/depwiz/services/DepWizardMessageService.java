package fresh.crafts.depwiz.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.depwiz.config.MessageProducer;
import fresh.crafts.depwiz.entities.CommandServiceResult;
import fresh.crafts.depwiz.entities.KEventFeedbackPayload;
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

        try {
            // Validations
            // check
            HashMap<String, Object> notNullables = new HashMap<>() {
                {
                    put("project", requestPayload.getProject());
                    put("deployment", requestPayload.getDeployment());
                    put("deployment.src", requestPayload.getDeployment().getSrc());
                    put("deployment.depCommands", requestPayload.getDeployment().getDepCommands());
                    put("deployment.depCommands.build",
                            requestPayload.getDeployment().getDepCommands().getBuild());
                }
            };

            CraftUtils.throwIfRequiredValuesAreNull(notNullables);

            // build
            // check if projectRoot exists

            this.sendPartialFeedback(feedbackKEvent, requestPayload, null, "Checking files and installing dependencies",
                    ProjectDeploymentStatus.DEPLOYMENT_BEING_PROCESSED);

            String rootDir = requestPayload.getDeployment().getSrc().getRootDirAbsPath();
            Boolean rootDirPathExists = fileOpsService.checkDirectoryExists(rootDir);
            CraftUtils.throwIfFalseOrNull(rootDirPathExists, "Project root directory does not exist");

            Boolean prevBuildRemoved = fileOpsService.ensurePreviousBuildFilesRemoved(requestPayload.getDeployment()); // throws
            CraftUtils.throwIfFalseOrNull(prevBuildRemoved, "Error: Could not remove previous build files");

            // install deps

            CommandServiceResult installDepsResult = commandService.freshInstallDeps(requestPayload.getDeployment());
            CraftUtils.throwIfFalseOrNull(installDepsResult.getExitCode() == 0,
                    installDepsResult.getError() != null ? installDepsResult.getError()
                            : "Failed to install dependencies");

            // build project
            if (requestPayload.getDeployment().getDepCommands().getBuild() != null) {
                // only build if build command is provided
                this.sendPartialFeedback(feedbackKEvent, requestPayload, null, "Building project", null);
                CommandServiceResult buildResult = commandService.freshBuildProject(requestPayload.getDeployment());
                CraftUtils.throwIfFalseOrNull(buildResult.getExitCode() == 0,
                        buildResult.getError() != null ? buildResult.getError() : "Failed to build project");
            }

            // ecosystem file

            this.sendPartialFeedback(feedbackKEvent, requestPayload, null, "generating ecosystem file ", null);
            // do something

        } catch (Exception e) {
            // e.printStackTrace();
            feedbackPayload.setSuccess(false);

            HashMap<String, Object> m = new HashMap<>();
            m.put("dep_status", ProjectDeploymentStatus.DEPLOYMENT_FAILED);
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
