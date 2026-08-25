package blank.noboss.unithon.service.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MessageRequest(
        @Schema(
                description = "사용자가 입력한 자연어 메시지",
                example = "사용자 인터뷰 마감을 9월 4일로 미뤄줘"
        )
        String text
) {
}
