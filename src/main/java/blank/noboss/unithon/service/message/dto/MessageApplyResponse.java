package blank.noboss.unithon.service.message.dto;

import blank.noboss.unithon.domain.message.enums.ActionType;
import blank.noboss.unithon.service.project.dto.ProjectResponse;
import blank.noboss.unithon.service.task.dto.TaskResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageApplyResponse(
        ActionType actionType,
        TaskResponse task,
        ProjectResponse project
) {
    public static MessageApplyResponse task(ActionType actionType, TaskResponse task) {
        return new MessageApplyResponse(actionType, task, null);
    }

    public static MessageApplyResponse project(ProjectResponse project) {
        return new MessageApplyResponse(ActionType.PROJECT_UPDATE, null, project);
    }
}
