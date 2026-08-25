package blank.noboss.unithon.global.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successResponseContainsCommonFields() {
        ApiResponse<String> response = ApiResponse.success("data");

        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.data()).isEqualTo("data");
        assertThat(response.timestamp().getOffset().getTotalSeconds()).isEqualTo(9 * 60 * 60);
    }

    @Test
    void createdResponseContainsCreatedStatus() {
        ApiResponse<String> response = ApiResponse.created("data");

        assertThat(response.success()).isTrue();
        assertThat(response.status()).isEqualTo(201);
        assertThat(response.data()).isEqualTo("data");
        assertThat(response.timestamp().getOffset().getTotalSeconds()).isEqualTo(9 * 60 * 60);
    }
}
