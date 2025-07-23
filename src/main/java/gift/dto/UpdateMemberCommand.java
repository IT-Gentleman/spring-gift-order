package gift.dto;

import gift.entity.Role;

public record UpdateMemberCommand(
        Long id,
        String email,
        Boolean resetPassword,
        Role role
) {

}
