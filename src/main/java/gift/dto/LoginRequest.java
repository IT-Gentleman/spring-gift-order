package gift.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "이메일은 제시되어야합니다.")
        @Email(message = "올바른 이메일 양식이 아닙니다.")
        String email,

        @NotNull(message = "비밀번호는 제시되어야합니다.")
        String password
) {

    public static LoginRequest empty() {
        return new LoginRequest("", "");
    }
}
