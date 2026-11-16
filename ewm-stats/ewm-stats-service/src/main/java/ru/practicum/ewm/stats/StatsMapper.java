package ru.practicum.ewm.stats;

import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.stats.dto.StatsProjection;

public class StatsMapper {

    public static Stats toStats(EndpointHit hit) {
        return new Stats(
                hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getTimestamp()
        );
    }

    public static ViewStats toViewStats(StatsProjection stats) {
        return new ViewStats(
                stats.getApp(),
                stats.getUri(),
                stats.getHits()
        );
    }
}