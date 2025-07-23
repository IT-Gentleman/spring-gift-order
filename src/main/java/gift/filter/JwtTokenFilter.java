package gift.filter;

import gift.token.JwtTokenProvider;
import gift.util.LoginMemberContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String token = Optional.ofNullable(getTokenFromCookies(request))
                .orElse(getTokenFromAuthorizationHeader(request));
        if (token != null && jwtTokenProvider.validateToken(token)) {
            LoginMemberContextHolder.set(jwtTokenProvider.getId(token));
            request.setAttribute("username", jwtTokenProvider.getEmail(token));
            request.setAttribute("role", jwtTokenProvider.getRole(token));
        } else {
            request.setAttribute("username", null);
            request.setAttribute("role", null);
        }
        chain.doFilter(request, response);
        LoginMemberContextHolder.clear();
    }

    private String getTokenFromCookies(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, "token"))
                .map(cookie -> cookie.getValue())
                .orElse(null);
    }

    private String getTokenFromAuthorizationHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
