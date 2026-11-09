package ro.rezervo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testeaza fluxul complet: cont furnizor -> serviciu -> cont client -> programare. */
@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;

    @Test
    void fullBookingFlow() throws Exception {
        String providerToken = register("Frizeria Ion", "PROVIDER");
        String clientToken = register("Maria Pop", "CLIENT");

        // Furnizorul adauga un serviciu
        JsonNode service = body(mvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tuns\",\"description\":\"Tuns clasic\",\"durationMinutes\":30,\"price\":50}"))
                .andExpect(status().isCreated()));
        long serviceId = service.get("id").asLong();

        // Clientul nu poate adauga servicii
        mvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"durationMinutes\":30,\"price\":1}"))
                .andExpect(status().isForbidden());

        // Orele libere de luni viitoare
        LocalDate monday = LocalDate.now(ZoneId.of("Europe/Bucharest")).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        JsonNode slots = body(mvc.perform(get("/api/services/" + serviceId + "/slots").param("date", monday.toString()))
                .andExpect(status().isOk()));
        assertThat(slots.size()).isEqualTo(16); // 09:00-17:00, cate 30 min
        String firstSlot = slots.get(0).asText();

        // Programare fara login -> 401
        mvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceId\":" + serviceId + ",\"startTime\":\"" + firstSlot + "\"}"))
                .andExpect(status().isUnauthorized());

        // Programare reusita
        String booking = "{\"serviceId\":" + serviceId + ",\"startTime\":\"" + firstSlot + "\",\"note\":\"Prima data\"}";
        JsonNode appt = body(mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(booking))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED")));

        // Acelasi interval a doua oara -> 409
        mvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(booking))
                .andExpect(status().isConflict());

        // Intervalul nu mai apare liber
        JsonNode slotsAfter = body(mvc.perform(get("/api/services/" + serviceId + "/slots").param("date", monday.toString())));
        assertThat(slotsAfter.size()).isEqualTo(15);

        // Furnizorul vede programarea
        mvc.perform(get("/api/appointments/mine").header("Authorization", "Bearer " + providerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Maria Pop"));

        // Alt client nu poate anula programarea
        String otherClient = register("Alt Client", "CLIENT");
        mvc.perform(post("/api/appointments/" + appt.get("id").asLong() + "/cancel")
                        .header("Authorization", "Bearer " + otherClient))
                .andExpect(status().isNotFound());

        // Clientul anuleaza
        mvc.perform(post("/api/appointments/" + appt.get("id").asLong() + "/cancel")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void loginAndValidation() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@test.ro";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"parola123\",\"role\":\"CLIENT\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());

        // Email duplicat
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana\",\"email\":\"" + email.toUpperCase() + "\",\"password\":\"parola123\",\"role\":\"CLIENT\"}"))
                .andExpect(status().isConflict());

        // Parola slaba
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ana\",\"email\":\"x@test.ro\",\"password\":\"scurta\",\"role\":\"CLIENT\"}"))
                .andExpect(status().isBadRequest());

        // Parola gresita
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"gresita123\"}"))
                .andExpect(status().isUnauthorized());

        // Login corect + /me
        JsonNode login = body(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"parola123\"}"))
                .andExpect(status().isOk()));
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + login.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Ana"));

        // Token fals
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer token.fals.123"))
                .andExpect(status().isUnauthorized());
    }

    private String register(String name, String role) throws Exception {
        String email = UUID.randomUUID() + "@test.ro";
        JsonNode res = body(mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"parola123\",\"role\":\"" + role + "\"}"))
                .andExpect(status().isCreated()));
        return res.get("token").asText();
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
}
