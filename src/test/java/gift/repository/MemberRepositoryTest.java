package gift.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gift.entity.Member;
import gift.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member existingMember;

    @BeforeEach
    void setUp() {
        existingMember = memberRepository.save(
                new Member(
                        null,
                        "existing@kakao.com",
                        "existingEncryptedPassword",
                        Role.ROLE_USER
                )
        );
    }

    @Nested
    @DisplayName("Member save() - 멤버 생성 테스트")
    class saveTests {

        @Test
        @DisplayName("정상적인 멤버 데이터 삽입")
        void 정상적인_멤버_삽입_시_정상반환() {
            Member exampleMember = new Member(
                    null,
                    "example@email.com",
                    "encryptedPasswordByBCryptEncryptor",
                    Role.ROLE_USER
            );
            Member savedMember = memberRepository.save(exampleMember);
            assertAll(
                    () -> assertThat(savedMember.getId()).isNotNull(),
                    () -> assertTrue(memberDetailsEquals(savedMember, exampleMember))
            );
        }

        @Test
        @DisplayName("중복 이메일 삽입 시 예외 발생")
        void 중복_이메일_삽입_시_예외발생() {
            Member duplicateEmailMember = new Member(
                    null,
                    "existing@kakao.com",
                    "anotherEncryptedPassword",
                    Role.ROLE_SELLER
            );
            assertThatExceptionOfType(DataIntegrityViolationException.class)
                    .isThrownBy(() -> memberRepository.save(duplicateEmailMember));
        }

        @Test
        @DisplayName("null 값이 포함된 멤버 데이터 삽입 시 예외 발생")
        void 널_값이_포함된_멤버_삽입_시_예외_발생() {
            // identifyNumber는 항상 null인 상태로 삽입되어야 함

            Member allNullMember = new Member(null, null, null, null);
            assertThatExceptionOfType(DataIntegrityViolationException.class)
                    .isThrownBy(() -> memberRepository.save(allNullMember));

            Member emailNullMember = new Member(null, null, "encryptedPasswordByBCryptEncryptor",
                    Role.ROLE_USER);
            assertThatExceptionOfType(DataIntegrityViolationException.class)
                    .isThrownBy(() -> memberRepository.save(emailNullMember));
            new Member(null, "example@email.com", "encryptedPasswordByBCryptEncryptor",
                    Role.ROLE_USER);

            Member passwordNullMember = new Member(null, "example@email.com", null, Role.ROLE_USER);
            assertThatExceptionOfType(DataIntegrityViolationException.class)
                    .isThrownBy(() -> memberRepository.save(passwordNullMember));

            Member authorityNullMember = new Member(null, "example@email.com",
                    "encryptedPasswordByBCryptEncryptor", null);
            assertThatExceptionOfType(DataIntegrityViolationException.class)
                    .isThrownBy(() -> memberRepository.save(authorityNullMember));
        }
    }

    @Nested
    @DisplayName("Optional<Member> findById() - 멤버 조회 테스트")
    class findByIdTests {

        @Test
        @DisplayName("존재하는 멤버의 ID로 조회 시 멤버 반환")
        void 존재하는_멤버의_ID로_조회_시_멤버반환() {
            assertThat(memberRepository.findById(existingMember.getId())).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
        void 존재하지_않는_ID로_조회_시_빈Optional반환() {
            assertThat(memberRepository.findById(500L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("void deleteById() - 멤버 삭제 테스트")
    class deleteByIdTests {

        @Test
        @DisplayName("존재하는 멤버의 ID로 삭제 시 삭제")
        void 존재하는_멤버의_ID로_삭제_시_삭제() {
            assertThat(memberRepository.findById(existingMember.getId())).isPresent();
            memberRepository.deleteById(existingMember.getId());
            assertThat(memberRepository.findById(existingMember.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 삭제 시 미삭제")
        void 존재하지_않는_ID로_삭제_시_미삭제() {
            assertThat(memberRepository.findById(existingMember.getId())).isPresent();
            memberRepository.deleteById(500L);
            assertThat(memberRepository.findById(existingMember.getId())).isPresent();
        }
    }

    private boolean memberDetailsEquals(Member member1, Member member2) {
        return member1.getEmail().equals(member2.getEmail())
                && member1.getPassword().equals(member2.getPassword())
                && member1.getRole().equals(member2.getRole());
    }
}