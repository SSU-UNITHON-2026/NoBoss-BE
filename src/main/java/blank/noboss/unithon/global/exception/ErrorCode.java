package blank.noboss.unithon.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON001", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다."),

    MESSAGE_TEXT_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE001", "메시지를 입력해주세요."),
    MESSAGE_TEXT_BLANK(HttpStatus.BAD_REQUEST, "MESSAGE002", "메시지는 공백일 수 없습니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE003", "제안을 찾을 수 없습니다."),
    MESSAGE_NO_CHANGES(HttpStatus.BAD_REQUEST, "MESSAGE004", "승인할 변경사항이 없습니다."),
    MESSAGE_ALREADY_APPLIED(HttpStatus.CONFLICT, "MESSAGE005", "이미 반영된 제안입니다."),

    AI_RESPONSE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI001", "AI 응답 생성에 실패했습니다."),
    AI_RESPONSE_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI002", "AI 응답을 처리할 수 없습니다."),

    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK001", "업무를 찾을 수 없습니다."),
    TASK_DONE_REQUIRED(HttpStatus.BAD_REQUEST, "TASK002", "완료 여부는 필수입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
