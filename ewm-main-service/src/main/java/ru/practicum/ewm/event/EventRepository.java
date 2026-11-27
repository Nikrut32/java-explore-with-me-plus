package ru.practicum.ewm.event;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);
}