package blank.noboss.unithon.service.message;

import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.message.MessageRepository;
import blank.noboss.unithon.repository.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessagePersistenceService {

    private final ProjectRepository projectRepository;
    private final MessageRepository messageRepository;
    private final ProposalJsonMapper proposalJsonMapper;

    @Transactional
    public Message save(Long projectId, String userText, NormalizedAiProposal aiProposal) {
        Project project = aiProposal.actionType() == ActionType.NONE
                ? findProject(projectId)
                : lockProject(projectId);
        Message message;

        if (aiProposal.actionType() == ActionType.NONE) {
            message = Message.createAnswer(project, userText, aiProposal.aiMessage());
        } else {
            messageRepository
                    .findFirstByProjectIdAndProposalStatusOrderByCreatedAtDescIdDesc(
                            projectId,
                            ProposalStatus.PENDING
                    )
                    .ifPresent(Message::supersede);

            String proposalJson = proposalJsonMapper.write(aiProposal.proposal());
            message = Message.createProposal(
                    project,
                    userText,
                    aiProposal.aiMessage(),
                    aiProposal.actionType(),
                    proposalJson
            );
        }

        return messageRepository.save(message);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private Project lockProject(Long projectId) {
        return projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }
}
