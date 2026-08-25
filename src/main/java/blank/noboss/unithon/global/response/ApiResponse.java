package blank.noboss.unithon.global.response;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ApiResponse<T>(
        boolean success,
        int status,
        T data,
        OffsetDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                200,
                data,
                OffsetDateTime.now(ZoneOffset.ofHours(9))
        );
    }
}
