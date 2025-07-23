package gift.config;

import gift.entity.Member;
import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditingTestConfig {

    @Bean
    public AuditorAware<Member> auditorAware() {
        return () -> Optional.empty();
        //return () -> Optional.of(Member.emptyOfId(1L));
    }
}