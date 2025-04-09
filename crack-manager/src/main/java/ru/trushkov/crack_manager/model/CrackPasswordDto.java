package ru.trushkov.crack_manager.model;

import com.sun.istack.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrackPasswordDto {

    @NotNull
    private String hash;

    @NotNull
    private Integer length;

    private String requestId;

}
