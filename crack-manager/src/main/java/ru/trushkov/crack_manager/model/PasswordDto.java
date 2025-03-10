package ru.trushkov.crack_manager.model;

import com.sun.istack.NotNull;
import lombok.*;
import ru.trushkov.crack_manager.model.enumeration.Status;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDto {

    @NotNull
    private Status status;
    private List<String> data;
}
