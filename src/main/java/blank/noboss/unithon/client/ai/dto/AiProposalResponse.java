package blank.noboss.unithon.client.ai.dto;

public record AiProposalResponse(
        String aiMessage,
        String actionType,
        boolean requiresApproval,
        AiProposalDetail proposal
) {
}
