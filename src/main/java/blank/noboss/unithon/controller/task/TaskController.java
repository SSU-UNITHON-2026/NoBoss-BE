package blank.noboss.unithon.controller.task;

import blank.noboss.unithon.global.response.ApiResponse;
import blank.noboss.unithon.service.task.TaskService;
import blank.noboss.unithon.service.task.dto.TaskCreateRequest;
import blank.noboss.unithon.service.task.dto.TaskDeleteResponse;
import blank.noboss.unithon.service.task.dto.TaskDoneUpdateRequest;
import blank.noboss.unithon.service.task.dto.TaskListResponse;
import blank.noboss.unithon.service.task.dto.TaskResponse;
import blank.noboss.unithon.service.task.dto.TaskRiskListResponse;
import blank.noboss.unithon.service.task.dto.TaskUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@Tag(name = "Task")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "프로젝트 전체 업무 조회")
    @GetMapping
    public ApiResponse<TaskListResponse> getTasks(@PathVariable Long projectId) {
        return ApiResponse.success(taskService.getTasks(projectId));
    }

    @Operation(summary = "프로젝트 업무 생성")
    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody(required = false) TaskCreateRequest request
    ) {
        return ApiResponse.created(taskService.createTask(projectId, request));
    }

    @Operation(summary = "프로젝트 업무 수정")
    @PatchMapping("/{taskId}")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody(required = false) TaskUpdateRequest request
    ) {
        return ApiResponse.success(taskService.updateTask(projectId, taskId, request));
    }

    @Operation(summary = "프로젝트 업무 삭제")
    @DeleteMapping("/{taskId}")
    public ApiResponse<TaskDeleteResponse> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        return ApiResponse.success(taskService.deleteTask(projectId, taskId));
    }

    @Operation(summary = "프로젝트 업무 완료 상태 변경")
    @PatchMapping("/{taskId}/done")
    public ApiResponse<TaskResponse> updateDone(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody(required = false) TaskDoneUpdateRequest request
    ) {
        Boolean done = request == null ? null : request.done();
        return ApiResponse.success(taskService.updateDone(projectId, taskId, done));
    }

    @Operation(summary = "프로젝트 지연 위험 업무 조회")
    @GetMapping("/risks")
    public ApiResponse<TaskRiskListResponse> getTaskRisks(@PathVariable Long projectId) {
        return ApiResponse.success(taskService.getTaskRisks(projectId));
    }
}
