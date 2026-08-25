package blank.noboss.unithon.global.exception;

import blank.noboss.unithon.global.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionUsesSpecifiedErrorCodeAndRequestPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/projects/2/tasks/999/done");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(
                new BusinessException(ErrorCode.TASK_NOT_FOUND),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().code()).isEqualTo("TASK001");
        assertThat(response.getBody().message()).isEqualTo("업무를 찾을 수 없습니다.");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/projects/2/tasks/999/done");
        assertThat(response.getBody().errors()).isEmpty();
        assertThat(response.getBody().timestamp().getOffset().getTotalSeconds()).isEqualTo(9 * 60 * 60);
    }
}
