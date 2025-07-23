package gift.dto;

import gift.entity.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateProductRequest(
        @NotNull(message = "상품명은 필수적으로 작성되어야 합니다.")
        @Size(min = 0, max = 15, message = "상품명은 15자 이내여야 합니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ ()\\[\\]+\\-&/_]*$",
                message = "허용되지 않는 문자열이 포함되어있습니다. 상품명은 한글, 영문자, 숫자, 공백 및 ()[]+-&/_만 사용 가능합니다."
        )
        String name,

        @NotNull(message = "상품 가격은 필수적으로 작성되어야 합니다.")
        @Positive(message = "상품 가격은 음수이거나 0일 수 없습니다.")
        Integer price,

        @NotNull(message = "상품 이미지 URL은 필수적으로 작성되어야 합니다.")
        @Size(min = 0, max = 255, message = "상품 이미지 URL은 255자 이내여야 합니다.")
        String imageUrl,

        @NotEmpty
        @Valid
        List<AddProductOptionRequest> options
) {

    public static CreateProductRequest empty() {
        return new CreateProductRequest("", 1, "", List.of());
    }

    public static CreateProductRequest from(Product product) {
        return new CreateProductRequest(
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getOptionList().stream()
                        .map(AddProductOptionRequest::from)
                        .toList()
        );
    }
}