package blank.noboss.unithon.repository.task;

import blank.noboss.unithon.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByProjectIdOrderByStageAscDoneAscDueDateAscIdAsc(Long projectId);

    List<Task> findAllByProjectIdAndDoneFalseAndDueDateLessThanEqualOrderByDueDateAscIdAsc(
            Long projectId,
            LocalDate dueDate
    );

    Optional<Task> findByIdAndProjectId(Long id, Long projectId);
}
