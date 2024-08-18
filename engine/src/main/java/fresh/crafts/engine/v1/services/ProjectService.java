package fresh.crafts.engine.v1.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.dtos.CreateProjectRequestDto;
import fresh.crafts.engine.v1.entities.GithubRepoDetailed;
import fresh.crafts.engine.v1.entities.ProjectGithubRepo;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.repositories.ProjectRepository;
import fresh.crafts.engine.v1.utils.enums.ProjectStatus;
import fresh.crafts.engine.v1.utils.enums.ProjectType;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    /// Create a project
    public CommonResponseDto createProject(CommonResponseDto res, CreateProjectRequestDto createProjectDto) {

        // create project
        System.out.println("Creating project...");
        res.setMessage("hobe hobe");
        System.out.println("Create Project Info: " + createProjectDto.toString());

        GithubRepoDetailed githubRepo = createProjectDto.getGithub_repo();
        // create
        Project newProject = new Project();
        newProject.setStatus(ProjectStatus.PROSESSING_SETUP);
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

        // save
        try {

            Project p = projectRepository.save(newProject);
            res.setPayload(p);
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
            res.setSuccess(true);
            res.setStatusCode(200);
            return;
        }
        res.setSuccess(false);
        res.setStatusCode(404);
        res.setMessage("Project not found with unique name: " + id);
    }
}
