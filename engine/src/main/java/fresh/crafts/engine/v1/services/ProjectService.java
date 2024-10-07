package fresh.crafts.engine.v1.services;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fresh.crafts.engine.v1.config.MessageProducer;
import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.dtos.CreateProjectRequestDto;
import fresh.crafts.engine.v1.entities.KEventPayloadDepWiz;
import fresh.crafts.engine.v1.entities.GithubRepoDetailed;
import fresh.crafts.engine.v1.entities.ProjectGithubRepo;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.repositories.ProjectRepository;
import fresh.crafts.engine.v1.utils.CraftUtils;
import fresh.crafts.engine.v1.utils.UlidGenerator;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsDepWiz;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;
import fresh.crafts.engine.v1.utils.enums.ProjectDeploymentStatus;
import fresh.crafts.engine.v1.utils.enums.ProjectStatus;
import fresh.crafts.engine.v1.utils.enums.ProjectType;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectDeploymentService projectDeploymentService;

    @Autowired
    private MessageProducer messageProducer;
    @Autowired
    private KEventService kEventService;

    /// Create a project
    public CommonResponseDto initProject(CommonResponseDto res, CreateProjectRequestDto createProjectDto) {

        // create project
        System.out.println("Creating project...");
        res.setMessage("hobe hobe");
        System.out.println("Create Project Info: " + createProjectDto.toString());

        GithubRepoDetailed githubRepo = createProjectDto.getGithub_repo();
        // create
        Project newProject = new Project();
        if (createProjectDto.getNewProjectId() == null) {
            res.setSuccess(false);
            res.setStatusCode(400);
            res.setMessage("newProjectId can not be null");
            return res;
        }
        newProject.setId(createProjectDto.getNewProjectId());
        newProject.setStatus(ProjectStatus.AWAIT_INITIAL_SETUP);
        newProject.setType(createProjectDto.getType());
        newProject.setTotalVersions(1); // for first dep

        newProject.setProjectDir(createProjectDto.getProjectDir());

        if (createProjectDto.getType() == ProjectType.GITHUB_REPO) {

            ProjectGithubRepo newGithubRepo = new ProjectGithubRepo();

            newGithubRepo.setId(githubRepo.getId());
            newGithubRepo.setName(githubRepo.getName());
            newGithubRepo.setFullName(githubRepo.getFull_name());
            newGithubRepo.setIsPrivate(githubRepo.getIs_private());
            newGithubRepo.setDownloadsUrl(githubRepo.getDownloads_url());
            newGithubRepo.setDeploymentsUrl(githubRepo.getDeployments_url());
            newGithubRepo.setDefaultBranch(githubRepo.getDefault_branch());
            newGithubRepo.setTarDownloadUrl(createProjectDto.getGithub_tar_download_url());
            newGithubRepo.setDefault_branch_commit_date(githubRepo.getDefault_branch_commit_date());
            newGithubRepo.setDefault_branch_commit_sha(githubRepo.getDefault_branch_commit_sha());
            newGithubRepo.setOwner_login(githubRepo.getOwner_login());
            newGithubRepo.setOwner_id(githubRepo.getOwner_id());
            newProject.setGithubRepo(newGithubRepo);
        }

        // create first version of project_deployment
        ProjectDeployment newDeployment = new ProjectDeployment();
        newDeployment.setVersion(1);
        newDeployment.setIteration(0); // first iteration
        newDeployment.setStatus(ProjectDeploymentStatus.PRE_CREATION);
        newDeployment.setIsDeployed(false);
        // newDeployment.setProject(newProject);
        newDeployment.setProjectId(newProject.getId());

        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
        System.out.println("\n\nCreateProjectDto: ");
        System.out.println(gson.toJson(createProjectDto));
        System.out.println("\nNew Deployment Info: ");
        System.out.println(gson.toJson(newDeployment));
        System.out.println("\nNew Project Info: ");
        System.out.println(gson.toJson(newProject));
        System.out.println("\n\n");

        if (createProjectDto.getFile() != null)
            newDeployment.setRawFile(createProjectDto.getFile());

        if (createProjectDto.getSrc() != null)
            newDeployment.setSrc(createProjectDto.getSrc());

        // newDeployment.setSrc(createProjectDto.getSrc());

        // TODO: srcFile will come from next request
        // TODO: envFile will come from next request

        newProject.setActiveDeploymentId(null); // null for new project
        newProject.setCurrentDeploymentId(newDeployment.getId()); // while deployment is in progress

        try {

            ProjectDeployment pd = projectDeploymentService.save(newDeployment);
            Project p = projectRepository.save(newProject);

            res.setPayload(p);
            res.setPayload2(pd);
            res.setSuccess(true);
            res.setStatusCode(201);
        } catch (Exception e) {
            res.setMessage("Error: " + e.getMessage());
            res.setSuccess(false);
        }

        return res;
    }

    /////////////// getProjectById ///////////////////////
    public CommonResponseDto getProjectById(CommonResponseDto res, String id) {
        Optional<Project> p = projectRepository.findById(id);
        if (p.isPresent()) {
            res.setPayload(p.get());
            res.setSuccess(true);
            res.setStatusCode(200);
            res.setPayload2(projectDeploymentService.getProjectDeploymentById(p.get().getActiveDeploymentId())); // get
                                                                                                                 // active
            // deployment
            res.setPayload3(projectDeploymentService.getProjectDeploymentById(p.get().getCurrentDeploymentId())); // get
                                                                                                                  // current
            // deployment
            return res;
        }
        res.setSuccess(false);
        res.setStatusCode(404);
        res.setMessage("Project not found with id: " + id);
        return res;
    }

    // The project
    public Project getProjectById(String id) {
        Optional<Project> p = projectRepository.findById(id);
        if (p.isPresent()) {
            return p.get();
        }
        return null;
    }

    public void getProjectByUniqueName(CommonResponseDto res, String id) {
        Optional<Project> p = projectRepository.findByUniqueName(id);
        if (p.isPresent()) {
            res.setPayload(p.get());
            res.setPayload2(projectDeploymentService.getProjectDeploymentById(p.get().getActiveDeploymentId())); // get
                                                                                                                 // active
            // deployment
            res.setPayload3(projectDeploymentService.getProjectDeploymentById(p.get().getCurrentDeploymentId())); // get
                                                                                                                  // current
            // deployment
            res.setMessage(
                    "[DEBUG]: Project found. payload: Project, payload2: Current Deployment, payload3: Active Deployment");
            res.setSuccess(true);
            res.setStatusCode(200);
            return;
        }
        res.setSuccess(false);
        res.setStatusCode(404);
        res.setMessage("Project not found with unique name: " + id);
    }

    public CommonResponseDto deleteIncompleteProject(String id) {

        // check stuff and delete
        CommonResponseDto res = new CommonResponseDto();

        Optional<Project> p = projectRepository.findById(id);
        if (p.isPresent()) {
            // incomplete project have currentDeploymentId set
            String currentDeploymentId = p.get().getCurrentDeploymentId();// as incomplete

            // FIXME: Validation required
            // TODO: Check if request is valid for this project and return accordingly

            // delete current deployment
            projectDeploymentService.delete(currentDeploymentId);
            projectRepository.deleteById(id);

            res.setSuccess(true);
            res.setStatusCode(200);
            res.setMessage("Project deleted successfully");
        } else {
            res.setSuccess(false);
            res.setStatusCode(404);
            res.setMessage("Project not found with id: " + id);
        }

        return res;

    }

    /////////////////////// getProjectDeploymentById ///////////////////////
    public CommonResponseDto getProjectDeploymentById(String id) {
        CommonResponseDto res = new CommonResponseDto();
        ProjectDeployment pd = projectDeploymentService.getProjectDeploymentById(id);
        if (pd != null) {
            res.setPayload(pd);
            res.setSuccess(true);
            res.setStatusCode(200);
            return res;
        }
        res.setSuccess(false);
        res.setStatusCode(404);
        res.setMessage("Project Deployment not found with id: " + id);
        return res;
    }

    public ProjectDeployment getDeploymentById(String id) {
        return projectDeploymentService.getProjectDeploymentById(id);
    }

    public CommonResponseDto updatePartialProjectDeployment(String id, ProjectDeployment pd) {
        CommonResponseDto res = new CommonResponseDto();

        try {
            ProjectDeployment existingPd = projectDeploymentService.getProjectDeploymentById(id);

            if (existingPd == null) {
                res.setSuccess(false);
                res.setStatusCode(404);
                res.setMessage("Project Deployment not found with id: " + id);
                return res;
            }

            // if(pd.getProject() != null ) // won't update externally
            if (pd.getVersion() != null)
                existingPd.setVersion(pd.getVersion());

            if (pd.getStatus() != null)
                existingPd.setStatus(pd.getStatus());

            if (pd.getIsDeployed() != null)
                existingPd.setIsDeployed(pd.getIsDeployed());

            if (pd.getRawFile() != null)
                existingPd.setRawFile(pd.getRawFile());
            if (pd.getEnvFile() != null)
                existingPd.setEnvFile(pd.getEnvFile());
            if (pd.getDepCommands() != null)
                existingPd.setDepCommands(pd.getDepCommands());
            if (pd.getSrc() != null)
                existingPd.setSrc(pd.getSrc());

            if (pd.getProdFiles() != null)
                existingPd.setProdFiles(pd.getProdFiles());

            ProjectDeployment updatedPd = projectDeploymentService.save(existingPd);
            res.setPayload(updatedPd);
            res.setSuccess(true);
            res.setStatusCode(200);
            return res;

        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(500);
            res.setMessage("Error: " + e.getMessage());
            return res;
        }
    }

    public ProjectDeployment updateProjectDeployment(ProjectDeployment pd) {
        return projectDeploymentService.save(pd);
    }

    public Project updateProject(Project p) {
        return projectRepository.save(p);
    }

    public CommonResponseDto deployProject(String id, HashMap<String, Object> deployInfo, boolean isReDeploy) {

        System.out.println("\n\nDeployInfo: " + id + " " + "First Deploy");
        CraftUtils.jsonLikePrint(deployInfo);
        System.out.println("\n\n");

        CommonResponseDto res = new CommonResponseDto();

        try {
            Project p = projectRepository.findById(id).get();
            ProjectDeployment pd = projectDeploymentService.getProjectDeploymentById(p.getCurrentDeploymentId());

            // if not p.getCurrentDeploymentId() or pd.getStatus() != READY_FOR_DEPLOYMENT,
            // return with success false

            HashMap<String, Object> requiredFields = new HashMap<>();
            requiredFields.put("domain", deployInfo.get("domain"));
            requiredFields.put("uniqueName", deployInfo.get("uniqueName"));
            requiredFields.put("ssl", deployInfo.get("ssl"));

            CraftUtils.throwIfRequiredValuesAreNull(requiredFields);

            p.setDomain(deployInfo.get("domain").toString());
            p.setUniqueName(deployInfo.get("uniqueName").toString());
            p.setSsl((Boolean) deployInfo.get("ssl"));

            // message list should be empty at first
            p.setPartialMessageList(new ArrayList<String>());
            // port will be added by dep_wiz

            if (isReDeploy) {
                p.setStatus(ProjectStatus.PROCESSING_REDEPLOYMENT);
                pd.setStatus(ProjectDeploymentStatus.REQUESTED_REDEPLOYMENT);
            } else {
                p.setStatus(ProjectStatus.PROCESSING_DEPLOYMENT);
                pd.setStatus(ProjectDeploymentStatus.REQUESTED_DEPLOYMENT);

            }
            // pd.setPartialDeploymentMsg("");

            pd.setErrorTraceback("");

            // update p && pd
            projectRepository.save(p);
            projectDeploymentService.save(pd);

            // send to dep wiz
            KEvent kevent = generateCommonKEvent();
            KEventPayloadDepWiz payload = new KEventPayloadDepWiz();

            if (isReDeploy) {
                payload.setCommand(KEventCommandsDepWiz.RE_DEPLOY);
            } else {
                payload.setCommand(KEventCommandsDepWiz.DEPLOY);
            }

            payload.setCurrentDeployment(projectDeploymentService.getProjectDeploymentById(p.getCurrentDeploymentId()));
            payload.setProject(p);
            kevent.setPayload(payload);

            res.setPayload(p);
            res.setSuccess(true);
            res.setStatusCode(202); // Processing
            res.setMessage("Deployment request is being processed");

            // save or update event
            kEventService.createOrUpdate(kevent);
            // send to dep_wiz
            messageProducer.sendEvent(kevent);

            return res;

        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(400);
            res.setMessage("Error: " + e.getMessage());
            return res;
        }

    }

    private KEvent generateCommonKEvent() {
        KEvent kevent = new KEvent();
        kevent.setEventSource(KEventProducers.ENGINE);
        kevent.setEventDestination(KEventProducers.DEPWIZ);
        return kevent;
    }

    public Page<Project> getProjectsPaginated(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    /**
     * Delete a project entirely
     * Also deletes corresponding project_deployment
     * 
     * @param respones
     * @param id
     */

    // public CommonResponseDto
    // forceDeleteProjectAndDeploymentsIfAny(CommonResponseDto response, String
    // projectId) {

    // if (projectId == null) {
    // response.setSuccess(false);
    // response.setStatusCode(400);
    // response.setMessage("Project id can not be null");
    // return response;
    // }

    // Optional<Project> proj = projectRepository.findById(projectId);
    // List<ProjectDeployment> projDeployments =
    // projectDeploymentService.getDeploymentsByProjectId(projectId);

    // // delete all deployments if any
    // if (projDeployments != null && projDeployments.size() > 0) {
    // projectDeploymentService.deleteAllByProjectId(projectId);
    // }

    // // delete project
    // if (proj.isPresent()) {
    // projectRepository.deleteById(projectId);
    // }

    // // all okay
    // response.setSuccess(true);
    // response.setStatusCode(200);

    // return response;

    // }

    public CommonResponseDto requestProjectDelete(CommonResponseDto respones, String id) {
        // list deployments for this project
        // delete all
        // then delete the project

        // if (p.isPresent()) {
        // Optional<Project> p = projectRepository.findById(id);
        // // delete all deployments [do it on depwiz event]
        // // projectDeploymentService.deleteAllProjectDeployments(p.get().getId());
        // // delete project [do it on depwiz event]
        // // projectRepository.deleteById(id);

        // // send to dep wiz
        // KEvent kevent = generateCommonKEvent();
        // DepWizKEventPayload payload = new DepWizKEventPayload();
        // payload.setCommand(KEventCommandsDepWiz.DELETE_DEPLOYMENT);

        // List<ProjectDeployment> deployments =
        // projectDeploymentService.getDeploymentsByProjectId(id);
        // if(deployments == null || deployments.size() == 0){
        // throw
        // }

        // respones.setSuccess(true);
        // respones.setStatusCode(200);
        // respones.setMessage("Project deleted successfully");
        // } else {
        // respones.setSuccess(false);
        // respones.setStatusCode(404);
        // respones.setMessage("Project not found with id: " + id);
        // }

        try {
            Optional<Project> p = projectRepository.findById(id);
            CraftUtils.throwIfFalseOrNull(p.isPresent(), "Project not found with id: " + id);

            List<ProjectDeployment> deployments = projectDeploymentService.getDeploymentsByProjectId(id);

            // it's ok if no deployments found
            // files must be deleted
            // CraftUtils.throwIfFalseOrNull(deployments != null & deployments.size() > 0,
            // "No deployments found for project with id: " + id);

            // send to dep wiz
            KEvent kevent = generateCommonKEvent();
            KEventPayloadDepWiz payload = new KEventPayloadDepWiz();

            payload.setCommand(KEventCommandsDepWiz.DELETE_DEPLOYMENTS);
            payload.setProject(p.get());
            payload.setDeploymentList(deployments);
            kevent.setPayload(payload);

            // set project status
            p.get().setStatus(ProjectStatus.PROCESSING_DELETION);
            // removing old messages
            p.get().setPartialMessageList(new ArrayList<String>());
            projectRepository.save(p.get());
            // setting deployments status doesn't matter
            // as we'll handle those from dep_wiz feedback

            respones.setSuccess(true);
            respones.setStatusCode(202); // accepted and processing
            respones.setMessage("Project deletion request is being proceed");

            // save or update event
            kEventService.createOrUpdate(kevent);
            // send to dep_wiz
            messageProducer.sendEvent(kevent);

        } catch (Exception e) {
            respones.setSuccess(false);
            // respones.setStatusCode(500);//400 is ok for all sorta errors
            respones.setMessage("Error: " + e.getMessage());
        }

        return respones;
    }

    public List<ProjectDeployment> getDeployments(String id) {
        return projectDeploymentService.getDeploymentsByProjectId(id);
    }

    public void deleteProject(String id) {
        projectRepository.deleteById(id);
    }

    public void deleteProjectAndDeployments(String id) throws Exception {
        try {
            projectRepository.deleteById(id);
        } catch (Exception e) {
            System.out.println("Error deleting project with id: " + id);
            throw e;
        }

        try {
            projectDeploymentService.deleteAllByProjectId(id);
        } catch (Exception e) {
            System.out.println("Error deleting deployments for project with id: " + id);
            throw e;
        }

    }

    public CommonResponseDto updateProjectDeployment(String id, ProjectDeployment currentDeployment) {
        CommonResponseDto res = new CommonResponseDto();

        System.out.println("updateProject" + "pathano hocche:" + id);
        CraftUtils.jsonLikePrint(currentDeployment);

        try {

            Project p = projectRepository.findById(id).get();

            // create new deployment currentDeployment
            currentDeployment.setId(UlidGenerator.generate());
            currentDeployment.setProjectId(p.getId());

            p.setCurrentDeploymentId(currentDeployment.getId());
            p.setStatus(ProjectStatus.PROCESSING_UPDATE);
            p.setPartialMessageList(new ArrayList<String>());

            currentDeployment.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);

            projectDeploymentService.save(currentDeployment);
            projectRepository.save(p);

            KEvent kevent = generateCommonKEvent();
            KEventPayloadDepWiz payload = new KEventPayloadDepWiz();

            payload.setCommand(KEventCommandsDepWiz.UPDATE_DEPLOYMENT);
            payload.setProject(p);
            payload.setCurrentDeployment(currentDeployment);
            payload.setActiveDeployment(projectDeploymentService.getProjectDeploymentById(p.getActiveDeploymentId()));

            kevent.setPayload(payload);
            kEventService.createOrUpdate(kevent);
            messageProducer.sendEvent(kevent);

            res.setSuccess(true);
            res.setStatusCode(202);
            res.setMessage("Deployment update request is being processed");

        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(400);
            res.setMessage("Error: " + e.getMessage());
        }

        return res;

    }

    public void createProjectDeployment(CommonResponseDto res, String projId, ProjectDeployment pd) {

        // create new deployment
        try {

            pd.setId(UlidGenerator.generate());
            pd.setProjectId(projId);
            pd.setStatus(ProjectDeploymentStatus.PRE_CREATION);
            pd.setIsDeployed(false);

            projectDeploymentService.save(pd);

            res.setSuccess(true);
            res.setStatusCode(201);
            res.setMessage("Project Deployment created successfully");
            res.setPayload(pd);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(400);
            res.setMessage("Error: " + e.getMessage());
        }

    }

    public CommonResponseDto rollforwardProject(String projId, HashMap<String, Object> deployInfo,
            Boolean preprocessing) {

        CommonResponseDto res = new CommonResponseDto();

        System.out
                .println("\n\nRollforwardInfo: " + projId + " " + "First Deploy" + " Preprocessing: " + preprocessing);
        CraftUtils.jsonLikePrint(deployInfo);
        System.out.println("\n\n");

        try {

            Gson gson = new Gson();
            // get project
            Project p = projectRepository.findById(projId).get();

            if (preprocessing) {
                ProjectDeployment npd = gson.fromJson(gson.toJson(deployInfo), ProjectDeployment.class);
                npd.setIsDeployed(false);
                npd.setErrorTraceback("");
                // proj dep from deployInfo
                npd.setProjectId(projId);
                // npd.setVersion(1);
                npd.setIteration(0);
                npd.setStatus(ProjectDeploymentStatus.PRE_CREATION);

                System.out.println("New Project Deployment Info: ");
                CraftUtils.jsonLikePrint(npd);
                System.out.println("\n\n");

                p.setRollforwardDeploymentId(npd.getId());

                p.setCurrentDeploymentId(npd.getId());

                p.setTotalVersions(p.getTotalVersions() + 1);
                npd.setVersion(p.getTotalVersions()); // next version

                // save project
                projectRepository.save(p);
                // save project deployment
                projectDeploymentService.save(npd);

                res.setSuccess(true);
                res.setStatusCode(201);
                res.setMessage("Rollforward pre-processing completed");

            } else {
                ProjectDeployment npdPartial = gson.fromJson(gson.toJson(deployInfo), ProjectDeployment.class);
                ProjectDeployment currDeployment = projectDeploymentService
                        .getProjectDeploymentById(p.getCurrentDeploymentId());

                p.setPartialMessageList(new ArrayList<String>());

                currDeployment.setSrc(npdPartial.getSrc());
                // p.setTotalVersions(p.getTotalVersions() + 1); // next version
                currDeployment.setEnvFile(npdPartial.getEnvFile());
                currDeployment.setDepCommands(npdPartial.getDepCommands());
                currDeployment.setProdFiles(npdPartial.getProdFiles());

                p.setStatus(ProjectStatus.PROCESSING_ROLLFORWARD);
                // p.setRollforwardDeploymentId(null); // remove on success
                currDeployment.setIteration(1);
                currDeployment.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);

                // save project
                projectRepository.save(p);
                // save project deployment
                projectDeploymentService.save(currDeployment);

                // The request

                KEvent kevent = generateCommonKEvent();
                KEventPayloadDepWiz payload = new KEventPayloadDepWiz();

                payload.setCommand(KEventCommandsDepWiz.ROLLFORWARD);
                payload.setProject(p);
                payload.setCurrentDeployment(currDeployment);
                // don't delete source file for rollforward, just do undeploy
                payload.setActiveDeployment(
                        projectDeploymentService.getProjectDeploymentById(p.getActiveDeploymentId()));

                kevent.setPayload(payload);
                kEventService.createOrUpdate(kevent);
                messageProducer.sendEvent(kevent);

                res.setSuccess(true);
                res.setStatusCode(202);
                res.setMessage("Rollforward request is being processed");

            }

        } catch (Exception e) {
            res.setSuccess(false);
            res.setStatusCode(400);
            res.setMessage("Error: " + e.getMessage());
        }

        return res;

    }

    public CommonResponseDto rollbackProject(String projId, HashMap<String, Object> rollbackInfo) {
        CommonResponseDto res = new CommonResponseDto();

        // throw if there is a currentDeployment id for some reason

        try {
            // project er status gonna change
            String rollbackId = (String) rollbackInfo.get("rollbackId");
            if (rollbackId == null) {
                throw new Exception("Rollback id not provided");
            }

            Project proj = getProjectById(projId);
            ProjectDeployment currentDeployment = projectDeploymentService.getProjectDeploymentById(rollbackId);
            ProjectDeployment activeDeployment = projectDeploymentService
                    .getProjectDeploymentById(proj.getActiveDeploymentId());

            System.out.println("Proj to -----> Rollback");

            CraftUtils.jsonLikePrint(proj);
            System.out.println("Active Deployment to -----> Rollback");
            CraftUtils.jsonLikePrint(activeDeployment);
            System.out.println("currentDeployment Deployment to -----> Rollback");
            CraftUtils.jsonLikePrint(currentDeployment);

            proj.setStatus(ProjectStatus.PROCESSING_ROLLBACK);
            currentDeployment.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);
            System.out.println("[DEBUG] Rollback Request: status set");
            if (activeDeployment != null) {
                activeDeployment.setStatus(ProjectDeploymentStatus.READY_FOR_DEPLOYMENT);
                System.out.println("[DEBUG] Rollback Request: activeDeployment status set");
            }

            // remove partial messages
            proj.getPartialMessageList().clear();
            System.out.println("[DEBUG] Rollback Request: partial messages cleared");

            proj.setRollbackDeploymentId(currentDeployment.getId());
            System.out.println("[DEBUG] Rollback Request: rollbackDeploymentId set");

            projectRepository.save(proj);
            projectDeploymentService.save(currentDeployment);
            if (activeDeployment != null) {
                projectDeploymentService.save(activeDeployment);
            }

            System.out.println("[DEBUG] Rollback Request: stuff saved");
            // f... send it

            KEvent kevent = generateCommonKEvent();
            KEventPayloadDepWiz payload = new KEventPayloadDepWiz();

            payload.setCommand(KEventCommandsDepWiz.ROLLBACK);
            payload.setProject(proj);
            payload.setCurrentDeployment(currentDeployment);
            System.out.println("[DEBUG] Rollback Request: currentDeployment set in payload");

            // it's ok if activeDeployment is null
            payload.setActiveDeployment(activeDeployment);
            System.out.println("[DEBUG] Rollback Request: activeDeployment set in payload");

            kevent.setPayload(payload);
            System.out.println("[DEBUG] Rollback Request: payload set");
            kEventService.createOrUpdate(kevent);
            System.out.println("[DEBUG] Rollback Request: kevent created or updated");

            messageProducer.sendEvent(kevent);

            System.out.println("[DEBUG] Rollback Request: kevent send");

            res.setSuccess(true);
            res.setStatusCode(202);
            res.setMessage("Rollforward request is being processed");

            System.out.println("[DEBUG] Rollback Request: success");

        } catch (Exception e) {
            System.out.println("[DEBUG] Rollback Request: failure");
            res.setMessage("Error: " + e.getMessage());

            res.setSuccess(false);
            res.setStatusCode(400); // shob dosh client er
        }

        return res;
    }

}
