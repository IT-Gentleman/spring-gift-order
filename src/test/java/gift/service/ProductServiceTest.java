package gift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import gift.dto.NewProductCommand;
import gift.dto.ProductDto;
import gift.entity.Product;
import gift.exception.NotFoundException;
import gift.repository.ProductRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("Product createProduct() - 상품 생성 테스트")
    class CreateProductTests {

        @Test
        @DisplayName("옵션과 함께 상품 생성 시 성공")
        void createProductWithOptions_Success() {
            String name = "Test Product";
            Integer price = 10000;
            String imageUrl = "http://example.com/image.jpg";
            var options = java.util.List.of(
                    new gift.dto.NewProductOptionCommand("Option 1", 10, null));
            Product expectedProduct = new Product(1L, name, price, imageUrl, false, false);
            when(productRepository.save(any(Product.class))).thenReturn(expectedProduct);

            NewProductCommand command = new NewProductCommand(name, price, imageUrl, options);
            ProductDto result = productService.createProduct(command);

            assertThat(result).isEqualTo(ProductDto.from(expectedProduct));
        }

        @Test
        @DisplayName("옵션 없이 상품 생성 시 BadRequestException 발생")
        void createProductWithoutOptions_ThrowsBadRequestException() {
            NewProductCommand command = new NewProductCommand("Test", 100, "url",
                    java.util.Collections.emptyList());

            assertThrows(gift.exception.BadRequestException.class, () -> {
                productService.createProduct(command);
            });
        }
    }

    @Nested
    @DisplayName("Product getProductById() - 상품 조회 테스트")
    class GetProductByIdTests {

        @Test
        @DisplayName("존재하는 상품 ID로 조회 시 상품 반환")
        void 존재하는상품ID로조회시_상품반환() {
            Long productId = 1L;
            Product expectedProduct = new Product(productId, "Test Product", 10000,
                    "http://example.com/image.jpg", false, false);
            when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(
                    Optional.of(expectedProduct));

            assertThat(productService.getProductById(productId)).isEqualTo(
                    ProductDto.from(expectedProduct));
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회 시 예외 발생")
        void 존재하지않는상품ID로조회시_예외발생() {
            Long productId = 999L;
            when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(
                    Optional.empty());

            assertThrows(NotFoundException.class, () -> productService.getProductById(productId));
        }
    }

    @Nested
    @DisplayName("Product getProductWhetherDeletedById() - 상품 조회 테스트 (MD 전용)")
    class GetProductWhetherDeletedByIdTests {

        @Test
        @DisplayName("존재하는 상품 ID로 조회 시 상품 반환")
        void 존재하는상품ID로조회시_상품반환() {
            Long productId = 1L;
            Product expectedProduct = new Product(productId, "Test Product", 10000,
                    "http://example.com/image.jpg", false, false);
            when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

            assertThat(productService.getProductWhetherDeletedById(productId)).isEqualTo(
                    ProductDto.from(expectedProduct));
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회 시 예외 발생")
        void 존재하지않는상품ID로조회시_예외발생() {
            Long productId = 999L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> productService.getProductWhetherDeletedById(productId));
        }
    }

    // 예외처리하지 않는 단순 CRUD 테스트 생략
}