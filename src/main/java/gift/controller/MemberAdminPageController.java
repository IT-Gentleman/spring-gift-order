package gift.controller;

import gift.dto.CreateMemberRequest;
import gift.dto.MemberDto;
import gift.dto.MemberResponse;
import gift.dto.NewMemberCommand;
import gift.dto.PageRequest;
import gift.dto.PageResponse;
import gift.dto.UpdateMemberCommand;
import gift.dto.UpdateMemberRequest;
import gift.service.MemberService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/members")
public class MemberAdminPageController {

    private final MemberService memberService;

    public MemberAdminPageController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String getMembers(
            Model model,
            @Valid PageRequest pageRequest
    ) {
        Set<String> allowedSortFields = Set.of("id", "email");
        String defaultSortField = "id";
        Sort.Direction defaultSortDirection = Sort.Direction.DESC;
        Pageable pageable = pageRequest.toPageable(allowedSortFields, defaultSortField,
                defaultSortDirection);

        Page<MemberDto> memberList = memberService.getMemberList(pageable);
        Page<MemberResponse> response = memberList.map(MemberResponse::from);
        model.addAttribute("members", PageResponse.from(response));
        return "admin/member-list";
    }

    @GetMapping("/new")
    public String createMember(Model model) {
        model.addAttribute("member", CreateMemberRequest.empty());
        model.addAttribute("memberId", null);
        return "admin/member-form";
    }

    @PostMapping
    public String createMember(
            @Valid @ModelAttribute CreateMemberRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", null);
            model.addAttribute("member", request);
            String errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> "- " + error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            model.addAttribute("message", "Invalid input. Check again.\n" + errorMessages);
            return "admin/member-form";
        }
        NewMemberCommand newMemberCommand = new NewMemberCommand(
                request.email(),
                request.password(),
                request.role()
        );
        MemberDto createdMember = memberService.createMember(newMemberCommand);
        model.addAttribute("member", UpdateMemberRequest.from(createdMember));
        redirectAttributes.addFlashAttribute("message", "Member created successfully.");
        return "redirect:/admin/members/" + createdMember.id();
    }

    @GetMapping("/{id}")
    public String getMember(
            @PathVariable("id") Long identifyNumber,
            Model model
    ) {
        MemberDto member = memberService.getMemberById(identifyNumber);
        model.addAttribute("member", UpdateMemberRequest.from(member));
        model.addAttribute("memberId", identifyNumber);
        return "admin/member-form";
    }

    @PutMapping("/{id}")
    public String updateMember(
            @PathVariable("id") Long identifyNumber,
            @Valid @ModelAttribute UpdateMemberRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", identifyNumber);
            model.addAttribute("member", request);
            String errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> "- " + error.getDefaultMessage())
                    .collect(Collectors.joining("\n"));
            model.addAttribute("message", "Invalid input. Check again.\n" + errorMessages);
            return "admin/member-form";
        }
        UpdateMemberCommand updateMemberCommand = new UpdateMemberCommand(
                identifyNumber,
                request.email(),
                request.resetPassword(),
                request.role()
        );
        MemberDto updatedMember = memberService.updateMember(updateMemberCommand);
        String temporalPassword = updatedMember.password();
        String temporalPasswordInstruction = temporalPassword != null && !temporalPassword.isEmpty()
                ? "\nTemporary password: " + temporalPassword
                : "";
        redirectAttributes.addFlashAttribute("message",
                "Member updated successfully." + temporalPasswordInstruction);
        return "redirect:/admin/members/" + identifyNumber;
    }

    @DeleteMapping("/{id}")
    public String deleteMember(
            @PathVariable("id") Long identifyNumber,
            RedirectAttributes redirectAttributes
    ) {
        memberService.deleteMember(identifyNumber);
        redirectAttributes.addFlashAttribute("message", "Member deleted successfully.");
        return "redirect:/admin/members";
    }
}
