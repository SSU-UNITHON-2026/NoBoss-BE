package blank.noboss.unithon.service.message;

import blank.noboss.unithon.client.ai.dto.AiProposalDetail;
import blank.noboss.unithon.client.ai.dto.AiProposalResponse;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class AiProposalNormalizer {

    public NormalizedAiProposal normalize(AiProposalResponse response, Set<Long> currentTaskIds) {
        if (response == null || isBlank(response.aiMessage())) {
            throw invalidAiResponse();
        }

        ActionType actionType = parseActionType(response.actionType());

        if (actionType == ActionType.NONE) {
            if (response.requiresApproval() || response.proposal() != null) {
                throw invalidAiResponse();
            }
            return new NormalizedAiProposal(response.aiMessage(), actionType, false, null);
        }

        if (!response.requiresApproval() || response.proposal() == null) {
            throw invalidAiResponse();
        }

        Map<String, Object> proposal = switch (actionType) {
            case TASK_CREATE -> normalizeTaskCreate(response.proposal());
            case TASK_UPDATE -> normalizeTaskUpdate(response.proposal(), currentTaskIds);
            case PROJECT_UPDATE -> normalizeProjectUpdate(response.proposal());
            case NONE -> throw invalidAiResponse();
        };

        return new NormalizedAiProposal(response.aiMessage(), actionType, true, proposal);
    }

    private Map<String, Object> normalizeTaskCreate(AiProposalDetail detail) {
        validateTaskFields(detail);

        Map<String, Object> proposal = new LinkedHashMap<>();
        proposal.put("stage", detail.stage());
        proposal.put("stageName", detail.stageName());
        proposal.put("title", detail.title());
        proposal.put("owner", detail.owner());
        proposal.put("dueDate", parseDate(detail.dueDate()).toString());
        return proposal;
    }

    private Map<String, Object> normalizeTaskUpdate(AiProposalDetail detail, Set<Long> currentTaskIds) {
        validateTaskFields(detail);
        if (detail.taskId() == null || !currentTaskIds.contains(detail.taskId())) {
            throw invalidAiResponse();
        }

        Map<String, Object> proposal = new LinkedHashMap<>();
        proposal.put("taskId", detail.taskId());
        proposal.put("stage", detail.stage());
        proposal.put("stageName", detail.stageName());
        proposal.put("title", detail.title());
        proposal.put("owner", detail.owner());
        proposal.put("dueDate", parseDate(detail.dueDate()).toString());
        return proposal;
    }

    private Map<String, Object> normalizeProjectUpdate(AiProposalDetail detail) {
        if (isBlank(detail.teamName())
                || isBlank(detail.subjectName())
                || isBlank(detail.projectTopic())
                || isBlank(detail.description())) {
            throw invalidAiResponse();
        }

        Map<String, Object> proposal = new LinkedHashMap<>();
        proposal.put("teamName", detail.teamName());
        proposal.put("subjectName", detail.subjectName());
        proposal.put("projectTopic", detail.projectTopic());
        proposal.put("deadline", parseDate(detail.deadline()).toString());
        proposal.put("description", detail.description());
        return proposal;
    }

    private void validateTaskFields(AiProposalDetail detail) {
        if (detail.stage() == null
                || detail.stage() <= 0
                || isBlank(detail.stageName())
                || isBlank(detail.title())
                || isBlank(detail.owner())) {
            throw invalidAiResponse();
        }
        parseDate(detail.dueDate());
    }

    private ActionType parseActionType(String value) {
        try {
            return ActionType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw invalidAiResponse();
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw invalidAiResponse();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BusinessException invalidAiResponse() {
        return new BusinessException(ErrorCode.AI_RESPONSE_PROCESSING_FAILED);
    }
}
