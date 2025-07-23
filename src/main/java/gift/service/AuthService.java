package gift.service;

import gift.dto.AuthenticatedMember;
import gift.dto.LoginCommand;
import gift.entity.Member;
import gift.exception.InvalidCredentialsException;
import gift.repository.MemberRepository;
import gift.token.JwtTokenProvider;
import gift.util.BCryptEncryptor;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public String login(LoginCommand command) {
        Optional<Member> optionalMember = memberRepository.findByEmailAndDeletedAtIsNull(
                command.email());
        if (optionalMember.isEmpty() || !BCryptEncryptor.matches(command.password(),
                optionalMember.get().getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return jwtTokenProvider.createToken(optionalMember.get());
    }

    @Transactional(readOnly = true)
    public AuthenticatedMember getAuthenticationFromToken(String token) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        Long memberId = jwtTokenProvider.getId(token);
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return AuthenticatedMember.from(optionalMember.get());
    }
}
