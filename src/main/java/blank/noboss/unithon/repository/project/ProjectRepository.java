package blank.noboss.unithon.repository.project;

import blank.noboss.unithon.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
