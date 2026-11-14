package ru.practicum.ewm.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.StatsServerApplication;
import ru.practicum.ewm.ViewStats;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {StatsController.class})
public class StatsControllerTest {

    @MockBean
    private StatsServiceImpl statsService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private final ViewStats viewStats = new ViewStats(
            "app",
            "/event1",
            10L
    );

    @Test
    void addNewBookingTest() throws Exception {
        EndpointHit endpointHit = new EndpointHit(
                1L,
                "app",
                "/event1",
                "1.2.3.4",
                LocalDateTime.of(10, 1, 2, 3, 4)
        );


        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHit))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllStatsWithOnlyRequiredParams() throws Exception {
        when(statsService.getAllStats(any(LocalDateTime.class), any(LocalDateTime.class),
                anyList(), anyBoolean()))
                .thenReturn(List.of(viewStats));

        mvc.perform(get("/stats")
                        .param("start", "2007-07-10 12:44:22")
                        .param("end", "2008-07-10 12:44:22")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].app", is(viewStats.getApp()), String.class))
                .andExpect(jsonPath("$[0].uri", is(viewStats.getUri()), String.class))
                .andExpect(jsonPath("$[0].hits", is(viewStats.getHits()), Long.class));
    }

    @Test
    void getAllStatsWithAllParams() throws Exception {
        when(statsService.getAllStats(any(LocalDateTime.class), any(LocalDateTime.class),
                anyList(), anyBoolean()))
                .thenReturn(List.of(viewStats));

        mvc.perform(get("/stats")
                        .param("start", "2007-07-10 12:44:22")
                        .param("end", "2008-07-10 12:44:22")
                        .param("uris", "/event1", "/event2")
                        .param("unique", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].app", is(viewStats.getApp()), String.class))
                .andExpect(jsonPath("$[0].uri", is(viewStats.getUri()), String.class))
                .andExpect(jsonPath("$[0].hits", is(viewStats.getHits()), Long.class));
    }
}
