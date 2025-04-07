package ru.trushkov.crack_manager.model.entity;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@Document
@ToString
public class Response {

    @Id
    private String id;

    private String requestId;

    private int partNumber;

    private List<String> answers;
}
