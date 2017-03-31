package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

/**
 * Created by Inso on 31.03.2017.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class City extends BaseEntity {
    @Column("name")
    private @NonNull
    String name;

    public City(Integer id, String name) {
        this(name);
        this.id=id;
    }
}
