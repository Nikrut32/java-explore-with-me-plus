package ru.practicum.ewm.stats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
public class StatsDtoJsonTest {
    @Autowired
    private JacksonTester<EndpointHit> endpointHitJson;

    @Autowired
    private JacksonTester<ViewStats> viewStatsJson;

    @Test
    void testEndpointHit() throws Exception {
        EndpointHit endpointHit = new EndpointHit(
                1L,
                "app",
                "/event1",
                "1.2.3.4",
                LocalDateTime.of(2000, 1, 2, 3, 4)
        );

        JsonContent<EndpointHit> result = endpointHitJson.write(endpointHit);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.app")
                .isEqualTo(endpointHit.getApp());
        assertThat(result).extractingJsonPathStringValue("$.uri")
                .isEqualTo(endpointHit.getUri());
        assertThat(result).extractingJsonPathStringValue("$.ip")
                .isEqualTo(endpointHit.getIp());
        assertThat(result).extractingJsonPathStringValue("$.timestamp")
                .isEqualTo("2000-01-02 03:04:00");
    }

    @Test
    void testViewStats() throws Exception {
        ViewStats viewStats = new ViewStats(
                "app",
                "/event1",
                10L
        );

        JsonContent<ViewStats> result = viewStatsJson.write(viewStats);

        assertThat(result).doesNotHaveJsonPath("$.id");
        assertThat(result).extractingJsonPathStringValue("$.app")
                .isEqualTo(viewStats.getApp());
        assertThat(result).extractingJsonPathStringValue("$.uri")
                .isEqualTo(viewStats.getUri());
        assertThat(result).extractingJsonPathNumberValue("$.hits")
                .isEqualTo(10);
    }
}