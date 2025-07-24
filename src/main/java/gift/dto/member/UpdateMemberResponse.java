package gift.dto.member;

import gift.entity.Member;

public record UpdateMemberResponse(
        Member member,
        String temporalPassword
) {

}
