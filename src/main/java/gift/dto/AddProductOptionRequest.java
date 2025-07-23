package gift.dto;

import gift.entity.ProductOption;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddProductOptionRequest(
        @NotNull(message = "Name should be presented")
        @Size(min = 1, max = 50, message = "Name should be between 1 and 50 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ ()\\[\\]+\\-&/_]*$",
                message = "허용되지 않는 문자열이 포함되어있습니다. 상품명은 한글, 영문자, 숫자, 공백 및 ()[]+-&/_만 사용 가능합니다."
        )
        String name,

        @NotNull(message = "Quantity should be presented")
        @Positive(message = "Quantity must be a positive number at the posting")
        @Max(value = 100_000_000 - 1, message = "Quantity must be less than 100,000,000")
        Integer quantity
) {

    public static AddProductOptionRequest from(ProductOption productOption) {
        return new AddProductOptionRequest(
                productOption.getName(),
                productOption.getQuantity()
        );
    }

}
