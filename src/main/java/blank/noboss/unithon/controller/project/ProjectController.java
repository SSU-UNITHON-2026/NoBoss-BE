package blank.noboss.unithon.controller.project;

import blank.noboss.unithon.global.response.ApiResponse;
import blank.noboss.unithon.service.project.ProjectService;
import blank.noboss.unithon.service.project.dto.ProjectCreateRequest;
import blank.noboss.unithon.service.project.dto.ProjectListResponse;
import blank.noboss.unithon.service.project.dto.ProjectResponse;
import blank.noboss.unithon.service.project.dto.ProjectUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@Tag(name = "Project")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "프로젝트 생성")
    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<ProjectResponse> createProject(
            @Valid @RequestBody(required = false) ProjectCreateRequest request
    ) {
        return ApiResponse.created(projectService.createProject(request));
    }

    @Operation(summary = "전체 프로젝트 조회")
    @GetMapping
    public ApiResponse<ProjectListResponse> getProjects() {
        return ApiResponse.success(projectService.getProjects());
    }

    @Operation(summary = "프로젝트 상세 조회")
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectResponse> getProject(
            @PathVariable Long projectId
    ) {
        return ApiResponse.success(projectService.getProject(projectId));
    }

    @Operation(summary = "프로젝트 기본 정보 수정")
    @PatchMapping("/{projectId}")
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody(required = false) ProjectUpdateRequest request
    ) {
        return ApiResponse.success(projectService.updateProject(projectId, request));
    }
}
