package gift.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import gift.dto.common.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoAuthRequest(
        String code,
        String error,
        String errorDescription//,
        //String state
) {

    public HttpStatus getHttpStatusOfError() {
        return switch (error) {
            case "KOE001", "KOE002", "KOE008", "KOE101", "KOE012", "KOE201", "KOE203",
                 "KOE204", "KOE205", "KOE207", "access_denied" -> HttpStatus.BAD_REQUEST;
            case "KOE003", "KOE023" -> HttpStatus.INTERNAL_SERVER_ERROR;
            case "KOE004", "KOE005", "KOE006", "KOE007" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.NOT_ACCEPTABLE;
        };
    }

}
