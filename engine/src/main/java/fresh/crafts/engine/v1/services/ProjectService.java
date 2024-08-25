package fresh.crafts.engine.v1.services;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.dtos.CreateProjectRequestDto;
import fresh.crafts.engine.v1.entities.GithubRepoDetailed;
import fresh.crafts.engine.v1.entities.ProjectGithubRepo;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.repositories.ProjectDeploymentRepository;
import fresh.crafts.engine.v1.repositories.ProjectRepository;
import fresh.crafts.engine.v1.utils.enums.ProjectDeploymentStatus;
import fresh.crafts.engine.v1.utils.enums.ProjectStatus;
import fresh.crafts.engine.v1.utils.enums.ProjectType;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectDeploymentRepository projectDeploymentRepository;

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

            ProjectDeployment pd = projectDeploymentRepository.save(newDeployment);
            Project p = projectRepository.save(newProject);

            res.setPayload(p);
            res.setPayload2(newDeployment);
            res.setSuccess(true);
            res.setStatusCode(201);
        } catch (Exception e) {
            res.setMessage("Error: " + e.getMessage());
            res.setSuccess(false);
        }

        return res;
    }

    public CommonResponseDto getProjectById(CommonResponseDto res, String id) {
        Optional<Project> p = projectRepository.findById(id);
        if (p.isPresent()) {
            res.setPayload(p.get());
            res.setSuccess(true);
            res.setStatusCode(200);
            res.setPayload2(this.getProjectDeploymentById(p.get().getActiveDeploymentId(), false)); // get active
                                                                                                    // deployment
            res.setPayload3(this.getProjectDeploymentById(p.get().getCurrentDeploymentId(), false)); // get current
                                                                                                     // deployment
            return res;
        }
        res.setSuccess(false);
        res.setStatusCode(404);
        res.setMessage("Project not found with id: " + id);
        return res;
    }

    public void getProjectByUniqueName(CommonResponseDto res, String id) {
        Optional<Project> p = projectRepository.findByUniqueName(id);
        if (p.isPresent()) {
            res.setPayload(p.get());
            res.setPayload2(this.getProjectDeploymentById(p.get().getActiveDeploymentId(), false)); // get active
                                                                                                    // deployment
            res.setPayload3(this.getProjectDeploymentById(p.get().getCurrentDeploymentId(), false)); // get current
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

    public ProjectDeployment getProjectDeploymentById(String id, Boolean includeProject) {
        if (id == null)
            return null;

        Optional<ProjectDeployment> pd = projectDeploymentRepository.findById(id);
        if (pd.isPresent()) {
            if (!includeProject) {
                pd.get().setProject(null);
            }
            return pd.get();
        }
        return null;
    }
}
