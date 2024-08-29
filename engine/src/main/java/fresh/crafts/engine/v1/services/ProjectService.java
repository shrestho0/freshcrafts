package fresh.crafts.engine.v1.services;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fresh.crafts.engine.v1.config.MessageProducer;
import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.dtos.CreateProjectRequestDto;
import fresh.crafts.engine.v1.entities.DepWizKEventPayload;
import fresh.crafts.engine.v1.entities.GithubRepoDetailed;
import fresh.crafts.engine.v1.entities.KEventPayloadInterface;
import fresh.crafts.engine.v1.entities.ProjectGithubRepo;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.repositories.ProjectDeploymentRepository;
import fresh.crafts.engine.v1.repositories.ProjectRepository;
import fresh.crafts.engine.v1.utils.CraftUtils;
import fresh.crafts.engine.v1.utils.enums.DepWizKEventCommands;
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
        newProject.setStatus(ProjectStatus.PROCESSING_SETUP);
        newProject.setType(createProjectDto.getType());

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

            newProject.setGithubRepo(newGithubRepo);
        }

        // create first version of project_deployment
        ProjectDeployment newDeployment = new ProjectDeployment();
        newDeployment.setVersion(1);
        newDeployment.setStatus(ProjectDeploymentStatus.PRE_CREATION);
        newDeployment.setIsDeployed(false);
        newDeployment.setProject(newProject);

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
            res.setPayload2(projectDeploymentService.getProjectDeploymentById(p.get().getActiveDeploymentId(), false)); // get
                                                                                                                        // active
            // deployment
            res.setPayload3(projectDeploymentService.getProjectDeploymentById(p.get().getCurrentDeploymentId(), false)); // get
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
            res.setPayload2(projectDeploymentService.getProjectDeploymentById(p.get().getActiveDeploymentId(), false)); // get
                                                                                                                        // active
            // deployment
            res.setPayload3(projectDeploymentService.getProjectDeploymentById(p.get().getCurrentDeploymentId(), false)); // get
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
        ProjectDeployment pd = projectDeploymentService.getProjectDeploymentById(id, true);
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

    public ProjectDeployment getProjectDeploymentById2(String id) {
        return projectDeploymentService.getProjectDeploymentById(id, true);
    }

    public CommonResponseDto updatePartialProjectDeployment(String id, ProjectDeployment pd) {
        CommonResponseDto res = new CommonResponseDto();

        try {
            ProjectDeployment existingPd = projectDeploymentService.getProjectDeploymentById(id, true);

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

    public CommonResponseDto deployProject(String id, HashMap<String, Object> deployInfo) {

        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();

        System.out.println("\n\nDeployInfo: " + id);
        System.out.println(gson.toJson(deployInfo));
        System.out.println("\n\n");

        CommonResponseDto res = new CommonResponseDto();

        try {
            Project p = projectRepository.findById(id).get();
            ProjectDeployment pd = projectDeploymentService.getProjectDeploymentById(p.getCurrentDeploymentId(), false);

            // if not p.getCurrentDeploymentId() or pd.getStatus() != READY_FOR_DEPLOYMENT,
            // return with success false

            HashMap<String, Object> requiredFields = new HashMap<>();
            requiredFields.put("domain", deployInfo.get("domain"));
            requiredFields.put("uniqueName", deployInfo.get("uniqueName"));

            CraftUtils.throwIfRequiredValuesAreNull(requiredFields);

            if (deployInfo.get("domain") != null)
                p.setDomain(deployInfo.get("domain").toString());

            if (deployInfo.get("uniqueName") != null)
                p.setUniqueName(deployInfo.get("uniqueName").toString());

            // port will be added by dep_wiz
            p.setStatus(ProjectStatus.PROCESSING_DEPLOYMENT);
            pd.setStatus(ProjectDeploymentStatus.REQUESTED_DEPLOYMENT);
            pd.setPartialDeploymentMsg("");
            pd.setErrorTraceback("");

            // update p && pd
            projectRepository.save(p);
            projectDeploymentService.save(pd);

            // send to dep wiz
            KEvent kevent = generateCommonKEvent();
            DepWizKEventPayload payload = new DepWizKEventPayload();
            payload.setCommand(DepWizKEventCommands.DEPLOY);
            payload.setDeployment(projectDeploymentService.getProjectDeploymentById(p.getCurrentDeploymentId(), false));
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
        kevent.setEventDestination(KEventProducers.DEP_WIZ);
        return kevent;
    }

}
