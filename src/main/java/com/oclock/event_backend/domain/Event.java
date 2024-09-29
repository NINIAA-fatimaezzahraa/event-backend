package com.oclock.event_backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "event_location_id")
    private EventLocation location;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User manager;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_sponsors",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "sponsor_id")
    )
    private Set<Sponsor> sponsors;
}
