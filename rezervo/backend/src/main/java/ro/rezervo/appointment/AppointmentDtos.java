package ro.rezervo.appointment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AppointmentDtos {

    private AppointmentDtos() {
    }

    public record BookingRequest(
            @NotNull(message = "Alege serviciul")
            Long serviceId,

            @NotNull(message = "Alege ora")
            LocalDateTime startTime,

            @Size(max = 500, message = "Nota poate avea maximum 500 de caractere")
            String note) {
    }

    public record AppointmentResponse(Long id, Long serviceId, String serviceName, String providerName,
                                      String clientName, LocalDateTime startTime, LocalDateTime endTime,
                                      AppointmentStatus status, String note) {
        public static AppointmentResponse from(Appointment a) {
            return new AppointmentResponse(a.getId(), a.getService().getId(), a.getService().getName(),
                    a.getService().getProvider().getFullName(), a.getClient().getFullName(),
                    a.getStartTime(), a.getEndTime(), a.getStatus(), a.getNote());
        }
    }
}
