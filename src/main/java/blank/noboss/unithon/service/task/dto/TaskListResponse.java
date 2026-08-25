package blank.noboss.unithon.service.task.dto;

import java.util.List;

public record TaskListResponse(
        List<TaskResponse> tasks
) {
    public TaskListResponse {
        tasks = List.copyOf(tasks);
    }
}
