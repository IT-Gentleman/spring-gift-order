package gift.dto.order;

public record CreateOrderCommand(
    Long productOptionId,
    Integer quantity,
    Long senderMemberId,
    Long receiverMemberId,
    String message
) {

}
