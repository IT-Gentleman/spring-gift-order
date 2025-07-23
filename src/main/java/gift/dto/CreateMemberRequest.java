package gift.dto;

import gift.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// for Admin Page. Authority is required.
public record CreateMemberRequest(
        @NotNull(message = "이메일은 제시되어야합니다.")
        @Email(message = "올바른 이메일 양식이 아닙니다.")
        String email,

        @NotNull(message = "비밀번호는 제시되어야합니다.")
        @Size(min = 15, max = 64, message = "비밀번호는 15자 이상 64자 이내여야 합니다.")
        String password,

        @NotNull(message = "권한은 제시되어야합니다.")
        Role role
) {

    public static CreateMemberRequest empty() {
        return new CreateMemberRequest("", "", Role.ROLE_USER);
    }
}
