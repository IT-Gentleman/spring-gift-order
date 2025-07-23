package gift.controller;

import gift.dto.AddProductOptionRequest;
import gift.dto.NewProductOptionCommand;
import gift.dto.PageRequest;
import gift.dto.PatchProductOptionRequest;
import gift.dto.ProductOptionDto;
import gift.dto.ProductOptionResponse;
import gift.dto.UpdateProductOptionCommand;
import gift.service.ProductOptionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/options")
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    public ProductOptionController(ProductOptionService productOptionService) {
        this.productOptionService = productOptionService;
    }

    // Create
    @PostMapping
    public ResponseEntity<ProductOptionResponse> create(@PathVariable Long productId,
            @RequestBody AddProductOptionRequest request) {
        NewProductOptionCommand command = new NewProductOptionCommand(
                request.name(),
                request.quantity(),
                productId
        );
        ProductOptionDto dto = productOptionService.addProductOption(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductOptionResponse.from(dto));
    }

    // Read
    @GetMapping
    public ResponseEntity<List<ProductOptionResponse>> readAll(@PathVariable Long productId,
            @Valid PageRequest pageRequest) {
        Set<String> allowedSortFields = Set.of("id", "name", "quantity");
        String defaultSortField = "id";
        Sort.Direction defaultSortDirection = Sort.Direction.ASC;
        Pageable pageable = pageRequest.toPageable(allowedSortFields, defaultSortField,
                defaultSortDirection);
        List<ProductOptionDto> dto = productOptionService.getProductOptionList(productId);
        List<ProductOptionResponse> responses = dto.stream().map(ProductOptionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{optionId}")
    public ResponseEntity<ProductOptionResponse> read(@PathVariable Long productId,
            @PathVariable Long optionId) {
        ProductOptionDto dto = productOptionService.getProductOptionById(optionId, productId);
        return ResponseEntity.ok(ProductOptionResponse.from(dto));
    }

    // Update
    @PatchMapping("/{optionId}")
    public ResponseEntity<ProductOptionResponse> update(@PathVariable Long productId,
            @PathVariable Long optionId, @RequestBody PatchProductOptionRequest request) {
        UpdateProductOptionCommand command = new UpdateProductOptionCommand(
                optionId,
                request.name(),
                request.quantity(),
                productId
        );
        ProductOptionDto dto = productOptionService.updateProductOption(command);
        return ResponseEntity.ok(ProductOptionResponse.from(dto));
    }

    // Delete
    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId,
            @PathVariable Long optionId) {
        productOptionService.deleteProductOption(optionId, productId);
        return ResponseEntity.noContent().build();
    }

}
