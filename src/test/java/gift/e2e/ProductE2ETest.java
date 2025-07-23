package gift.e2e;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.config.AuditingTestConfig;
import gift.dto.CreateProductRequest;
import gift.dto.PageResponse;
import gift.dto.ProductResponse;
import gift.dto.UpdateProductRequest;
import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Role;
import gift.repository.MemberRepository;
import gift.repository.ProductRepository;
import gift.token.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Import(AuditingTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductE2ETest {

    private final String baseUrl = "http://localhost:";

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    private RestClient restClient;
    private String mdToken;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create();
        Member md = memberRepository.save(
                new Member(null, "md@example.com", "mdpassword123456789", Role.ROLE_MD));
        mdToken = jwtTokenProvider.createToken(md);
        savedProduct = productRepository.save(
                new Product(null, "Initial Product", 10000, "initial.jpg", true, false));
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/products - 상품생성 테스트")
    class CreateProduct {

        String url = baseUrl + port + "/api/products";

        @Test
        @DisplayName("POST /api/products - 유효한 생성 시 201 CREATED")
        void 유효한_생성_시_201_CREATED() {
            var options = java.util.List.of(new gift.dto.AddProductOptionRequest("Option 1", 10));
            CreateProductRequest requestDto = new CreateProductRequest(
                    "아이스 카페 아메리카노 T",
                    4700,
                    "https://st.kakaocdn.net/product/gift/product/20231010111814_9a667f9eccc943648797925498bdd8a3.jpg",
                    options
            );
            ResponseEntity<ProductResponse> response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .body(requestDto)
                    .retrieve()
                    .toEntity(ProductResponse.class);
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                    () -> assertThat(response.getBody().name()).isEqualTo("아이스 카페 아메리카노 T")
            );
        }

        @Test
        @DisplayName("POST /api/products - 옵션 없이 생성 시 400 BAD_REQUEST")
        void 옵션_없이_생성_시_400_BAD_REQUEST() {
            CreateProductRequest requestDto = new CreateProductRequest(
                    "Test Product",
                    1000,
                    "test.jpg",
                    java.util.Collections.emptyList()
            );
            assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
                    .isThrownBy(
                            () ->
                                    restClient.post()
                                            .uri(url)
                                            .header("Authorization", "Bearer " + mdToken)
                                            .body(requestDto)
                                            .retrieve()
                                            .toEntity(Void.class)
                    );
        }

        @Test
        @DisplayName("POST /api/products - 유효하지 않은 생성 시 400 BAD_REQUEST")
        void 유효하지_않은_생성_시_400_BAD_REQUEST() {
            CreateProductRequest requestDto = new CreateProductRequest(null, null, null,
                    java.util.Collections.emptyList());
            assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
                    .isThrownBy(
                            () ->
                                    restClient.post()
                                            .uri(url)
                                            .header("Authorization", "Bearer " + mdToken)
                                            .body(requestDto)
                                            .retrieve()
                                            .toEntity(Void.class)
                    );
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id} - 상품조회 테스트")
    class GetProduct {

        @Test
        @DisplayName("GET /api/products/{id} - 유효한 조회 시 200 OK")
        void 유효한_조회_시_200_OK() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId();
            ResponseEntity<ProductResponse> response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .retrieve()
                    .toEntity(ProductResponse.class);
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().name()).isEqualTo("Initial Product")
            );
        }

        @Test
        @DisplayName("GET /api/products/{id} - 유효하지 않은 id로 조회 시 404 NOT_FOUND")
        void 유효하지_않은_ID로_조회_시_404_NOT_FOUND() {
            String url = baseUrl + port + "/api/products/0";
            assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .isThrownBy(
                            () ->
                                    restClient.get()
                                            .uri(url)
                                            .header("Authorization", "Bearer " + mdToken)
                                            .retrieve()
                                            .toEntity(Void.class)
                    );
        }
    }

    @Nested
    @DisplayName("PATCH /api/products/{id} - 상품수정 테스트")
    class UpdateProduct {

        @Test
        @DisplayName("PATCH /api/products/{id} - 유효한 수정 시 200 OK")
        void 유효한_수정_시_200_OK() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId();
            UpdateProductRequest patchDto = new UpdateProductRequest(
                    "각하오 커피",
                    7800,
                    null
            );
            ResponseEntity<ProductResponse> response = restClient.patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .body(patchDto)
                    .retrieve()
                    .toEntity(ProductResponse.class);
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().name()).isEqualTo("각하오 커피"),
                    () -> assertThat(response.getBody().imageUrl()).isNotNull()
            );
        }

        @Test
        @DisplayName("PATCH /api/products/{id} - 유효하지 않은 데이터로 수정 시 404 NOT_FOUND")
        void 유효하지_않은_ID로_수정_시_404_NOT_FOUND() {
            String url = baseUrl + port + "/api/products/0";
            UpdateProductRequest patchDto = new UpdateProductRequest(
                    "각하오 커피",
                    7800,
                    null
            );
            assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .isThrownBy(
                            () ->
                                    restClient.patch()
                                            .uri(url)
                                            .header("Authorization", "Bearer " + mdToken)
                                            .body(patchDto)
                                            .retrieve()
                                            .toEntity(Product.class)
                    );
        }
    }

    @Test
    @DisplayName("GET /api/products - 리스트 조회 시 200 OK")
    void 리스트_조회_시_200_OK() {
        String url = baseUrl + port + "/api/products";
        ResponseEntity<PageResponse<ProductResponse>> response = restClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + mdToken)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().content().size()).isNotZero()
        );
    }

    @Nested
    @DisplayName("DELETE /api/products/{id} - 상품삭제 테스트")
    class DeleteProduct {

        @Test
        @DisplayName("DELETE /api/products/{id} - 유효한 삭제 시 204 NO_CONTENT")
        void 유효한_삭제_시_204_NO_CONTENT() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId();
            ResponseEntity<Void> response = restClient.delete()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .retrieve()
                    .toEntity(Void.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("DELETE /api/products/{id} - 유효하지 않은 id로 삭제 시 404 NOT_FOUND")
        void 유효하지_않은_ID로_삭제_시_404_NOT_FOUND() {
            String url = baseUrl + port + "/api/products/0";
            assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .isThrownBy(
                            () ->
                                    restClient.delete()
                                            .uri(url)
                                            .header("Authorization", "Bearer " + mdToken)
                                            .retrieve()
                                            .toEntity(Void.class)
                    );
        }
    }
}
