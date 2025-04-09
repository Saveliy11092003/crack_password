package ru.trushkov.crack_manager.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.trushkov.crack_manager.model.entity.Request;

import java.util.List;

@Repository
public interface RequestRepository extends MongoRepository<Request, String> {

    List<Request> findAllBySentFalse();


}
