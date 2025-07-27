package gift.service;

import gift.dto.order.CreateOrderCommand;
import gift.dto.order.OrderDto;
import gift.entity.Member;
import gift.entity.Order;
import gift.entity.ProductOption;
import gift.exception.BadRequestException;
import gift.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private final ProductOptionService productOptionService;
    private final MemberService memberService;
    private final OrderRepository orderRepository;

    public OrderService(ProductOptionService productOptionService,
            MemberService memberService, OrderRepository orderRepository) {
        this.productOptionService = productOptionService;
        this.memberService = memberService;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderDto createOrder(CreateOrderCommand command) {
        Member senderMember = memberService
                .findMemberByIdNotDeleted(command.senderMemberId());
        Member recieverMember = command.senderMemberId().equals(command.receiverMemberId()) ?
                senderMember : memberService.findMemberByIdNotDeleted(command.receiverMemberId());
        ProductOption productOption = productOptionService
                .findProductOptionById(command.productOptionId());

        // 수량 감소 시도
        try {
            productOption.decreaseQuantity(command.quantity());
        } catch (IllegalStateException e) {
            // 수량이 부족한 경우
            throw new BadRequestException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Order quantity should be a positive number.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 위시리스트 반영
        recieverMember.removeWishByProductId(productOption.getProduct().getId());

        // 주문 생성
        Order createdOrder = orderRepository.save(
                new Order(
                        productOption,
                        senderMember.getId(),
                        recieverMember.getId(),
                        command.quantity(),
                        command.message()
                )
        );
        //orderRepository.flush();
        return OrderDto.from(createdOrder, senderMember, recieverMember);
    }
}