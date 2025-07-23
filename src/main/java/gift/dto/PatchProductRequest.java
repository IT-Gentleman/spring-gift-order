package gift.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PatchProductRequest(
        @Size(min = 0, max = 15, message = "상품명은 15자 이내여야 합니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ ()\\[\\]+\\-&/_]*$",
                message = "허용되지 않는 문자열이 포함되어있습니다. 상품명은 한글, 영문자, 숫자, 공백 및 ()[]+-&/_만 사용 가능합니다."
        )
        String name,

        @Positive(message = "상품 가격은 음수이거나 0일 수 없습니다.")
        Integer price,

        @Size(min = 0, max = 255, message = "상품 이미지 URL은 255자 이내여야 합니다.")
        String imageUrl
) {

}