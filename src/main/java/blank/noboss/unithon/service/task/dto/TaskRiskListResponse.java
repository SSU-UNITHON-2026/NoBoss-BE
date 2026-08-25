package blank.noboss.unithon.service.task.dto;

import java.util.List;

public record TaskRiskListResponse(
        List<TaskRiskResponse> risks
) {
    public TaskRiskListResponse {
        risks = List.copyOf(risks);
    }
}
