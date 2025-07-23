package gift.filter;

import gift.entity.Role;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

// 2. using extracted token, checking if the user has the right permissions to access the requested resource
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String role = (String) httpRequest.getAttribute("role");
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod().toUpperCase();

        if (!isAuthorized(uri, method, role)) {
            sendError(httpResponse, role);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAuthorized(String uri, String method, String role) {
        // /api/products/{id}/options : method에 따라 제한 role 존재
        if (uri.startsWith("/api/products/") && uri.contains("options")) {
            return checkApiProductsOptionsAuthorization(method, role);
        }
        // /api/products : method에 따라 제한 role 존재
        if (uri.startsWith("/api/products")) {
            return checkApiProductsAuthorization(method, role);
        }
        // /api/wishes : 인증된 사용자만 접근 가능
        if (uri.startsWith("/api/wishes")) {
            return isAuthenticated(role);
        }
        // /admin/products/new : MD role만 접근 가능
        if (uri.startsWith("/admin/products/new")) {
            return hasRole(role, Role.ROLE_MD);
        }
        // /admin/products : method에 따라 제한 role 존재
        if (uri.startsWith("/admin/products")) {
            return checkAdminProductsAuthorization(method, role);
        }
        // /admin/members/new : ADMIN role만 접근 가능
        if (uri.startsWith("/admin/members/new")) {
            return hasRole(role, Role.ROLE_ADMIN);
        }
        // /admin/members : method에 따라 제한 role 존재
        if (uri.startsWith("/admin/members")) {
            return checkAdminMembersAuthorization(method, role);
        }
        return true; // Authorize by default for public endpoints (ex. /api/members : for register & login)
    }

    private boolean checkApiProductsOptionsAuthorization(String method, String role) {
        switch (method) {
            case "POST", "PATCH", "DELETE":
                return hasAnyRole(role, Role.ROLE_MD);
            default:
                return true;
        }
    }

    private boolean checkApiProductsAuthorization(String method, String role) {
        switch (method) {
            case "POST":
                return hasAnyRole(role, Role.ROLE_SELLER, Role.ROLE_MD);
            case "PATCH", "DELETE":
                return hasRole(role, Role.ROLE_MD);
            default:
                return true;
        }
    }

    private boolean checkAdminProductsAuthorization(String method, String role) {
        switch (method) {
            case "GET":
                return hasAnyRole(role, Role.ROLE_MD, Role.ROLE_CS);
            case "POST", "PATCH", "DELETE":
                return hasRole(role, Role.ROLE_MD);
            default:
                return true;
        }
    }

    private boolean checkAdminMembersAuthorization(String method, String role) {
        switch (method) {
            case "GET":
                return hasAnyRole(role, Role.ROLE_ADMIN, Role.ROLE_CS);
            case "POST", "PATCH", "DELETE":
                return hasRole(role, Role.ROLE_ADMIN);
            default:
                return true;
        }
    }

    private boolean isAuthenticated(String role) {
        return role != null && !role.isEmpty();
    }

    private boolean hasRole(String userRole, Role requiredRole) {
        return isAuthenticated(userRole) && userRole.equals(requiredRole.name());
    }

    private boolean hasAnyRole(String userRole, Role... requiredRoles) {
        return isAuthenticated(userRole)
                && Arrays.stream(requiredRoles).anyMatch(r -> userRole.equals(r.name()));
    }

    private void sendError(HttpServletResponse httpResponse, String role) throws IOException {
        if (!isAuthenticated(role)) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } else {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        }
    }
}
