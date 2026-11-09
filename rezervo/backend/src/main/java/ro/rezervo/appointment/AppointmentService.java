package ro.rezervo.appointment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.rezervo.appointment.AppointmentDtos.AppointmentResponse;
import ro.rezervo.common.ApiException;
import ro.rezervo.offering.ServiceOffering;
import ro.rezervo.offering.ServiceOfferingRepository;
import ro.rezervo.security.AuthUser;
import ro.rezervo.user.Role;
import ro.rezervo.user.User;
import ro.rezervo.user.UserRepository;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

/** Regulile de business pentru programari. */
@Service
public class AppointmentService {

    /** Program de lucru: Luni-Sambata, 09:00-17:00. */
    static final LocalTime OPEN = LocalTime.of(9, 0);
    static final LocalTime CLOSE = LocalTime.of(17, 0);
    static final int MAX_DAYS_AHEAD = 90;
    static final int MAX_ACTIVE_BOOKINGS_PER_CLIENT = 10;

    private final AppointmentRepository appointments;
    private final ServiceOfferingRepository offerings;
    private final UserRepository users;
    private final ZoneId zone;

    public AppointmentService(AppointmentRepository appointments, ServiceOfferingRepository offerings,
                              UserRepository users, @Value("${app.timezone}") String timezone) {
        this.appointments = appointments;
        this.offerings = offerings;
        this.users = users;
        this.zone = ZoneId.of(timezone);
    }

    @Transactional(readOnly = true)
    public List<LocalDateTime> availableSlots(Long serviceId, LocalDate date) {
        ServiceOffering s = offerings.findActiveById(serviceId)
                .orElseThrow(() -> ApiException.notFound("Serviciul nu exista"));
        LocalDate today = LocalDate.now(zone);
        if (date.isBefore(today) || date.isAfter(today.plusDays(MAX_DAYS_AHEAD)) || !isWorkingDay(date)) {
            return List.of();
        }
        int duration = s.getDurationMinutes();
        LocalDateTime now = LocalDateTime.now(zone);
        List<Appointment> booked = appointments.findOverlapping(s.getProvider().getId(), AppointmentStatus.BOOKED,
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        List<LocalDateTime> free = new ArrayList<>();
        LocalDateTime close = date.atTime(CLOSE);
        for (LocalDateTime start = date.atTime(OPEN); !start.plusMinutes(duration).isAfter(close); start = start.plusMinutes(duration)) {
            LocalDateTime slotStart = start;
            LocalDateTime slotEnd = start.plusMinutes(duration);
            if (!slotStart.isAfter(now)) {
                continue;
            }
            boolean taken = booked.stream().anyMatch(a -> a.getStartTime().isBefore(slotEnd) && a.getEndTime().isAfter(slotStart));
            if (!taken) {
                free.add(slotStart);
            }
        }
        return free;
    }

    @Transactional
    public AppointmentResponse book(AuthUser me, Long serviceId, LocalDateTime start, String note) {
        if (me.role() != Role.CLIENT) {
            throw ApiException.forbidden("Doar clientii pot face programari");
        }
        ServiceOffering s = offerings.findActiveById(serviceId)
                .orElseThrow(() -> ApiException.notFound("Serviciul nu exista"));

        // Blocam furnizorul ca doua rezervari simultane sa nu ia acelasi interval.
        users.findByIdForUpdate(s.getProvider().getId());

        LocalDateTime end = start.plusMinutes(s.getDurationMinutes());
        validateSlot(start, end, s.getDurationMinutes());

        if (!appointments.findOverlapping(s.getProvider().getId(), AppointmentStatus.BOOKED, start, end).isEmpty()) {
            throw ApiException.conflict("Intervalul tocmai a fost ocupat. Alege alta ora.");
        }
        long active = appointments.countByClient_IdAndStatusAndStartTimeAfter(
                me.id(), AppointmentStatus.BOOKED, LocalDateTime.now(zone));
        if (active >= MAX_ACTIVE_BOOKINGS_PER_CLIENT) {
            throw ApiException.badRequest("Ai prea multe programari active");
        }

        User client = users.findById(me.id()).orElseThrow(() -> ApiException.unauthorized("Contul nu mai exista"));
        String cleanNote = (note == null || note.isBlank()) ? null : note.trim();
        Appointment saved = appointments.save(new Appointment(client, s, start, end, cleanNote));
        return AppointmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> mine(AuthUser me) {
        List<Appointment> list = me.role() == Role.PROVIDER
                ? appointments.findForProvider(me.id())
                : appointments.findForClient(me.id());
        return list.stream().map(AppointmentResponse::from).toList();
    }

    @Transactional
    public AppointmentResponse cancel(AuthUser me, Long id) {
        Appointment a = appointments.findDetailedById(id)
                .orElseThrow(() -> ApiException.notFound("Programarea nu exista"));
        boolean isClient = a.getClient().getId().equals(me.id());
        boolean isProvider = a.getService().getProvider().getId().equals(me.id());
        if (!isClient && !isProvider) {
            throw ApiException.notFound("Programarea nu exista");
        }
        if (a.getStatus() != AppointmentStatus.BOOKED) {
            throw ApiException.conflict("Programarea este deja anulata");
        }
        if (!a.getStartTime().isAfter(LocalDateTime.now(zone))) {
            throw ApiException.badRequest("Nu poti anula o programare care a inceput deja");
        }
        a.cancel();
        return AppointmentResponse.from(a);
    }

    private void validateSlot(LocalDateTime start, LocalDateTime end, int duration) {
        LocalDate day = start.toLocalDate();
        LocalDateTime now = LocalDateTime.now(zone);
        if (!start.isAfter(now)) {
            throw ApiException.badRequest("Nu poti face programari in trecut");
        }
        if (day.isAfter(LocalDate.now(zone).plusDays(MAX_DAYS_AHEAD))) {
            throw ApiException.badRequest("Poti face programari cu maximum " + MAX_DAYS_AHEAD + " de zile inainte");
        }
        if (!isWorkingDay(day)) {
            throw ApiException.badRequest("Duminica nu se lucreaza");
        }
        LocalDateTime open = day.atTime(OPEN);
        boolean aligned = start.getSecond() == 0 && start.getNano() == 0
                && Duration.between(open, start).toMinutes() % duration == 0;
        if (start.isBefore(open) || end.isAfter(day.atTime(CLOSE)) || !aligned) {
            throw ApiException.badRequest("Ora aleasa nu este un interval valid");
        }
    }

    private static boolean isWorkingDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }
}
