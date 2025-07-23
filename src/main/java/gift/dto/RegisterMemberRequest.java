package gift.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// for API. Authority is fixed to ROLE_USER.
public record RegisterMemberRequest(
        @NotNull(message = "이메일은 제시되어야합니다.")
        @Email(message = "올바른 이메일 양식이 아닙니다.")
        String email,

        @NotNull(message = "비밀번호는 제시되어야합니다.")
        @Size(min = 15, max = 64, message = "비밀번호는 15자 이상 64자 이내여야 합니다.")
        String password
) {

    public static RegisterMemberRequest empty() {
        return new RegisterMemberRequest("", "");
    }
}
