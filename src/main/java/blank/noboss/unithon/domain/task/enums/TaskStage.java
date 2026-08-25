package blank.noboss.unithon.domain.task.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum TaskStage {

    TOPIC_SELECTION(1, "주제 선정"),
    RESEARCH(2, "리서치"),
    DRAFT(3, "초안 작성"),
    FEEDBACK(4, "피드백 반영"),
    FINAL_SUBMISSION(5, "최종본 제출");

    private final int number;
    private final String stageName;

    public static Optional<TaskStage> fromNumber(Integer number) {
        if (number == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(stage -> stage.number == number)
                .findFirst();
    }
}
