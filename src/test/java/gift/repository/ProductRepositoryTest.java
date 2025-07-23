package gift.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gift.config.AuditingTestConfig;
import gift.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import(AuditingTestConfig.class)
class ProductRepositoryTest {

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
    @DisplayName("Product save() - 상품 생성 테스트")
    class saveTests {

        @Test
        @DisplayName("정상적인 상품 데이터 삽입")
        void 정상적인_상품_삽입_시_정상반환() {
            Product exampleProduct = new Product(
                    null,
                    "Test Product",
                    5000,
                    "http://kakao.com/test.png",
                    true,
                    false
            );
            Product savedProduct = productRepository.save(exampleProduct);
            assertAll(
                    () -> assertThat(savedProduct.getId()).isNotNull(),
                    () -> assertThat(productDetailsEquals(savedProduct, exampleProduct)).isTrue()
            );
        }

        @Test
        @DisplayName("null 값이 포함된 상품 데이터 삽입 시 예외 발생")
        void 널_값이_포함된_상품_삽입_시_예외_발생() {
            // id는 항상 null인 상태로 삽입되어야 함

            Product allNullProduct = new Product(null, null, null, null, null, null);
            assertThrows(DataIntegrityViolationException.class,
                    () -> productRepository.save(allNullProduct));

            Product nameNullProduct = new Product(null, null, 5000, "http://kakao.com", true,
                    false);
            assertThrows(DataIntegrityViolationException.class,
                    () -> productRepository.save(nameNullProduct));

            Product priceNullProduct = new Product(null, "Test Product", null, "http://kakao.com",
                    true, false);
            assertThrows(DataIntegrityViolationException.class,
                    () -> productRepository.save(priceNullProduct));

            Product imageUrlNullProduct = new Product(null, "Test Product", 5000, null, true,
                    false);
            assertThrows(DataIntegrityViolationException.class,
                    () -> productRepository.save(imageUrlNullProduct));

            Product validatedNullProduct = new Product(null, "Test Product", 5000,
                    "http://kakao.com", null, false);
            assertThrows(DataIntegrityViolationException.class,
                    () -> productRepository.save(validatedNullProduct));
        }
    }

    @Nested
    @DisplayName("Optional<Product> findByIdAndDeletedAtIsNull(Long id) - 일반사용자 상품 조회 테스트")
    class findByIdTests {

        @Test
        @DisplayName("삭제되지 않은 상품의 ID로 조회 시 상품 반환")
        void 존재하는_상품의_ID로_조회_시_상품반환() {
            assertThat(productRepository.findByIdAndDeletedAtIsNull(
                    existingProduct.getId())).isPresent();
        }

        @Test
        @DisplayName("삭제된 상품의 ID로 조회 시 빈 Optional 반환")
        void 삭제된_상품의_ID로_조회_시_빈Optional반환() {
            existingProduct.setDeleted();
            assertThat(
                    productRepository.findByIdAndDeletedAtIsNull(
                            existingProduct.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 상품의 ID로 조회 시 빈 Optional 반환")
        void 존재하지_않는_상품의_ID로_조회_시_빈Optional반환() {
            assertThat(productRepository.findByIdAndDeletedAtIsNull(500L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("List<Product> findAllByDeletedAtIsNullAndValidated(Boolean visibility) - 상품 목록 조회 테스트 (visibility=false is used only for MD)")
    class findAllByValidatedTests {

        @Test
        @DisplayName("deleted가 false이며 validated가 true인 상품 목록 조회 시 정상 반환")
        void deleted가_false이며_validated가_true인_상품_목록_조회_시_정상반환() {
            assertThat(
                    productRepository.findAllByDeletedAtIsNullAndValidated(true, null)).hasSize(
                    1);
            assertThat(
                    productRepository.findAllByDeletedAtIsNullAndValidated(false, null)).hasSize(
                    0);
        }

        @Test
        @DisplayName("validated가 false인 상품은 목록에 포함되지 않음")
        void deleted가_false이며_validated가_false인_상품_추가_후_상품_목록_조회_시_정상반환() {
            existingProduct.setValidated(false);
            assertThat(
                    productRepository.findAllByDeletedAtIsNullAndValidated(true, null)).hasSize(
                    0);
            assertThat(
                    productRepository.findAllByDeletedAtIsNullAndValidated(false, null)).hasSize(
                    1);
        }

        @Test
        @DisplayName("deleted가 true인 상품은 목록에 포함되지 않음")
        void deleted가_true인_상품은_목록에_포함되지_않음() {
            existingProduct.setDeleted();
            assertThat(
                    productRepository.findAllByDeletedAtIsNullAndValidated(true, null)).hasSize(
                    0);
            assertThat(
                    productRepository.findAllByDeletedAtIsNullAndValidated(false, null)).hasSize(
                    0);
        }

    }

    private boolean productDetailsEquals(Product product1, Product product2) {
        return product1.getName().equals(product2.getName()) &&
                product1.getPrice().equals(product2.getPrice()) &&
                product1.getImageUrl().equals(product2.getImageUrl()) &&
                product1.isValidated() == product2.isValidated() &&
                product1.isDeleted() == product2.isDeleted();
    }

}