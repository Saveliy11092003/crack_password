package ru.trushkov.crack_manager.model.entity;

import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Builder
@Document
@ToString
public class Request {

    @Id
    private String id;

    @NotNull
    private String hash;

    @NotNull
    private Integer length;

    private Boolean done;

}
