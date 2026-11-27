package ru.practicum.ewm.request;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByEventIdAndIdIn(Long eventId, List<Long> requestIds);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("SELECT r.event.id AS eventId, COUNT(r.id) AS cnt " + "FROM ParticipationRequest r " + "WHERE r.event.id IN :eventIds AND r.status = :status " + "GROUP BY r.event.id")
    List<ConfirmedCountProjection> countConfirmedByEventIds(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);

    interface ConfirmedCountProjection {
        Long getEventId();

        Long getCnt();
    }
}