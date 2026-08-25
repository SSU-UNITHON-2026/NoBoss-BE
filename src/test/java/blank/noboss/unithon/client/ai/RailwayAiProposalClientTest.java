package blank.noboss.unithon.client.ai;

import blank.noboss.unithon.client.ai.dto.AiProjectContext;
import blank.noboss.unithon.client.ai.dto.AiProposalRequest;
import blank.noboss.unithon.client.ai.dto.AiProposalResponse;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RailwayAiProposalClientTest {

    private MockRestServiceServer server;
    private RailwayAiProposalClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://ai.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RailwayAiProposalClient(builder.build());
    }

    @Test
    void parsesSuccessfulProposalResponse() {
        server.expect(once(), requestTo("https://ai.test/proposal/generate"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "aiMessage": "마감일을 변경할까요?",
                          "actionType": "TASK_UPDATE",
                          "requiresApproval": true,
                          "proposal": {
                            "taskId": 2,
                            "stage": 2,
                            "stageName": "리서치",
                            "title": "사용자 인터뷰 5명 진행",
                            "owner": "윤세아",
                            "dueDate": "2026-09-04",
                            "teamName": null
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        AiProposalResponse response = client.generate(request());

        assertThat(response.actionType()).isEqualTo("TASK_UPDATE");
        assertThat(response.proposal().taskId()).isEqualTo(2L);
        assertThat(response.proposal().dueDate()).isEqualTo("2026-09-04");
        server.verify();
    }

    @Test
    void mapsServerErrorToAiGenerationError() {
        server.expect(requestTo("https://ai.test/proposal/generate"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.generate(request()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_GENERATION_FAILED)
                );
    }

    @Test
    void mapsClientErrorToAiProcessingError() {
        server.expect(requestTo("https://ai.test/proposal/generate"))
                .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> client.generate(request()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_PROCESSING_FAILED)
                );
    }

    private AiProposalRequest request() {
        return new AiProposalRequest(
                "2026-08-26",
                "마감을 변경해줘",
                new AiProjectContext(
                        1L,
                        "B_LANK",
                        "서비스디자인 캡스톤",
                        "캠퍼스 중고거래 앱 UX 개선",
                        "2026-12-11",
                        "프로젝트 설명",
                        List.of(),
                        null
                )
        );
    }
}
