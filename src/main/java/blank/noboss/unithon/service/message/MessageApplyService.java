package blank.noboss.unithon.service.message;

import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.domain.task.entity.Task;
import blank.noboss.unithon.domain.task.enums.TaskStage;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.message.MessageRepository;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.repository.task.TaskRepository;
import blank.noboss.unithon.service.message.StoredProposalParser.ProjectProposal;
import blank.noboss.unithon.service.message.StoredProposalParser.TaskProposal;
import blank.noboss.unithon.service.message.dto.MessageApplyResponse;
import blank.noboss.unithon.service.project.dto.ProjectResponse;
import blank.noboss.unithon.service.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageApplyService {

    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProposalJsonMapper proposalJsonMapper;
    private final StoredProposalParser storedProposalParser;

    @Transactional
    public MessageApplyResponse apply(Long projectId, Long messageId) {
        Project project = projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        Message message = messageRepository.findByIdAndProjectIdForUpdate(messageId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        validateApplicable(message);
        Map<String, Object> proposal = proposalJsonMapper.read(message.getProposal());
        MessageApplyResponse response = switch (message.getActionType()) {
            case TASK_CREATE -> applyTaskCreate(project, proposal);
            case TASK_UPDATE -> applyTaskUpdate(projectId, proposal);
            case PROJECT_UPDATE -> applyProjectUpdate(project, proposal);
            case NONE -> throw new BusinessException(ErrorCode.MESSAGE_NO_CHANGES);
        };

        message.apply();
        return response;
    }

    private void validateApplicable(Message message) {
        if (message.getActionType() == ActionType.NONE
                || !message.isRequiresApproval()
                || message.getProposal() == null) {
            throw new BusinessException(ErrorCode.MESSAGE_NO_CHANGES);
        }
        if (message.getProposalStatus() != ProposalStatus.PENDING) {
            throw new BusinessException(ErrorCode.MESSAGE_ALREADY_APPLIED);
        }
    }

    private MessageApplyResponse applyTaskCreate(Project project, Map<String, Object> proposal) {
        TaskProposal taskProposal = storedProposalParser.parseTaskCreate(proposal);
        Task task = Task.create(
                project,
                requireStage(taskProposal.stage()),
                taskProposal.title(),
                taskProposal.owner(),
                taskProposal.dueDate()
        );
        Task savedTask = taskRepository.save(task);
        return MessageApplyResponse.task(ActionType.TASK_CREATE, TaskResponse.from(savedTask));
    }

    private MessageApplyResponse applyTaskUpdate(Long projectId, Map<String, Object> proposal) {
        TaskProposal taskProposal = storedProposalParser.parseTaskUpdate(proposal);
        Task task = taskRepository.findByIdAndProjectId(taskProposal.taskId(), projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));
        task.updateDetails(
                requireStage(taskProposal.stage()),
                taskProposal.title(),
                taskProposal.owner(),
                taskProposal.dueDate()
        );
        return MessageApplyResponse.task(ActionType.TASK_UPDATE, TaskResponse.from(task));
    }

    private MessageApplyResponse applyProjectUpdate(Project project, Map<String, Object> proposal) {
        ProjectProposal projectProposal = storedProposalParser.parseProjectUpdate(proposal);
        project.update(
                projectProposal.teamName(),
                projectProposal.subjectName(),
                projectProposal.projectTopic(),
                projectProposal.deadline(),
                projectProposal.description()
        );
        return MessageApplyResponse.project(ProjectResponse.from(project));
    }

    private TaskStage requireStage(Integer stage) {
        return TaskStage.fromNumber(stage)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_RESPONSE_PROCESSING_FAILED));
    }
}
