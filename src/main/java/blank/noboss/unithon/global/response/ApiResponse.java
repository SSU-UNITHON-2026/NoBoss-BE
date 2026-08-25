package blank.noboss.unithon.global.response;

import java.time.OffsetDateTime;

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
                OffsetDateTime.now()
        );
    }
}