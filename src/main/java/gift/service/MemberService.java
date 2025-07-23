package gift.service;

import gift.dto.MemberDto;
import gift.dto.NewMemberCommand;
import gift.dto.UpdateMemberCommand;
import gift.entity.Member;
import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.repository.MemberRepository;
import gift.util.BCryptEncryptor;
import gift.util.PasswordUtility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Create
    @Transactional
    public MemberDto createMember(NewMemberCommand newMemberCommand) {
        if (memberRepository.existsByEmail(newMemberCommand.email())) {
            throw new ConflictException("Email already in use: email=" + newMemberCommand.email());
        }
        String encodedPassword = BCryptEncryptor.encrypt(newMemberCommand.password());
        Member member = new Member(newMemberCommand.email(), encodedPassword,
                newMemberCommand.role());
        return MemberDto.from(memberRepository.save(member));
    }

    // Read
    // 동일 패키지 내 사용 제한
    Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Member not found or deleted: id=" + id));
    }

    @Transactional(readOnly = true)
    public Page<MemberDto> getMemberList(Pageable pageable) {
        Page<Member> memberPage = memberRepository.findAllByDeletedAtIsNull(pageable);
        return memberPage.map(MemberDto::from);
    }

    @Transactional(readOnly = true)
    public MemberDto getMemberById(Long id) {
        Member member = findMemberById(id);
        return MemberDto.from(member);
    }

    // Update
    @Transactional
    public MemberDto updateMember(UpdateMemberCommand updateMemberCommand) {
        Member member = findMemberById(updateMemberCommand.id());
        if (!member.getEmail().equals(updateMemberCommand.email())
                && memberRepository.existsByEmail(updateMemberCommand.email())) {
            throw new ConflictException(
                    "Email already in use: email=" + updateMemberCommand.email());
        }
        String rawPassword = null;
        String encodedPassword = null;
        if (updateMemberCommand.resetPassword()) {
            rawPassword = PasswordUtility.generateRandomPassword();
            encodedPassword = BCryptEncryptor.encrypt(rawPassword);
        }
        member.applyPatch(updateMemberCommand.email(), encodedPassword, updateMemberCommand.role());
        return MemberDto.from(member, rawPassword);
    }

    // Delete
    @Transactional
    public void deleteMember(Long id) {
        Member member = findMemberById(id);
        member.setDeleted(); // soft delete
    }
}
