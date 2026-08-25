package blank.noboss.unithon.client.ai;

import blank.noboss.unithon.client.ai.dto.AiProposalRequest;
import blank.noboss.unithon.client.ai.dto.AiProposalResponse;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class RailwayAiProposalClient implements AiProposalClient {

    private final RestClient aiRestClient;

    @Override
    public AiProposalResponse generate(AiProposalRequest request) {
        try {
            AiProposalResponse response = aiRestClient.post()
                    .uri("/proposal/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiProposalResponse.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_PROCESSING_FAILED);
            }

            return response;
        } catch (ResourceAccessException exception) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_GENERATION_FAILED);
        } catch (RestClientResponseException exception) {
            throw mapResponseException(exception.getStatusCode());
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_PROCESSING_FAILED);
        }
    }

    private BusinessException mapResponseException(HttpStatusCode statusCode) {
        if (statusCode.is5xxServerError()) {
            return new BusinessException(ErrorCode.AI_RESPONSE_GENERATION_FAILED);
        }
        return new BusinessException(ErrorCode.AI_RESPONSE_PROCESSING_FAILED);
    }
}
