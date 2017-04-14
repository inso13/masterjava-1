package ru.javaops.masterjava.persist.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class EmailResult extends BaseEntity {

    @NonNull
    private String result;
    @NonNull
    private LocalDateTime dateTime;
    @NonNull
    private String email;
}