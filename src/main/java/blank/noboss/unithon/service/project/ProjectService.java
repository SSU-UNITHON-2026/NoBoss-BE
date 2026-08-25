package blank.noboss.unithon.service.project;

import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.global.exception.BusinessException;
import blank.noboss.unithon.global.exception.ErrorCode;
import blank.noboss.unithon.repository.project.ProjectRepository;
import blank.noboss.unithon.service.project.dto.ProjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private static final long CURRENT_PROJECT_ID = 1L;

    private final ProjectRepository projectRepository;

    public ProjectResponse getProject() {
        Project project = projectRepository.findById(CURRENT_PROJECT_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        return ProjectResponse.from(project);
    }
}
