package blank.noboss.unithon.service.message.dto;

import blank.noboss.unithon.domain.message.enums.ActionType;

import java.util.Map;

public record MessageResponse(
        Long messageId,
        String aiMessage,
        ActionType actionType,
        boolean requiresApproval,
        Map<String, Object> proposal
) {
}
