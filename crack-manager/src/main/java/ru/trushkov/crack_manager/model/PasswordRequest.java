package ru.trushkov.crack_manager.model;

import com.sun.istack.NotNull;
import lombok.*;
import ru.trushkov.crack_manager.model.enumeration.Status;

import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordRequest {

    private CopyOnWriteArrayList<String> data;

    @NotNull
    private Status status;

    @NotNull
    private Integer successWork;

    @NotNull
    private Integer maxLength;
}
