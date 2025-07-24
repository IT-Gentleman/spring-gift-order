package gift.service;

import gift.dto.auth.KakaoMemberResponse;
import gift.dto.auth.KakaoTokenCommand;
import gift.dto.auth.KakaoTokenResponse;
import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.token.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KakaoAuthService {

    private final RestClient restClient;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public KakaoAuthService(RestClient restClient, MemberRepository memberRepository,
            JwtTokenProvider jwtTokenProvider) {
        this.restClient = restClient;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public String loginWithKakao(KakaoTokenCommand command) {
        KakaoTokenResponse kakaoToken = getKakaoToken(command);
        KakaoMemberResponse kakaoMember = getKakaoMember(kakaoToken.accessToken());
        Member ourMember = getMemberOrCreateFromKakaoId(kakaoMember.id());
        return jwtTokenProvider.createToken(ourMember);
    }

    private KakaoTokenResponse getKakaoToken(KakaoTokenCommand command) {
        ResponseEntity<KakaoTokenResponse> response = restClient.post()
                .uri(command.uri())
                .contentType(command.contentType())
                .header("Charset", "UTF-8")
                .body(command.getFormData())
                .retrieve()
                .toEntity(KakaoTokenResponse.class);
        return getBodyOf(response);
    }

    private KakaoMemberResponse getKakaoMember(String accessToken) {
        ResponseEntity<KakaoMemberResponse> response = restClient.post()
                .uri("https://kapi.kakao.com/v2/user/me")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + accessToken)
                .header("Charset", "UTF-8")
                .retrieve()
                .toEntity(KakaoMemberResponse.class);
        return getBodyOf(response);
    }

    private <T> T getBodyOf(ResponseEntity<T> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else if (response.getStatusCode().is4xxClientError()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "올바르지 않은 입력값입니다: " + response.getStatusCode());
        } else if (response.getStatusCode().is5xxServerError()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "알 수 없는 오류 발생: " + response.getStatusCode());
        }
        return null;
    }

    private Member getMemberOrCreateFromKakaoId(Long kakaoId) {
        return memberRepository.findByKakaoId(kakaoId)
                .orElse(memberRepository.save(new Member(kakaoId)));
    }
}
