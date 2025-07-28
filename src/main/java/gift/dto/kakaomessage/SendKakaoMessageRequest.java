package gift.dto.kakaomessage;

import gift.dto.order.OrderDto;

public record SendKakaoMessageRequest(
        Long senderKakaoId,
        Long receiverKakaoId,
        String messageContent,
        String imageUrl,
        String linkUrl
) {
    public static SendKakaoMessageRequest from(OrderDto orderDto) {
        return new SendKakaoMessageRequest(
                orderDto.senderMember().getId(),
                orderDto.receiverMember().getId(),
                orderDto.senderMember().getEmail() + " send you '" + orderDto.productOption().getName() + "'(Quantity: " + orderDto.quantity() + ")!\n" + orderDto.message(),
                orderDto.productOption().getProduct().getImageUrl(),
                orderDto.productOption().getProduct().getImageUrl()
        );
    }

}
