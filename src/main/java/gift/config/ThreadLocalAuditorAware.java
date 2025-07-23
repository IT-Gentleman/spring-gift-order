package gift.config;

import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.util.LoginMemberContextHolder;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component("threadLocalAuditorAware")
public class ThreadLocalAuditorAware implements AuditorAware<Member> {

    private final MemberRepository memberRepository;

    public ThreadLocalAuditorAware(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Optional<Member> getCurrentAuditor() {
        Long userId = LoginMemberContextHolder.get();
        if (userId == null) {
            return Optional.empty();
        }
        return memberRepository.findById(userId);
        //return Optional.of(Member.emptyOfId(userId));
    }
}
