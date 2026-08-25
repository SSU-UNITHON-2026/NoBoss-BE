package blank.noboss.unithon.global.response;

import java.time.OffsetDateTime;

public record ErrorResponse(
        boolean success,
        int status,
        String code,
        String message,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(
                false,
                status,
                code,
                message,
                OffsetDateTime.now()
        );
    }
}