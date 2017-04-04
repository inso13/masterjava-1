package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

import java.util.List;

/**
 * Created by Inso on 31.03.2017.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Project extends BaseEntity {
    @Column("description")
    private @NonNull
    String description;

    private List<Group> groups;

    public Project(Integer id, String description) {
        this(description);
        this.id=id;
    }
}
