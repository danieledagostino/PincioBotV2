package com.pincio.telegramwebhook.repository;

import com.pincio.telegramwebhook.model.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class QuestionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public QuestionRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Salva una domanda
    public void save(Question question) {
        String id = question.getId() != null ? question.getId() : UUID.randomUUID().toString();
        question.setId(id);
        redisTemplate.opsForHash().put("questions", id, question);
    }

    public Question findByQuestionText(String questionText) {
        Question result = null;
        try {
            List<Object> questions = redisTemplate.opsForHash().values("questions");
            for (Object obj : questions) {
                if (obj instanceof Question) {
                    if (((Question) obj).getQuestionText().equals(questionText)) {
                        result = (Question) obj;
                        break;
                    }
                }
            }
        }catch (Exception e) {
            log.error("Error while retrieving questions", e);
        }
        return result;
    }

    // Trova una domanda per ID
    public Optional<Question> findById(String id) {
        Question question = null;
        try{
            question = (Question) redisTemplate.opsForHash().get("questions", id);
        }catch (Exception e) {
            log.error("Error while retrieving question", e);
        }
        return Optional.ofNullable(question);
    }

    // Elimina una domanda per ID
    public void deleteById(String id) {

        try{
            redisTemplate.opsForHash().delete("questions", id);
        }catch (Exception e) {
            log.error("Error while deleting question", e);
        }
    }

    // Trova tutte le domande
    public List<Question> findAll() {
        List<Question> result = new ArrayList<>();
        try {
            List<Object> questions = redisTemplate.opsForHash().values("questions");
            for (Object obj : questions) {
                if (obj instanceof Question) {
                    result.add((Question) obj);
                }
            }
        }catch (Exception e) {
            log.error("Error while retrieving questions", e);
        }
        return result;
    }
}
