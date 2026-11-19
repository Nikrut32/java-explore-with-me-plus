package ru.practicum.ewm.stats;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
public interface StatsService {

    @Transactional
    void addStats(EndpointHit endpointHit);

    List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end,
                                List<String> uris, Boolean unique);
}
