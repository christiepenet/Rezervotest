package ro.rezervo.security;

import ro.rezervo.user.Role;

/** Utilizatorul autentificat pentru cererea curenta. */
public record AuthUser(Long id, String email, Role role) {
}
