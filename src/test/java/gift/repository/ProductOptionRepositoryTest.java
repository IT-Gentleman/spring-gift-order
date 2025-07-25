package gift.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.entity.Product;
import gift.entity.ProductOption;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
public class ProductOptionRepositoryTest {

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = productRepository.save(
                new Product(
                        null,
                        "existing product",
                        5000,
                        "http://image.com/existing.png",
                        true,
                        false
                )
        );
    }

    @Nested
    @DisplayName("ProductOption save() - 상품 옵션 생성 테스트")
    class SaveProductOption {

        @Test
        @DisplayName("정상적인 상품 옵션 데이터 삽입")
        void 정상적인_상품_옵션_삽입_시_정상반환() {
            ProductOption productOption = new ProductOption(
                    "Example Option",
                    20192,
                    existingProduct
            );
            ProductOption savedProductOption = productOptionRepository.save(productOption);

            assertAll(
                    () -> assertThat(savedProductOption).isNotNull(),
                    () -> assertThat(savedProductOption.getId()).isNotNull(),
                    () -> assertThat(
                            productOptionDetailsEquals(productOption, savedProductOption)).isTrue()
            );
        }

        @Test
        @DisplayName("동일한 상품에 동일한 이름의 옵션 추가 시 예외 발생")
        void saveWithDuplicateName_thenThrowsException() {
            // given
            ProductOption option1 = new ProductOption("Duplicate Name", 100, existingProduct);
            productOptionRepository.save(option1);

            // when
            ProductOption option2 = new ProductOption("Duplicate Name", 200, existingProduct);

            // then
            assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(
                    () -> productOptionRepository.saveAndFlush(option2));
        }
    }

    @Nested
    @DisplayName("ProductOption 제약 조건 테스트")
    class ConstraintValidationTest {

        @DisplayName("유효한 수량 값(0, 1, 1억-1)으로 저장 성공")
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100_000_000 - 1})
        void 유효한_수량_값으로_저장_성공(int quantity) {
            // given
            ProductOption productOption = new ProductOption("Valid Quantity Option", quantity,
                    existingProduct);

            // when
            ProductOption savedOption = productOptionRepository.save(productOption);

            // then
            assertAll(
                    () -> assertThat(savedOption).isNotNull(),
                    () -> assertThat(savedOption.getId()).isNotNull(),
                    () -> assertThat(savedOption.getName()).isEqualTo(productOption.getName()),
                    () -> assertThat(savedOption.getProduct()).isEqualTo(existingProduct)
            );
        }

        @DisplayName("유효하지 않은 수량 값(-1, 1억)으로 저장 시 예외 발생")
        @ParameterizedTest
        @ValueSource(ints = {-1, 100_000_000})
        void 유효하지_않은_수량_값으로_저장_시_예외_발생(int quantity) {
            // given
            ProductOption productOption = new ProductOption("Invalid Quantity Option", quantity,
                    existingProduct);

            // when & then
            assertThatExceptionOfType(ConstraintViolationException.class).isThrownBy(
                    () -> productOptionRepository.saveAndFlush(productOption)
            );
        }
    }

    private boolean productOptionDetailsEquals(ProductOption actual, ProductOption expected) {
        return actual.getName().equals(expected.getName())
                && actual.getQuantity().equals(expected.getQuantity())
                && actual.getProduct().equals(expected.getProduct());
    }

}
