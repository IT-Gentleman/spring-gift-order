package gift.controller.order;

import gift.dto.common.AuthenticatedMember;
import gift.dto.order.CreateOrderCommand;
import gift.dto.order.CreateOrderRequest;
import gift.dto.order.OrderDto;
import gift.dto.order.OrderResponse;
import gift.service.OrderService;
import gift.validator.LoginMember;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;

    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
            @LoginMember AuthenticatedMember member) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.optionId(),
                request.quantity(),
                member.id(),
                member.id(), // reciever를 sender와 동일하게 설정
                request.message()
        );
        OrderDto orderDto = orderService.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(orderDto));
    }
}
