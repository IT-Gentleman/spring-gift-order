package gift.dto.auth;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

public record KakaoRefreshTokenCommand(
        String uri,
        MediaType contentType,
        String grantType,
        String clientId,
        String refreshToken
) implements KakaoTokenCommand {

    public KakaoRefreshTokenCommand(String clientId, String refreshToken) {
        this("https://kauth.kakao.com/oauth/token", MediaType.APPLICATION_FORM_URLENCODED,
                "refresh_token", clientId, refreshToken);
    }

    @Override
    public MultiValueMap<String, String> getFormData() {
        MultiValueMap<String, String> formData = new org.springframework.util.LinkedMultiValueMap<>();
        formData.add("grant_type", grantType);
        formData.add("client_id", clientId);
        formData.add("refresh_token", refreshToken);
        return formData;
    }

}
