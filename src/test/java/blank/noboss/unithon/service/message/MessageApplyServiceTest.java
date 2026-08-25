package blank.noboss.unithon.service.message;

import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.message.MessageRepository;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.repository.task.TaskRepository;
import blank.noboss.unithon.service.message.dto.MessageApplyResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageApplyServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProposalJsonMapper proposalJsonMapper;

    @Mock
    private StoredProposalParser storedProposalParser;

    @InjectMocks
    private MessageApplyService messageApplyService;

    @Test
    void createsTaskAndMarksProposalApplied() {
        Message message = pending(ActionType.TASK_CREATE);
        Project project = mock(Project.class);
        Map<String, Object> proposal = Map.of("title", "사용자 인터뷰 5명 진행");
        StoredProposalParser.TaskProposal taskProposal = new StoredProposalParser.TaskProposal(
                null, 2, "리서치", "사용자 인터뷰 5명 진행", "정하람", LocalDate.of(2026, 9, 4)
        );
        when(messageRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(message));
        when(proposalJsonMapper.read(message.getProposal())).thenReturn(proposal);
        when(storedProposalParser.parseTaskCreate(proposal)).thenReturn(taskProposal);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageApplyResponse response = messageApplyService.apply(1L);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task createdTask = taskCaptor.getValue();
        assertThat(createdTask.getProject()).isSameAs(project);
        assertThat(createdTask.getOwner()).isEqualTo("정하람");
        assertThat(createdTask.isDone()).isFalse();
        assertThat(response.actionType()).isEqualTo(ActionType.TASK_CREATE);
        assertThat(message.getProposalStatus().name()).isEqualTo("APPLIED");
    }

    @Test
    void updatesTaskWithoutChangingDoneStatus() {
        Message message = pending(ActionType.TASK_UPDATE);
        Task task = mock(Task.class);
        Map<String, Object> proposal = Map.of("taskId", 2);
        StoredProposalParser.TaskProposal taskProposal = new StoredProposalParser.TaskProposal(
                2L, 2, "리서치", "사용자 인터뷰 5명 진행", "정하람", LocalDate.of(2026, 9, 4)
        );
        when(messageRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(message));
        when(proposalJsonMapper.read(message.getProposal())).thenReturn(proposal);
        when(storedProposalParser.parseTaskUpdate(proposal)).thenReturn(taskProposal);
        when(taskRepository.findByIdAndProjectId(2L, 1L)).thenReturn(Optional.of(task));

        MessageApplyResponse response = messageApplyService.apply(2L);

        verify(task).updateDetails(2, "리서치", "사용자 인터뷰 5명 진행", "정하람",
                LocalDate.of(2026, 9, 4));
        verify(task, never()).updateDone(false);
        assertThat(response.actionType()).isEqualTo(ActionType.TASK_UPDATE);
    }

    @Test
    void updatesProject() {
        Message message = pending(ActionType.PROJECT_UPDATE);
        Project project = mock(Project.class);
        Map<String, Object> proposal = Map.of("deadline", "2026-12-20");
        StoredProposalParser.ProjectProposal projectProposal = new StoredProposalParser.ProjectProposal(
                "B_LANK",
                "서비스디자인 캡스톤",
                "캠퍼스 중고거래 앱 UX 개선",
                LocalDate.of(2026, 12, 20),
                "교내 중고거래 과정의 불편함을 개선하는 UX 프로젝트"
        );
        when(messageRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(message));
        when(proposalJsonMapper.read(message.getProposal())).thenReturn(proposal);
        when(storedProposalParser.parseProjectUpdate(proposal)).thenReturn(projectProposal);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        MessageApplyResponse response = messageApplyService.apply(3L);

        verify(project).update(
                projectProposal.teamName(),
                projectProposal.subjectName(),
                projectProposal.projectTopic(),
                projectProposal.deadline(),
                projectProposal.description()
        );
        assertThat(response.actionType()).isEqualTo(ActionType.PROJECT_UPDATE);
    }

    @Test
    void rejectsMessageWithoutChanges() {
        Message message = Message.createAnswer("안녕", "안녕하세요");
        when(messageRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(message));

        assertError(() -> messageApplyService.apply(4L), ErrorCode.MESSAGE_NO_CHANGES);
        verifyNoInteractions(proposalJsonMapper, projectRepository, taskRepository);
    }

    @Test
    void rejectsAlreadyAppliedProposal() {
        Message message = pending(ActionType.TASK_CREATE);
        message.apply();
        when(messageRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(message));

        assertError(() -> messageApplyService.apply(5L), ErrorCode.MESSAGE_ALREADY_APPLIED);
        verifyNoInteractions(proposalJsonMapper, projectRepository, taskRepository);
    }

    @Test
    void rejectsSupersededProposal() {
        Message message = pending(ActionType.TASK_CREATE);
        message.supersede();
        when(messageRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(message));

        assertError(() -> messageApplyService.apply(6L), ErrorCode.MESSAGE_ALREADY_APPLIED);
        verifyNoInteractions(proposalJsonMapper, projectRepository, taskRepository);
    }

    @Test
    void rejectsMissingMessage() {
        when(messageRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertError(() -> messageApplyService.apply(999L), ErrorCode.MESSAGE_NOT_FOUND);
        verifyNoInteractions(proposalJsonMapper, projectRepository, taskRepository);
    }

    @Test
    void rejectsTaskThatNoLongerExists() {
        Message message = pending(ActionType.TASK_UPDATE);
        Map<String, Object> proposal = Map.of("taskId", 99);
        StoredProposalParser.TaskProposal taskProposal = new StoredProposalParser.TaskProposal(
                99L, 2, "리서치", "사라진 업무", "정하람", LocalDate.of(2026, 9, 4)
        );
        when(messageRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(message));
        when(proposalJsonMapper.read(message.getProposal())).thenReturn(proposal);
        when(storedProposalParser.parseTaskUpdate(proposal)).thenReturn(taskProposal);
        when(taskRepository.findByIdAndProjectId(99L, 1L)).thenReturn(Optional.empty());

        assertError(() -> messageApplyService.apply(7L), ErrorCode.TASK_NOT_FOUND);
        assertThat(message.getProposalStatus().name()).isEqualTo("PENDING");
    }

    private Message pending(ActionType actionType) {
        return Message.createProposal("요청", "적용할까요?", actionType, "{\"proposal\":true}");
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode)
                );
    }
}
