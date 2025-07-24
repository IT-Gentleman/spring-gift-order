package gift.dto.member;

import gift.entity.Role;

public record UpdateMemberCommand(
        Long id,
        String email,
        Boolean resetPassword,
        Role role
) {

}
