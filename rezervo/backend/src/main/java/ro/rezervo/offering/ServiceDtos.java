package ro.rezervo.offering;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public final class ServiceDtos {

    private ServiceDtos() {
    }

    public record ServiceRequest(
            @NotBlank(message = "Numele serviciului este obligatoriu")
            @Size(min = 2, max = 100, message = "Numele trebuie sa aiba intre 2 si 100 de caractere")
            String name,

            @Size(max = 1000, message = "Descrierea poate avea maximum 1000 de caractere")
            String description,

            @NotNull(message = "Durata este obligatorie")
            @Min(value = 10, message = "Durata minima este 10 minute")
            @Max(value = 480, message = "Durata maxima este 480 de minute")
            Integer durationMinutes,

            @NotNull(message = "Pretul este obligatoriu")
            @DecimalMin(value = "0", message = "Pretul nu poate fi negativ")
            @DecimalMax(value = "100000", message = "Pretul este prea mare")
            @Digits(integer = 6, fraction = 2, message = "Pret invalid")
            BigDecimal price) {
    }

    public record ServiceResponse(Long id, String name, String description, int durationMinutes,
                                  BigDecimal price, Long providerId, String providerName) {
        public static ServiceResponse from(ServiceOffering s) {
            return new ServiceResponse(s.getId(), s.getName(), s.getDescription(), s.getDurationMinutes(),
                    s.getPrice(), s.getProvider().getId(), s.getProvider().getFullName());
        }
    }
}
