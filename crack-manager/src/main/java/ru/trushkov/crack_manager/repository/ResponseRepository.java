package ru.trushkov.crack_manager.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.trushkov.crack_manager.model.entity.Response;

import java.util.List;

public interface ResponseRepository extends MongoRepository<Response, String> {

    List<Response> findAllByRequestId(String requestId);
}
