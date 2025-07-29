package gift.service;

import static gift.util.HttpUtil.sendPost;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.kakaomessage.Content;
import gift.dto.kakaomessage.FeedKakaoMessageRequest;
import gift.dto.kakaomessage.Link;
import gift.dto.kakaomessage.SendKakaoMessageRequest;
import gift.dto.kakaomessage.SendKakaoMessageResponse;
import gift.util.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KakaoMessageService {

    private final RestClient restClient;
    private final KakaoAuthService kakaoAuthService;
    private final ObjectMapper objectMapper;

    public KakaoMessageService(RestClient restClient, KakaoAuthService kakaoAuthService,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.kakaoAuthService = kakaoAuthService;
        this.objectMapper = objectMapper;
    }

    public void sendMessageToSelf(Long memberId, SendKakaoMessageRequest request) {
        FeedKakaoMessageRequest templateDto = new FeedKakaoMessageRequest(
                new Content(
                        "You got a new message!",
                        request.messageContent(),
                        request.imageUrl(),
                        new Link(request.linkUrl())
                )
        );
        String templateObjectJson = JsonUtil.toJsonString(objectMapper, templateDto);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("template_object", templateObjectJson);


        SendKakaoMessageResponse response = kakaoAuthService.getBodyOf(
                kakaoAuthService.executeWithKakaoTokenRefresh(memberId, accessToken -> {
                    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                    headers.add("Authorization", "Bearer " + accessToken);

                    return sendPost(restClient,
                            "https://kapi.kakao.com/v2/api/talk/memo/default/send",
                            MediaType.APPLICATION_FORM_URLENCODED,
                            headers,
                            body,
                            SendKakaoMessageResponse.class
                    );
                })
        );
        if (response.resultCode() != 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to send Kakao message");
        }
    }
}
