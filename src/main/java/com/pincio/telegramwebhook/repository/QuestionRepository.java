
package com.pincio.telegramwebhook.repository;

import com.pincio.telegramwebhook.model.Question;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends CrudRepository<Question, String> {
}
