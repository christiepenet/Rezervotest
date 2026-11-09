package ro.rezervo.offering;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ro.rezervo.appointment.AppointmentService;
import ro.rezervo.common.ApiException;
import ro.rezervo.offering.ServiceDtos.ServiceRequest;
import ro.rezervo.offering.ServiceDtos.ServiceResponse;
import ro.rezervo.security.AuthUser;
import ro.rezervo.user.Role;
import ro.rezervo.user.User;
import ro.rezervo.user.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceOfferingController {

    private final ServiceOfferingRepository offerings;
    private final UserRepository users;
    private final AppointmentService appointmentService;

    public ServiceOfferingController(ServiceOfferingRepository offerings, UserRepository users,
                                     AppointmentService appointmentService) {
        this.offerings = offerings;
        this.users = users;
        this.appointmentService = appointmentService;
    }

    /** Lista publica de servicii. */
    @GetMapping
    public List<ServiceResponse> list() {
        return offerings.findAllActive().stream().map(ServiceResponse::from).toList();
    }

    /** Serviciile furnizorului autentificat. */
    @GetMapping("/mine")
    public List<ServiceResponse> mine(@AuthenticationPrincipal AuthUser me) {
        requireProvider(me);
        return offerings.findActiveByProvider(me.id()).stream().map(ServiceResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ServiceResponse get(@PathVariable Long id) {
        return offerings.findActiveById(id)
                .map(ServiceResponse::from)
                .orElseThrow(() -> ApiException.notFound("Serviciul nu exista"));
    }

    /** Orele libere pentru o zi (ex: /api/services/1/slots?date=2026-11-16). */
    @GetMapping("/{id}/slots")
    public List<LocalDateTime> slots(@PathVariable Long id,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.availableSlots(id, date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ServiceResponse create(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody ServiceRequest req) {
        requireProvider(me);
        User provider = users.findById(me.id()).orElseThrow(() -> ApiException.unauthorized("Contul nu mai exista"));
        String description = req.description() == null ? null : req.description().trim();
        ServiceOffering saved = offerings.save(new ServiceOffering(
                provider, req.name().trim(), description, req.durationMinutes(), req.price()));
        return ServiceResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@AuthenticationPrincipal AuthUser me, @PathVariable Long id) {
        requireProvider(me);
        ServiceOffering s = offerings.findActiveById(id)
                .orElseThrow(() -> ApiException.notFound("Serviciul nu exista"));
        if (!s.getProvider().getId().equals(me.id())) {
            // Nu dezvaluim ca serviciul exista daca nu e al tau.
            throw ApiException.notFound("Serviciul nu exista");
        }
        s.deactivate();
    }

    private static void requireProvider(AuthUser me) {
        if (me.role() != Role.PROVIDER) {
            throw ApiException.forbidden("Doar furnizorii de servicii pot face asta");
        }
    }
}
