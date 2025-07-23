package gift.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.config.AuditingTestConfig;
import gift.dto.AddWishRequest;
import gift.dto.PageResponse;
import gift.dto.WishResponse;
import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Role;
import gift.entity.Wish;
import gift.repository.MemberRepository;
import gift.repository.ProductRepository;
import gift.repository.WishRepository;
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
public class WishE2ETest {

    private final String baseUrl = "http://localhost:";

    @LocalServerPort
    private int port;

    @Autowired
    private WishRepository wishRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private RestClient restClient;
    private String userToken;
    private Member savedUser;
    private Product savedProduct1;
    private Product savedProduct2;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create();

        savedUser = memberRepository.save(
                new Member(null, "user@example.com", "password123456789", Role.ROLE_USER));
        userToken = jwtTokenProvider.createToken(savedUser);

        savedProduct1 = productRepository.save(
                new Product(null, "Product 1", 1000, "prod1.jpg", true, false));
        savedProduct2 = productRepository.save(
                new Product(null, "Product 2", 2000, "prod2.jpg", true, false));
    }

    @AfterEach
    void tearDown() {
        wishRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /api/wishes - 위시리스트 조회 테스트")
    class GetWishList {

        String url = baseUrl + port + "/api/wishes";

        @Test
        @DisplayName("GET /api/wishes - 위시리스트 조회 시 200 OK")
        void 위시리스트_조회_시_200_OK() {
            wishRepository.save(new Wish(savedUser, savedProduct1));

            ResponseEntity<PageResponse<WishResponse>> response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().content().size()).isEqualTo(1),
                    () -> assertThat(response.getBody().content().get(0).productName()).isEqualTo(
                            "Product 1")
            );
        }

        @Test
        @DisplayName("GET /api/wishes - 비로그인 상태로 위시리스트 조회 시 401 UNAUTHORIZED")
        void 비로그인_상태로_위시리스트_조회_시_401_UNAUTHORIZED() {
            assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class)
                    .isThrownBy(() -> restClient.get()
                            .uri(url)
                            .retrieve()
                            .toEntity(new ParameterizedTypeReference<PageResponse<Wish>>() {
                            }));
        }
    }

    @Nested
    @DisplayName("POST /api/wishes - 위시리스트 아이템 추가 테스트")
    class PostWish {

        String url = baseUrl + port + "/api/wishes";

        @Test
        @DisplayName("POST /api/wishes - 유효한 아이템 추가 시 201 CREATED")
        void 유효한_아이템_추가_시_201_CREATED() {
            AddWishRequest request = new AddWishRequest(savedProduct1.getId());

            ResponseEntity<WishResponse> response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + userToken)
                    .body(request)
                    .retrieve()
                    .toEntity(WishResponse.class);

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().productId()).isEqualTo(
                            savedProduct1.getId())
            );
        }

        @Test
        @DisplayName("POST /api/wishes - 유효하지 않은 아이템 추가 시 404 NOT_FOUND")
        void 유효하지_않은_아이템_추가_시_404_NOT_FOUND() {
            AddWishRequest request = new AddWishRequest(9999L);
            assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .isThrownBy(() -> restClient.post()
                            .uri(url)
                            .header("Authorization", "Bearer " + userToken)
                            .body(request)
                            .retrieve()
                            .toEntity(WishResponse.class));
        }

        @Test
        @DisplayName("POST /api/wishes - 이미 존재하는 아이템 추가 시 409 CONFLICT")
        void 이미_존재하는_아이템_추가_시_409_CONFLICT() {
            // 첫 번째 추가
            wishRepository.save(new Wish(savedUser, savedProduct1));
            AddWishRequest request = new AddWishRequest(savedProduct1.getId());

            // 두 번째 추가 시도
            assertThatExceptionOfType(HttpClientErrorException.Conflict.class)
                    .isThrownBy(() -> restClient.post()
                            .uri(url)
                            .header("Authorization", "Bearer " + userToken)
                            .body(request)
                            .retrieve()
                            .toEntity(WishResponse.class));
        }

        @Test
        @DisplayName("POST /api/wishes - 비로그인 상태로 위시리스트 추가 시 401 UNAUTHORIZED")
        void 비로그인_상태로_위시리스트_추가_시_401_UNAUTHORIZED() {
            assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class)
                    .isThrownBy(() -> restClient.get()
                            .uri(url)
                            .retrieve()
                            .toEntity(new ParameterizedTypeReference<PageResponse<Wish>>() {
                            }));
        }
    }

    @Nested
    @DisplayName("DELETE /api/wishes/{wishItemId} - 위시리스트 아이템 삭제 테스트")
    class DeleteWish {

        String url = baseUrl + port + "/api/wishes";

        @Test
        @DisplayName("DELETE /api/wishes/{wishItemId} - 유효한 아이템 삭제 시 204 NO_CONTENT")
        void 유효한_아이템_삭제_시_204_NO_CONTENT() {
            Wish wish = wishRepository.save(new Wish(savedUser, savedProduct1));

            ResponseEntity<Void> response = restClient.delete()
                    .uri(url + "/" + wish.getId())
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .toEntity(Void.class);

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT),
                    () -> assertThat(wishRepository.existsById(wish.getId())).isFalse()
            );
        }

        @Test
        @DisplayName("DELETE /api/wishes/{wishItemId} - 존재하지 않는 아이템 삭제 시 404 NOT_FOUND")
        void 존재하지_않는_아이템_삭제_시_404_NOT_FOUND() {
            assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .isThrownBy(() -> restClient.delete()
                            .uri(url + "/" + 9999L)
                            .header("Authorization", "Bearer " + userToken)
                            .retrieve()
                            .toEntity(Void.class));
        }

        @Test
        @DisplayName("DELETE /api/wishes/{wishItemId} - 다른 사용자의 아이템 삭제 시 404 NOT_FOUND")
        void 다른_사용자의_아이템_삭제_시_404_NOT_FOUND() {
            Member otherUser = memberRepository.save(new Member(null, "", "", Role.ROLE_USER));
            Wish otherUsersWish = wishRepository.save(new Wish(otherUser, savedProduct1));

            assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                    .isThrownBy(() -> restClient.delete()
                            .uri(url + "/" + otherUsersWish.getId())
                            .header("Authorization", "Bearer " + userToken)
                            .retrieve()
                            .toEntity(Void.class));
        }

        @Test
        @DisplayName("DELETE /api/wishes/{wishItemId} - 비로그인 상태로 위시리스트 추가 시 401 UNAUTHORIZED")
        void 비로그인_상태로_아이템_삭제_시_401_UNAUTHORIZED() {
            assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class)
                    .isThrownBy(() -> restClient.get()
                            .uri(url)
                            .retrieve()
                            .toEntity(new ParameterizedTypeReference<PageResponse<Wish>>() {
                            }));
        }
    }
}
