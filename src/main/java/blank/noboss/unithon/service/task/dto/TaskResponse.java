package blank.noboss.unithon.service.task.dto;

import blank.noboss.unithon.domain.task.entity.Task;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        Integer stage,
        String stageName,
        String title,
        String owner,
        LocalDate dueDate,
        boolean done
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getStage(),
                task.getStageName(),
                task.getTitle(),
                task.getOwner(),
                task.getDueDate(),
                task.isDone()
        );
    }
}
