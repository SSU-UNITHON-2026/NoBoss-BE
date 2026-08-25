package blank.noboss.unithon.service.message;

import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.domain.task.enums.TaskStage;
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
    void createsTaskInMessageProject() {
        Project project = project();
        Message message = pending(project, ActionType.TASK_CREATE);
        Map<String, Object> proposal = Map.of("title", "사용자 인터뷰 5명 진행");
        var parsed = new StoredProposalParser.TaskProposal(
                null, 2, "리서치", "사용자 인터뷰 5명 진행", "정하람", LocalDate.of(2026, 9, 4)
        );
        prepareMessage(project, message, 1L);
        when(proposalJsonMapper.read(message.getProposal())).thenReturn(proposal);
        when(storedProposalParser.parseTaskCreate(proposal)).thenReturn(parsed);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageApplyResponse response = messageApplyService.apply(2L, 1L);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getProject()).isSameAs(project);
        assertThat(captor.getValue().getStageName()).isEqualTo("리서치");
        assertThat(captor.getValue().isDone()).isFalse();
        assertThat(response.actionType()).isEqualTo(ActionType.TASK_CREATE);
        assertThat(message.getProposalStatus().name()).isEqualTo("APPLIED");
    }

    @Test
    void updatesTaskOnlyInMessageProjectWithoutChangingDone() {
        Project project = project();
        Message message = pending(project, ActionType.TASK_UPDATE);
        Task task = mock(Task.class);
        Map<String, Object> proposal = Map.of("taskId", 3);
        var parsed = new StoredProposalParser.TaskProposal(
                3L, 4, "피드백 반영", "사용성 테스트", "윤세아", LocalDate.of(2026, 11, 20)
        );
        prepareMessage(project, message, 2L);
        when(proposalJsonMapper.read(message.getProposal())).thenReturn(proposal);
        when(storedProposalParser.parseTaskUpdate(proposal)).thenReturn(parsed);
        when(taskRepository.findByIdAndProjectId(3L, 2L)).thenReturn(Optional.of(task));

        messageApplyService.apply(2L, 2L);

        verify(task).updateDetails(
                TaskStage.FEEDBACK, "사용성 테스트", "윤세아", LocalDate.of(2026, 11, 20)
        );
        verify(task, never()).updateDone(false);
    }

    @Test
    void updatesMessageProject() {
        Project project = project();
        Message message = pending(project, ActionType.PROJECT_UPDATE);
        Map<String, Object> proposal = Map.of("deadline", "2026-12-20");
        var parsed = new StoredProposalParser.ProjectProposal(
                "B_LANK", "서비스디자인 캡스톤", "UX 개선",
                LocalDate.of(2026, 12, 20), "프로젝트 설명"
        );
        prepareMessage(project, message, 3L);
        when(proposalJsonMapper.read(message.getProposal())).thenReturn(proposal);
        when(storedProposalParser.parseProjectUpdate(proposal)).thenReturn(parsed);

        messageApplyService.apply(2L, 3L);

        verify(project).update(
                parsed.teamName(), parsed.subjectName(), parsed.projectTopic(),
                parsed.deadline(), parsed.description()
        );
    }

    @Test
    void rejectsMessageFromAnotherProject() {
        Project project = project();
        when(projectRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(project));
        when(messageRepository.findByIdAndProjectIdForUpdate(9L, 2L)).thenReturn(Optional.empty());

        assertError(() -> messageApplyService.apply(2L, 9L), ErrorCode.MESSAGE_NOT_FOUND);
        verifyNoInteractions(proposalJsonMapper, taskRepository);
    }

    @Test
    void rejectsMissingProject() {
        when(projectRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertError(() -> messageApplyService.apply(999L, 1L), ErrorCode.PROJECT_NOT_FOUND);
        verifyNoInteractions(messageRepository, proposalJsonMapper, taskRepository);
    }

    @Test
    void rejectsMessageWithoutChanges() {
        Project project = project();
        Message message = Message.createAnswer(project, "안녕", "안녕하세요");
        prepareMessage(project, message, 4L);

        assertError(() -> messageApplyService.apply(2L, 4L), ErrorCode.MESSAGE_NO_CHANGES);
        verifyNoInteractions(proposalJsonMapper, taskRepository);
    }

    @Test
    void rejectsAppliedOrSupersededProposal() {
        Project project = project();
        Message applied = pending(project, ActionType.TASK_CREATE);
        applied.apply();
        prepareMessage(project, applied, 5L);
        assertError(() -> messageApplyService.apply(2L, 5L), ErrorCode.MESSAGE_ALREADY_APPLIED);

        Message superseded = pending(project, ActionType.TASK_CREATE);
        superseded.supersede();
        when(messageRepository.findByIdAndProjectIdForUpdate(6L, 2L)).thenReturn(Optional.of(superseded));
        assertError(() -> messageApplyService.apply(2L, 6L), ErrorCode.MESSAGE_ALREADY_APPLIED);
    }

    @Test
    void rejectsDeletedTaskWithoutApplyingMessage() {
        Project project = project();
        Message message = pending(project, ActionType.TASK_UPDATE);
        Map<String, Object> proposal = Map.of("taskId", 99);
        var parsed = new StoredProposalParser.TaskProposal(
                99L, 2, "리서치", "사라진 업무", "정하람", LocalDate.of(2026, 9, 4)
        );
        prepareMessage(project, message, 7L);
        when(proposalJsonMapper.read(message.getProposal())).thenReturn(proposal);
        when(storedProposalParser.parseTaskUpdate(proposal)).thenReturn(parsed);
        when(taskRepository.findByIdAndProjectId(99L, 2L)).thenReturn(Optional.empty());

        assertError(() -> messageApplyService.apply(2L, 7L), ErrorCode.TASK_NOT_FOUND);
        assertThat(message.getProposalStatus().name()).isEqualTo("PENDING");
    }

    private void prepareMessage(Project project, Message message, Long messageId) {
        when(projectRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(project));
        when(messageRepository.findByIdAndProjectIdForUpdate(messageId, 2L)).thenReturn(Optional.of(message));
    }

    private Project project() {
        return mock(Project.class);
    }

    private Message pending(Project project, ActionType actionType) {
        return Message.createProposal(project, "요청", "적용할까요?", actionType, "{\"proposal\":true}");
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode)
                );
    }
}
