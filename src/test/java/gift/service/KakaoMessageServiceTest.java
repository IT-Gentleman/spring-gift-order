package gift.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.kakaomessage.SendKakaoMessageRequest;
import gift.dto.kakaomessage.SendKakaoMessageResponse;
import gift.entity.Member;
import gift.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class KakaoMessageServiceTest {

    @Mock
    private KakaoAuthService kakaoAuthService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KakaoMessageService kakaoMessageService;

    @Nested
    @DisplayName("sendMessageToSelf 메서드")
    class SendMessageToSelfTests {

        Member kakaoMember = new Member(1L, 121L, null);
        Product product = new Product(1L, "Test Product", 10000, "sample-image-url", true, false);
        SendKakaoMessageRequest request = new SendKakaoMessageRequest(
                "sample message title",
                kakaoMember.getId(),
                kakaoMember.getId(),
                "sample kakao message including message which user sent",
                product.getImageUrl(),
                product.getImageUrl()
        );

        @Test
        @DisplayName("메시지 전송 성공")
        void sendMessageToSelf_Success() throws JsonProcessingException {
            // arrange
            when(objectMapper.writeValueAsString(any()))
                    .thenReturn("{\"template_object\":\"sample template object\"}");
            when(kakaoAuthService.executeWithKakaoTokenRefresh(any(), any())).thenReturn(new SendKakaoMessageResponse(0));

            // act & assert
            assertDoesNotThrow(() -> {
                kakaoMessageService.sendMessageToSelf(kakaoMember.getId(), request);
            });
        }

        @Test
        @DisplayName("메시지 전송 실패 - 전송실패코드 반환")
        void sendMessageToSelf_Failure() throws JsonProcessingException {
            // arrange
            when(objectMapper.writeValueAsString(any()))
                    .thenReturn("{\"template_object\":\"sample template object\"}");
            when(kakaoAuthService.executeWithKakaoTokenRefresh(any(), any())).thenReturn(new SendKakaoMessageResponse(100));

            // act & assert
            assertThrows(ResponseStatusException.class, () -> {
                kakaoMessageService.sendMessageToSelf(kakaoMember.getId(), request);
            });
        }
    }

}
