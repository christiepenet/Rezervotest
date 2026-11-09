package ro.rezervo.appointment;

import jakarta.persistence.*;
import ro.rezervo.offering.ServiceOffering;
import ro.rezervo.user.User;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffering service;

    /** Ora locala a furnizorului (fusul orar din app.timezone). */
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    protected Appointment() {
    }

    public Appointment(User client, ServiceOffering service, LocalDateTime startTime, LocalDateTime endTime, String note) {
        this.client = client;
        this.service = service;
        this.startTime = startTime;
        this.endTime = endTime;
        this.note = note;
        this.status = AppointmentStatus.BOOKED;
        this.createdAt = Instant.now();
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public User getClient() { return client; }
    public ServiceOffering getService() { return service; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public AppointmentStatus getStatus() { return status; }
    public String getNote() { return note; }
}
