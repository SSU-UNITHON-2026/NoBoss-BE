package blank.noboss.unithon.service.task;

import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.domain.task.enums.TaskStage;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.repository.task.TaskRepository;
import blank.noboss.unithon.service.task.dto.TaskCreateRequest;
import blank.noboss.unithon.service.task.dto.TaskListResponse;
import blank.noboss.unithon.service.task.dto.TaskResponse;
import blank.noboss.unithon.service.task.dto.TaskRiskListResponse;
import blank.noboss.unithon.service.task.dto.TaskUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private TaskService taskService;

    @Test
    void returnsOnlySelectedProjectTasks() {
        Project project = mock(Project.class);
        Task first = task(1L, 1, LocalDate.of(2026, 8, 20));
        Task second = task(2L, 2, LocalDate.of(2026, 8, 21));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.findAllByProjectIdOrderByStageAscDoneAscDueDateAscIdAsc(2L))
                .thenReturn(List.of(first, second));

        TaskListResponse response = taskService.getTasks(2L);

        assertThat(response.tasks()).extracting(TaskResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void createsTaskWithServerStageNameAndUndoneStatus() {
        Project project = mock(Project.class);
        TaskCreateRequest request = new TaskCreateRequest(
                3, "와이어프레임 제작", "배시현", LocalDate.of(2026, 11, 7)
        );
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(2L, request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getProject()).isSameAs(project);
        assertThat(captor.getValue().getStageName()).isEqualTo("초안 작성");
        assertThat(captor.getValue().isDone()).isFalse();
        assertThat(response.stage()).isEqualTo(3);
    }

    @Test
    void updatesOnlySelectedProjectTask() {
        Project project = mock(Project.class);
        Task task = task(2L, 2, LocalDate.of(2026, 8, 27));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(2L, 2L)).thenReturn(Optional.of(task));

        taskService.updateTask(2L, 2L, new TaskUpdateRequest(4, null, "정하람", null));

        verify(task).updateDetails(
                TaskStage.FEEDBACK,
                "업무 2",
                "정하람",
                LocalDate.of(2026, 8, 27)
        );
    }

    @Test
    void deletesOnlySelectedProjectTask() {
        Project project = mock(Project.class);
        Task task = mock(Task.class);
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(2L, 2L)).thenReturn(Optional.of(task));

        taskService.deleteTask(2L, 2L);

        verify(taskRepository).delete(task);
    }

    @Test
    void updatesTaskDoneStatus() {
        Project project = mock(Project.class);
        Task task = task(2L, 2, LocalDate.of(2026, 8, 27));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(2L, 2L)).thenReturn(Optional.of(task));

        taskService.updateDone(2L, 2L, true);

        verify(task).updateDone(true);
    }

    @Test
    void rejectsMissingDoneValueBeforeDatabaseAccess() {
        assertError(() -> taskService.updateDone(2L, 2L, null), ErrorCode.TASK_DONE_REQUIRED);
        verifyNoInteractions(projectRepository, taskRepository);
    }

    @Test
    void rejectsInvalidStage() {
        TaskCreateRequest request = new TaskCreateRequest(
                6, "잘못된 단계", "윤세아", LocalDate.of(2026, 9, 10)
        );
        assertError(() -> taskService.createTask(2L, request), ErrorCode.TASK_INVALID);
        verifyNoInteractions(projectRepository, taskRepository);
    }

    @Test
    void rejectsTaskFromAnotherProject() {
        Project project = mock(Project.class);
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(9L, 2L)).thenReturn(Optional.empty());

        assertError(() -> taskService.updateDone(2L, 9L, true), ErrorCode.TASK_NOT_FOUND);
    }

    @Test
    void rejectsMissingProject() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(() -> taskService.getTasks(999L), ErrorCode.PROJECT_NOT_FOUND);
        verifyNoInteractions(taskRepository);
    }

    @Test
    void calculatesRisksOnlyForSelectedProject() {
        LocalDate today = LocalDate.now(KST);
        Project project = mock(Project.class);
        Task overdue = task(2L, 2, today.minusDays(2));
        Task imminent = task(3L, 2, today.plusDays(1));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(taskRepository.findAllByProjectIdAndDoneFalseAndDueDateLessThanEqualOrderByDueDateAscIdAsc(
                2L, today.plusDays(2)
        )).thenReturn(List.of(overdue, imminent));

        TaskRiskListResponse response = taskService.getTaskRisks(2L);

        assertThat(response.risks()).extracting(risk -> risk.taskId()).containsExactly(2L, 3L);
        assertThat(response.risks()).extracting(risk -> risk.daysRemaining()).containsExactly(-2L, 1L);
    }

    private Task task(Long id, int stage, LocalDate dueDate) {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn(id);
        when(task.getStage()).thenReturn(stage);
        when(task.getStageName()).thenReturn(TaskStage.fromNumber(stage).orElseThrow().getStageName());
        when(task.getTitle()).thenReturn("업무 " + id);
        when(task.getOwner()).thenReturn("윤세아");
        when(task.getDueDate()).thenReturn(dueDate);
        return task;
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode)
                );
    }
}
