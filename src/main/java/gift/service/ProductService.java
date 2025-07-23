package gift.service;

import gift.dto.NewProductCommand;
import gift.dto.ProductDto;
import gift.dto.UpdateProductCommand;
import gift.entity.Product;
import gift.entity.ProductOption;
import gift.exception.BadRequestException;
import gift.exception.NotFoundException;
import gift.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create
    @Transactional
    public ProductDto createProduct(NewProductCommand command) {
        if (command.options().isEmpty()) {
            throw new BadRequestException("Product must have at least one option.");
        }
        Product product = new Product(command.name(), command.price(), command.imageUrl(),
                command.options().stream().map(
                                option -> new ProductOption(option.name(), option.quantity(), null))
                        .toList()
        );
        return ProductDto.from(productRepository.save(product));
    }

    // Read
    // 동일 패키지 내 사용 제한
    Product findProductByIdAndNotDeleted(Long id) {
        return productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id));
    }

    // 동일 패키지 내 사용 제한
    Product findProductByIdIncludingDeleted(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id));
    }

    // for normal users
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = findProductByIdAndNotDeleted(id);
        return ProductDto.from(product);
    }

    // for md users
    @Transactional(readOnly = true)
    public ProductDto getProductWhetherDeletedById(Long id) {
        Product product = findProductByIdIncludingDeleted(id);
        return ProductDto.from(product);
    }

    // TODO : validated T/F로 나누지 말고, 위 처럼 whetherDeleted로 나누는 걸로 변경 (findAll 사용)
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductList(Boolean validated, Pageable pageable) {
        Page<Product> pageProduct = productRepository.findAllByDeletedAtIsNullAndValidated(
                validated,
                pageable);
        return pageProduct.map(ProductDto::from);
    }

    // Update

    @Transactional
    public ProductDto updateProductById(UpdateProductCommand command) {
        Product product = findProductByIdAndNotDeleted(command.id());
        product.applyPatch(command.name(), command.price(), command.imageUrl());
        return ProductDto.from(product);
    }

    @Transactional
    public void setProductValidated(Long id, Boolean validated) {
        Product product = findProductByIdAndNotDeleted(id);
        product.setValidated(validated);
    }

    // Delete
    @Transactional
    public void softDeleteProductById(Long id) {
        Product product = findProductByIdAndNotDeleted(id);
        product.setDeleted();
    }
}
