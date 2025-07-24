package gift.config;

import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.util.LoginMemberContextHolder;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component("threadLocalAuditorAware")
public class ThreadLocalAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(LoginMemberContextHolder.get());
    }
}
