package gift.service;

import gift.dto.NewWishCommand;
import gift.dto.WishDto;
import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Wish;
import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.repository.WishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishService {

    private final WishRepository wishRepository;
    private final ProductService productService;
    private final MemberService memberService;

    public WishService(WishRepository wishRepository, ProductService productService,
            MemberService memberService) {
        this.wishRepository = wishRepository;
        this.productService = productService;
        this.memberService = memberService;
    }

    // Create
    @Transactional
    public WishDto addWishItem(NewWishCommand wishCommand) {
        Member member = memberService.findMemberById(wishCommand.memberId());
        Product product = productService.findProductByIdAndNotDeleted(wishCommand.productId());

        if (wishRepository.existsByMemberIdAndProductId(member.getId(), product.getId())) {
            throw new ConflictException("You already added this product to wishlist: productId="
                    + product.getId() + "(product name=" + product.getName() + ")");
        }
        Wish wish = new Wish(member, product);
        return WishDto.from(wishRepository.save(wish));
    }

    // Read
    @Transactional(readOnly = true)
    public Page<WishDto> getWishListByMemberId(Long memberId, Pageable pageable) {
        Page<Wish> wishPage = wishRepository.findAllByMemberId(memberId, pageable);
        return wishPage.map(wish -> WishDto.from(wish));
    }

    // Update

    // Delete
    @Transactional
    public void removeWishItemByWishId(Long memberId, Long wishId) {
        // 삭제하고자 하는 wish 조회
        Wish wish = wishRepository.findById(wishId)
                .orElseThrow(() -> new NotFoundException("Wish not found: id=" + wishId));
        // wish의 소유자가 본인인지 확인
        if (!wish.getMember().getId().equals(memberId)) {
            // 본인소유가 아닌 wish의 경우는 hiding 처리됨
            throw new NotFoundException("Wish not found: id=" + wishId);
        }
        // 본인소유인 경우 삭제
        wishRepository.deleteById(wishId);
    }
}
