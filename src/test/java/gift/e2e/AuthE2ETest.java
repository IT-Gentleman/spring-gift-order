package gift.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.config.AuditingTestConfig;
import gift.dto.LoginRequest;
import gift.dto.LoginResponse;
import gift.dto.RegisterMemberRequest;
import gift.dto.RegisterMemberResponse;
import gift.repository.MemberRepository;
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
class AuthE2ETest {

    private final String baseUrl = "http://localhost:";

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create();
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/members/register - 회원가입 테스트")
    class Register {

        String url = baseUrl + port + "/api/auth/register";

        @Test
        @DisplayName("POST /api/members/register - 유효한 정보 입력 시 201 CREATED")
        void 유효한_정보_입력_시_201_CREATED() {
            RegisterMemberRequest request = new RegisterMemberRequest("test@example.com",
                    "password123456789");

            ResponseEntity<RegisterMemberResponse> response = restClient.post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .toEntity(RegisterMemberResponse.class);

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                    () -> assertThat(response.getBody()).hasFieldOrProperty("token")
            );
        }

        @Test
        @DisplayName("POST /api/members/register - 이메일 중복 시 409 CONFLICT")
        void 이메일_중복_시_409_CONFLICT() {
            RegisterMemberRequest request = new RegisterMemberRequest("existing@example.com",
                    "password123456789");

            // 먼저 회원가입을 수행
            restClient.post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            // 동일한 이메일로 두 번째 회원가입 시도
            assertThatExceptionOfType(HttpClientErrorException.class)
                    .isThrownBy(() -> restClient.post()
                            .uri(url)
                            .body(request)
                            .retrieve()
                            .toBodilessEntity())
                    .satisfies(ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        }

        @Test
        @DisplayName("POST /api/members/register - 비밀번호 길이 부족 시 400 BAD_REQUEST")
        void 비밀번호_길이_부족_시_400_BAD_REQUEST() {
            RegisterMemberRequest request = new RegisterMemberRequest("test@example.com",
                    "password");
            assertThatExceptionOfType(HttpClientErrorException.class)
                    .isThrownBy(() -> restClient.post()
                            .uri(url)
                            .body(request)
                            .retrieve()
                            .toBodilessEntity())
                    .satisfies(
                            ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("POST /api/members/register - 잘못된 이메일 형식 시 400 BAD_REQUEST")
        void 잘못된_이메일_형식_시_400_BAD_REQUEST() {
            RegisterMemberRequest request = new RegisterMemberRequest("invalid-email",
                    "password123456789");
            assertThatExceptionOfType(HttpClientErrorException.class)
                    .isThrownBy(() -> restClient.post()
                            .uri(url)
                            .body(request)
                            .retrieve()
                            .toBodilessEntity())
                    .satisfies(
                            ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    @Nested
    @DisplayName("POST /api/members/login - 로그인 테스트")
    class Login {

        String registerUrl = baseUrl + port + "/api/auth/register";
        String loginUrl = baseUrl + port + "/api/auth/login";
        String userEmail = "existing@example.com";
        String userPassword = "password123456789";

        @BeforeEach
        void setUp() {
            // 테스트를 위해 유효한 회원을 먼저 생성
            RegisterMemberRequest createRequest = new RegisterMemberRequest(userEmail,
                    userPassword);
            restClient.post()
                    .uri(registerUrl)
                    .body(createRequest)
                    .retrieve()
                    .toBodilessEntity();
        }

        @Test
        @DisplayName("POST /api/members/login - 유효한 정보로 로그인 시 200 OK")
        void 유효한_정보로_로그인_시_200_OK() {
            LoginRequest loginRequest = new LoginRequest(userEmail, userPassword);
            ResponseEntity<LoginResponse> response = restClient.post()
                    .uri(loginUrl)
                    .body(loginRequest)
                    .retrieve()
                    .toEntity(LoginResponse.class);

            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).hasFieldOrProperty("token")
            );
        }

        @Test
        @DisplayName("POST /api/members/login - 잘못된 비밀번호로 로그인 시 403 FORBIDDEN")
        void 잘못된_비밀번호로_로그인_시_403_FORBIDDEN() {
            LoginRequest loginRequest = new LoginRequest(userEmail, "wrongpassword");

            assertThatExceptionOfType(HttpClientErrorException.class)
                    .isThrownBy(() -> restClient.post()
                            .uri(loginUrl)
                            .body(loginRequest)
                            .retrieve()
                            .toBodilessEntity())
                    .satisfies(
                            ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        }

        @Test
        @DisplayName("POST /api/members/login - 존재하지 않는 이메일로 로그인 시 403 FORBIDDEN")
        void 존재하지_않는_이메일로_로그인_시_403_FORBIDDEN() {
            LoginRequest loginRequest = new LoginRequest("noexisting@example.com",
                    userPassword);

            assertThatExceptionOfType(HttpClientErrorException.class)
                    .isThrownBy(() -> restClient.post()
                            .uri(loginUrl)
                            .body(loginRequest)
                            .retrieve()
                            .toBodilessEntity())
                    .satisfies(
                            ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        }
    }
}
