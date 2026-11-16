package ru.practicum.ewm.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.stats.dto.StatsProjection;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    void addStatsTest() {
        Stats stats = new Stats(
                1L,
                "app",
                "/event1",
                "1.2.3.4",
                LocalDateTime.of(20, 1, 2, 3, 4)
        );
        when(statsRepository.save(any(Stats.class)))
                .thenReturn(stats);

        EndpointHit endpointHit = new EndpointHit(
                1L,
                "app",
                "/event1",
                "1.2.3.4",
                LocalDateTime.of(20, 1, 2, 3, 4)
        );
        statsService.addStats(endpointHit);

        verify(statsRepository, times(1)).save(any(Stats.class));
    }

    @Test
    void getStatsTest() {
        when(statsRepository.getStats(any(LocalDateTime.class),
                any(LocalDateTime.class), anyList(), anyBoolean(), anyBoolean()))
                .thenReturn(List.of(new StatsProjection() {
                    @Override
                    public String getApp() {
                        return "app";
                    }

                    @Override
                    public String getUri() {
                        return "/event1";
                    }

                    @Override
                    public Long getHits() {
                        return 1L;
                    }
                }));

        List<ViewStats> stats = statsService.getAllStats(
                LocalDateTime.of(2000, 1, 2, 3, 4),
                LocalDateTime.of(2001, 1, 2, 3, 4),
                List.of(),
                true
        );

        assertThat(stats.size(), is(1));
        assertThat(stats.getFirst().getApp(), is("app"));
        assertThat(stats.getFirst().getUri(), is("/event1"));
        assertThat(stats.getFirst().getHits(), is(1L));

        verify(statsRepository, times(1))
                .getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                        anyList(), anyBoolean(), anyBoolean());
    }

}