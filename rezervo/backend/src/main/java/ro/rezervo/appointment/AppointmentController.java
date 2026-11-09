package ro.rezervo.appointment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ro.rezervo.appointment.AppointmentDtos.AppointmentResponse;
import ro.rezervo.appointment.AppointmentDtos.BookingRequest;
import ro.rezervo.security.AuthUser;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse book(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody BookingRequest req) {
        return service.book(me, req.serviceId(), req.startTime(), req.note());
    }

    /** Clientul vede programarile lui; furnizorul vede programarile la serviciile lui. */
    @GetMapping("/mine")
    public List<AppointmentResponse> mine(@AuthenticationPrincipal AuthUser me) {
        return service.mine(me);
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@AuthenticationPrincipal AuthUser me, @PathVariable Long id) {
        return service.cancel(me, id);
    }
}
