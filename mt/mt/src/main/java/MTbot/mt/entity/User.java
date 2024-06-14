package MTbot.mt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_table")
public class User {
    /**
     * уникальный идентификатор пользователя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * ник пользователя
     */
    @Column(unique = true)
    private String nickname;

    @Column(name = "time_last_message")
    private LocalDateTime localDateTime;

    @Column(name = "is_ping")
    private Boolean isPing;
}
