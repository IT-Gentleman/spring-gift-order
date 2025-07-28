package gift.dto.kakaomessage;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record FeedKakaoMessageRequest(
        String objectType,
        Content content
) {
    public FeedKakaoMessageRequest(String title, String imageUrl, String description, String commonUrl) {
        this("feed", new Content(title, imageUrl, description, commonUrl));
    }
}
