package gift.dto;

public record MemberResponse(
        Long id,
        String email,
        String role
) {

    public static MemberResponse from(MemberDto memberDto) {
        return new MemberResponse(
                memberDto.id(),
                memberDto.email(),
                memberDto.role().name()
        );
    }
}
