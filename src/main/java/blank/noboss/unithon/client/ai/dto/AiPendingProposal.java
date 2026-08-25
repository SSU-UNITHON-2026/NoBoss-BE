package blank.noboss.unithon.client.ai.dto;

import blank.noboss.unithon.domain.message.enums.ActionType;

import java.util.Map;

public record AiPendingProposal(
        Long messageId,
        ActionType actionType,
        Map<String, Object> proposal
) {
}
