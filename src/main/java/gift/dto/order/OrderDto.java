package gift.dto.order;

import gift.entity.Member;
import gift.entity.Order;
import gift.entity.ProductOption;
import java.time.LocalDateTime;

public record OrderDto(
    Long id,
    ProductOption productOption,
    Member senderMember,
    Member receiverMember,
    Integer quantity,
    LocalDateTime orderDateTime,
    String message
) {
    public static OrderDto from(Order order, Member sender, Member receiver) {
        return new OrderDto(
            order.getId(),
            order.getProductOption(),
            sender,
            receiver,
            order.getQuantity(),
            order.getOrderDateTime(),
            order.getMessage()
        );
    }
}
