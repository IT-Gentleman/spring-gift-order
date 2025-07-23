package gift.controller;

import gift.dto.CreateProductRequest;
import gift.dto.NewProductCommand;
import gift.dto.NewProductOptionCommand;
import gift.dto.PageRequest;
import gift.dto.PageResponse;
import gift.dto.PatchProductRequest;
import gift.dto.ProductDto;
import gift.dto.ProductResponse;
import gift.dto.UpdateProductCommand;
import gift.service.ProductService;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        NewProductCommand command = new NewProductCommand(
                request.name(),
                request.price(),
                request.imageUrl(),
                request.options().stream().map(NewProductOptionCommand::from).toList()
        );
        ProductDto dto = productService.createProduct(command);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/products/" + dto.id())
                .body(ProductResponse.from(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id
    ) {
        ProductDto dto = productService.getProductById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ProductResponse.from(dto));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @Valid PageRequest pageRequest) {

        Set<String> allowedSortFields = Set.of("id", "name", "price");
        String defaultSortField = "id";
        Sort.Direction defaultSortDirection = Sort.Direction.ASC;
        Pageable pageable = pageRequest.toPageable(allowedSortFields, defaultSortField,
                defaultSortDirection);

        Page<ProductDto> pagedDto = productService.getProductList(true, pageable);
        Page<ProductResponse> response = pagedDto.map(ProductResponse::from);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(PageResponse.from(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProductById(
            @PathVariable Long id,
            @Valid @RequestBody PatchProductRequest patch
    ) {
        UpdateProductCommand command = new UpdateProductCommand(
                id,
                patch.name(),
                patch.price(),
                patch.imageUrl()
        );
        ProductDto dto = productService.updateProductById(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ProductResponse.from(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(
            @PathVariable Long id
    ) {
        productService.softDeleteProductById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
