package blank.noboss.unithon.client.ai.dto;

public record AiProposalDetail(
        Long taskId,
        Integer stage,
        String stageName,
        String title,
        String owner,
        String dueDate,
        String teamName,
        String subjectName,
        String projectTopic,
        String description,
        String deadline
) {
}
