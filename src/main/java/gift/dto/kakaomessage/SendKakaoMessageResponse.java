package gift.dto.kakaomessage;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SendKakaoMessageResponse(
        Integer resultCode
) {

}
