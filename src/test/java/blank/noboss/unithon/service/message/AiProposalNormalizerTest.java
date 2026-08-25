package blank.noboss.unithon.service.message;

import blank.noboss.unithon.client.ai.dto.AiProposalDetail;
import blank.noboss.unithon.client.ai.dto.AiProposalResponse;
import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiProposalNormalizerTest {

    private final AiProposalNormalizer normalizer = new AiProposalNormalizer();

    @Test
    void normalizesNoneResponse() {
        AiProposalResponse response = new AiProposalResponse(
                "프로젝트 마감일은 12월 11일이에요.",
                "NONE",
                false,
                null
        );

        NormalizedAiProposal normalized = normalizer.normalize(response, Set.of());

        assertThat(normalized.actionType()).isEqualTo(ActionType.NONE);
        assertThat(normalized.requiresApproval()).isFalse();
        assertThat(normalized.proposal()).isNull();
    }

    @Test
    void removesUnrelatedFieldsFromTaskUpdateProposal() {
        AiProposalDetail detail = new AiProposalDetail(
                2L,
                2,
                "리서치",
                "사용자 인터뷰 5명 진행",
                "윤세아",
                "2026-09-04",
                null,
                null,
                null,
                null,
                null
        );
        AiProposalResponse response = new AiProposalResponse(
                "마감일을 변경할까요?",
                "TASK_UPDATE",
                true,
                detail
        );

        NormalizedAiProposal normalized = normalizer.normalize(response, Set.of(2L));

        assertThat(normalized.proposal())
                .containsEntry("taskId", 2L)
                .containsEntry("dueDate", "2026-09-04")
                .doesNotContainKeys("teamName", "subjectName", "projectTopic", "description", "deadline");
    }

    @Test
    void rejectsTaskUpdateWithUnknownTaskId() {
        AiProposalDetail detail = new AiProposalDetail(
                999L,
                2,
                "리서치",
                "존재하지 않는 업무",
                "윤세아",
                "2026-09-04",
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> normalizer.normalize(
                new AiProposalResponse("변경할까요?", "TASK_UPDATE", true, detail),
                Set.of(1L, 2L)
        )).isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_PROCESSING_FAILED)
        );
    }

    @Test
    void rejectsUnsupportedActionType() {
        assertThatThrownBy(() -> normalizer.normalize(
                new AiProposalResponse("삭제할까요?", "TASK_DELETE", true, null),
                Set.of()
        )).isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_PROCESSING_FAILED)
        );
    }

    @Test
    void rejectsApprovalForNoneResponse() {
        assertThatThrownBy(() -> normalizer.normalize(
                new AiProposalResponse("답변", "NONE", true, null),
                Set.of()
        )).isInstanceOf(BusinessException.class);
    }
}
