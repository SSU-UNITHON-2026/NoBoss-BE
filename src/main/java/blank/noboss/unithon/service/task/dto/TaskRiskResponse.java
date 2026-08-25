package blank.noboss.unithon.service.task.dto;

import blank.noboss.unithon.domain.task.entity.Task;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record TaskRiskResponse(
        Long taskId,
        Integer stage,
        String stageName,
        String taskTitle,
        String owner,
        LocalDate dueDate,
        long daysRemaining
) {
    public static TaskRiskResponse from(Task task, LocalDate today) {
        return new TaskRiskResponse(
                task.getId(),
                task.getStage(),
                task.getStageName(),
                task.getTitle(),
                task.getOwner(),
                task.getDueDate(),
                ChronoUnit.DAYS.between(today, task.getDueDate())
        );
    }
}
