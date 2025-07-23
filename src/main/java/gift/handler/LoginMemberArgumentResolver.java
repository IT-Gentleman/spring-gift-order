package gift.handler;

import gift.service.AuthService;
import gift.validator.LoginMember;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    public LoginMemberArgumentResolver(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        //LoginMember loginMember = parameter.getParameterAnnotation(LoginMember.class);

        String authority = webRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authority == null || !authority.startsWith("Bearer ") || Optional.ofNullable(
                authority.split("Bearer ")[1]).isEmpty()) {
            throw new IllegalArgumentException("Invalid or missing authorization header");
        }
        String token = authority.split("Bearer ")[1].trim();
        return authService.getAuthenticationFromToken(token);
    }
}
