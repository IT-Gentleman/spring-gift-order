package gift.handler;

import gift.controller.MemberAdminPageController;
import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = MemberAdminPageController.class)
@Order(1)
public class MemberAdminPageControllerExceptionHandler {

    private final String mainPage = "/admin/members";

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException ex, Model model, HttpServletRequest request) {
        String errorMessage = "Member not found : 유효하지 않은 회원ID로 접근하였습니다.\n" + ex.getMessage();
        model.addAttribute("errorMessage", errorMessage);
        String referer = request.getHeader("Referer");
        model.addAttribute("prevPage", referer);
        model.addAttribute("mainPage", mainPage);
        return "error/custom-error";
    }

    @ExceptionHandler(ConflictException.class)
    public String handleConflictException(ConflictException ex, Model model, HttpServletRequest request) {
        String errorMessage = "Conflict Error : 이미 존재하는 회원 이메일입니다.\n" + ex.getMessage();
        model.addAttribute("errorMessage", errorMessage);
        String referer = request.getHeader("Referer");
        model.addAttribute("prevPage", referer);
        model.addAttribute("mainPage", mainPage);
        return "error/custom-error";
    }
}