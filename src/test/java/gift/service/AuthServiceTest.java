package gift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import gift.config.AuditingTestConfig;
import gift.dto.auth.LoginCommand;
import gift.dto.common.AuthenticatedMember;
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
    private MemberRepository memberRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Happy path: 정상적인 로그인 시 토큰 반환")
        void 정상적인_로그인_시_토큰반환() {
            try (MockedStatic<BCryptEncryptor> encryptor = mockStatic(BCryptEncryptor.class)) {
                // arrange
                String email = "registered@email.com";
                String rawPassword = "password123456789";
                String hashedPassword = "hashedPassword";
                Member existingMember = new Member(1L, email, hashedPassword, Role.ROLE_USER);

                when(memberRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(
                        Optional.of(existingMember));
                encryptor.when(() -> BCryptEncryptor.matches(rawPassword, hashedPassword))
                        .thenReturn(true);
                when(jwtTokenProvider.createToken(existingMember)).thenReturn("validToken");

                // act
                String token = authService.login(new LoginCommand(email, rawPassword));

                // assert
                assertThat(token).isEqualTo("validToken");
            }
        }

        @Test
        @DisplayName("Unhappy path: 존재하지 않는 이메일로 로그인 시 예외 발생")
        void 존재하지_않는_이메일로_로그인_시_예외발생() {
            // arrange
            String email = "not.registered@email.com";
            String rawPassword = "password123456789";
            String hashedPassword = "hashedPassword";

            when(memberRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(
                    Optional.empty());

            // act & assert
            assertThrows(InvalidCredentialsException.class,
                    () -> authService.login(new LoginCommand(email, rawPassword)));
        }

        @Test
        @DisplayName("Unhappy path: 잘못된 비밀번호로 로그인 시 예외 발생")
        void 잘못된_비밀번호로_로그인_시_예외발생() {
            try (MockedStatic<BCryptEncryptor> encryptor = mockStatic(BCryptEncryptor.class)) {
                // arrange
                String email = "registered@email.com";
                String rawPassword = "wrongPassword123456789";
                String hashedPassword = "hashedPassword";
                Member existingMember = new Member(1L, email, hashedPassword, Role.ROLE_USER);

                when(memberRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(
                        Optional.of(existingMember));
                encryptor.when(() -> BCryptEncryptor.matches(rawPassword, hashedPassword))
                        .thenReturn(false);

                // act & assert
                assertThrows(InvalidCredentialsException.class,
                        () -> authService.login(new LoginCommand(email, rawPassword)));
            }
        }
    }

    @Nested
    @DisplayName("getAuthenticationFromMemberId()")
    class GetAuthenticationFromMemberIdTests {

        @Test
        @DisplayName("Happy path: 정상적인 멤버 ID로 인증 정보 반환")
        void 정상적인_멤버_ID로_인증_정보반환() {
            // arrange
            Long memberId = 1L;
            Member member = new Member(memberId, "some@email", "hashedPassword", Role.ROLE_USER);

            when(memberRepository.findByIdAndDeletedAtIsNull(memberId)).thenReturn(
                    Optional.of(member));

            // act
            AuthenticatedMember actual = authService.getAuthenticationFromMemberId(memberId);

            // assert
            assertThat(actual.id()).isEqualTo(member.getId());
            assertThat(actual.email()).isEqualTo(member.getEmail());
            assertThat(actual.role()).isEqualTo(member.getRole());
        }

        @Test
        @DisplayName("Unhappy path: 존재하지 않는 멤버 ID로 인증 정보 조회 시 예외 발생")
        void 존재하지_않는_멤버_ID로_인증_정보조회_시_예외발생() {
            // arrange
            Long memberId = 999L;

            when(memberRepository.findByIdAndDeletedAtIsNull(memberId)).thenReturn(
                    Optional.empty());

            // act & assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.getAuthenticationFromMemberId(memberId));
        }

        @Test
        @DisplayName("Unhappy path: 삭제된 멤버 ID로 인증 정보 조회 시 예외 발생")
        void 삭제된_멤버_ID로_인증_정보조회_시_예외발생() {
            // arrange
            Long memberId = 1L;
            Member deletedMember = new Member(memberId, "deleted@email", "hashedPassword",
                    Role.ROLE_USER);
            deletedMember.setDeleted();

            when(memberRepository.findByIdAndDeletedAtIsNull(memberId)).thenReturn(
                    Optional.empty());

            // act & assert
            assertThrows(ResponseStatusException.class,
                    () -> authService.getAuthenticationFromMemberId(memberId));
        }
    }
}
