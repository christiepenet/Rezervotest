package ro.rezervo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limiteaza numarul de incercari de login / creare cont pe adresa IP,
 * ca sa ingreuneze ghicirea parolelor (brute force).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final int maxAttempts;
    private final Map<String, Window> attempts = new ConcurrentHashMap<>();

    public RateLimitFilter(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && request.getRequestURI().startsWith("/api/auth/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Instant now = Instant.now();
        if (attempts.size() > 10_000) {
            attempts.values().removeIf(w -> w.start.plus(WINDOW).isBefore(now));
        }
        String ip = request.getRemoteAddr();
        Window w = attempts.compute(ip, (k, old) ->
                (old == null || old.start.plus(WINDOW).isBefore(now)) ? new Window(now, 1) : new Window(old.start, old.count + 1));

        if (w.count > maxAttempts) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"error\":\"Prea multe incercari. Incearca din nou peste cateva minute.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private record Window(Instant start, int count) {
    }
}
