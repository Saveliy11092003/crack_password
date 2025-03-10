package ru.trushkov.crack_manager.model;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrackPasswordDto {

    @NotNull
    private String hash;

    @NotNull
    private Integer length;

    private String requestId;

}
