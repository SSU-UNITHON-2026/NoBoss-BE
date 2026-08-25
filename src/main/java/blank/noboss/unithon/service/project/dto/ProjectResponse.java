package blank.noboss.unithon.service.project.dto;

import blank.noboss.unithon.domain.project.entity.Project;

import java.time.LocalDate;

public record ProjectResponse(
        Long id,
        String teamName,
        String subjectName,
        String projectTopic,
        LocalDate deadline,
        String description
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTeamName(),
                project.getSubjectName(),
                project.getProjectTopic(),
                project.getDeadline(),
                project.getDescription()
        );
    }
}
