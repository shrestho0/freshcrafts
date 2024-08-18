package fresh.crafts.engine.v1.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.dtos.CreateProjectRequestDto;
import fresh.crafts.engine.v1.services.ProjectService;

@RestController
@RequestMapping(value = "/api/v1/projects", consumes = "application/json", produces = "application/json")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("")
    public ResponseEntity<CommonResponseDto> createProject(
            @RequestBody CreateProjectRequestDto createProjectDto) {

        // response dto
        CommonResponseDto res = new CommonResponseDto();

        // create project
        projectService.createProject(res, createProjectDto);

        ResponseEntity<CommonResponseDto> response = ResponseEntity.status(res.getStatusCode()).body(res);
        return response;

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

}
