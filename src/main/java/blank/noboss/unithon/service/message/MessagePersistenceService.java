package blank.noboss.unithon.service.message;

import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import blank.noboss.unithon.repository.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessagePersistenceService {

    private final MessageRepository messageRepository;
    private final ProposalJsonMapper proposalJsonMapper;

    @Transactional
    public Message save(String userText, NormalizedAiProposal aiProposal) {
        Message message;

        if (aiProposal.actionType() == ActionType.NONE) {
            message = Message.createAnswer(userText, aiProposal.aiMessage());
        } else {
            messageRepository
                    .findFirstByProposalStatusOrderByCreatedAtDescIdDesc(ProposalStatus.PENDING)
                    .ifPresent(Message::supersede);

            String proposalJson = proposalJsonMapper.write(aiProposal.proposal());
            message = Message.createProposal(
                    userText,
                    aiProposal.aiMessage(),
                    aiProposal.actionType(),
                    proposalJson
            );
        }

        return messageRepository.save(message);
    }
}
