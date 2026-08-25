package blank.noboss.unithon.service.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectUpdateRequest(
        @Size(max = 100)
        @Schema(example = "B_LANK")
        String teamName,

        @Size(max = 100)
        @Schema(example = "서비스디자인 캡스톤")
        String subjectName,

        @Size(max = 200)
        @Schema(example = "캠퍼스 중고거래 앱 UX 개선")
        String projectTopic,

        @Schema(example = "2026-12-20")
        LocalDate deadline,

        @Schema(example = "교내 중고거래 과정의 불편함을 개선하는 UX 프로젝트")
        String description
) {
}
