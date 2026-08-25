package blank.noboss.unithon.service.project.dto;

import java.util.List;

public record ProjectListResponse(
        List<ProjectResponse> projects
) {
}
