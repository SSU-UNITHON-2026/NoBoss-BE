package blank.noboss.unithon.controller.project;

import blank.noboss.unithon.global.response.ApiResponse;
import blank.noboss.unithon.service.project.ProjectService;
import blank.noboss.unithon.service.project.dto.ProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Project")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/project")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "프로젝트 정보 조회")
    @GetMapping
    public ApiResponse<ProjectResponse> getProject() {
        return ApiResponse.success(projectService.getProject());
    }
}
