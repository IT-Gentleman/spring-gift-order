package gift.handler;

import gift.controller.ProductAdminPageController;
import gift.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = ProductAdminPageController.class)
@Order(1)
public class ProductAdminPageControllerExceptionHandler {

    private final String mainPage = "/admin/products";

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException ex, Model model,
            HttpServletRequest request) {
        String errorMessage = "Product not found : 유효하지 않은 상품ID로 접근하였습니다.\n" + ex.getMessage();
        model.addAttribute("errorMessage", errorMessage);
        String referer = request.getHeader("Referer");
        model.addAttribute("prevPage", referer);
        model.addAttribute("mainPage", mainPage);
        return "error/custom-error";
    }
}