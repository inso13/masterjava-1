package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

/**
 * Created by Котик on 04.04.2017.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class Group
{
    @Column("project_id")
    private @NonNull int projectId;
    @Column("description")
    private @NonNull
    String description;
    @Column("type")
    private @NonNull
    String type;
}
