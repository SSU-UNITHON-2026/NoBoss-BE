package blank.noboss.unithon.service.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskUpdateRequest(
        @Min(1)
        @Max(5)
        @Schema(example = "3")
        Integer stage,

        @Size(max = 200)
        @Schema(example = "사용자 인터뷰 결과 정리")
        String title,

        @Size(max = 100)
        @Schema(example = "윤세아")
        String owner,

        @Schema(example = "2026-09-15")
        LocalDate dueDate
) {
}
