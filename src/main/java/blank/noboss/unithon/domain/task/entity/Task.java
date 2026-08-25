package blank.noboss.unithon.domain.task.entity;

import blank.noboss.unithon.domain.project.entity.Project;
import blank.noboss.unithon.domain.task.enums.TaskStage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "tasks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Integer stage;

    @Column(name = "stage_name", nullable = false, length = 100)
    private String stageName;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String owner;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean done;

    private Task(
            Project project,
            TaskStage stage,
            String title,
            String owner,
            LocalDate dueDate
    ) {
        this.project = project;
        this.stage = stage.getNumber();
        this.stageName = stage.getStageName();
        this.title = title;
        this.owner = owner;
        this.dueDate = dueDate;
        this.done = false;
    }

    public static Task create(
            Project project,
            TaskStage stage,
            String title,
            String owner,
            LocalDate dueDate
    ) {
        return new Task(project, stage, title, owner, dueDate);
    }

    public void updateDetails(
            TaskStage stage,
            String title,
            String owner,
            LocalDate dueDate
    ) {
        this.stage = stage.getNumber();
        this.stageName = stage.getStageName();
        this.title = title;
        this.owner = owner;
        this.dueDate = dueDate;
    }

    public void updateDone(boolean done) {
        this.done = done;
    }
}
