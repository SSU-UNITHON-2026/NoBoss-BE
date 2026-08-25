package blank.noboss.unithon.global.response;

import blank.noboss.unithon.global.exception.ErrorCode;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public record ErrorResponse(
        boolean success,
        int status,
        String code,
        String message,
        String path,
        OffsetDateTime timestamp,
        List<Object> errors
) {
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                false,
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                path,
                OffsetDateTime.now(ZoneOffset.ofHours(9)),
                List.of()
        );
    }
}
