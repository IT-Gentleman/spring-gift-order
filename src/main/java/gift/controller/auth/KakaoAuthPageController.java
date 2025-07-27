package gift.controller.auth;

import gift.dto.auth.KakaoAuthRequest;
import gift.dto.auth.KakaoCreateTokenCommand;
import gift.dto.auth.LoginResponse;
import gift.service.KakaoAuthService;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping
public class KakaoAuthPageController {

    private final KakaoAuthService kakaoAuthService;

    private final String kakaoClientId;
    private final String baseUri;
    private final String kakaoLoginRedirectUri = "/kakao-auth";

    public KakaoAuthPageController(KakaoAuthService kakaoAuthService,
            @Value("${kakao.client-id}") String kakaoClientId,
            @Value("${uri}") String baseUri) {
        this.kakaoAuthService = kakaoAuthService;
        this.kakaoClientId = kakaoClientId;
        this.baseUri = baseUri;
    }

    @GetMapping("/kakao-login")
    public ResponseEntity<Void> kakaoLogin() {
        URI redirectUri = URI.create(
                "https://kauth.kakao.com/oauth/authorize?client_id=" + kakaoClientId
                        + "&redirect_uri=" + baseUri + kakaoLoginRedirectUri
                        + "&response_type=code"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(redirectUri);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping(kakaoLoginRedirectUri)
    public ResponseEntity<LoginResponse> kakaoAuth(KakaoAuthRequest request) {
        if (request.code() == null || request.code().isEmpty()) {
            throw new ResponseStatusException(request.getHttpStatusOfError(),
                    request.errorDescription());
        }
        KakaoCreateTokenCommand command = new KakaoCreateTokenCommand(
                "authorization_code",
                kakaoClientId,
                baseUri + kakaoLoginRedirectUri,
                request.code()
        );
        String ourToken = kakaoAuthService.loginWithKakao(command);
        return ResponseEntity.ok(new LoginResponse(ourToken));
    }

}
