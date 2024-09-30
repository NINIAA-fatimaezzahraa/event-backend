package com.oclock.event_backend.repository;

import com.oclock.event_backend.domain.Event;
import com.oclock.event_backend.domain.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Set<Event> findByCategory(EventCategory category);
}
