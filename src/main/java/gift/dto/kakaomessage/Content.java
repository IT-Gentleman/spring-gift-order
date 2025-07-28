package gift.dto.kakaomessage;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record Content(
        String title,
        String imageUrl,
        String description,
        Link link
) {
    public Content(String title, String imageUrl, String description, String commonUrl) {
        this(title, imageUrl, description, new Link(commonUrl));
    }

}
