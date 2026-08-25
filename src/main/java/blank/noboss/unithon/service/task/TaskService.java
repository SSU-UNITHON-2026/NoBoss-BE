package blank.noboss.unithon.service.task;

import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.task.TaskRepository;
import blank.noboss.unithon.service.task.dto.TaskListResponse;
import blank.noboss.unithon.service.task.dto.TaskResponse;
import blank.noboss.unithon.service.task.dto.TaskRiskListResponse;
import blank.noboss.unithon.service.task.dto.TaskRiskResponse;
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

    private static final long CURRENT_PROJECT_ID = 1L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TaskRepository taskRepository;

    public TaskListResponse getTasks() {
        List<TaskResponse> tasks = taskRepository
                .findAllByProjectIdOrderByStageAscDoneAscDueDateAscIdAsc(CURRENT_PROJECT_ID)
                .stream()
                .map(TaskResponse::from)
                .toList();

        return new TaskListResponse(tasks);
    }

    @Transactional
    public TaskResponse updateDone(Long taskId, Boolean done) {
        if (done == null) {
            throw new BusinessException(ErrorCode.TASK_DONE_REQUIRED);
        }

        Task task = taskRepository.findByIdAndProjectId(taskId, CURRENT_PROJECT_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        task.updateDone(done);

        return TaskResponse.from(task);
    }

    public TaskRiskListResponse getTaskRisks() {
        LocalDate today = LocalDate.now(KST);
        LocalDate riskThreshold = today.plusDays(2);

        List<TaskRiskResponse> risks = taskRepository
                .findAllByProjectIdAndDoneFalseAndDueDateLessThanEqualOrderByDueDateAscIdAsc(
                        CURRENT_PROJECT_ID,
                        riskThreshold
                )
                .stream()
                .map(task -> TaskRiskResponse.from(task, today))
                .toList();

        return new TaskRiskListResponse(risks);
    }
}
