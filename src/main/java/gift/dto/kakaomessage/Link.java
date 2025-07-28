package gift.dto.kakaomessage;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record Link(
        String webUrl,
        String mobileWebUrl
) {
    public Link(String commonUrl) {
        this(commonUrl, commonUrl);
    }
}
