package ru.practicum.ewm.stats;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceIntegrationTest {

    private final EntityManager em;
    private final StatsService service;

    @Test
    void addStats() {
        EndpointHit endpointHit = new EndpointHit(
                1L,
                "app",
                "/event1",
                "1.2.3.4",
                LocalDateTime.of(20, 1, 2, 3, 4)
        );

        service.addStats(endpointHit);

        TypedQuery<Stats> query = em.createQuery("Select s from Stats s where s.id=:id", Stats.class);
        Stats statsSql = query.setParameter("id", endpointHit.getId()).getSingleResult();

        assertThat(statsSql.getId(), is(endpointHit.getId()));
        assertThat(statsSql.getApp(), is(endpointHit.getApp()));
        assertThat(statsSql.getUri(), is(endpointHit.getUri()));
        assertThat(statsSql.getIp(), is(endpointHit.getIp()));
        assertThat(statsSql.getTimestamp(), is(endpointHit.getTimestamp()));
    }

    @Test
    void getStatsDefaultTest() {
        Stats stats1 = newStats("/event1", "1.2.3.4");
        Stats stats2 = newStats("/event2", "1.2.3.4");
        Stats stats3 = newStats("/event1", "1.2.3.4");

        em.persist(stats1);
        em.persist(stats2);
        em.persist(stats3);
        em.flush();
        em.clear();
        ;

        List<ViewStats> result = service.getAllStats(
                LocalDateTime.of(2000, 1, 2, 3, 4),
                LocalDateTime.of(2001, 1, 2, 3, 4),
                List.of(),
                false
        );

        assertThat(result.size(), is(2));
        assertThat(result.getFirst().getApp(), is(stats1.getApp()));
        assertThat(result.getFirst().getUri(), is(stats1.getUri()));
        assertThat(result.getFirst().getHits(), is(2L));
    }

    @Test
    void getStatsWithUriTest() {
        Stats stats1 = newStats("/event1", "1.2.3.4");
        Stats stats2 = newStats("/event2", "1.2.3.4");

        em.persist(stats1);
        em.persist(stats2);
        em.flush();
        em.clear();

        List<ViewStats> result = service.getAllStats(
                LocalDateTime.of(2000, 1, 2, 3, 4),
                LocalDateTime.of(2001, 1, 2, 3, 4),
                List.of("/event1"),
                true
        );

        assertThat(result.size(), is(1));
        assertThat(result.getFirst().getApp(), is(stats1.getApp()));
        assertThat(result.getFirst().getUri(), is(stats1.getUri()));
        assertThat(result.getFirst().getHits(), is(1L));
    }

    @Test
    void getStatsUniqueTest() {
        Stats stats1 = newStats("/event1", "1.2.3.4");
        Stats stats2 = newStats("/event1", "1.2.3.2");
        Stats stats3 = newStats("/event1", "1.2.3.4");

        em.persist(stats1);
        em.persist(stats2);
        em.persist(stats3);
        em.flush();
        em.clear();
        ;

        List<ViewStats> result = service.getAllStats(
                LocalDateTime.of(2000, 1, 2, 3, 4),
                LocalDateTime.of(2001, 1, 2, 3, 4),
                List.of(),
                true
        );
        System.out.println(result);

        assertThat(result.size(), is(1));
        assertThat(result.getFirst().getApp(), is(stats1.getApp()));
        assertThat(result.getFirst().getUri(), is(stats1.getUri()));
        assertThat(result.getFirst().getHits(), is(2L));
    }

    private static Stats newStats(String uri, String ip) {
        return new Stats(
                null,
                "app",
                uri,
                ip,
                LocalDateTime.of(2000, 3, 2, 3, 4)
        );
    }
}

