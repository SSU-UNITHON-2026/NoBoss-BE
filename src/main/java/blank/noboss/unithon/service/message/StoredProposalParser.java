package blank.noboss.unithon.service.message;

import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class StoredProposalParser {

    public TaskProposal parseTaskCreate(Map<String, Object> proposal) {
        return parseTask(proposal, false);
    }

    public TaskProposal parseTaskUpdate(Map<String, Object> proposal) {
        return parseTask(proposal, true);
    }

    public ProjectProposal parseProjectUpdate(Map<String, Object> proposal) {
        if (proposal == null) {
            throw invalidProposal();
        }

        return new ProjectProposal(
                requiredText(proposal, "teamName"),
                requiredText(proposal, "subjectName"),
                requiredText(proposal, "projectTopic"),
                requiredDate(proposal, "deadline"),
                requiredText(proposal, "description")
        );
    }

    private TaskProposal parseTask(Map<String, Object> proposal, boolean taskIdRequired) {
        if (proposal == null) {
            throw invalidProposal();
        }

        Long taskId = taskIdRequired ? requiredLong(proposal, "taskId") : null;
        Integer stage = requiredInteger(proposal, "stage");
        if (stage <= 0) {
            throw invalidProposal();
        }

        return new TaskProposal(
                taskId,
                stage,
                requiredText(proposal, "stageName"),
                requiredText(proposal, "title"),
                requiredText(proposal, "owner"),
                requiredDate(proposal, "dueDate")
        );
    }

    private String requiredText(Map<String, Object> proposal, String key) {
        Object value = proposal.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw invalidProposal();
        }
        return text;
    }

    private Long requiredLong(Map<String, Object> proposal, String key) {
        Object value = proposal.get(key);
        if (!(value instanceof Number number) || number.longValue() <= 0) {
            throw invalidProposal();
        }
        return number.longValue();
    }

    private Integer requiredInteger(Map<String, Object> proposal, String key) {
        Object value = proposal.get(key);
        if (!(value instanceof Number number)) {
            throw invalidProposal();
        }
        return number.intValue();
    }

    private LocalDate requiredDate(Map<String, Object> proposal, String key) {
        try {
            return LocalDate.parse(requiredText(proposal, key));
        } catch (DateTimeParseException exception) {
            throw invalidProposal();
        }
    }

    private BusinessException invalidProposal() {
        return new BusinessException(ErrorCode.AI_RESPONSE_PROCESSING_FAILED);
    }

    public record TaskProposal(
            Long taskId,
            Integer stage,
            String stageName,
            String title,
            String owner,
            LocalDate dueDate
    ) {
    }

    public record ProjectProposal(
            String teamName,
            String subjectName,
            String projectTopic,
            LocalDate deadline,
            String description
    ) {
    }
}
