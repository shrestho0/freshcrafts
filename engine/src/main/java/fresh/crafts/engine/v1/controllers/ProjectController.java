package fresh.crafts.engine.v1.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.dtos.CreateProjectRequestDto;
import fresh.crafts.engine.v1.models.ProjectDeployment;
import fresh.crafts.engine.v1.services.ProjectService;

@RestController
@RequestMapping(value = "/api/v1/projects", consumes = "application/json", produces = "application/json")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

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
        CommonResponseDto res = projectService.deployProject(id, deployInfo);
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
