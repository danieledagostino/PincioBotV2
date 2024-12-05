package com.pincio.telegramwebhook.controller;

import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.service.AIResponseService;
import com.pincio.telegramwebhook.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AIResponseService aiResponseService;

    @GetMapping("/admin/questions")
    public String viewQuestionsToReview(Model model) {
        Iterable<Question> questions = questionService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "review_questions";
    }

    @PostMapping("/admin/questions/approve")
    public String approveQuestion(@RequestParam("questionText") String questionText, Model model) {
        // Aggiungi [MASK] alla domanda se non presente
        if (!questionText.contains("[MASK]")) {
            questionText = questionText + " [MASK]";
        }

        // Invia la domanda corretta a Hugging Face (chiamata al servizio Hugging Face)
        String response = aiResponseService.callHuggingFaceApiWithMask(questionText);

        // Salva la risposta e segna la domanda come risolta
        questionService.updateQuestionResponse(questionText, response);

        model.addAttribute("response", response);
        return "question_approved";
    }
}
