
package com.pincio.telegramwebhook.repository;

import com.pincio.telegramwebhook.model.Question;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends CrudRepository<Question, String> {

    List<Question> findAll();
    Optional<Question> findById(String id); // Per cercare una domanda specifica
}
