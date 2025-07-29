package gift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gift.dto.auth.KakaoMemberResponse;
import gift.dto.auth.KakaoCreateTokenCommand;
import gift.dto.auth.KakaoTokenResponse;
import gift.entity.KakaoToken;
import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.token.JwtTokenProvider;
import gift.util.HttpUtil;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenEncryptionService tokenEncryptionService;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;


    @BeforeEach
    void setUp() {
        // Manually inject the @Value field for the test environment
        ReflectionTestUtils.setField(kakaoAuthService, "kakaoClientId", "test-client-id");
    }

    @Nested
    @DisplayName("카카오 로그인 테스트")
    class loginWithKakaoTests {

        KakaoCreateTokenCommand command = new KakaoCreateTokenCommand("code", "id", "uri",
                "secret");

        @Test
        @DisplayName("카카오 로그인 성공 - 신규 회원")
        void loginWithKakao_NewMember() {
            try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
                // given
                var kakaoTokenResponse = new KakaoTokenResponse("bearer", "test-access-token",
                        21599L, "test-refresh-token", 43199L);
                var kakaoMemberResponse = new KakaoMemberResponse(12345L);
                var newMember = new Member(12345L);
                var expectedToken = "jwt-token";

                mockedStatic.when(() -> HttpUtil.sendPost(any(), anyString(), any(), any(), any(),
                                eq(KakaoTokenResponse.class)))
                        .thenReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));
                mockedStatic.when(() -> HttpUtil.sendBodilessPost(any(), anyString(), any(), any(),
                                eq(KakaoMemberResponse.class)))
                        .thenReturn(new ResponseEntity<>(kakaoMemberResponse, HttpStatus.OK));

                when(memberRepository.findByKakaoId(12345L)).thenReturn(Optional.empty());
                when(memberRepository.save(any(Member.class))).thenReturn(newMember);
                when(jwtTokenProvider.createToken(newMember)).thenReturn(expectedToken);

                // when
                String actualToken = kakaoAuthService.loginWithKakao(command);

                // then
                assertEquals(expectedToken, actualToken);
                verify(memberRepository).findByKakaoId(12345L);
                verify(memberRepository).save(any(Member.class));
                verify(jwtTokenProvider).createToken(newMember);
            }
        }

        @Test
        @DisplayName("카카오 로그인 성공 - 기존 회원")
        void loginWithKakao_ExistingMember() {
            try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
                // given
                var kakaoTokenResponse = new KakaoTokenResponse("bearer", "test-access-token",
                        21599L, "test-refresh-token", 43199L);
                var kakaoMemberResponse = new KakaoMemberResponse(12345L);
                var existingMember = new Member(12345L);
                var expectedToken = "jwt-token";

                mockedStatic.when(() -> HttpUtil.sendPost(any(), anyString(), any(), any(), any(),
                                eq(KakaoTokenResponse.class)))
                        .thenReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));
                mockedStatic.when(() -> HttpUtil.sendBodilessPost(any(), anyString(), any(), any(),
                                eq(KakaoMemberResponse.class)))
                        .thenReturn(new ResponseEntity<>(kakaoMemberResponse, HttpStatus.OK));

                when(memberRepository.findByKakaoId(12345L)).thenReturn(
                        Optional.of(existingMember));
                when(jwtTokenProvider.createToken(existingMember)).thenReturn(expectedToken);

                // when
                String actualToken = kakaoAuthService.loginWithKakao(command);

                // then
                assertEquals(expectedToken, actualToken);
                verify(memberRepository).findByKakaoId(12345L);
                verify(memberRepository, never()).save(any(Member.class));
                verify(jwtTokenProvider).createToken(existingMember);
            }
        }

        @Test
        @DisplayName("카카오 토큰 요청 실패 (4xx)")
        void getKakaoToken_ClientError() {
            try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
                // given
                mockedStatic.when(() -> HttpUtil.sendPost(any(), anyString(), any(), any(), any(),
                                eq(KakaoTokenResponse.class)))
                        .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

                // when & then
                assertThrows(ResponseStatusException.class,
                        () -> kakaoAuthService.loginWithKakao(command));
            }
        }

        @Test
        @DisplayName("카카오 사용자 정보 요청 실패 (5xx)")
        void getKakaoMember_ServerError() {
            try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
                // given
                var kakaoTokenResponse = new KakaoTokenResponse("bearer", "test-access-token",
                        21599L, "test-refresh-token", 43199L);
                mockedStatic.when(() -> HttpUtil.sendPost(any(), anyString(), any(), any(), any(),
                                eq(KakaoTokenResponse.class)))
                        .thenReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));
                mockedStatic.when(() -> HttpUtil.sendBodilessPost(any(), anyString(), any(), any(),
                                eq(KakaoMemberResponse.class)))
                        .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

                // when & then
                assertThrows(ResponseStatusException.class,
                        () -> kakaoAuthService.loginWithKakao(command));
            }
        }
    }

    @Nested
    @DisplayName("카카오 토큰 갱신 테스트")
    class ExecuteWithKakaoTokenRefreshTests {

        @Test
        @DisplayName("Access Token이 유효할 때 API 호출이 한 번에 성공한다")
        void executeWithValidToken() {
            // given
            Long memberId = 1L;
            Member mockMember = mock(Member.class);
            KakaoToken kakaoToken = new KakaoToken(mockMember, "valid_access_token", "valid_refresh_token");
            Function<String, ResponseEntity<String>> apiCall = (token) -> ResponseEntity.ok("Success");

            when(memberService.findMemberByIdNotDeleted(memberId)).thenReturn(mockMember);
            when(mockMember.getKakaoToken()).thenReturn(kakaoToken);
            when(mockMember.getKakaoId()).thenReturn(12345L);
            when(tokenEncryptionService.decrypt(anyString())).thenReturn("valid_access_token");

            // when
            String result = kakaoAuthService.executeWithKakaoTokenRefresh(memberId, apiCall);

            // then
            assertThat(result).isEqualTo("Success");
            verify(tokenEncryptionService, times(1)).decrypt(
                    anyString()); // Decryption happens once
        }

        @Test
        @DisplayName("Access Token 만료 시, 토큰 갱신 후 API 호출에 성공한다")
        void executeWithExpiredTokenAndRefreshSuccess() {
            // given
            Long memberId = 1L;
            Member mockMember = mock(Member.class);
            KakaoToken kakaoToken = new KakaoToken(mockMember, "expired_access_token", "valid_refresh_token");

            // Mock the API call function to throw an exception on the first call and succeed on the second
            Function<String, ResponseEntity<String>> mockApiCall = mock(Function.class);
            when(mockApiCall.apply("decrypted_expired_access_token"))
                    .thenThrow(
                            new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
            when(mockApiCall.apply("new_access_token"))
                    .thenReturn(ResponseEntity.ok("Success after refresh"));

            // Configure mock dependencies
            when(memberService.findMemberByIdNotDeleted(memberId)).thenReturn(mockMember);
            when(mockMember.getKakaoToken()).thenReturn(kakaoToken);
            when(mockMember.getKakaoId()).thenReturn(12345L);
            when(tokenEncryptionService.decrypt(anyString()))
                    .thenReturn(
                            "decrypted_expired_access_token");

            // Mock the static HttpUtil.sendPost method
            try (MockedStatic<HttpUtil> mockedHttpUtil = Mockito.mockStatic(HttpUtil.class)) {
                KakaoTokenResponse newKakaoTokenResponse = new KakaoTokenResponse(
                        "bearer", "new_access_token", 3600L, "new_refresh_token", 5000L);
                ResponseEntity<KakaoTokenResponse> mockResponseEntity = ResponseEntity.ok(
                        newKakaoTokenResponse);

                // Configure the static mock to return our fake response when called
                mockedHttpUtil.when(() -> HttpUtil.sendPost(any(), any(), any(), any(), any(), any()))
                        .thenReturn(mockResponseEntity);

                // when: execute the method under test
                String result = kakaoAuthService.executeWithKakaoTokenRefresh(memberId,
                        mockApiCall);

                // then: assert the final result
                assertThat(result).isEqualTo("Success after refresh");

                // Verify: the apiCall function was invoked twice (one failure, one success)
                verify(mockApiCall, times(2)).apply(anyString());

                // Verify: the encryption service was called to encrypt the new tokens
                verify(tokenEncryptionService, times(2)).encrypt(anyString());
            }
        }
    }


}
