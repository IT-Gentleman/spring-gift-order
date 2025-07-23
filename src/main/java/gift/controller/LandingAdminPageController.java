package gift.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class LandingAdminPageController {

    @GetMapping
    public String getAdminLandingPage(
            HttpServletRequest httpRequest,
            Model model
    ) {
        model.addAttribute("username", httpRequest.getAttribute("username"));
        model.addAttribute("role", httpRequest.getAttribute("role"));
        return "admin/landing-page";
    }
}
