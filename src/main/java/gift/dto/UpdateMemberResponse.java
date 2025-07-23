package gift.dto;

import gift.entity.Member;

public record UpdateMemberResponse(
        Member member,
        String temporalPassword
) {

}
