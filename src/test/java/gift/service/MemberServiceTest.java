package gift.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import gift.dto.MemberDto;
import gift.dto.NewMemberCommand;
import gift.dto.UpdateMemberCommand;
import gift.entity.Member;
import gift.entity.Role;
import gift.exception.ConflictException;
import gift.repository.MemberRepository;
import gift.util.BCryptEncryptor;
import gift.util.PasswordUtility;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Nested
    @DisplayName("Member createMember() - 회원 생성 테스트")
    class CreateMemberTests {

        @Test
        @DisplayName("정상적인 회원 데이터 삽입")
        void 정상적인_회원_삽입_시_정상반환() {
            String email = "new@email.com";
            String rawPassword = "newpassword123456789";
            String hashedPassword = "hashedPassword";

            Member expectedMember = new Member(1L, email, hashedPassword, Role.ROLE_USER);

            when(memberRepository.existsByEmail(email)).thenReturn(false);

            try (MockedStatic<BCryptEncryptor> encryptor = mockStatic(BCryptEncryptor.class)) {

                encryptor.when(() -> BCryptEncryptor.encrypt(rawPassword))
                        .thenReturn(hashedPassword);
                when(memberRepository.save(any(Member.class))).thenReturn(expectedMember);

                MemberDto resolve = memberService.createMember(
                        new NewMemberCommand(email, rawPassword));

                assertAll(
                        () -> assertThat(resolve.email()).isEqualTo(email),
                        () -> assertThat(resolve.password()).isNull(),
                        () -> assertThat(resolve.id()).isEqualTo(1L),
                        () -> assertThat(resolve.role()).isEqualTo(Role.ROLE_USER)
                );
            }
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원 생성 시 예외 발생")
        void 이미_존재하는_이메일로_회원_생성_시_예외발생() {
            String email = "existing@email.com";
            String rawPassword = "password123456789";

            when(memberRepository.existsByEmail(email)).thenReturn(true);

            assertThrows(ConflictException.class,
                    () -> memberService.createMember(new NewMemberCommand(email, rawPassword)));
        }
    }

    @Nested
    @DisplayName("Member updateMember() - 회원 정보 수정 테스트")
    class UpdateMemberTests {

        @Test
        @DisplayName("정상적인 회원 정보 수정 - 비밀번호 유지")
        void 정상적인_회원_정보_수정_비밀번호_유지() {
            Long id = 1L;
            String email = "existing@email.com";
            String newEmail = "new@email.com";
            String hashedPassword = "hashedPassword";
            Role role = Role.ROLE_USER;
            Role newRole = Role.ROLE_ADMIN;

            Member existingMember = new Member(id, email, hashedPassword, role);
            when(memberRepository.findById(id)).thenReturn(Optional.of(existingMember));

            MemberDto result = memberService.updateMember(
                    new UpdateMemberCommand(id, newEmail, false, newRole));

            assertAll(
                    () -> assertThat(result.id()).isEqualTo(id),
                    () -> assertThat(result.email()).isEqualTo(newEmail),
                    () -> assertThat(result.password()).isNull(),
                    () -> assertThat(result.role()).isEqualTo(newRole)
            );
        }

        @Test
        @DisplayName("정상적인 회원 정보 수정 - 비밀번호 변경")
        void 정상적인_회원_정보_수정_비밀번호_변경() {
            Long id = 1L;
            String email = "existing@email.com";
            String newEmail = "new@email.com";
            String hashedPassword = "hashedPassword";
            String newPassword = "newPassword";
            Role role = Role.ROLE_USER;
            Role newRole = Role.ROLE_ADMIN;

            Member existingMember = new Member(id, email, hashedPassword, role);
            when(memberRepository.findById(id)).thenReturn(Optional.of(existingMember));

            try (
                    MockedStatic<BCryptEncryptor> encryptor = mockStatic(BCryptEncryptor.class);
                    MockedStatic<PasswordUtility> generateRandomPassword = mockStatic(
                            PasswordUtility.class)
            ) {
                encryptor.when(() -> BCryptEncryptor.encrypt(anyString()))
                        .thenReturn(hashedPassword); // 검증대상은 아님
                generateRandomPassword.when(() -> PasswordUtility.generateRandomPassword())
                        .thenReturn(newPassword);

                MemberDto result = memberService.updateMember(
                        new UpdateMemberCommand(id, newEmail, true, newRole));

                assertAll(
                        () -> assertThat(result.id()).isEqualTo(id),
                        () -> assertThat(result.email()).isEqualTo(newEmail),
                        () -> assertThat(result.password()).isEqualTo(newPassword),
                        () -> assertThat(result.role()).isEqualTo(newRole)
                );
            }
        }

        // 이메일 유지하는 정상적인 회원 정보 수정
        // 이메일 중복인 비정상적인 회원 정보 수정
    }

    // 단순 CRUD 테스트 생략

}