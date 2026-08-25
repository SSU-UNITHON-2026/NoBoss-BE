package blank.noboss.unithon.service.project;

import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.service.project.dto.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void returnsCurrentProject() {
        Project project = org.mockito.Mockito.mock(Project.class);
        LocalDate deadline = LocalDate.of(2026, 12, 11);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(project.getId()).thenReturn(1L);
        when(project.getTeamName()).thenReturn("서비스디자인 캡스톤");
        when(project.getSubjectName()).thenReturn("서비스디자인 캡스톤");
        when(project.getProjectTopic()).thenReturn("캠퍼스 중고거래 앱 UX 개선");
        when(project.getDeadline()).thenReturn(deadline);
        when(project.getDescription()).thenReturn("프로젝트 설명");

        ProjectResponse response = projectService.getProject();

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.teamName()).isEqualTo("서비스디자인 캡스톤");
        assertThat(response.deadline()).isEqualTo(deadline);
    }

    @Test
    void throwsInternalServerErrorWhenCurrentProjectDoesNotExist() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(projectService::getProject)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR)
                );
    }
}
