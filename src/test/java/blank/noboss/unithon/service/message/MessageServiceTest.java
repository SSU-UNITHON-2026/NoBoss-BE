package blank.noboss.unithon.service.message;

import blank.noboss.unithon.client.ai.AiProposalClient;
import blank.noboss.unithon.client.ai.dto.AiProposalRequest;
import blank.noboss.unithon.client.ai.dto.AiProposalResponse;
import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.message.MessageRepository;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.repository.task.TaskRepository;
import blank.noboss.unithon.service.message.dto.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private AiProposalClient aiProposalClient;
    @Mock
    private AiProposalNormalizer aiProposalNormalizer;
    @Mock
    private ProposalJsonMapper proposalJsonMapper;
    @Mock
    private MessagePersistenceService messagePersistenceService;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(
                projectRepository,
                taskRepository,
                messageRepository,
                aiProposalClient,
                aiProposalNormalizer,
                proposalJsonMapper,
                messagePersistenceService
        );
    }

    @Test
    void sendsProjectTasksAndPendingProposalToAi() {
        Project project = project();
        Task task = task();
        Message pending = mock(Message.class);
        Map<String, Object> pendingProposal = Map.of("title", "사용자 인터뷰 5명 진행");
        AiProposalResponse aiResponse = new AiProposalResponse("답변", "NONE", false, null);
        NormalizedAiProposal normalized = new NormalizedAiProposal("답변", ActionType.NONE, false, null);
        Message saved = mock(Message.class);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findAllByProjectIdOrderByStageAscDoneAscDueDateAscIdAsc(1L))
                .thenReturn(List.of(task));
        when(messageRepository.findFirstByProposalStatusOrderByCreatedAtDescIdDesc(ProposalStatus.PENDING))
                .thenReturn(Optional.of(pending));
        when(pending.getId()).thenReturn(15L);
        when(pending.getActionType()).thenReturn(ActionType.TASK_CREATE);
        when(pending.getProposal()).thenReturn("{\"title\":\"사용자 인터뷰 5명 진행\"}");
        when(proposalJsonMapper.read(pending.getProposal())).thenReturn(pendingProposal);
        when(aiProposalClient.generate(org.mockito.ArgumentMatchers.any())).thenReturn(aiResponse);
        when(aiProposalNormalizer.normalize(aiResponse, java.util.Set.of(2L))).thenReturn(normalized);
        when(messagePersistenceService.save("담당자는 정하람이야", normalized)).thenReturn(saved);
        when(saved.getId()).thenReturn(16L);

        MessageResponse response = messageService.createMessage("담당자는 정하람이야");

        ArgumentCaptor<AiProposalRequest> captor = ArgumentCaptor.forClass(AiProposalRequest.class);
        verify(aiProposalClient).generate(captor.capture());
        AiProposalRequest request = captor.getValue();
        assertThat(request.project().id()).isEqualTo(1L);
        assertThat(request.project().tasks()).hasSize(1);
        assertThat(request.project().tasks().getFirst().taskId()).isEqualTo(2L);
        assertThat(request.project().tasks().getFirst().done()).isFalse();
        assertThat(request.project().pendingProposal().messageId()).isEqualTo(15L);
        assertThat(request.project().pendingProposal().proposal()).isEqualTo(pendingProposal);
        assertThat(response.messageId()).isEqualTo(16L);
    }

    @Test
    void rejectsMissingMessage() {
        assertThatThrownBy(() -> messageService.createMessage(null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MESSAGE_TEXT_REQUIRED)
                );
        verifyNoInteractions(aiProposalClient);
    }

    @Test
    void rejectsBlankMessage() {
        assertThatThrownBy(() -> messageService.createMessage("   "))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MESSAGE_TEXT_BLANK)
                );
        verifyNoInteractions(aiProposalClient);
    }

    private Project project() {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(1L);
        when(project.getTeamName()).thenReturn("B_LANK");
        when(project.getSubjectName()).thenReturn("서비스디자인 캡스톤");
        when(project.getProjectTopic()).thenReturn("캠퍼스 중고거래 앱 UX 개선");
        when(project.getDeadline()).thenReturn(LocalDate.of(2026, 12, 11));
        when(project.getDescription()).thenReturn("프로젝트 설명");
        return project;
    }

    private Task task() {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn(2L);
        when(task.getStage()).thenReturn(2);
        when(task.getStageName()).thenReturn("리서치");
        when(task.getTitle()).thenReturn("사용자 인터뷰 5명 진행");
        when(task.getOwner()).thenReturn("윤세아");
        when(task.getDueDate()).thenReturn(LocalDate.of(2026, 8, 28));
        when(task.isDone()).thenReturn(false);
        return task;
    }
}
