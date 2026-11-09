package ro.rezervo.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ro.rezervo.auth.AuthDtos.AuthResponse;
import ro.rezervo.auth.AuthDtos.LoginRequest;
import ro.rezervo.auth.AuthDtos.RegisterRequest;
import ro.rezervo.auth.AuthDtos.UserResponse;
import ro.rezervo.common.ApiException;
import ro.rezervo.security.AuthUser;
import ro.rezervo.security.JwtService;
import ro.rezervo.user.User;
import ro.rezervo.user.UserRepository;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    /** Hash folosit cand email-ul nu exista, ca timpul de raspuns sa nu tradeze conturile existente. */
    private final String dummyHash;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("parola-inexistenta-123");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        String email = normalize(req.email());
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Exista deja un cont cu acest email");
        }
        User user = users.save(new User(
                req.fullName().trim(),
                email,
                passwordEncoder.encode(req.password()),
                req.role()));
        return new AuthResponse(jwtService.createToken(user), UserResponse.from(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = users.findByEmail(normalize(req.email())).orElse(null);
        String hash = user != null ? user.getPasswordHash() : dummyHash;
        boolean ok = passwordEncoder.matches(req.password(), hash);
        if (user == null || !ok) {
            throw ApiException.unauthorized("Email sau parola gresita");
        }
        return new AuthResponse(jwtService.createToken(user), UserResponse.from(user));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser me) {
        return users.findById(me.id())
                .map(UserResponse::from)
                .orElseThrow(() -> ApiException.unauthorized("Contul nu mai exista"));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
