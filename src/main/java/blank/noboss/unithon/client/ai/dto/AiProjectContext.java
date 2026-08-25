package blank.noboss.unithon.client.ai.dto;

import java.util.List;

public record AiProjectContext(
        Long id,
        String teamName,
        String subjectName,
        String projectTopic,
        String deadline,
        String description,
        List<AiTaskContext> tasks,
        AiPendingProposal pendingProposal
) {
    public AiProjectContext {
        tasks = List.copyOf(tasks);
    }
}
