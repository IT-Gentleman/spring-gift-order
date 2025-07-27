package gift.dto.auth;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

public record KakaoCreateTokenCommand(
        String uri,
        MediaType contentType,
        String grantType,
        String clientId,
        String redirectUri,
        String code
) implements KakaoTokenCommand {

    public KakaoCreateTokenCommand(String grantType, String clientId, String redirectUri, String code) {
        this("https://kauth.kakao.com/oauth/token", MediaType.APPLICATION_FORM_URLENCODED,
                grantType, clientId, redirectUri, code);
    }

    @Override
    public MultiValueMap<String, String> getFormData() {
        MultiValueMap<String, String> formData = new org.springframework.util.LinkedMultiValueMap<>();
        formData.add("grant_type", grantType);
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        return formData;
    }
}
