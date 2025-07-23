package gift.dto;

import gift.entity.Role;

// register(api), create member(admin page) 공통 사용
public record NewMemberCommand(
        String email,
        String password,
        Role role
) {

    public NewMemberCommand(String email, String password) {
        this(email, password, Role.ROLE_USER);
    }
}
