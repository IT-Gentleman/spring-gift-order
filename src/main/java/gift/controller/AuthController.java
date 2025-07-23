package gift.controller;

import gift.dto.LoginCommand;
import gift.dto.LoginRequest;
import gift.dto.LoginResponse;
import gift.dto.NewMemberCommand;
import gift.dto.RegisterMemberRequest;
import gift.dto.RegisterMemberResponse;
import gift.service.AuthService;
import gift.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    private final AuthService authService;

    public AuthController(MemberService memberService, AuthService authService) {
        this.memberService = memberService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterMemberResponse> createMember(
            @Valid @RequestBody RegisterMemberRequest registerMemberRequest
    ) {
        NewMemberCommand newMemberCommand = new NewMemberCommand(
                registerMemberRequest.email(),
                registerMemberRequest.password()
        );
        memberService.createMember(newMemberCommand);

        LoginCommand loginCommand = new LoginCommand(
                registerMemberRequest.email(),
                registerMemberRequest.password()
        );
        String token = authService.login(loginCommand);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RegisterMemberResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginCommand loginCommand = new LoginCommand(
                loginRequest.email(),
                loginRequest.password()
        );
        String token = authService.login(loginCommand);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new LoginResponse(token));
    }
}
