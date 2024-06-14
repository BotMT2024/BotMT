package MTbot.mt.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "document")
public class Document {
    /**
     * уникальный идентификатор документа
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * название раздела для изучения
     */
    @Column(unique = true)
    private String nameChapter;

    /**
     * ссылка на раздел
     */
    @Column(unique = true)
    private String reference;

}
