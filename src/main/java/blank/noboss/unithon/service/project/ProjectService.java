package blank.noboss.unithon.service.project;

import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.service.project.dto.ProjectCreateRequest;
import blank.noboss.unithon.service.project.dto.ProjectListResponse;
import blank.noboss.unithon.service.project.dto.ProjectResponse;
import blank.noboss.unithon.service.project.dto.ProjectUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        validateCreateRequest(request);
        Project project = Project.create(
                request.teamName(),
                request.subjectName(),
                request.projectTopic(),
                request.deadline(),
                request.description()
        );
        return ProjectResponse.from(projectRepository.save(project));
    }

    public ProjectListResponse getProjects() {
        return new ProjectListResponse(
                projectRepository.findAllByOrderByIdAsc().stream()
                        .map(ProjectResponse::from)
                        .toList()
        );
    }

    public ProjectResponse getProject(Long projectId) {
        return ProjectResponse.from(findProject(projectId));
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request) {
        validateUpdateRequest(request);
        Project project = findProject(projectId);
        project.update(
                valueOrCurrent(request.teamName(), project.getTeamName()),
                valueOrCurrent(request.subjectName(), project.getSubjectName()),
                valueOrCurrent(request.projectTopic(), project.getProjectTopic()),
                request.deadline() == null ? project.getDeadline() : request.deadline(),
                valueOrCurrent(request.description(), project.getDescription())
        );

        return ProjectResponse.from(project);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private void validateCreateRequest(ProjectCreateRequest request) {
        if (request == null
                || isBlank(request.teamName())
                || isBlank(request.subjectName())
                || isBlank(request.projectTopic())
                || request.deadline() == null
                || isBlank(request.description())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateUpdateRequest(ProjectUpdateRequest request) {
        if (request == null || hasNoChanges(request)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (isProvidedBlank(request.teamName())
                || isProvidedBlank(request.subjectName())
                || isProvidedBlank(request.projectTopic())
                || isProvidedBlank(request.description())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private boolean hasNoChanges(ProjectUpdateRequest request) {
        return request.teamName() == null
                && request.subjectName() == null
                && request.projectTopic() == null
                && request.deadline() == null
                && request.description() == null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isProvidedBlank(String value) {
        return value != null && value.isBlank();
    }

    private String valueOrCurrent(String value, String current) {
        return value == null ? current : value;
    }
}
