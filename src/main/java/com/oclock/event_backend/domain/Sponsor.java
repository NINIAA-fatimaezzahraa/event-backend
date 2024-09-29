package com.oclock.event_backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sponsors")
public class Sponsor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sponsor_id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String logo;
}
