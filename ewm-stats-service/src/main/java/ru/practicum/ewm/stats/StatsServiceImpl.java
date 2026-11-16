package ru.practicum.ewm.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public void addStats(EndpointHit endpointHit) {
        log.info("Stats service add hit request");

        log.debug("Mapping endpointHit to stats");
        Stats stats = StatsMapper.toStats(endpointHit);
        log.debug("Saving stats");
        statsRepository.save(stats);

        log.info("Stats successfully saved");
    }

    @Override
    public List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Stats service get all stats request");

        log.debug("Check uris list is empty");
        Boolean isUrisExists = !uris.isEmpty();

        log.debug("Getting stats list");
        return statsRepository.getStats(start, end, uris, isUrisExists, unique).stream().map(StatsMapper::toViewStats).toList();
    }
}
