package blank.noboss.unithon.service.message;

import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoredProposalParserTest {

    private final StoredProposalParser parser = new StoredProposalParser();

    @Test
    void parsesNormalizedTaskUpdateProposal() {
        StoredProposalParser.TaskProposal proposal = parser.parseTaskUpdate(Map.of(
                "taskId", 2,
                "stage", 2,
                "stageName", "리서치",
                "title", "사용자 인터뷰 5명 진행",
                "owner", "정하람",
                "dueDate", "2026-09-04"
        ));

        assertThat(proposal.taskId()).isEqualTo(2L);
        assertThat(proposal.dueDate()).isEqualTo(LocalDate.of(2026, 9, 4));
    }

    @Test
    void rejectsInvalidStoredProposal() {
        assertThatThrownBy(() -> parser.parseTaskCreate(Map.of(
                "stage", 0,
                "stageName", "리서치",
                "title", "업무",
                "owner", "정하람",
                "dueDate", "잘못된 날짜"
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_PROCESSING_FAILED)
                );
    }
}
