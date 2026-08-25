package blank.noboss.unithon.service.message;

import blank.noboss.unithon.domain.message.enums.ActionType;

import java.util.Map;

public record NormalizedAiProposal(
        String aiMessage,
        ActionType actionType,
        boolean requiresApproval,
        Map<String, Object> proposal
) {
}
