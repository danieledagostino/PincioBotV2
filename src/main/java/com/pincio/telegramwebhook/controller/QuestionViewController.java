package com.pincio.telegramwebhook.controller;

import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class QuestionViewController {

    private final QuestionRepository questionRepository;

    // Visualizza una domanda specifica e le risposte
    @GetMapping("/question")
    public String getQuestion(@RequestParam("id") String id, Model model) {
        Optional<Question> question = questionRepository.findById(id);
        question.ifPresent(q -> model.addAttribute("question", q));
        return "question";
    }

    // Conferma la risposta selezionata per una domanda
    @PostMapping("/confirmAnswer")
    public String confirmAnswer(@RequestParam("id") String id, @RequestParam("answer") String answer) {
        Optional<Question> question = questionRepository.findById(id);
        if (question.isPresent()) {
            Question q = question.get();
            q.setConfirmedAnswer(answer);
            questionRepository.save(q); // Salva la risposta confermata nel database
        }
        return "redirect:/questions"; // Torna alla lista delle domande
    }
}
