package com.oclock.event_backend.repository;

import com.oclock.event_backend.domain.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
}
