package ro.rezervo.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ro.rezervo.user.Role;
import ro.rezervo.user.User;

/** Obiectele JSON primite si trimise de endpoint-urile de autentificare. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Numele este obligatoriu")
            @Size(min = 2, max = 100, message = "Numele trebuie sa aiba intre 2 si 100 de caractere")
            String fullName,

            @NotBlank(message = "Email-ul este obligatoriu")
            @Email(message = "Email invalid")
            @Size(max = 255, message = "Email prea lung")
            String email,

            @NotBlank(message = "Parola este obligatorie")
            @Size(min = 8, max = 72, message = "Parola trebuie sa aiba intre 8 si 72 de caractere")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Parola trebuie sa contina cel putin o litera si o cifra")
            String password,

            @NotNull(message = "Alege tipul de cont")
            Role role) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email-ul este obligatoriu")
            @Email(message = "Email invalid")
            String email,

            @NotBlank(message = "Parola este obligatorie")
            @Size(max = 72, message = "Parola este prea lunga")
            String password) {
    }

    public record UserResponse(Long id, String fullName, String email, Role role) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole());
        }
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}
