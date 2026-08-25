package blank.noboss.unithon.client.ai.dto;

public record AiProposalRequest(
        String currentDate,
        String userText,
        AiProjectContext project
) {
}
