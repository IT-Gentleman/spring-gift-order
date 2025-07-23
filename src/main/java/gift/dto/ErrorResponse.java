package gift.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {

    public ErrorResponse(HttpStatus status, String message) {
        this(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
    }
}
