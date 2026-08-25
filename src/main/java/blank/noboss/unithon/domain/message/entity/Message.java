package blank.noboss.unithon.domain.message.entity;

import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_text", nullable = false, columnDefinition = "text")
    private String userText;

    @Column(name = "ai_message", nullable = false, columnDefinition = "text")
    private String aiMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String proposal;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposal_status", length = 30)
    private ProposalStatus proposalStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private Message(
            String userText,
            String aiMessage,
            ActionType actionType,
            boolean requiresApproval,
            String proposal,
            ProposalStatus proposalStatus
    ) {
        this.userText = userText;
        this.aiMessage = aiMessage;
        this.actionType = actionType;
        this.requiresApproval = requiresApproval;
        this.proposal = proposal;
        this.proposalStatus = proposalStatus;
    }

    public static Message createAnswer(String userText, String aiMessage) {
        return new Message(userText, aiMessage, ActionType.NONE, false, null, null);
    }

    public static Message createProposal(
            String userText,
            String aiMessage,
            ActionType actionType,
            String proposal
    ) {
        return new Message(userText, aiMessage, actionType, true, proposal, ProposalStatus.PENDING);
    }

    public void supersede() {
        if (proposalStatus == ProposalStatus.PENDING) {
            proposalStatus = ProposalStatus.SUPERSEDED;
        }
    }
}
