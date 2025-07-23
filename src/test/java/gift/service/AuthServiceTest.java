package gift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import gift.config.AuditingTestConfig;
import gift.dto.LoginCommand;
import gift.entity.Member;
import gift.entity.Role;
import gift.exception.InvalidCredentialsException;
import gift.repository.MemberRepository;
import gift.token.JwtTokenProvider;
import gift.util.BCryptEncryptor;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@Import(AuditingTestConfig.class)
public class AuthServiceTest {

    @Mock
    private BCryptEncryptor bCryptEncryptor;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Member login() - 로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("정상적인 로그인 시 토큰 반환")
        void 정상적인_로그인_시_토큰반환() {
            String email = "registered@email.com";
            String rawPassword = "password123456789";
            String hashedPassword = "hashedPassword";
            Member existingMember = new Member(1L, email, hashedPassword, Role.ROLE_USER);

            when(memberRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(
                    Optional.of(existingMember));

            try (MockedStatic<BCryptEncryptor> encryptor = mockStatic(BCryptEncryptor.class)) {

                encryptor.when(() -> BCryptEncryptor.matches(rawPassword, hashedPassword))
                        .thenReturn(true);
                when(jwtTokenProvider.createToken(existingMember)).thenReturn("validToken");

                String token = authService.login(new LoginCommand(email, rawPassword));

                assertThat(token).isEqualTo("validToken");
            }
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
        void 존재하지_않는_이메일로_로그인_시_예외발생() {
            String email = "not.registered@email.com";
            String rawPassword = "password123456789";
            String hashedPassword = "hashedPassword";

            when(memberRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(
                    Optional.empty());

            assertThrows(InvalidCredentialsException.class,
                    () -> authService.login(new LoginCommand(email, rawPassword)));
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
        void 잘못된_비밀번호로_로그인_시_예외발생() {
            String email = "registered@email.com";
            String rawPassword = "wrongPassword123456789";
            String hashedPassword = "hashedPassword";

            Member existingMember = new Member(1L, email, hashedPassword, Role.ROLE_USER);

            when(memberRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(
                    Optional.of(existingMember));

            try (MockedStatic<BCryptEncryptor> encryptor = mockStatic(BCryptEncryptor.class)) {

                encryptor.when(() -> BCryptEncryptor.matches(rawPassword, hashedPassword))
                        .thenReturn(false);

                assertThrows(InvalidCredentialsException.class,
                        () -> authService.login(new LoginCommand(email, rawPassword)));
            }
        }
    }


    @Nested
    @DisplayName("Member getAuthenticationFromToken() - 토큰으로 인증 정보 조회 테스트")
    class GetAuthenticationFromTokenTests {

        @Test
        @DisplayName("유효한 토큰으로 인증 정보 조회")
        void 유효한_토큰으로_인증정보조회() {
            String username = "email@email.com";
            String token = "validToken";
            Member member = new Member(1L, "email", "hashedPassword", Role.ROLE_USER);
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.getId(token)).thenReturn(member.getId());
            when(memberRepository.findById(member.getId())).thenReturn(
                    Optional.of(member));
            assertThat(authService.getAuthenticationFromToken(token)).isNotNull();
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 인증 정보 조회 시 예외 발생")
        void 유효하지_않은_토큰으로_인증정보조회시_예외발생() {
            String token = "validToken";
            when(jwtTokenProvider.validateToken(token)).thenReturn(false);
            assertThrows(ResponseStatusException.class,
                    () -> authService.getAuthenticationFromToken(token));
        }

        @Test
        @DisplayName("유효한 토큰이지만 회원 정보가 없는 경우 예외 발생")
        void 유효한_토큰이지만_회원정보가_없는_경우_예외발생() {
            String username = "deletedMember@email.com";
            String token = "validToken";
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.getId(token)).thenReturn(null);
            when(memberRepository.findById(any())).thenReturn(
                    Optional.empty());
            assertThrows(ResponseStatusException.class,
                    () -> authService.getAuthenticationFromToken(token));
        }
    }
}
