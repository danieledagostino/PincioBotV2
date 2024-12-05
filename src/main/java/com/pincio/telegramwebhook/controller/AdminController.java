package com.pincio.telegramwebhook.controller;

import com.pincio.telegramwebhook.exception.MaskTokenNotFoundException;
import com.pincio.telegramwebhook.model.Question;
import com.pincio.telegramwebhook.service.AIResponseService;
import com.pincio.telegramwebhook.service.MaskReplacementService;
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

    @Autowired
    MaskReplacementService maskReplacementService;

    @GetMapping("/admin/questions")
    public String viewQuestionsToReview(Model model) {
        Iterable<Question> questions = questionService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "review_questions";
    }

    @PostMapping("/admin/questions/approve")
    public String approveQuestion(@RequestParam("questionText") String questionText, Model model) {
        try {
            // Sostituisci automaticamente una parola con [MASK]
            String maskedQuestion = maskReplacementService.replaceWithMask(questionText);

            // Aggiorna lo storico delle sostituzioni
            questionService.updateReplacementHistory(questionText, maskedQuestion);

            // Chiamata al servizio Hugging Face
            String response = aiResponseService.callHuggingFaceApiWithMask(maskedQuestion);

            // Salva la domanda e la risposta
            questionService.updateQuestionResponse(maskedQuestion, response);

            model.addAttribute("response", response);
            return "question_approved";
        } catch (MaskTokenNotFoundException e) {
            // Registra la domanda originale per revisione manuale
            questionService.saveUnprocessedQuestion(questionText);
            model.addAttribute("error", "The question was not processed because it did not contain the required [MASK] token.");
            return "question_error";
        }
    }

    @PostMapping("/admin/questions/manual-mask")
    public String manuallyMaskQuestion(@RequestParam("questionId") String questionId,
                                       @RequestParam("maskedQuestion") String maskedQuestion,
                                       Model model) {
        // Recupera la domanda originale
        String originalQuestion = questionService.getQuestionById(questionId);

        // Aggiorna lo storico delle sostituzioni
        questionService.updateReplacementHistory(originalQuestion, maskedQuestion);

        // Salva la domanda mascherata
        questionService.saveMaskedQuestion(maskedQuestion);

        model.addAttribute("message", "Masked question saved successfully.");
        return "manual_mask_success";
    }


}
