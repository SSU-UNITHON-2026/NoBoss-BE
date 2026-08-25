package blank.noboss.unithon.controller.task;

import blank.noboss.unithon.global.response.ApiResponse;
import blank.noboss.unithon.service.task.TaskService;
import blank.noboss.unithon.service.task.dto.TaskDoneUpdateRequest;
import blank.noboss.unithon.service.task.dto.TaskListResponse;
import blank.noboss.unithon.service.task.dto.TaskResponse;
import blank.noboss.unithon.service.task.dto.TaskRiskListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Task")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "전체 업무 조회")
    @GetMapping
    public ApiResponse<TaskListResponse> getTasks() {
        return ApiResponse.success(taskService.getTasks());
    }

    @Operation(summary = "업무 완료 상태 변경")
    @PatchMapping("/{taskId}/done")
    public ApiResponse<TaskResponse> updateDone(
            @PathVariable Long taskId,
            @RequestBody(required = false) TaskDoneUpdateRequest request
    ) {
        Boolean done = request == null ? null : request.done();
        return ApiResponse.success(taskService.updateDone(taskId, done));
    }

    @Operation(summary = "지연 위험 업무 조회")
    @GetMapping("/risks")
    public ApiResponse<TaskRiskListResponse> getTaskRisks() {
        return ApiResponse.success(taskService.getTaskRisks());
    }
}
