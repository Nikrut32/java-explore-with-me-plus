package ru.practicum.ewm.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.stats.dto.StatsProjection;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {

    @Query("""
            SELECT s.app AS app, s.uri AS uri,
            CASE
            WHEN :unique = true THEN COUNT(DISTINCT s.ip)
            ELSE COUNT(s.ip)
            END AS hits
            FROM Stats s
            WHERE s.timestamp BETWEEN :start AND :end AND (:urisExists = false OR s.uri IN :uris)
            GROUP BY s.app, s.uri
            ORDER BY hits DESC
            """)
    List<StatsProjection> getStats(LocalDateTime start, LocalDateTime end,
                                     List<String> uris, Boolean urisExists, Boolean unique);
}
