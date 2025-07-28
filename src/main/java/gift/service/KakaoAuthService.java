package gift.service;

import static gift.util.HttpUtil.sendBodilessPost;
import static gift.util.HttpUtil.sendPost;

import gift.dto.auth.KakaoMemberResponse;
import gift.dto.auth.KakaoRefreshTokenCommand;
import gift.dto.auth.KakaoTokenCommand;
import java.util.function.Function;
import gift.dto.auth.KakaoCreateTokenCommand;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KakaoAuthService {

    private final RestClient restClient;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenEncryptionService tokenEncryptionService;

    private final String kakaoClientId;

    public KakaoAuthService(RestClient restClient, MemberRepository memberRepository,
            MemberService memberService,
            JwtTokenProvider jwtTokenProvider, TokenEncryptionService tokenEncryptionService,
            @Value("${kakao.client-id}") String kakaoClientId) {
        this.restClient = restClient;
        this.memberRepository = memberRepository;
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenEncryptionService = tokenEncryptionService;
        this.kakaoClientId = kakaoClientId;
    }

    @Transactional
    public String loginWithKakao(KakaoCreateTokenCommand command) {
        KakaoTokenResponse kakaoToken = getKakaoToken(command);
        KakaoMemberResponse kakaoMember = getKakaoMember(kakaoToken.accessToken());
        Member ourMember = getMemberOrCreateFromKakaoId(kakaoMember.id());
        saveOrUpdateKakaoToken(ourMember, kakaoToken.accessToken(), kakaoToken.refreshToken());
        return jwtTokenProvider.createToken(ourMember);
    }

    @Transactional
    public <T> T executeWithKakaoTokenRefresh(Long memberId, Function<String, T> apiCall) {
        Member member = memberService.findMemberByIdNotDeleted(memberId);
        KakaoToken kakaoToken = member.getKakaoToken();
        if (kakaoToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Kakao token not found for member");
        } else if (member.getKakaoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You are not logged in with Kakao");
        }
        String accessToken = tokenEncryptionService.decrypt(kakaoToken.getAccessToken());
        try {
            return apiCall.apply(accessToken);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                throw e; // 다른 오류는 그대로 던짐
            }
            // Access token이 만료된 경우, refresh token을 사용하여 새로운 access token을 받아옴
            KakaoTokenResponse newKakaoToken = getKakaoToken(new KakaoRefreshTokenCommand(
                    kakaoClientId,
                    tokenEncryptionService.decrypt(kakaoToken.getRefreshToken())
            ));
            saveOrUpdateKakaoToken(member, newKakaoToken.accessToken(), newKakaoToken.refreshToken());
            accessToken = newKakaoToken.accessToken();
            return apiCall.apply(accessToken);
        }
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

    private void saveOrUpdateKakaoToken(Member member, String accessToken, String refreshToken) {
        String encryptedAccessToken = tokenEncryptionService.encrypt(accessToken);
        // 카카오는 리프레시 토큰 만료 시에만 새로 발급해주므로, null이 올 수 있음
        String encryptedRefreshToken = refreshToken != null
                ? tokenEncryptionService.encrypt(refreshToken)
                : null;

        KakaoToken kakaoToken = member.getKakaoToken();
        if (kakaoToken != null) {
            // 기존 토큰이 있으면 업데이트
            String finalRefreshToken = encryptedRefreshToken != null ? encryptedRefreshToken :
                    kakaoToken.getRefreshToken();
            kakaoToken.updateTokens(encryptedAccessToken, finalRefreshToken);
        } else {
            // 기존 토큰이 없으면 새로 생성
            KakaoToken newKakaoToken = new KakaoToken(member, encryptedAccessToken,
                    encryptedRefreshToken);
            member.updateKakaoToken(newKakaoToken);
        }
    }

    public <T> T getBodyOf(ResponseEntity<T> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else if (response.getStatusCode().is4xxClientError()) {
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Kakao Access Token Expired");
            }
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
