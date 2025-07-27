package gift.service;

import static gift.util.HttpUtil.sendBodilessPost;
import static gift.util.HttpUtil.sendPost;

import gift.dto.auth.KakaoMemberResponse;
import gift.dto.auth.KakaoTokenCommand;
import gift.dto.auth.KakaoTokenResponse;
import gift.entity.KakaoToken;
import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.token.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KakaoAuthService {

    private final RestClient restClient;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenEncryptionService tokenEncryptionService;

    private final String kakaoClientId;

    public KakaoAuthService(RestClient restClient, MemberRepository memberRepository,
            JwtTokenProvider jwtTokenProvider, TokenEncryptionService tokenEncryptionService,
            @Value("${kakao.client-id}") String kakaoClientId) {
        this.restClient = restClient;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenEncryptionService = tokenEncryptionService;
        this.kakaoClientId = kakaoClientId;
    }

    @Transactional
    public String loginWithKakao(KakaoTokenCommand command) {
        KakaoTokenResponse kakaoToken = getKakaoToken(command);
        KakaoMemberResponse kakaoMember = getKakaoMember(kakaoToken.accessToken());
        Member ourMember = getMemberOrCreateFromKakaoId(kakaoMember.id());
        saveKakaoToken(ourMember, kakaoToken.accessToken(), kakaoToken.refreshToken());
        return jwtTokenProvider.createToken(ourMember);
    }

    private KakaoTokenResponse getKakaoToken(KakaoTokenCommand command) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Charset", "UTF-8");
        ResponseEntity<KakaoTokenResponse> response =
                sendPost(restClient, command.uri(), command.contentType(), headers,
                        command.getFormData(), KakaoTokenResponse.class);
        return getBodyOf(response);
    }

    private KakaoMemberResponse getKakaoMember(String accessToken) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Charset", "UTF-8");
        ResponseEntity<KakaoMemberResponse> response =
                sendBodilessPost(restClient, "https://kapi.kakao.com/v2/user/me",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        headers, KakaoMemberResponse.class);
        return getBodyOf(response);
    }

    private void saveKakaoToken(Member member, String accessToken, String refreshToken) {
        KakaoToken tokenToSave = new KakaoToken(member,
                tokenEncryptionService.encrypt(accessToken),
                tokenEncryptionService.encrypt(refreshToken)
        );
        member.updateKakaoToken(tokenToSave);
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
                .orElseGet(() -> memberRepository.save(new Member(kakaoId)));
    }
}
