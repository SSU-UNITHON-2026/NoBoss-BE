package blank.noboss.unithon.service.message;

import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProposalJsonMapper {

    private final ObjectMapper objectMapper;

    public String write(Map<String, Object> proposal) {
        try {
            return objectMapper.writeValueAsString(proposal);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_PROCESSING_FAILED);
        }
    }

    public Map<String, Object> read(String proposal) {
        try {
            return objectMapper.readValue(proposal, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_PROCESSING_FAILED);
        }
    }
}
