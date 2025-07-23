package gift.dto;

import gift.entity.Member;
import gift.entity.Role;

// MemberService -> Controller
public record MemberDto(
        Long id,
        String email,
        String password, // only password reset knowledge. else is null
        Role role
) {

    public static MemberDto from(Member member) {
        return new MemberDto(
                member.getId(),
                member.getEmail(),
                null,
                member.getRole()
        );
    }

    // only password reset knowledge
    public static MemberDto from(Member member, String password) {
        return new MemberDto(
                member.getId(),
                member.getEmail(),
                password,
                member.getRole()
        );
    }
}
