package gift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import gift.dto.NewWishCommand;
import gift.dto.WishDto;
import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Role;
import gift.entity.Wish;
import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.repository.WishRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class WishServiceTest {

    @Mock
    private WishRepository wishRepository;

    @Mock
    private ProductService productService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private WishService wishService;

    private Member member;
    private Product product;
    private Wish wish;
    private NewWishCommand newWishCommand;

    @BeforeEach
    void setUp() {
        member = new Member(1L, "test@example.com", "password", Role.ROLE_USER);
        product = new Product(1L, "product", 1000, "image.jpg", true, false);
        wish = new Wish(member, product);
        newWishCommand = new NewWishCommand(member.getId(), product.getId());
    }

    @Nested
    @DisplayName("WishService addWishItem() - 위시리스트 아이템 추가 테스트")
    class AddWishItemTests {

        @Test
        @DisplayName("정상적인 위시리스트 아이템 추가")
        void 정상적인_위시리스트_아이템_추가() {
            // given
            when(memberService.findMemberById(member.getId())).thenReturn(member);
            when(productService.findProductByIdAndNotDeleted(product.getId())).thenReturn(product);
            when(wishRepository.existsByMemberIdAndProductId(member.getId(),
                    product.getId())).thenReturn(false);
            when(wishRepository.save(any(Wish.class))).thenReturn(new Wish(member, product));

            // when
            WishDto wishDto = wishService.addWishItem(newWishCommand);

            // then
            assertThat(wishDto.productId()).isEqualTo(product.getId());
        }

        @Test
        @DisplayName("이미 존재하는 위시리스트 아이템 추가 시 예외 발생")
        void 이미_존재하는_위시리스트_아이템_추가_시_예외_발생() {
            // given
            when(memberService.findMemberById(member.getId())).thenReturn(member);
            when(productService.findProductByIdAndNotDeleted(product.getId())).thenReturn(product);
            when(wishRepository.existsByMemberIdAndProductId(member.getId(),
                    product.getId())).thenReturn(true);

            // when & then
            assertThrows(ConflictException.class,
                    () -> wishService.addWishItem(newWishCommand));
        }
    }

    @Nested
    @DisplayName("WishService getWishListByMemberId() - 위시리스트 조회 테스트")
    class GetWishListByMemberIdTests {

        @Test
        @DisplayName("정상적인 위시리스트 조회")
        void 정상적인_위시리스트_조회() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Wish> wishes = List.of(wish);
            Page<Wish> wishPage = new PageImpl<>(wishes, pageable, wishes.size());
            when(wishRepository.findAllByMemberId(member.getId(), pageable)).thenReturn(wishPage);

            // when
            Page<WishDto> result = wishService.getWishListByMemberId(1L, pageable);

            // then
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getContent()).hasSize(1),
                    () -> assertThat(result.getContent().get(0).productId()).isEqualTo(
                            product.getId())
            );
        }
    }

    @Nested
    @DisplayName("void removeWishItemByWishId() - 위시리스트 아이템 삭제 테스트")
    class RemoveWishItemByWishIdTests {

        @Test
        @DisplayName("위시리스트 아이템 삭제")
        void 위시리스트_아이템_삭제() {
            // given
            when(wishRepository.findById(wish.getId())).thenReturn(Optional.of(wish));

            // when & then
            assertDoesNotThrow(() -> wishService.removeWishItemByWishId(
                    member.getId(), wish.getId()));
        }

        @Test
        @DisplayName("존재하지 않는 위시리스트 아이템 삭제 시 예외 발생")
        void 존재하지_않는_위시리스트_아이템_삭제_시_예외_발생() {
            // given
            when(wishRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThrows(NotFoundException.class, () -> wishService.removeWishItemByWishId(
                    member.getId(), 999L));
        }

        @Test
        @DisplayName("다른 회원의 위시리스트 아이템 삭제 시 예외 발생")
        void 다른_회원의_위시리스트_아이템_삭제_시_예외_발생() {
            // given
            Long otherMemberId = 999L;
            when(wishRepository.findById(wish.getId())).thenReturn(Optional.of(wish));

            // when & then
            assertThrows(NotFoundException.class,
                    () -> wishService.removeWishItemByWishId(otherMemberId,
                            wish.getId()));
        }
    }


}