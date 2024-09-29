package com.oclock.event_backend.repository;

import com.oclock.event_backend.domain.EventLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventLocationRepository  extends JpaRepository<EventLocation, Long> {
}
