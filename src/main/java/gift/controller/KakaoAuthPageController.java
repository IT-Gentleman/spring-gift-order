package gift.controller;

import gift.dto.KakaoTokenCommand;
import gift.dto.LoginResponse;
import gift.service.KakaoAuthService;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public ResponseEntity<LoginResponse> kakaoAuth(String code) {
        KakaoTokenCommand command = new KakaoTokenCommand(
                "https://kauth.kakao.com/oauth/token",
                MediaType.APPLICATION_FORM_URLENCODED,
                "authorization_code",
                kakaoClientId,
                baseUri + kakaoLoginRedirectUri,
                code
        );
        String ourToken = kakaoAuthService.loginWithKakao(command);
        return ResponseEntity.ok(new LoginResponse(ourToken));
    }

}
