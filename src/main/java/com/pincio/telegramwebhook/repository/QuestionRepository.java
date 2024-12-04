package com.pincio.telegramwebhook.repository;

import com.pincio.telegramwebhook.model.Question;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
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

    // Trova una domanda per ID
    public Optional<Question> findById(String id) {
        Question question = (Question) redisTemplate.opsForHash().get("questions", id);
        return Optional.ofNullable(question);
    }

    // Elimina una domanda per ID
    public void deleteById(String id) {
        redisTemplate.opsForHash().delete("questions", id);
    }
}
