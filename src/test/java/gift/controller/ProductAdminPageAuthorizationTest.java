package gift.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import gift.config.AuditingTestConfig;
import gift.entity.Member;
import gift.entity.Role;
import gift.repository.MemberRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AuditingTestConfig.class)
class ProductAdminPageAuthorizationTest {

    private final String baseUrl = "http://localhost:";

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private RestClient restClient;
    private String mdToken;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create();

        Member user = new Member(0L, "md@example.com", "mdpassword123456789", Role.ROLE_MD);
        mdToken = jwtTokenProvider.createToken(user);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /admin/products - 상품 목록 조회 테스트")
    class GetProductList {

        String url = baseUrl + port + "/admin/products";

        @Test
        @DisplayName("GET /admin/products - 유효한 권한으로 상품 목록 조회 시 200 OK")
        void 유효한_권한으로_상품목록_조회_시_200_OK() {
            ResponseEntity<String> response = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + mdToken)
                    .retrieve()
                    .toEntity(String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("GET /admin/products - 권한 없이 상품 목록 조회 시 401 UNAUTHORIZED")
        void 권한_없이_상품목록_조회_시_401_UNAUTHORIZED() {
            assertThatExceptionOfType(HttpClientErrorException.class)
                    .isThrownBy(() -> restClient.get()
                            .uri(url)
                            .retrieve()
                            .toBodilessEntity())
                    .satisfies(ex -> assertThat(ex.getStatusCode()).isEqualTo(
                            HttpStatus.UNAUTHORIZED));
        }

        @Test
        @DisplayName("GET /admin/products - 다른 권한으로 상품 목록 조회 시 403 FORBIDDEN")
        void 다른_권한으로_상품목록_조회_시_403_FORBIDDEN() {
            String userToken = jwtTokenProvider.createToken(
                    new Member(
                            2L,
                            "user@example.com",
                            "userpassword123456789",
                            Role.ROLE_USER
                    )
            );

            assertThatExceptionOfType(HttpClientErrorException.class)
                    .isThrownBy(() -> restClient.get()
                            .uri(url)
                            .header("Authorization", "Bearer " + userToken)
                            .retrieve()
                            .toBodilessEntity())
                    .satisfies(
                            ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        }
    }


}