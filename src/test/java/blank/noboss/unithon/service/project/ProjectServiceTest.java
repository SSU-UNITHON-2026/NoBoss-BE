package blank.noboss.unithon.service.project;

import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.service.project.dto.ProjectCreateRequest;
import blank.noboss.unithon.service.project.dto.ProjectListResponse;
import blank.noboss.unithon.service.project.dto.ProjectResponse;
import blank.noboss.unithon.service.project.dto.ProjectUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createsProject() {
        ProjectCreateRequest request = createRequest();
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.teamName()).isEqualTo("B_LANK");
        assertThat(response.deadline()).isEqualTo(LocalDate.of(2026, 12, 11));
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void rejectsMissingCreateRequest() {
        assertError(() -> projectService.createProject(null), ErrorCode.INVALID_REQUEST);
        verifyNoInteractions(projectRepository);
    }

    @Test
    void returnsProjectsInIdOrderFromRepository() {
        Project first = project(1L, "B_LANK");
        Project second = project(2L, "NoBoss");
        when(projectRepository.findAllByOrderByIdAsc()).thenReturn(List.of(first, second));

        ProjectListResponse response = projectService.getProjects();

        assertThat(response.projects()).extracting(ProjectResponse::id).containsExactly(1L, 2L);
    }

    @Test
    void returnsProjectById() {
        Project project = project(2L, "NoBoss");
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProject(2L);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.teamName()).isEqualTo("NoBoss");
    }

    @Test
    void updatesOnlyProvidedProjectFields() {
        Project project = project(2L, "B_LANK");
        ProjectUpdateRequest request = new ProjectUpdateRequest("NoBoss", null, null, null, null);
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.updateProject(2L, request);

        verify(project).update(
                "NoBoss",
                "서비스디자인 캡스톤",
                "캠퍼스 중고거래 앱 UX 개선",
                LocalDate.of(2026, 12, 11),
                "프로젝트 설명"
        );
        assertThat(response.id()).isEqualTo(2L);
    }

    @Test
    void rejectsUpdateWithoutChanges() {
        ProjectUpdateRequest request = new ProjectUpdateRequest(null, null, null, null, null);

        assertError(() -> projectService.updateProject(1L, request), ErrorCode.INVALID_REQUEST);
        verifyNoInteractions(projectRepository);
    }

    @Test
    void rejectsBlankUpdateValue() {
        ProjectUpdateRequest request = new ProjectUpdateRequest(" ", null, null, null, null);

        assertError(() -> projectService.updateProject(1L, request), ErrorCode.INVALID_REQUEST);
        verifyNoInteractions(projectRepository);
    }

    @Test
    void throwsProjectNotFoundWhenIdDoesNotExist() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertError(() -> projectService.getProject(999L), ErrorCode.PROJECT_NOT_FOUND);
    }

    private ProjectCreateRequest createRequest() {
        return new ProjectCreateRequest(
                "B_LANK",
                "서비스디자인 캡스톤",
                "캠퍼스 중고거래 앱 UX 개선",
                LocalDate.of(2026, 12, 11),
                "교내 중고거래 과정의 불편함을 개선하는 UX 프로젝트"
        );
    }

    private Project project(Long id, String teamName) {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(id);
        when(project.getTeamName()).thenReturn(teamName);
        when(project.getSubjectName()).thenReturn("서비스디자인 캡스톤");
        when(project.getProjectTopic()).thenReturn("캠퍼스 중고거래 앱 UX 개선");
        when(project.getDeadline()).thenReturn(LocalDate.of(2026, 12, 11));
        when(project.getDescription()).thenReturn("프로젝트 설명");
        return project;
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode)
                );
    }
}
