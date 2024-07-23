package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.StatsDto;
import ru.yandex.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<Hit, Long> {
    @Query(value = "select app, uri, count(ip) as hits " +
            "from hits h " +
            "where request_dt between ?1 and ?2 " +
            "group by app, uri " +
            "order by hits desc", nativeQuery = true)
    List<StatsDto> findHits(LocalDateTime start, LocalDateTime end);

    @Query(value = "select app, uri, count(distinct ip) as hits " +
            "from hits h " +
            "where request_dt between ?1 and ?2 " +
            "and uri in ?3 " +
            "group by app, uri " +
            "order by hits desc", nativeQuery = true)
    List<StatsDto> findUniqueHitsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "select app, uri, count(ip) as hits " +
            "from hits h " +
            "where request_dt between ?1 and ?2 " +
            "and uri in ?3 " +
            "group by app, uri " +
            "order by hits desc", nativeQuery = true)
    List<StatsDto> findHitsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(value = "select app, uri, count(distinct ip) as hits " +
            "from hits h " +
            "where request_dt between ?1 and ?2 " +
            "group by app, uri " +
            "order by hits desc", nativeQuery = true)
    List<StatsDto> findUniqueHits(LocalDateTime start, LocalDateTime end);
}