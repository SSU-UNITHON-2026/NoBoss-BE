package blank.noboss.unithon.service.task;

import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.domain.task.enums.TaskStage;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.repository.task.TaskRepository;
import blank.noboss.unithon.service.task.dto.TaskCreateRequest;
import blank.noboss.unithon.service.task.dto.TaskDeleteResponse;
import blank.noboss.unithon.service.task.dto.TaskListResponse;
import blank.noboss.unithon.service.task.dto.TaskResponse;
import blank.noboss.unithon.service.task.dto.TaskRiskListResponse;
import blank.noboss.unithon.service.task.dto.TaskRiskResponse;
import blank.noboss.unithon.service.task.dto.TaskUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public TaskListResponse getTasks(Long projectId) {
        requireProject(projectId);
        List<TaskResponse> tasks = taskRepository
                .findAllByProjectIdOrderByStageAscDoneAscDueDateAscIdAsc(projectId)
                .stream()
                .map(TaskResponse::from)
                .toList();

        return new TaskListResponse(tasks);
    }

    @Transactional
    public TaskResponse createTask(Long projectId, TaskCreateRequest request) {
        validateCreateRequest(request);
        Project project = requireProject(projectId);
        Task task = Task.create(
                project,
                requireStage(request.stage()),
                request.title(),
                request.owner(),
                request.dueDate()
        );
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, TaskUpdateRequest request) {
        validateUpdateRequest(request);
        requireProject(projectId);
        Task task = requireTask(projectId, taskId);
        TaskStage stage = request.stage() == null
                ? requireStage(task.getStage())
                : requireStage(request.stage());
        task.updateDetails(
                stage,
                valueOrCurrent(request.title(), task.getTitle()),
                valueOrCurrent(request.owner(), task.getOwner()),
                request.dueDate() == null ? task.getDueDate() : request.dueDate()
        );
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskDeleteResponse deleteTask(Long projectId, Long taskId) {
        requireProject(projectId);
        Task task = requireTask(projectId, taskId);
        taskRepository.delete(task);
        return new TaskDeleteResponse(taskId);
    }

    @Transactional
    public TaskResponse updateDone(Long projectId, Long taskId, Boolean done) {
        if (done == null) {
            throw new BusinessException(ErrorCode.TASK_DONE_REQUIRED);
        }
        requireProject(projectId);
        Task task = requireTask(projectId, taskId);
        task.updateDone(done);
        return TaskResponse.from(task);
    }

    public TaskRiskListResponse getTaskRisks(Long projectId) {
        requireProject(projectId);
        LocalDate today = LocalDate.now(KST);
        LocalDate riskThreshold = today.plusDays(2);
        List<TaskRiskResponse> risks = taskRepository
                .findAllByProjectIdAndDoneFalseAndDueDateLessThanEqualOrderByDueDateAscIdAsc(
                        projectId,
                        riskThreshold
                )
                .stream()
                .map(task -> TaskRiskResponse.from(task, today))
                .toList();

        return new TaskRiskListResponse(risks);
    }

    private Project requireProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private Task requireTask(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
    }

    private TaskStage requireStage(Integer stage) {
        return TaskStage.fromNumber(stage)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_INVALID));
    }

    private void validateCreateRequest(TaskCreateRequest request) {
        if (request == null
                || TaskStage.fromNumber(request.stage()).isEmpty()
                || isBlank(request.title())
                || isBlank(request.owner())
                || request.dueDate() == null) {
            throw new BusinessException(ErrorCode.TASK_INVALID);
        }
    }

    private void validateUpdateRequest(TaskUpdateRequest request) {
        if (request == null || hasNoChanges(request)) {
            throw new BusinessException(ErrorCode.TASK_INVALID);
        }
        if ((request.stage() != null && TaskStage.fromNumber(request.stage()).isEmpty())
                || isProvidedBlank(request.title())
                || isProvidedBlank(request.owner())) {
            throw new BusinessException(ErrorCode.TASK_INVALID);
        }
    }

    private boolean hasNoChanges(TaskUpdateRequest request) {
        return request.stage() == null
                && request.title() == null
                && request.owner() == null
                && request.dueDate() == null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isProvidedBlank(String value) {
        return value != null && value.isBlank();
    }

    private String valueOrCurrent(String value, String current) {
        return value == null ? current : value;
    }
}
