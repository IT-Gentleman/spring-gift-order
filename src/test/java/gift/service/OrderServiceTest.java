package gift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import gift.dto.order.CreateOrderCommand;
import gift.dto.order.OrderDto;
import gift.entity.Member;
import gift.entity.Order;
import gift.entity.Product;
import gift.entity.ProductOption;
import gift.entity.Wish;
import gift.exception.BadRequestException;
import gift.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductOptionService productOptionService;

    @Mock
    private MemberService memberService;

    @Mock
    private KakaoMessageService kakaoMessageService;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("createOrder 메서드")
    class CreateOrderTests {

        Member kakaoMember = new Member(1L, 121L, null);
        Product product = new Product(1L, "Test Product", 10000, "sample-image-url", true, false);
        ProductOption productOption = new ProductOption(1L, "Test Option", 10, product);

        @Test
        @DisplayName("주문 생성 성공 - 위시리스트 비어있음")
        void createOrder_Success() {
            // arrange
            CreateOrderCommand command = new CreateOrderCommand(
                    productOption.getId(),
                    1,
                    kakaoMember.getId(),
                    kakaoMember.getId(),
                    "Sample Message"
            );

            when(memberService.findMemberByIdNotDeleted(kakaoMember.getId())).thenReturn(
                    kakaoMember);
            when(productOptionService.findProductOptionById(productOption.getId())).thenReturn(
                    productOption);
            when(orderRepository.save(any())).thenReturn(new Order(
                    productOption,
                    kakaoMember.getId(),
                    kakaoMember.getId(),
                    command.quantity(),
                    command.message()
            ));

            // act
            OrderDto orderDto = orderService.createOrder(command);

            // assert
            assertAll(
                    () -> assertThat(orderDto.message()).isEqualTo(command.message()),
                    () -> assertThat(orderDto.productOption()).isEqualTo(productOption),
                    () -> assertThat(orderDto.senderMember()).isEqualTo(kakaoMember),
                    () -> assertThat(orderDto.receiverMember()).isEqualTo(kakaoMember),
                    () -> assertThat(orderDto.quantity()).isEqualTo(command.quantity())
            );
        }

        @Test
        @DisplayName("주문 생성 성공 - 위시리스트에 있어서 제거됨")
        void createOrder_Success_WithWishlistRemoval() {
            // arrange
            Wish wish = new Wish(kakaoMember, product);
            kakaoMember = new Member(1L, 121L, new ArrayList<>(List.of(wish)));
            CreateOrderCommand command = new CreateOrderCommand(
                    productOption.getId(),
                    1,
                    kakaoMember.getId(),
                    kakaoMember.getId(),
                    "Sample Message"
            );

            when(memberService.findMemberByIdNotDeleted(kakaoMember.getId())).thenReturn(
                    kakaoMember);
            when(productOptionService.findProductOptionById(productOption.getId())).thenReturn(
                    productOption);
            when(orderRepository.save(any())).thenReturn(new Order(
                    productOption,
                    kakaoMember.getId(),
                    kakaoMember.getId(),
                    command.quantity(),
                    command.message()
            ));

            // act
            OrderDto orderDto = orderService.createOrder(command);

            // assert
            assertAll(
                    () -> assertThat(orderDto.message()).isEqualTo(command.message()),
                    () -> assertThat(orderDto.productOption()).isEqualTo(productOption),
                    () -> assertThat(orderDto.senderMember()).isEqualTo(kakaoMember),
                    () -> assertThat(orderDto.receiverMember()).isEqualTo(kakaoMember),
                    () -> assertThat(orderDto.quantity()).isEqualTo(command.quantity()),
                    () -> assertThat(kakaoMember.getWishCount()).isEqualTo(0)
            );
        }

        @Test
        @DisplayName("주문 생성 실패 - 수량 부족")
        void createOrder_Failure_InsufficientQuantity() {
            // arrange
            CreateOrderCommand command = new CreateOrderCommand(
                    productOption.getId(),
                    100,
                    kakaoMember.getId(),
                    kakaoMember.getId(),
                    "Sample Message"
            );

            when(memberService.findMemberByIdNotDeleted(kakaoMember.getId())).thenReturn(
                    kakaoMember);
            when(productOptionService.findProductOptionById(productOption.getId())).thenReturn(
                    productOption);

            // act & assert
            assertThrows(BadRequestException.class, () -> orderService.createOrder(command));
        }

        @Test
        @DisplayName("주문 생성 실패 - 카카오 메시지 전송 실패")
        void createOrder_Failure_KakaoMessageSend() {
            // arrange
            CreateOrderCommand command = new CreateOrderCommand(
                    productOption.getId(),
                    1,
                    kakaoMember.getId(),
                    kakaoMember.getId(),
                    "Sample Message"
            );

            when(memberService.findMemberByIdNotDeleted(kakaoMember.getId())).thenReturn(
                    kakaoMember);
            when(productOptionService.findProductOptionById(productOption.getId())).thenReturn(
                    productOption);
            when(orderRepository.save(any())).thenReturn(new Order(
                    productOption,
                    kakaoMember.getId(),
                    kakaoMember.getId(),
                    command.quantity(),
                    command.message()
            ));
            ResponseStatusException exception = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            doThrow(exception).when(
                    kakaoMessageService).sendMessageToSelf(any(), any());

            // act & assert
            assertThrows(exception.getClass(), () -> orderService.createOrder(command));
        }

    }

}
