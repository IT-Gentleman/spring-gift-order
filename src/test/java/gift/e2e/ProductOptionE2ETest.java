package gift.e2e;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.config.AuditingTestConfig;
import gift.dto.AddProductOptionRequest;
import gift.dto.PatchProductOptionRequest;
import gift.dto.ProductOptionResponse;
import gift.entity.Member;
import gift.entity.Product;
import gift.entity.ProductOption;
import gift.entity.Role;
import gift.repository.MemberRepository;
import gift.repository.ProductOptionRepository;
import gift.repository.ProductRepository;
import gift.token.JwtTokenProvider;
import java.util.List;
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
public class ProductOptionE2ETest {

    private final String baseUrl = "http://localhost:";

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private MemberRepository memberRepository;

    private RestClient restClient;
    private String mdToken;
    private Product savedProduct;
    private ProductOption savedProductOption;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create();
        Member md = memberRepository.save(
                new Member(null, "md@example.com", "mdpassword123456789", Role.ROLE_MD));
        mdToken = jwtTokenProvider.createToken(md);
        savedProduct = productRepository.save(
                new Product(null, "Initial Product", 10000, "initial.jpg", true, false));
        savedProductOption = productOptionRepository.save(
                new ProductOption("Initial Option", 10, savedProduct));
    }

    @AfterEach
    void tearDown() {
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/products/{productId}/options - 상품 옵션 생성 테스트")
    class CreateProductOption {

        @Test
        @DisplayName("유효한 생성 시 201 CREATED")
        void create_valid_product_option() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options";
            AddProductOptionRequest requestDto = new AddProductOptionRequest("New Option", 20);

            ResponseEntity<ProductOptionResponse> response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .body(requestDto)
                    .retrieve()
                    .toEntity(ProductOptionResponse.class);

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                    () -> assertThat(response.getBody().name()).isEqualTo("New Option")
            );
        }

        @Test
        @DisplayName("중복된 이름으로 생성 시 409 CONFLICT")
        void create_duplicate_name_product_option() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options";
            AddProductOptionRequest requestDto = new AddProductOptionRequest(
                    savedProductOption.getName(), 20);

            assertThatExceptionOfType(HttpClientErrorException.class)
                    .isThrownBy(() ->
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
    @DisplayName("GET /api/products/{productId}/options/{optionId} - 상품 옵션 조회 테스트")
    class GetProductOption {

        @Test
        @DisplayName("유효한 조회 시 200 OK")
        void get_valid_product_option() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options/"
                    + savedProductOption.getId();

            ResponseEntity<ProductOptionResponse> response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .retrieve()
                    .toEntity(ProductOptionResponse.class);

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().name()).isEqualTo(
                            savedProductOption.getName())
            );
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 404 NOT_FOUND")
        void get_non_existent_product_option() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options/999";

            assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .isThrownBy(() ->
                            restClient.get()
                                    .uri(url)
                                    .header("Authorization", "Bearer " + mdToken)
                                    .retrieve()
                                    .toEntity(Void.class)
                    );
        }
    }

    @Nested
    @DisplayName("PATCH /api/products/{productId}/options/{optionId} - 상품 옵션 수정 테스트")
    class UpdateProductOption {

        @Test
        @DisplayName("유효한 수정 시 200 OK")
        void update_valid_product_option() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options/"
                    + savedProductOption.getId();
            PatchProductOptionRequest requestDto = new PatchProductOptionRequest("Updated Option",
                    5);

            ResponseEntity<ProductOptionResponse> response = restClient.patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .body(requestDto)
                    .retrieve()
                    .toEntity(ProductOptionResponse.class);

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().name()).isEqualTo(requestDto.name()),
                    () -> assertThat(response.getBody().quantity()).isEqualTo(requestDto.quantity())
            );
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{productId}/options/{optionId} - 상품 옵션 삭제 테스트")
    class DeleteProductOption {

        @Test
        @DisplayName("유효한 삭제 시 204 NO_CONTENT")
        void delete_valid_product_option() {
            productOptionRepository.save(
                    new ProductOption("new Option", 120, savedProduct));

            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options/"
                    + savedProductOption.getId();

            ResponseEntity<Void> response = restClient.delete()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .retrieve()
                    .toEntity(Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("마지막 옵션 삭제 시 Bad Request")
        void delete_last_product_option() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options/"
                    + savedProductOption.getId();

            assertThatExceptionOfType(HttpClientErrorException.BadRequest.class)
                    .isThrownBy(() ->
                            restClient.delete()
                                    .uri(url)
                                    .header("Authorization", "Bearer " + mdToken)
                                    .retrieve()
                                    .toEntity(Void.class)
                    );
        }

        @Test
        @DisplayName("존재하지 않는 ID로 삭제 시 404 NOT_FOUND")
        void delete_non_existent_product_option() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options/999";

            assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .isThrownBy(() ->
                            restClient.delete()
                                    .uri(url)
                                    .header("Authorization", "Bearer " + mdToken)
                                    .retrieve()
                                    .toEntity(Void.class)
                    );
        }
    }

    @Nested
    @DisplayName("GET /api/products/{productId}/options - 상품 옵션 리스트 조회 테스트")
    class GetProductOptionList {

        @Test
        @DisplayName("유효한 조회 시 200 OK")
        void get_valid_product_option_list() {
            String url = baseUrl + port + "/api/products/" + savedProduct.getId() + "/options";

            ResponseEntity<List<ProductOptionResponse>> response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().size()).isEqualTo(1)
            );
        }
    }
}
