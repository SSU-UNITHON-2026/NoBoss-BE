package blank.noboss.unithon.domain.message.entity;

import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.domain.message.enums.ProposalStatus;
import blank.noboss.unithon.domain.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

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
            Project project,
            String userText,
            String aiMessage,
            ActionType actionType,
            boolean requiresApproval,
            String proposal,
            ProposalStatus proposalStatus
    ) {
        this.project = project;
        this.userText = userText;
        this.aiMessage = aiMessage;
        this.actionType = actionType;
        this.requiresApproval = requiresApproval;
        this.proposal = proposal;
        this.proposalStatus = proposalStatus;
    }

    public static Message createAnswer(Project project, String userText, String aiMessage) {
        return new Message(project, userText, aiMessage, ActionType.NONE, false, null, null);
    }

    public static Message createProposal(
            Project project,
            String userText,
            String aiMessage,
            ActionType actionType,
            String proposal
    ) {
        return new Message(project, userText, aiMessage, actionType, true, proposal, ProposalStatus.PENDING);
    }

    public void supersede() {
        if (proposalStatus == ProposalStatus.PENDING) {
            proposalStatus = ProposalStatus.SUPERSEDED;
        }
    }

    public void apply() {
        this.proposalStatus = ProposalStatus.APPLIED;
    }
}
