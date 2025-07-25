package gift.util;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

public final class HttpUtil {

    private HttpUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static  <T> ResponseEntity<T> sendGet(RestClient restClient, String uri, MultiValueMap<String, String> headers, Class<T> response) {
        return restClient.get()
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(response);
    }

    public static  <T> ResponseEntity<T> sendPost(RestClient restClient, String uri, MediaType contentType, MultiValueMap<String, String> headers, Object body, Class<T> response) {
        return restClient.post()
                .uri(uri)
                .contentType(contentType)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(body)
                .retrieve()
                .toEntity(response);
    }

    public static  <T> ResponseEntity<T> sendBodilessPost(RestClient restClient, String uri, MediaType contentType, MultiValueMap<String, String> headers, Class<T> response) {
        return restClient.post()
                .uri(uri)
                .contentType(contentType)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .toEntity(response);
    }
}
