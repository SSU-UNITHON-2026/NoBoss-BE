package blank.noboss.unithon.service.message;

import blank.noboss.unithon.domain.message.entity.Message;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import blank.noboss.unithon.repository.message.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagePersistenceServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ProposalJsonMapper proposalJsonMapper;

    @InjectMocks
    private MessagePersistenceService persistenceService;

    @Test
    void savesNoneResponseWithoutSupersedingPendingProposal() {
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message saved = persistenceService.save(
                "마감일 언제야?",
                new NormalizedAiProposal("12월 11일이에요.", ActionType.NONE, false, null)
        );

        assertThat(saved.getActionType()).isEqualTo(ActionType.NONE);
        assertThat(saved.getProposalStatus()).isNull();
    }

    @Test
    void supersedesPreviousPendingProposalWhenSavingNewProposal() {
        Message pending = Message.createProposal(
                "업무 추가해줘",
                "추가할까요?",
                ActionType.TASK_CREATE,
                "{\"title\":\"기존 제안\"}"
        );
        Map<String, Object> proposal = Map.of(
                "stage", 2,
                "stageName", "리서치",
                "title", "새 제안",
                "owner", "윤세아",
                "dueDate", "2026-09-04"
        );
        when(messageRepository.findFirstByProposalStatusOrderByCreatedAtDescIdDesc(ProposalStatus.PENDING))
                .thenReturn(Optional.of(pending));
        when(proposalJsonMapper.write(proposal)).thenReturn("{\"title\":\"새 제안\"}");
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message saved = persistenceService.save(
                "새 업무 추가해줘",
                new NormalizedAiProposal("추가할까요?", ActionType.TASK_CREATE, true, proposal)
        );

        assertThat(pending.getProposalStatus()).isEqualTo(ProposalStatus.SUPERSEDED);
        assertThat(saved.getProposalStatus()).isEqualTo(ProposalStatus.PENDING);
        verify(proposalJsonMapper).write(proposal);
    }
}
