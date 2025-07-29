package gift.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(

        @NotNull(message = "Option ID must not be null")
        Long optionId,

        @NotNull(message = "Quantity must not be null")
        @Positive(message = "Quantity must be a positive number")
        Integer quantity,

        @NotNull(message = "Message must not be null")
        String message
) {

}
