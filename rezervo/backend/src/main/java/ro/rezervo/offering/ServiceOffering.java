package ro.rezervo.offering;

import jakarta.persistence.*;
import ro.rezervo.user.User;

import java.math.BigDecimal;

/** Un serviciu oferit de un furnizor (ex: "Tuns barbati, 30 min, 50 lei"). */
@Entity
@Table(name = "service_offerings")
public class ServiceOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    /** Serviciile sterse sunt doar dezactivate, ca programarile vechi sa ramana vizibile. */
    @Column(nullable = false)
    private boolean active = true;

    protected ServiceOffering() {
    }

    public ServiceOffering(User provider, String name, String description, int durationMinutes, BigDecimal price) {
        this.provider = provider;
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() { return id; }
    public User getProvider() { return provider; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDurationMinutes() { return durationMinutes; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
}
