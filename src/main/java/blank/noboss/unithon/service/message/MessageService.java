package blank.noboss.unithon.service.message;

import blank.noboss.unithon.client.ai.AiProposalClient;
import blank.noboss.unithon.client.ai.dto.AiPendingProposal;
import blank.noboss.unithon.client.ai.dto.AiProjectContext;
import blank.noboss.unithon.client.ai.dto.AiProposalRequest;
import blank.noboss.unithon.client.ai.dto.AiProposalResponse;
import blank.noboss.unithon.client.ai.dto.AiTaskContext;
import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.message.MessageRepository;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.repository.task.TaskRepository;
import blank.noboss.unithon.service.message.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final long CURRENT_PROJECT_ID = 1L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MessageRepository messageRepository;
    private final AiProposalClient aiProposalClient;
    private final AiProposalNormalizer aiProposalNormalizer;
    private final ProposalJsonMapper proposalJsonMapper;
    private final MessagePersistenceService messagePersistenceService;

    public MessageResponse createMessage(String userText) {
        validateUserText(userText);

        Project project = projectRepository.findById(CURRENT_PROJECT_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        List<Task> tasks = taskRepository
                .findAllByProjectIdOrderByStageAscDoneAscDueDateAscIdAsc(CURRENT_PROJECT_ID);
        AiPendingProposal pendingProposal = findPendingProposal();

        AiProposalRequest aiRequest = new AiProposalRequest(
                LocalDate.now(KST).toString(),
                userText,
                toProjectContext(project, tasks, pendingProposal)
        );

        AiProposalResponse aiResponse = aiProposalClient.generate(aiRequest);
        Set<Long> currentTaskIds = tasks.stream().map(Task::getId).collect(Collectors.toSet());
        NormalizedAiProposal normalized = aiProposalNormalizer.normalize(aiResponse, currentTaskIds);
        Message savedMessage = messagePersistenceService.save(userText, normalized);

        return new MessageResponse(
                savedMessage.getId(),
                normalized.aiMessage(),
                normalized.actionType(),
                normalized.requiresApproval(),
                normalized.proposal()
        );
    }

    private AiProjectContext toProjectContext(
            Project project,
            List<Task> tasks,
            AiPendingProposal pendingProposal
    ) {
        List<AiTaskContext> taskContexts = tasks.stream()
                .map(task -> new AiTaskContext(
                        task.getId(),
                        task.getStage(),
                        task.getStageName(),
                        task.getTitle(),
                        task.getOwner(),
                        task.getDueDate().toString(),
                        task.isDone()
                ))
                .toList();

        return new AiProjectContext(
                project.getId(),
                project.getTeamName(),
                project.getSubjectName(),
                project.getProjectTopic(),
                project.getDeadline().toString(),
                project.getDescription(),
                taskContexts,
                pendingProposal
        );
    }

    private AiPendingProposal findPendingProposal() {
        return messageRepository
                .findFirstByProposalStatusOrderByCreatedAtDescIdDesc(ProposalStatus.PENDING)
                .map(message -> new AiPendingProposal(
                        message.getId(),
                        message.getActionType(),
                        proposalJsonMapper.read(message.getProposal())
                ))
                .orElse(null);
    }

    private void validateUserText(String userText) {
        if (userText == null) {
            throw new BusinessException(ErrorCode.MESSAGE_TEXT_REQUIRED);
        }
        if (userText.isBlank()) {
            throw new BusinessException(ErrorCode.MESSAGE_TEXT_BLANK);
        }
    }
}
