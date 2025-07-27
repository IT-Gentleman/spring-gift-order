package gift.dto.auth;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

public interface KakaoTokenCommand {
    String uri();
    MediaType contentType();
    MultiValueMap<String, String> getFormData();

}
