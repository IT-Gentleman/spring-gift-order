package gift.dto;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

public record KakaoTokenCommand(
        String uri,
        MediaType contentType,
        String grantType,
        String clientId,
        String redirectUri,
        String code
) {

    public KakaoTokenCommand(String grantType, String clientId, String redirectUri, String code) {
        this("https://kauth.kakao.com/oauth/token", MediaType.APPLICATION_FORM_URLENCODED,
                grantType, clientId, redirectUri, code);
    }

    public MultiValueMap<String, String> getFormData() {
        MultiValueMap<String, String> formData = new org.springframework.util.LinkedMultiValueMap<>();
        formData.add("grant_type", grantType);
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        return formData;
    }
}
