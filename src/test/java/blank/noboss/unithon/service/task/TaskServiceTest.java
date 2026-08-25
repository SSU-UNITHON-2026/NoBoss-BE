package blank.noboss.unithon.service.task;

import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.task.TaskRepository;
import blank.noboss.unithon.service.task.dto.TaskListResponse;
import blank.noboss.unithon.service.task.dto.TaskResponse;
import blank.noboss.unithon.service.task.dto.TaskRiskListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void returnsTasksInRepositoryOrder() {
        Task first = task(1L, 1, LocalDate.of(2026, 8, 20));
        Task second = task(2L, 2, LocalDate.of(2026, 8, 21));
        when(first.isDone()).thenReturn(false);
        when(second.isDone()).thenReturn(false);
        when(taskRepository.findAllByProjectIdOrderByStageAscDoneAscDueDateAscIdAsc(1L))
                .thenReturn(List.of(first, second));

        TaskListResponse response = taskService.getTasks();

        assertThat(response.tasks()).extracting(TaskResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void updatesTaskDoneStatus() {
        Task task = task(2L, 2, LocalDate.of(2026, 8, 27));
        when(task.isDone()).thenReturn(true);
        when(taskRepository.findByIdAndProjectId(2L, 1L)).thenReturn(Optional.of(task));

        taskService.updateDone(2L, true);

        verify(task).updateDone(true);
    }

    @Test
    void rejectsMissingDoneValue() {
        assertThatThrownBy(() -> taskService.updateDone(2L, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TASK_DONE_REQUIRED)
                );

        verifyNoInteractions(taskRepository);
    }

    @Test
    void rejectsTaskThatDoesNotBelongToCurrentProject() {
        when(taskRepository.findByIdAndProjectId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateDone(999L, true))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TASK_NOT_FOUND)
                );
    }

    @Test
    void calculatesRiskDaysFromKoreanDate() {
        LocalDate today = LocalDate.now(KST);
        Task overdue = task(2L, 2, today.minusDays(2));
        Task imminent = task(3L, 2, today.plusDays(1));
        when(taskRepository
                .findAllByProjectIdAndDoneFalseAndDueDateLessThanEqualOrderByDueDateAscIdAsc(
                        1L,
                        today.plusDays(2)
                ))
                .thenReturn(List.of(overdue, imminent));

        TaskRiskListResponse response = taskService.getTaskRisks();

        assertThat(response.risks()).extracting(risk -> risk.taskId()).containsExactly(2L, 3L);
        assertThat(response.risks()).extracting(risk -> risk.daysRemaining()).containsExactly(-2L, 1L);
    }

    private Task task(Long id, int stage, LocalDate dueDate) {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn(id);
        when(task.getStage()).thenReturn(stage);
        when(task.getStageName()).thenReturn("리서치");
        when(task.getTitle()).thenReturn("업무 " + id);
        when(task.getOwner()).thenReturn("윤세아");
        when(task.getDueDate()).thenReturn(dueDate);
        return task;
    }
}
