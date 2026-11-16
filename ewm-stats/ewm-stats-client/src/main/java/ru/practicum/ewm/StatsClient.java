package ru.practicum.ewm;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.client.BaseClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatsClient extends BaseClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build()
        );
    }

    public void addStats(EndpointHit endpointHit) {
        post("/hit", endpointHit);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start,
                                                 LocalDateTime end,
                                                 List<String> uris,
                                                 Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(FORMATTER),
                "end", end.format(FORMATTER),
                "unique", unique != null ? unique : false
        );

        if (uris != null && !uris.isEmpty()) {
            parameters.put("uris", String.join(",", uris));
            return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", null, parameters);
        }

        return get("/stats?start={start}&end={end}&unique={unique}", null, parameters);
    }
}
