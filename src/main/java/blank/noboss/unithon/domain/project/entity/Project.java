package blank.noboss.unithon.domain.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "projects")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    @Column(name = "project_topic", nullable = false, length = 200)
    private String projectTopic;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, columnDefinition = "text")
    private String description;
}
