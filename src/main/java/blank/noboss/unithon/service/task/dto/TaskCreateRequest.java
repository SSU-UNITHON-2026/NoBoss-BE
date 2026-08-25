package blank.noboss.unithon.service.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskCreateRequest(
        @NotNull
        @Min(1)
        @Max(5)
        @Schema(example = "2")
        Integer stage,

        @NotBlank
        @Size(max = 200)
        @Schema(example = "사용자 인터뷰 결과 정리")
        String title,

        @NotBlank
        @Size(max = 100)
        @Schema(example = "정하람")
        String owner,

        @NotNull
        @Schema(example = "2026-09-10")
        LocalDate dueDate
) {
}
