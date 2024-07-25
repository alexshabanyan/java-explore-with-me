package ru.yandex.practicum.model;

import lombok.*;
import ru.yandex.practicum.model.enums.RatingType;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "events_rating")
public class Rating {
    @EqualsAndHashCode.Exclude
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column
    @Enumerated(EnumType.STRING)
    private RatingType rating;

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", userId=" + user.getId() +
                ", eventId=" + event.getId() +
                ", rating=" + rating +
                '}';
    }
}
