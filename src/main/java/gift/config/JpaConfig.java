package gift.config;

import gift.entity.Member;
import gift.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    private final MemberRepository memberRepository;

    public JpaConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Bean
    public AuditorAware<Member> auditorAware() {
        return new ThreadLocalAuditorAware(memberRepository);
    }
}