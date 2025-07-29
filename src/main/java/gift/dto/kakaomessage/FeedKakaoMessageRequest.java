package gift.dto.kakaomessage;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record FeedKakaoMessageRequest(
        String objectType,
        Content content
) {
    public FeedKakaoMessageRequest(Content content) {
        this("feed", content);
    }
}
