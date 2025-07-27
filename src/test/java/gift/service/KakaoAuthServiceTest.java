package gift.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gift.dto.auth.KakaoMemberResponse;
import gift.dto.auth.KakaoCreateTokenCommand;
import gift.dto.auth.KakaoTokenResponse;
import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.token.JwtTokenProvider;
import gift.util.HttpUtil;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    private final KakaoCreateTokenCommand command = new KakaoCreateTokenCommand("code", "id", "uri", "secret");

    @Test
    @DisplayName("카카오 로그인 성공 - 신규 회원")
    void loginWithKakao_NewMember() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            // given
            var kakaoTokenResponse = new KakaoTokenResponse("bearer", "test-access-token", 21599L, "test-refresh-token", 43199L);
            var kakaoMemberResponse = new KakaoMemberResponse(12345L);
            var newMember = new Member(12345L);
            var expectedToken = "jwt-token";

            mockedStatic.when(() -> HttpUtil.sendPost(any(), anyString(), any(), any(), any(), eq(KakaoTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));
            mockedStatic.when(() -> HttpUtil.sendBodilessPost(any(), anyString(), any(), any(), eq(KakaoMemberResponse.class)))
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
            var kakaoTokenResponse = new KakaoTokenResponse("bearer", "test-access-token", 21599L, "test-refresh-token", 43199L);
            var kakaoMemberResponse = new KakaoMemberResponse(12345L);
            var existingMember = new Member(12345L);
            var expectedToken = "jwt-token";

            mockedStatic.when(() -> HttpUtil.sendPost(any(), anyString(), any(), any(), any(), eq(KakaoTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));
            mockedStatic.when(() -> HttpUtil.sendBodilessPost(any(), anyString(), any(), any(), eq(KakaoMemberResponse.class)))
                .thenReturn(new ResponseEntity<>(kakaoMemberResponse, HttpStatus.OK));

            when(memberRepository.findByKakaoId(12345L)).thenReturn(Optional.of(existingMember));
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
            mockedStatic.when(() -> HttpUtil.sendPost(any(), anyString(), any(), any(), any(), eq(KakaoTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

            // when & then
            assertThrows(ResponseStatusException.class, () -> kakaoAuthService.loginWithKakao(command));
        }
    }

    @Test
    @DisplayName("카카오 사용자 정보 요청 실패 (5xx)")
    void getKakaoMember_ServerError() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            // given
            var kakaoTokenResponse = new KakaoTokenResponse("bearer", "test-access-token", 21599L, "test-refresh-token", 43199L);
            mockedStatic.when(() -> HttpUtil.sendPost(any(), anyString(), any(), any(), any(), eq(KakaoTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(kakaoTokenResponse, HttpStatus.OK));
            mockedStatic.when(() -> HttpUtil.sendBodilessPost(any(), anyString(), any(), any(), eq(KakaoMemberResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

            // when & then
            assertThrows(ResponseStatusException.class, () -> kakaoAuthService.loginWithKakao(command));
        }
    }
}
