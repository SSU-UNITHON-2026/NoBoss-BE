package blank.noboss.unithon.repository.task;

import blank.noboss.unithon.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
