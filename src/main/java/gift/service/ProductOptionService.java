package gift.service;

import gift.dto.NewProductOptionCommand;
import gift.dto.ProductOptionDto;
import gift.dto.UpdateProductOptionCommand;
import gift.entity.Product;
import gift.entity.ProductOption;
import gift.exception.NotFoundException;
import gift.repository.ProductOptionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductOptionService {

    private final ProductService productService;
    private final ProductOptionRepository productOptionRepository;

    public ProductOptionService(ProductService productService,
            ProductOptionRepository productOptionRepository) {
        this.productService = productService;
        this.productOptionRepository = productOptionRepository;
    }

    // Create
    @Transactional
    public ProductOptionDto addProductOption(NewProductOptionCommand newProductOptionCommand) {
        Product product = productService.findProductByIdAndNotDeleted(
                newProductOptionCommand.productId());

        ProductOption productOption = product.addOption(newProductOptionCommand.name(),
                newProductOptionCommand.quantity());
        return ProductOptionDto.from(productOptionRepository.save(productOption));
    }

    // Read
    // 동일 패키지 내 사용 제한
    ProductOption findProductOptionById(Long id) {
        return productOptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product option not found: id=" + id));
    }

    ProductOption findProductOptionMatchesToProduct(Long id, Long productId) {
        ProductOption productOption = findProductOptionById(id);
        if (!productOption.getProduct().getId().equals(productId)) {
            throw new NotFoundException("Product option does not belong to product: id=" + id
                    + ", productId=" + productId);
        }
        return productOption;
    }

    @Transactional(readOnly = true)
    public ProductOptionDto getProductOptionById(Long id, Long productId) {
        ProductOption productOption = findProductOptionMatchesToProduct(id, productId);
        return ProductOptionDto.from(productOption);
    }

    public List<ProductOptionDto> getProductOptionList(Long productId) {
        Product product = productService.findProductByIdAndNotDeleted(productId);
        List<ProductOption> productOptionList = product.getOptionList();
        return productOptionList.stream().map(ProductOptionDto::from).toList();
    }

    // Update
    @Transactional
    public ProductOptionDto updateProductOption(
            UpdateProductOptionCommand updateProductOptionCommand) {
        ProductOption productOption = findProductOptionMatchesToProduct(
                updateProductOptionCommand.id(), updateProductOptionCommand.productId());
        Product product = productOption.getProduct();
        ProductOption updated = product.updateOption(updateProductOptionCommand.id(),
                updateProductOptionCommand.name(), updateProductOptionCommand.quantity());
        return ProductOptionDto.from(updated);
    }

    // Delete
    @Transactional
    public void deleteProductOption(Long id, Long productId) {
        ProductOption productOption = findProductOptionMatchesToProduct(id, productId);
        Product product = productOption.getProduct();
        product.deleteOption(id);
    }
}