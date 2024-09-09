package fresh.crafts.engine.v1.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.dtos.CreateProjectRequestDto;
import fresh.crafts.engine.v1.models.Project;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import java.util.List;
import fresh.crafts.engine.v1.services.ProjectService;
import fresh.crafts.engine.v1.utils.enums.CommonSortField;

@RestController
@RequestMapping(value = "/api/v1/projects", consumes = "application/json", produces = "application/json")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // get chat history
    @GetMapping("")
    public Page<Project> getChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") CommonSortField orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort) {

        // no search on notifications, cause, that's the plan

        Pageable pageable = PageRequest.of(page, pageSize, sort, orderBy.getFieldName());

        return projectService.getProjectsPaginated(pageable);

    }

    @PostMapping("/init")
    public ResponseEntity<CommonResponseDto> initProject(
            @RequestBody CreateProjectRequestDto createProjectDto) {

        // response dto
        CommonResponseDto res = new CommonResponseDto();

        // create project
        projectService.initProject(res, createProjectDto);

        ResponseEntity<CommonResponseDto> response = ResponseEntity.status(res.getStatusCode()).body(res);
        return response;
    }

    @PostMapping("/deploy/{id}")
    public ResponseEntity<CommonResponseDto> deployProject(
            @PathVariable String id,
            @RequestBody HashMap<String, Object> deployInfo) {
        // return "deployProjectById";

        System.out.println("deployProjectById" + "deploy pathano hocche:" + id);

        CommonResponseDto res = projectService.deployProject(id, deployInfo, false);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PostMapping("/redeploy/{id}")
    public ResponseEntity<CommonResponseDto> reDeployProject(
            @PathVariable String id,
            @RequestBody HashMap<String, Object> deployInfo) {
        System.out.println("reDeployProjectById" + "redeploy pathano hocche:" + id);

        CommonResponseDto res = projectService.deployProject(id, deployInfo, true);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<CommonResponseDto> updateProjectDeployment(
            @PathVariable String id,
            @RequestBody ProjectDeployment currentDeployment) {

        System.out.println("updateProject" + "redeploy pathano hocche:" + id);

        CommonResponseDto res = projectService.updateProjectDeployment(id, currentDeployment);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponseDto> getProjectById(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") Boolean isIdUniqueName) {

        // response dto
        CommonResponseDto res = new CommonResponseDto();

        if (isIdUniqueName) {
            // get project by unique name
            projectService.getProjectByUniqueName(res, id);
        } else {
            // get project by id
            projectService.getProjectById(res, id);
        }

        ResponseEntity<CommonResponseDto> response = ResponseEntity.status(res.getStatusCode()).body(res);
        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponseDto> deleteProjectRequest(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") Boolean force) {
        CommonResponseDto res = new CommonResponseDto();

        // if (force) {
        // projectService.forceDeleteProjectAndDeploymentsIfAny(res, id);
        // } else {
        // projectService.requestProjectDelete(res, id);
        // }

        projectService.requestProjectDelete(res, id);

        ResponseEntity<CommonResponseDto> response = ResponseEntity.status(res.getStatusCode()).body(res);
        return response;
    }

    @GetMapping("/{id}/deployments")
    public ResponseEntity<CommonResponseDto> getDeploymentsByProjectId(
            @PathVariable String id) {
        System.out.println("getDeploymentsByProjectId" + "deployments pathano hocche");
        // response dto
        CommonResponseDto res = new CommonResponseDto();

        // get project by id
        List<ProjectDeployment> deps = projectService.getDeployments(id);
        res.setPayload(
                new HashMap<>() {
                    {
                        put("total", deps.size());
                    }
                });
        res.setPayload2(deps);
        ResponseEntity<CommonResponseDto> response = ResponseEntity.status(res.getStatusCode()).body(res);
        return response;
    }

    @DeleteMapping("/incomplete/{id}")
    public ResponseEntity<CommonResponseDto> deleteIncompleteProjectById(@PathVariable String id) {

        // response dto
        CommonResponseDto res = projectService.deleteIncompleteProject(id);

        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @GetMapping("/deployments/{id}")
    public ResponseEntity<CommonResponseDto> getProjectDeployment(@PathVariable String id) {
        // return "getProjectDeploymentById";
        CommonResponseDto res = projectService.getProjectDeploymentById(id);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PatchMapping("/deployments/{id}")
    public ResponseEntity<CommonResponseDto> updatePartialProjectDeployment(
            @PathVariable String id,
            @RequestBody ProjectDeployment pd) {
        CommonResponseDto res = projectService.updatePartialProjectDeployment(id, pd);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

}
