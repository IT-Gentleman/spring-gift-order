package gift.dto.auth;

public record LoginCommand(
        String email,
        String password
) {

}
