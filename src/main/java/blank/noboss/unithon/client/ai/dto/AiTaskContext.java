package blank.noboss.unithon.client.ai.dto;

public record AiTaskContext(
        Long taskId,
        Integer stage,
        String stageName,
        String title,
        String owner,
        String dueDate,
        boolean done
) {
}
