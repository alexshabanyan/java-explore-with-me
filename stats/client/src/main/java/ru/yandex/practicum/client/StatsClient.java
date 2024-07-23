package ru.yandex.practicum.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.yandex.practicum.dto.HitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class StatsClient extends BaseClient {

    private static final String MAIN_SERVICE_NAME = "ewm-main-service";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory("http://stats-server:9090"))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build());
    }

    public ResponseEntity<Object> hit(String uri, String ip,
                                      LocalDateTime requestDateTime) {
        HitDto hitDto = new HitDto(MAIN_SERVICE_NAME, uri, ip, requestDateTime);
        return post(hitDto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Map<String, Object> parameters = Map.of("start", startDateTime.format(formatter), "end",
                endDateTime.format(formatter));
        return get("/stats?start={start}&end={end}", parameters);
    }

    public ResponseEntity<Object> getStats(LocalDateTime startDateTime, LocalDateTime endDateTime,
                                           List<String> uris) {
        Map<String, Object> parameters = Map.of(
                "start", startDateTime.format(formatter),
                "end", endDateTime.format(formatter),
                "uris", uris
        );
        return get("/stats?start={start}&end={end}&uris={uris}", parameters);
    }

    public ResponseEntity<Object> getStats(LocalDateTime startDateTime, LocalDateTime endDateTime,
                                           String[] uris, boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", startDateTime.format(formatter),
                "end", endDateTime.format(formatter),
                "uris", uris,
                "unique", unique
        );
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }

    public ResponseEntity<Object> getStats(String startDateTime, String endDateTime,
                                           boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", startDateTime,
                "end", endDateTime,
                "unique", unique
        );
        return get("/stats?start={start}&end={end}&unique={unique}", parameters);
    }

}
