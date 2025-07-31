package gift.dto.kakaomessage;

import gift.dto.order.OrderDto;

public record SendKakaoMessageRequest(
        String messageTitle,
        Long senderKakaoId,
        Long receiverKakaoId,
        String messageContent,
        String imageUrl,
        String linkUrl
) {
    public static SendKakaoMessageRequest from(OrderDto orderDto) {
        return new SendKakaoMessageRequest(
                orderDto.getPresentTitle(),
                orderDto.senderMember().getId(),
                orderDto.receiverMember().getId(),
                orderDto.getPresentMessage(),
                orderDto.productOption().getProduct().getImageUrl(),
                orderDto.productOption().getProduct().getImageUrl()
        );
    }

}
