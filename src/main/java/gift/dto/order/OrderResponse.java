package gift.dto.order;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long optionId,
        Integer quantity,
        LocalDateTime orderDateTime,
        String message
) {
    public static OrderResponse from(OrderDto orderDto) {
        return new OrderResponse(
                orderDto.id(),
                orderDto.productOption().getId(),
                orderDto.quantity(),
                orderDto.orderDateTime(),
                orderDto.message()
        );
    }
}
