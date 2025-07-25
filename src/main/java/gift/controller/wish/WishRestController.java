package gift.controller.wish;

import gift.dto.wish.AddWishRequest;
import gift.dto.common.AuthenticatedMember;
import gift.dto.wish.NewWishCommand;
import gift.dto.common.PageRequest;
import gift.dto.common.PageResponse;
import gift.dto.wish.WishDto;
import gift.dto.wish.WishResponse;
import gift.service.WishService;
import gift.validator.LoginMember;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishes")
public class WishRestController {

    private final WishService wishService;

    public WishRestController(WishService wishService) {
        this.wishService = wishService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<WishResponse>> getWishList(
            @Valid PageRequest pageRequest,
            @LoginMember AuthenticatedMember member
    ) {
        Set<String> allowedSortFields = Set.of("createdAt", "product.name", "product.price");
        String defaultSortField = "createdAt";
        Sort.Direction defaultSortDirection = Sort.Direction.DESC;
        Pageable pageable = pageRequest.toPageable(allowedSortFields, defaultSortField,
                defaultSortDirection);

        Page<WishDto> wishPage = wishService.getWishListByMemberId(member.id(), pageable);
        Page<WishResponse> wishResponsePage = wishPage.map(wish -> WishResponse.from(wish));
        PageResponse<WishResponse> pageResponse = PageResponse.from(wishResponsePage);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(pageResponse);
    }

    @PostMapping
    public ResponseEntity<WishResponse> addWishItem(
            @LoginMember AuthenticatedMember member,
            @RequestBody @Valid AddWishRequest request
    ) {
        NewWishCommand wishCommand = new NewWishCommand(member.id(), request.productId());
        WishDto created = wishService.addWishItem(wishCommand);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/wish/" + created.id())
                .body(WishResponse.from(created));
    }

    @DeleteMapping("/{wishItemId}")
    public ResponseEntity<Void> deleteWishItem(
            @LoginMember AuthenticatedMember member,
            @PathVariable Long wishItemId
    ) {
        wishService.removeWishItemByWishId(member.id(), wishItemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
